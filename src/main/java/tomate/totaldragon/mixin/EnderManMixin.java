package tomate.totaldragon.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderMan.class)
public abstract class EnderManMixin extends Monster {
    protected EnderManMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    // Make EnderMans immune to potions so that they don't die from the DragonBreath AreaEffectClouds
    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    void setTarget(LivingEntity livingEntity, CallbackInfo ci) {
        if(livingEntity instanceof EnderDragon) {
            ci.cancel();
        }
    }
}
