package tomate.totaldragon.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tomate.totaldragon.DragonConfig;

@Mixin(Phantom.class)
public abstract class PhantomMixin extends FlyingMob {
    @Shadow public abstract int getPhantomSize();

    protected PhantomMixin(EntityType<? extends FlyingMob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    void init(EntityType<?> entityType, Level level, CallbackInfo ci) {
        if(DragonConfig.phantomBehaviour != DragonConfig.PhantomBehaviour.IMPROVED_PHANTOMS_ALWAYS && !(DragonConfig.phantomBehaviour == DragonConfig.PhantomBehaviour.IMPROVED_PHANTOMS_IN_FIGHT && level().dimension() != Level.END))
            return;

        updatePhantomSize();
    }

    @Inject(at = @At("HEAD"), method = "updatePhantomSizeInfo", cancellable = true)
    void updatePhantomSizeInfo(CallbackInfo ci) {
        updatePhantomSize();
        ci.cancel();
    }


    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if(damageSource.getEntity() instanceof Player && level().dimension() == Level.END) {
            return super.hurt(damageSource, 1000);
        }

        return super.hurt(damageSource, f);
    }

    private void updatePhantomSize() {
        var dimensionsAccessible = ((EntityDimensionsAccessor)this);
        dimensionsAccessible.setDimensions(dimensionsAccessible.getDimensions().scale(2, 1.2f));

        if(level().dimension() == Level.END) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(25 + this.getPhantomSize());
            return;
        }
    }
}
