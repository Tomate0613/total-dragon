package tomate.totaldragon.mixin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tomate.totaldragon.TotalDragon;

import java.util.List;

@Mixin(DragonFireball.class)
public class DragonFireballMixin extends AbstractHurtingProjectile {
    @Unique
    private Vec3 startingPos;

    protected DragonFireballMixin(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return false;
    }

    @Override
    public void moveTo(double x, double y, double z, float rotY, float rotX) {
        super.moveTo(x, y, z, rotY, rotX);
        startingPos = new Vec3(x, y, z);
    }

    @Override
    public void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (hitResult.getType() == HitResult.Type.ENTITY && this.ownedBy(((EntityHitResult)hitResult).getEntity())) {
            return;
        }
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(100, 100, 100));

        double nearestDist = 10000;
        LivingEntity nearestEntity = null;

        if (!list.isEmpty()) {
            for (LivingEntity livingEntity : list) {
                double d = this.distanceToSqr(livingEntity);
                if (d > nearestDist || !(livingEntity instanceof Player)) continue;

                nearestDist = d;
                nearestEntity = livingEntity;
                break;
            }
        }

        if(nearestEntity == null) {
            return;
        }

        if (this.level().isClientSide || !(nearestEntity instanceof ServerPlayer))
            return;

        TotalDragon.spawnParticleLine(startingPos, nearestEntity.position(), 10, (ServerPlayer) nearestEntity, this.level());

        AreaEffectCloud areaEffectCloud = new AreaEffectCloud(this.level(), nearestEntity.getX(), nearestEntity.getY() + 0.5, nearestEntity.getZ());

        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity) {
            areaEffectCloud.setOwner((LivingEntity)entity);
        }

        areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
        areaEffectCloud.setRadius(level().getRandom().nextIntBetweenInclusive(10, 14)); // 6 is a good value for easier difficulty, which I might add in the future
        areaEffectCloud.setDuration(600);
        areaEffectCloud.setRadiusPerTick(0.001f); // 0.01f is a good value for easier difficulty
        areaEffectCloud.setWaitTime(20);
        areaEffectCloud.addEffect(new MobEffectInstance(TotalDragon.DRAGON_BREATH, 1, 2));
        ((AreaEffectCloudReapplicationDelayAccessor)areaEffectCloud).reapplicationDelay(10);
        areaEffectCloud.setRadiusOnUse(1);

        this.level().levelEvent(2006, this.blockPosition(), this.isSilent() ? -1 : 1);
        this.level().addFreshEntity(areaEffectCloud);
        this.discard();
    }
}
