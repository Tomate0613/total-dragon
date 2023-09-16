package tomate.totaldragon.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tomate.totaldragon.DragonCurve;
import tomate.totaldragon.FightState;

import java.util.List;

import static tomate.totaldragon.FightState.dragon;
import static tomate.totaldragon.FightState.knockbackCooldown;

@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin extends Mob {
    @Shadow @Final private EnderDragonPhaseManager phaseManager;
    @Shadow private int growlTime;
    @Shadow public float oFlapTime;
    @Shadow public float flapTime;
    @Shadow protected abstract void checkCrystals();
    @Shadow public boolean inWall;
    @Shadow public int posPointer;
    @Shadow @Final public double[][] positions;
    @Shadow public float yRotA;
    @Shadow @Final private EnderDragonPart[] subEntities;
    @Shadow public abstract double[] getLatencyPos(int i, float f);
    @Shadow protected abstract void tickPart(EnderDragonPart enderDragonPart, double d, double e, double f);
    @Shadow @Final private EnderDragonPart body;
    @Shadow @Final private EnderDragonPart wing1;
    @Shadow @Final private EnderDragonPart wing2;
    @Shadow @Final public EnderDragonPart head;
    @Shadow @Final private EnderDragonPart neck;
    @Shadow protected abstract float getHeadYOffset();
    @Shadow @Final private EnderDragonPart tail1;
    @Shadow @Final private EnderDragonPart tail2;
    @Shadow @Final private EnderDragonPart tail3;
    @Shadow protected abstract float rotWrap(double d);
    @Shadow protected abstract boolean checkWalls(AABB aABB);
    @Shadow private @Nullable EndDragonFight dragonFight;
    @Shadow protected abstract void hurt(List<Entity> list);

    @Unique
    private final DragonCurve curve = new DragonCurve((EnderDragon)(Object) this);
    @Unique
    private final TargetingConditions DEATH_TARGETING = TargetingConditions.forCombat().range(200);


    public EnderDragonMixin(EntityType<? extends EnderDragon> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    void init(EntityType<?> entityType, Level level, CallbackInfo ci) {
        if(level.isClientSide)
            return;

        FightState.onEnderDragonSpawned((EnderDragon)(Object) this);
    }


    @Inject(at = @At("HEAD"), method = "onCrystalDestroyed")
    void onCrystalDestroyed(EndCrystal endCrystal, BlockPos blockPos, DamageSource damageSource, CallbackInfo ci) {
        BlockPos.betweenClosed(blockPos.offset(-5, -25, -5), blockPos.offset(5, 0, 5)).forEach(blockPos1 -> {
            var blockState = level().getBlockState(blockPos1);

            var distance = Mth.clamp((blockPos.getY() - blockPos1.getY()) / (float)(25 + 1), .1, .9);

            if(blockState.getBlock() == Blocks.OBSIDIAN && random.nextFloat() > distance) {
                level().setBlockAndUpdate(blockPos1, Blocks.CRYING_OBSIDIAN.defaultBlockState());
            }
        });

        if(dragonFight == null)
            return;

        FightState.crystalsDestroyed++;
        FightState.crystalPositions.add(blockPos);
    }

    @Unique
    private void growl() {
        if (!isSilent() && !phaseManager.getCurrentPhase().isSitting() && --growlTime < 0) {
            level().playLocalSound(getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_GROWL, getSoundSource(), 2.5F, 0.8F + random.nextFloat() * 0.3F, false);
            growlTime = 200 + random.nextInt(210);
        }
    }

    @Unique
    private void spawnDeathParticles() {
        float xParticleOffset = (random.nextFloat() - .5f) * 8f;
        float yParticleOffset = (random.nextFloat() - .5f) * 4f;
        float zParticleOffset = (random.nextFloat() - .5f) * 8f;

        level().addParticle(ParticleTypes.EXPLOSION, getX() + xParticleOffset, getY() + 2.0D + yParticleOffset, getZ() + zParticleOffset, 0, 0, 0);
    }


    @Unique
    private void knockBack(List<Entity> entities) {
        double centerX = (body.getBoundingBox().minX + body.getBoundingBox().maxX) / 2;
        double centerZ = (body.getBoundingBox().minZ + body.getBoundingBox().maxZ) / 2;

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity)) continue;

            float deltaX = (float) (entity.getX() - centerX);
            float deltaZ = (float) (entity.getZ() - centerZ);
            Vec2 normalizedDeltaMovement = new Vec2(deltaX, deltaZ).normalized();

            if (phaseManager.getCurrentPhase().isSitting() || --knockbackCooldown > 0) continue;

            knockbackCooldown = 10;

            var knockbackStrength = Mth.lerp(10, 1, entity.position().length() / 200);;

            entity.setDeltaMovement(new Vec3(normalizedDeltaMovement.x * knockbackStrength, 3, normalizedDeltaMovement.y * knockbackStrength));
            entity.hasImpulse = true;

            if(!level().isClientSide) {
                entity.hurt(damageSources().mobAttack(this), 5.0f);
                doEnchantDamageEffects(this, entity);
            }
        }
    }

    @Unique
    private void kickPlayers() {
        knockBack(level().getEntities(this, wing1.getBoundingBox().inflate(4.0D, 2.0D, 4.0D).move(0.0D, -2.0D, 0.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
        knockBack(level().getEntities(this, wing2.getBoundingBox().inflate(4.0D, 2.0D, 4.0D).move(0.0D, -2.0D, 0.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));

        hurt(level().getEntities((EnderDragon) (Object)this, head.getBoundingBox().inflate(1.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
        hurt(level().getEntities((EnderDragon) (Object)this, neck.getBoundingBox().inflate(1.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
    }

    @Unique
    private void updateDragonTailSegment(EnderDragonPart dragonPart, int tailSegmentIndex, float sinYRotation, float cosYRotation, float sinDifference, float cosDifference, double[] previousPosition) {
        double[] currentPosition = getLatencyPos(12 + tailSegmentIndex * 2, 1.0F);
        float segmentYaw = (float) (Math.toRadians(getYRot()) +  Math.toRadians(rotWrap(currentPosition[0] - previousPosition[0])));
        float segmentPitch = Mth.sin(segmentYaw);
        float segmentRoll = Mth.cos(segmentYaw);
        float segmentMultiplier = (float)(tailSegmentIndex + 1) * 2.0F;
        tickPart(dragonPart, (-(sinYRotation * 1.5F + segmentPitch * segmentMultiplier) * cosDifference), currentPosition[1] - previousPosition[1] - (double)((segmentMultiplier + 1.5F) * sinDifference) + 1.5D, ((cosYRotation * 1.5F + segmentRoll * segmentMultiplier) * cosDifference));
    }

    @Unique
    private void updateOldSubEntityPositions(Vec3[] oldPositions) {
        for(int i = 0; i < subEntities.length; ++i) {
            subEntities[i].xo = oldPositions[i].x;
            subEntities[i].yo = oldPositions[i].y;
            subEntities[i].zo = oldPositions[i].z;

            subEntities[i].xOld = oldPositions[i].x;
            subEntities[i].yOld = oldPositions[i].y;
            subEntities[i].zOld = oldPositions[i].z;
        }
    }
    @Unique
    private void tickParts() {
        Vec3[] subEntityPositions = new Vec3[subEntities.length];

        for (int i = 0; i < subEntities.length; ++i) {
            subEntityPositions[i] = subEntities[i].position();
        }


        float differenceInYPos = (float) (getLatencyPos(5, 1.0F)[1] - getLatencyPos(10, 1.0F)[1]) * 10.0F * 0.017453292F;
        float cosineOfDifference = Mth.cos(differenceInYPos);
        float sineOfDifference = Mth.sin(differenceInYPos);
        float yRotationInRadians = (float) Math.toRadians(getYRot());
        float sineOfYRotation = Mth.sin(yRotationInRadians);
        float cosineOfYRotation = Mth.cos(yRotationInRadians);

        tickPart(body, (sineOfYRotation * 0.5F), 0.0D, -cosineOfYRotation * 0.5F);
        tickPart(wing1, (cosineOfYRotation * 4.5F), 2.0D, sineOfYRotation * 4.5F);
        tickPart(wing2, (cosineOfYRotation * -4.5F), 2.0D, sineOfYRotation * -4.5F);

        if (hurtTime == 0) {
            kickPlayers();
        }

        float sinOfDeltaYRot = Mth.sin((float) (Math.toRadians(getYRot()) - yRotA * 0.01F));
        float cosOfDeltaYRot = Mth.cos((float) (Math.toRadians(getYRot()) - yRotA * 0.01F));

        float headYOffset = getHeadYOffset();

        tickPart(head, (sinOfDeltaYRot * 6.5F * cosineOfDifference), (headYOffset + sineOfDifference * 6.5F), (-cosOfDeltaYRot * 6.5F * cosineOfDifference));
        tickPart(neck, (sinOfDeltaYRot * 5.5F * cosineOfDifference), (headYOffset + sineOfDifference * 5.5F), (-cosOfDeltaYRot * 5.5F * cosineOfDifference));

        double[] latencyPos = getLatencyPos(5, 1.0F);

        updateDragonTailSegment(tail1, 0, sineOfYRotation, cosineOfYRotation, sineOfDifference, cosineOfDifference, latencyPos);
        updateDragonTailSegment(tail2, 1, sineOfYRotation, cosineOfYRotation, sineOfDifference, cosineOfDifference, latencyPos);
        updateDragonTailSegment(tail3, 2, sineOfYRotation, cosineOfYRotation, sineOfDifference, cosineOfDifference, latencyPos);


        updateOldSubEntityPositions(subEntityPositions);
    }

    @Unique
    private void moveTowardsFlyTargetLocation(DragonPhaseInstance currentPhase) {
        Vec3 flyTargetLocation = currentPhase.getFlyTargetLocation();

        if (flyTargetLocation == null)
            return;

        curve.nextStep(flyTargetLocation);
    }

    @Unique
    private void tickCurrentPhase() {
        DragonPhaseInstance currentPhase = phaseManager.getCurrentPhase();
        currentPhase.doServerTick();

        // If the phase has changed in that tick we tick the new phase
        if (phaseManager.getCurrentPhase() != currentPhase) {
            currentPhase = phaseManager.getCurrentPhase();
            currentPhase.doServerTick();
        }

        moveTowardsFlyTargetLocation(currentPhase);
    }

    @Unique
    private void killFightEntities() {
        var endIslandEnderMans = level().getNearbyEntities(EnderMan.class, DEATH_TARGETING, this, new AABB(-100, -100, -100, 100, 100, 100));
        var endIslandPhantoms = level().getNearbyEntities(Phantom.class, DEATH_TARGETING, this, new AABB(-100, -100, -100, 100, 100, 100));

        endIslandEnderMans.forEach(enderMan -> {
            if(enderMan.isAggressive()) {
                enderMan.kill();
            }
        });

        endIslandPhantoms.forEach(LivingEntity::kill);
    }


    @Override
    public void aiStep() {
        processFlappingMovement();
        oFlapTime = flapTime;

        if(FightState.vulnerableEndCrystalPos != null) {
            var entities = level().getEntities(null, new AABB(FightState.vulnerableEndCrystalPos.x - 1, FightState.vulnerableEndCrystalPos.y - 1, FightState.vulnerableEndCrystalPos.z - 1, FightState.vulnerableEndCrystalPos.x + 1, FightState.vulnerableEndCrystalPos.y + 1, FightState.vulnerableEndCrystalPos.z + 1));

            entities.forEach(entity -> {
                if(entity instanceof EndCrystal crystal) {
                    crystal.setInvulnerable(false);
                    crystal.setBeamTarget(null);
                }
            });
        }

        if(dragon == null)
            dragon = (EnderDragon)(Object)this;

        if (dragonFight == null) {
            if (level() instanceof ServerLevel serverLevel) {
                EndDragonFight endDragonFight = serverLevel.getDragonFight();

                if (endDragonFight != null && getUUID().equals(endDragonFight.getDragonUUID())) {
                    dragonFight = endDragonFight;

                    FightState.resetData();
                }
            }
        }

        if (level().isClientSide) {
            setHealth(getHealth());
            growl();
        }

        if (isDeadOrDying()) {
            killFightEntities();

            spawnDeathParticles();
            return;
        }

        checkCrystals();
        Vec3 deltaMovement = getDeltaMovement();

        float movementModifier = 0.2F / ((float)deltaMovement.horizontalDistance() * 10.0F + 1.0F);
        movementModifier *= (float)Math.pow(2.0D, deltaMovement.y);


        if (phaseManager.getCurrentPhase().isSitting()) {
            flapTime += 0.1F;
        } else if (inWall) {
            flapTime += movementModifier * 0.5F;
        } else {
            flapTime += movementModifier;
        }

        setYRot(Mth.wrapDegrees(getYRot()));
        if (isNoAi()) {
            flapTime = 0.5F;
            return;
        }

        if (posPointer < 0) {
            for(int i = 0; i < positions.length; ++i) {
                positions[i][0] = getYRot();
                positions[i][1] = getY();
            }
        }

        if (++posPointer == positions.length) {
            posPointer = 0;
        }

        positions[posPointer][0] = getYRot();
        positions[posPointer][1] = getY();

        if (level().isClientSide) {
            if (lerpSteps > 0) {
                double lerpedX = getX() + (lerpX - getX()) / (double)lerpSteps;
                double lerpedY = getY() + (lerpY - getY()) / (double)lerpSteps;
                double lerpedZ = getZ() + (lerpZ - getZ()) / (double)lerpSteps;

                double deltaZ = Mth.wrapDegrees(lerpYRot - (double)getYRot());

                setYRot(getYRot() + (float)deltaZ / (float)lerpSteps);
                setXRot(getXRot() + (float)(lerpXRot - (double)getXRot()) / (float)lerpSteps);

                --lerpSteps;

                setPos(lerpedX, lerpedY, lerpedZ);
                setRot(getYRot(), getXRot());
            }

            phaseManager.getCurrentPhase().doClientTick();
        } else {
            tickCurrentPhase();
        }

        yBodyRot = getYRot();

        tickParts();

        if (!level().isClientSide) {
            inWall = checkWalls(head.getBoundingBox()) | checkWalls(neck.getBoundingBox()) | checkWalls(body.getBoundingBox());
            if (dragonFight != null) {
                dragonFight.updateDragon((EnderDragon) (Object)this);
            }
        }

    }
}
