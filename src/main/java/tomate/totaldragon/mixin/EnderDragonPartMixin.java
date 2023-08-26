package tomate.totaldragon.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonPart.class)
public abstract class EnderDragonPartMixin extends Entity {
    @Mutable
    @Shadow @Final private EntityDimensions size;

    public EnderDragonPartMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    void init(EnderDragon enderDragon, String string, float width, float height, CallbackInfo ci) {
        if(string.contains("wing"))
            return;

        size = EntityDimensions.scalable(width * 2, height);
        refreshDimensions();
    }
}
