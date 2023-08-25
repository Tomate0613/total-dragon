package tomate.totaldragon.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static tomate.totaldragon.FightState.targetPlayer;

@Mixin(DragonChargePlayerPhase.class)
public abstract class DragonChargePlayerPhaseMixin extends AbstractDragonPhaseInstance {
    @Shadow @Final private static Logger LOGGER;

    @Shadow private int timeSinceCharge;

    @Shadow private @Nullable Vec3 targetLocation;

    @Unique
    private float damageTaken;

    public DragonChargePlayerPhaseMixin(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public void begin() {
        this.targetLocation = null;
        this.timeSinceCharge = 0;
        this.damageTaken = 0;

    }

    @Override
    public float onHurt(DamageSource damageSource, float damage) {
        if(damageSource.getEntity() != targetPlayer || damageSource.is(DamageTypes.ARROW))
            return 0;

        this.damageTaken += damage;
        return damage;
    }

    @Override
    public void doServerTick() {
        if(targetPlayer != null && targetPlayer.isAlive())
            targetLocation = targetPlayer.position();

        if (targetPlayer == null || targetLocation == null) {
            LOGGER.warn("Aborting charge player as no target was set.");

            dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            return;
        }

        if (timeSinceCharge > 0 && timeSinceCharge++ >= 10) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            return;
        }

        double distanceToPlayerSquared = targetLocation.distanceToSqr(dragon.getX(), dragon.getY(), dragon.getZ());
        if (distanceToPlayerSquared > 22500.0 || this.damageTaken > 10 || !targetPlayer.isAlive() || targetPlayer.level() != dragon.level()) {
            ++timeSinceCharge;
        }
    }
}
