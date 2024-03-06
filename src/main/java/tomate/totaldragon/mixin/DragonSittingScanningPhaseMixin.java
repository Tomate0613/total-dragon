package tomate.totaldragon.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonSittingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingScanningPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tomate.totaldragon.FightState;
import tomate.totaldragon.phases.CustomEnderDragonPhase;

@Mixin(DragonSittingScanningPhase.class)
public abstract class DragonSittingScanningPhaseMixin extends AbstractDragonSittingPhase {
    @Shadow private int scanningTime;

    @Shadow @Final private static TargetingConditions CHARGE_TARGETING;

    @Unique
    private final TargetingConditions SCAN_TARGETING = TargetingConditions.forCombat().range(100).selector(livingEntity -> Math.abs(livingEntity.getY() - dragon.getY()) <= 10.0);

    public DragonSittingScanningPhaseMixin(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Unique
    private int cooldown = 0;

    @Override
    public void doServerTick() {
        ++scanningTime;
        Player nearestPlayer = dragon.level().getNearestPlayer(SCAN_TARGETING, dragon, dragon.getX(), dragon.getY(), dragon.getZ());
        var shouldSpawnCrystal = !FightState.crystalPositions.isEmpty() && FightState.getCrystalsToDestroy() > 0;

        if (nearestPlayer != null && !shouldSpawnCrystal) {
            if (scanningTime > 25) {
                if(dragon.position().distanceToSqr(nearestPlayer.position()) < 100) {
                    dragon.getPhaseManager().setPhase(CustomEnderDragonPhase.SITTING_KICKING);
                } else {
                    dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_ATTACKING);
                }
            } else {
                Vec3 toPlayerRot = new Vec3(nearestPlayer.getX() - dragon.getX(), 0.0, nearestPlayer.getZ() - dragon.getZ()).normalize();
                Vec3 rotation = new Vec3(Mth.sin(dragon.getYRot() * ((float)Math.PI / 180)), 0.0, -Mth.cos(dragon.getYRot() * ((float)Math.PI / 180))).normalize();
                float dotProduct = (float)rotation.dot(toPlayerRot);

                double angle = Math.toDegrees(Math.acos(dotProduct));

                // If the angle is high, then rotate towards the player
                if (angle > 10f) {
                    double headDeltaX = nearestPlayer.getX() - dragon.head.getX();
                    double headDeltaZ = nearestPlayer.getZ() - dragon.head.getZ();

                    double desiredDeltaRotation  = Mth.clamp(Mth.wrapDegrees(180.0 - Math.toDegrees(Mth.atan2(headDeltaX, headDeltaZ)) - (double)dragon.getYRot()), -100.0, 100.0);
                    dragon.yRotA *= 0.8f;

                    float distanceToPlayer;
                    float combinedDistance = distanceToPlayer = (float)Math.sqrt(headDeltaX * headDeltaX + headDeltaZ * headDeltaZ) + 1.0f;
                    if (distanceToPlayer > 40.0f) {
                        distanceToPlayer = 40.0f;
                    }

                    dragon.yRotA += (float)desiredDeltaRotation  * (0.7f / distanceToPlayer / combinedDistance );
                    dragon.setYRot(dragon.getYRot() + dragon.yRotA);
                }
            }
        } else if (scanningTime >= 100 && !shouldSpawnCrystal) {
            nearestPlayer = dragon.level().getNearestPlayer(CHARGE_TARGETING, dragon, dragon.getX(), dragon.getY(), dragon.getZ());

            if (nearestPlayer != null) {
                dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                FightState.targetPlayer = (ServerPlayer) nearestPlayer;
            } else {
                dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
            }
        }

        if(cooldown-- < 0 && shouldSpawnCrystal) {
            cooldown += 20;

            var pos = FightState.crystalPositions.poll();
        }
    }
}
