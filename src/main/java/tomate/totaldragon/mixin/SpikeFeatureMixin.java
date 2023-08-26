package tomate.totaldragon.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tomate.totaldragon.FightState;

@Mixin(SpikeFeature.class)
public class SpikeFeatureMixin {

    // TODO This is kinda scuffed; Find a better way to do this
    @Inject(method = "placeSpike", at = @At("TAIL"))
    void placeSpike(ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, SpikeConfiguration spikeConfiguration, SpikeFeature.EndSpike endSpike, CallbackInfo ci) {
        if(!FightState.isNextSpikeEndCrystalVulnerable)
            return;

        FightState.isNextSpikeEndCrystalVulnerable = false;

        FightState.vulnerableEndCrystalPos = new Vec3(endSpike.getCenterX() + 0.5, endSpike.getHeight() + 1, endSpike.getCenterZ() + 0.5);
    }
}
