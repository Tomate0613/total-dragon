package tomate.totaldragon.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tomate.totaldragon.FightState;

import java.util.List;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Shadow
    @Final
    private List<ServerPlayer> players;

    @Inject(at = @At("HEAD"), method = "placeNewPlayer")
    void onPlayerJoined(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        FightState.onPlayerJoin();
    }

    @Inject(at = @At("HEAD"), method = "remove")
    void onPlayerLeave(ServerPlayer serverPlayer, CallbackInfo ci) {
        FightState.onPlayerLeave();
    }
}
