package tomate.totaldragon.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tomate.totaldragon.TotalDragon;

@Mixin(EnderDragonPhaseManager.class)
public class EnderDragonPhaseManagerMixin {
    @Shadow @Final private EnderDragon dragon;

    @Inject(at = @At("HEAD"), method = "setPhase")
    void setPhase(EnderDragonPhase<?> enderDragonPhase, CallbackInfo ci) {
        if(!TotalDragon.CONFIG.logDragonPhasesToInGameChat())
            return;

        MinecraftServer server = this.dragon.getServer();

        if(server == null)
            return;

        Component message = Component.literal("Changed phase to " + enderDragonPhase.toString());

        var players = server.getPlayerList().getPlayers();

        for (ServerPlayer serverPlayer : players) {
            if(serverPlayer.hasPermissions(2))
                serverPlayer.sendSystemMessage(message, false);
        }
    }
}
