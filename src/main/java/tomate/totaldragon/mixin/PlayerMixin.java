package tomate.totaldragon.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tomate.totaldragon.DragonConfig;

import java.util.Objects;

// TODO Make sure the player does not land in the ocean/any other water
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Unique
    private DamageSource fellOutOfWorldSource;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("HEAD"), method="isInvulnerableTo", cancellable = true)
    void isInvulnerableTo(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if(!DragonConfig.playersFallIntoOverworld)
            return;

        if(damageSource.is(DamageTypes.FELL_OUT_OF_WORLD)){
            var dimension = level().dimension();

            if(dimension == Level.END) {
                var overworld = Objects.requireNonNull(getServer()).getLevel(Level.OVERWORLD);
                var position = position();
                teleportTo(overworld, position.x, 400, position.z, null, getYHeadRot(), xRotO);
                fellOutOfWorldSource = damageSource;
            }
        }

        if(damageSource.is(DamageTypes.FALL) && fellOutOfWorldSource != null) {
            hurt(fellOutOfWorldSource, 10000);
            fellOutOfWorldSource = null;

            cir.setReturnValue(true);
        }
    }
}