package tomate.totaldragon.effects;

import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class DragonBreath extends InstantenousMobEffect {
    public DragonBreath() {
        super(
                MobEffectCategory.HARMFUL,
                0x98D982
        );
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity2, LivingEntity livingEntity, int i, double d) {
        // vanilla damage calculation
        int damage = (int) (d * (double)(6 << i) + 0.5);

        livingEntity.hurt(livingEntity.damageSources().dragonBreath(), damage);
    }
}