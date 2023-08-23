package tomate.totaldragon;

import com.mojang.blaze3d.vertex.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;


public class ShockwaveParticle {
    @Environment(value= EnvType.CLIENT)
    public static class Provider
            implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double x, double y, double z, double dX, double dY, double dZ) {
            ShockwaveOverlayParticle overlayParticle = new ShockwaveOverlayParticle(clientLevel, x, y, z);
            overlayParticle.pickSprite(this.sprite);
            return overlayParticle;
        }
    }


    @Environment(value=EnvType.CLIENT)
    public static class ShockwaveOverlayParticle
            extends TextureSheetParticle {
        ShockwaveOverlayParticle(ClientLevel clientLevel, double x, double y, double z) {
            super(clientLevel, x, y, z);
            this.lifetime = 15;
        }

        @Override
        public @NotNull ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        @Override
        public void render(VertexConsumer vertexConsumer, Camera camera, float size) {

            this.setAlpha(0.9f);
            super.render(vertexConsumer, camera, size);
        }

        @Override
        public float getQuadSize(float f) {
            return 2f * this.age;
        }
    }
}
