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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tomate.totaldragon.TotalDragon;

import java.util.Objects;

// TODO Make sure the player does not land in the ocean/any other water
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Unique
    private DamageSource fellOutOfWorldSource;

    @Unique
    private int fallOutOfWorldTicks = 0;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("HEAD"), method="isInvulnerableTo", cancellable = true)
    void isInvulnerableTo(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if(!TotalDragon.CONFIG.playersFallIntoOverworld())
            return;

        if(damageSource.is(DamageTypes.FELL_OUT_OF_WORLD)){
            var dimension = level().dimension();

            if(dimension == Level.END) {
                var overworld = Objects.requireNonNull(getServer()).getLevel(Level.OVERWORLD);
                var position = position();
                teleportTo(overworld, position.x, 400, position.z, null, getYHeadRot(), xRotO);
                fellOutOfWorldSource = damageSource;
                fallOutOfWorldTicks = 0;
            }
        }

        if(damageSource.is(DamageTypes.FALL) && fellOutOfWorldSource != null) {
            hurt(fellOutOfWorldSource, 10000);
            fellOutOfWorldSource = null;

            cir.setReturnValue(true);
        }
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {
        if(fellOutOfWorldSource != null) {
            if(fallOutOfWorldTicks++ > 8 * 20) {
                hurt(fellOutOfWorldSource, 10000);
                fellOutOfWorldSource = null;
            }
        }
    }
}
