package tomate.totaldragon.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityDimensionsAccessor {
    @Accessor("dimensions")
    public EntityDimensions getDimensions();
    @Accessor("dimensions")
    public void setDimensions(EntityDimensions dimensions);
}
