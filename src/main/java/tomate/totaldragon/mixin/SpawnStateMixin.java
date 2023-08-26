package tomate.totaldragon.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

// This mixin is currently not active
@Mixin(NaturalSpawner.SpawnState.class)
public class SpawnStateMixin {

    // Prevents enderman from spawning
    @Inject(at = @At("HEAD"), method = "canSpawn", cancellable = true)
    void canSpawn(EntityType<?> entityType, BlockPos blockPos, ChunkAccess chunkAccess, CallbackInfoReturnable<Boolean> cir) {
        if(entityType != EntityType.ENDERMAN)
            return;

        var biome = getBiomeKey(blockPos, chunkAccess);

        if(biome.isEmpty() || (biome.get() != Biomes.THE_END || biome.get() != Biomes.END_BARRENS))
            return;
        if(Math.abs(blockPos.getX()) < 200 && Math.abs(blockPos.getY()) < 200 && Math.abs(blockPos.getZ()) < 200)
            cir.setReturnValue(false);
    }

    Optional<ResourceKey<Biome>> getBiomeKey(BlockPos blockPos, ChunkAccess chunkAccess) {
        return chunkAccess.getNoiseBiome(QuartPos.fromBlock(blockPos.getX()), QuartPos.fromBlock(blockPos.getY()), QuartPos.fromBlock(blockPos.getZ())).unwrapKey();
    }
}
