package tomate.totaldragon.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonHoldingPatternPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tomate.totaldragon.FightState;

@Mixin(DragonHoldingPatternPhase.class)
public abstract class DragonHoldingPatternPhaseMixin extends AbstractDragonPhaseInstance {
    @Shadow private @Nullable Path currentPath;

    @Unique
    private static final TargetingConditions CHARGE_TARGETING = TargetingConditions.forCombat().range(200.0); // 150 normally

    public DragonHoldingPatternPhaseMixin(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Inject(at = @At("HEAD"), method = "findNewTarget", cancellable = true)
    void findNewTarget(CallbackInfo ci) {
        if (this.currentPath == null || !this.currentPath.isDone())
            return;

        int numberOfEndCrystals = this.dragon.getDragonFight() == null ? 0 : this.dragon.getDragonFight().getCrystalsAlive();

        if (this.dragon.getRandom().nextInt(numberOfEndCrystals + 3) == 0) {
            var nearestPlayer = dragon.level().getNearestPlayer(CHARGE_TARGETING, dragon, dragon.getX(), dragon.getY(), dragon.getZ());

            if(nearestPlayer != null) {
                FightState.targetPlayer = (ServerPlayer) nearestPlayer;

                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                ci.cancel();
            }
        }
    }
}
