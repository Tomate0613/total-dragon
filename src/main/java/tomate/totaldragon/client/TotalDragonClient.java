package tomate.totaldragon.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.particle.FlameParticle;
import tomate.totaldragon.ShockwaveParticle;
import tomate.totaldragon.TotalDragon;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class TotalDragonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(TotalDragon.SHOCKWAVE, ShockwaveParticle.Provider::new);
    }
}
