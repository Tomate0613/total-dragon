package tomate.totaldragon.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonLandingPhase;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DragonLandingPhase.class)
public abstract class DragonLandingPhaseMixin extends AbstractDragonPhaseInstance {
    public DragonLandingPhaseMixin(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override
    public float onHurt(DamageSource damageSource, float f) {
        if (damageSource.getDirectEntity() instanceof AbstractArrow) {
            damageSource.getDirectEntity().setSecondsOnFire(1);
            return 0.0f;
        }

        if(damageSource.is(DamageTypes.BAD_RESPAWN_POINT)) {
            return 0.0f;
        }

        return super.onHurt(damageSource, f);
    }
}
