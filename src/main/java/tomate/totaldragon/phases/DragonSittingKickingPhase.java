package tomate.totaldragon.phases;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonSittingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import tomate.totaldragon.FightState;

import java.util.List;

import static tomate.totaldragon.FightState.knockbackCooldown;

public class DragonSittingKickingPhase extends AbstractDragonSittingPhase {
    private int sittingTicks = 0;
    private static final TargetingConditions CHARGE_TARGETING = TargetingConditions.forCombat().range(200.0); // 150 normally

    public DragonSittingKickingPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public EnderDragonPhase<? extends DragonPhaseInstance> getPhase() {
        return CustomEnderDragonPhase.SITTING_KICKING;
    }

    public void doServerTick() {
        tick();

        if(++sittingTicks > 200) {
            sittingTicks = 0;

            var nearestPlayer = dragon.level().getNearestPlayer(CHARGE_TARGETING, dragon, dragon.getX(), dragon.getY(), dragon.getZ());

            if (nearestPlayer != null) {
                dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                FightState.targetPlayer = nearestPlayer;
                return;
            }

            dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
        }
    }

    public void tick() {
        this.knockBack(this.dragon.level().getEntities(this.dragon, dragon.getBoundingBox().inflate(4.0D, 4.0D, 4.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(entity -> entity instanceof Player)));
    }

    @Override
    public void doClientTick() {
        tick();

        var pos = dragon.position();
        var random = dragon.getRandom();

        for (int i = 0; i < 100; ++i) {
            var x = (random.nextDouble() - 0.5) * 10;
            var y = (random.nextDouble() - 0.5) * 10;
            var z = (random.nextDouble() - 0.5) * 10;

            this.dragon.level().addParticle(ParticleTypes.ENCHANT, pos.x + x * 1.5, pos.y + y * 1.2 + 1, pos.z + z * 1.5, x, y, z);
        }
    }

    private void knockBack(List<Entity> entities) {
        double centerX = (this.dragon.getBoundingBox().minX + this.dragon.getBoundingBox().maxX) / 2.0;
        double centerZ = (this.dragon.getBoundingBox().minZ + this.dragon.getBoundingBox().maxZ) / 2.0;
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity)) continue;
            float deltaX = (float) (entity.getX() - centerX);
            float deltaZ = (float) (entity.getZ() - centerZ);
            Vec2 normalizedDeltaMovement = new Vec2(deltaX, deltaZ).normalized();

            if (--knockbackCooldown > 0) continue;
            knockbackCooldown = 20;

            var knockbackStrength = Mth.lerp(10, 1, entity.position().length() / 200);;

            entity.setDeltaMovement(new Vec3(normalizedDeltaMovement.x * knockbackStrength, 3, normalizedDeltaMovement.y * knockbackStrength));
            entity.hasImpulse = true;

            if(!this.dragon.level().isClientSide) {
                entity.hurt(this.dragon.damageSources().mobAttack(this.dragon), 5.0f);
                this.dragon.doEnchantDamageEffects(this.dragon, entity);
            }
        }
    }
}
