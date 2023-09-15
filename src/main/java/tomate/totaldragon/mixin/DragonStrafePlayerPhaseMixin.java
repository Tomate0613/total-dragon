package tomate.totaldragon.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(DragonStrafePlayerPhase.class)
public abstract class DragonStrafePlayerPhaseMixin extends AbstractDragonPhaseInstance {
    @Shadow private @Nullable LivingEntity attackTarget;
    @Unique private int giveUpTimer;

    public DragonStrafePlayerPhaseMixin(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Inject(at = @At("RETURN"), method="getFlyTargetLocation", cancellable = true)
    void getFlyTargetLocation(CallbackInfoReturnable<Vec3> cir) {
        if(attackTarget == null)
            return;

        var newPos = attackTarget.position().add(0, 10, 0);
        cir.setReturnValue(newPos);
    }

    @Inject(at = @At("HEAD"), method = "doServerTick", cancellable = true)
    void tick(CallbackInfo ci) {
        if(++giveUpTimer > 2400) {
            dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            ci.cancel();
        }

        if(attackTarget != null && attackTarget.getY() > 70) {
            dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "begin")
    void begin(CallbackInfo ci) {
        giveUpTimer = 0;
    }
}
