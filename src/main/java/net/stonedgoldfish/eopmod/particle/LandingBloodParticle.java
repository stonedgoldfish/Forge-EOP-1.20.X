package net.stonedgoldfish.eopmod.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class LandingBloodParticle extends TextureSheetParticle {

    protected LandingBloodParticle(
            ClientLevel level,
            double x,
            double y,
            double z
    ) {
        super(level, x, y, z);

        this.setSize(0.01F, 0.01F);
        this.lifetime = 8;
        this.gravity = 0.0F;
        this.quadSize *= 0.6F;

        this.setColor(0.65F, 0.0F, 0.0F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            LandingBloodParticle particle =
                    new LandingBloodParticle(level, x, y, z);

            particle.pickSprite(this.sprites);

            return particle;
        }
    }
}