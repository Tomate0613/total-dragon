package tomate.totaldragon.mixin;

import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tomate.totaldragon.phases.CustomEnderDragonPhase;

@Mixin(EnderDragonPhase.class)
public class EnderDragonPhaseMixin {

    // Make sure CustomEnderDragonPhases is loaded before getCount is called to prevent an ArrayIndexOutOfBoundsException
    @Inject(at = @At("HEAD"), method = "getCount")
    private static void getCount(CallbackInfoReturnable<Integer> cir) {
        CustomEnderDragonPhase.load();
    }
}
