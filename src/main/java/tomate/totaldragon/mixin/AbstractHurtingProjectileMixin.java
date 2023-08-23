package tomate.totaldragon.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHurtingProjectile.class)
public class AbstractHurtingProjectileMixin extends Projectile {


    public AbstractHurtingProjectileMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "getInertia", at = @At("TAIL"), cancellable = true)
    public void getInertia(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof DragonFireball) {
            if (getDeltaMovement().length() < 2)
                cir.setReturnValue(2f);
        }
    }

    public void defineSynchedData() {

    }
}
