package tomate.totaldragon.mixin;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tomate.totaldragon.DragonConfig;

@Mixin(EndCrystal.class)
public abstract class EndCrystalMixin extends Entity {
    @Unique
    private boolean active = true;
    @Shadow protected abstract void onDestroyedBy(DamageSource damageSource);

    public EndCrystalMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }

        if (damageSource.getEntity() instanceof EnderDragon) {
            return false;
        }

        if (!this.isRemoved() && !this.level().isClientSide && active) {
            active = false;

            /*
            if(level instanceof ServerLevel serverLevel && damageSource.getEntity() instanceof ServerPlayer player) {
            serverLevel.sendParticles(player, TotalDragon.SHOCKWAVE, true, getX(), getY(), getZ(), 0, 0, 0, 0, 1);
            }
            level.addParticle(TotalDragon.SHOCKWAVE, getX(),getY(),getZ(),0,0,0);
            */

            this.remove(Entity.RemovalReason.KILLED);

            if (!damageSource.is(DamageTypeTags.IS_EXPLOSION) || DragonConfig.chainReactionEndCrystals) {
                DamageSource explosionDamageSource = damageSource.getEntity() != null ? this.damageSources().explosion(this, damageSource.getEntity()) : null;
                this.level().explode(this, explosionDamageSource, null, this.getX(), this.getY(), this.getZ(), 6.0f, false, Level.ExplosionInteraction.BLOCK);

                if(DragonConfig.endCrystalSpawns) {
                    if (damageSource.getEntity() instanceof Player player) {
                        if (player.position().distanceToSqr(position()) < 100) {
                            int count = random.nextInt(5, 10);
                            for (int i = 0; i < count; i++) {
                                spawnEndermite();
                            }
                        } else {
                            int count = random.nextInt(3, 7);
                            for (int i = 0; i < count; i++) {
                                spawnPhantoms(player);
                            }
                        }
                    }
                }
            }

            this.onDestroyedBy(damageSource);
        }
        return true;
    }

    @Unique
    private void spawnEndermite() {
        var endermite = new Endermite(EntityType.ENDERMITE, level());
        endermite.setPos(position());
        level().addFreshEntity(endermite);
    }

    @Unique
    private void spawnPhantoms(LivingEntity target) {
        var phantom = new Phantom(EntityType.PHANTOM, level());
        phantom.setPos(position());
        phantom.setTarget(target);
        level().addFreshEntity(phantom);
    }
}
