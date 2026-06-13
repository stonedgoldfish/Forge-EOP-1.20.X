package net.stonedgoldfish.eopmod.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class LightEnergyParticle extends TextureSheetParticle {

    private final SpriteSet sprites;
    protected LightEnergyParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            SpriteSet sprites
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        this.sprites = sprites;
        this.gravity = 0.002F;
        this.lifetime = 10;
        this.quadSize *= 2F;

        this.xd = xSpeed;
        this.yd = ySpeed + 0.03D;
        this.zd = zSpeed;

        this.setColor(1.0F, 1.0F, 1.0F);
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void tick() {
        super.tick();

        this.setSpriteFromAge(this.sprites);
        this.alpha = 1.0F - ((float) this.age / (float) this.lifetime);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 15728880;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            LightEnergyParticle particle = new LightEnergyParticle(
                    level,
                    x,
                    y,
                    z,
                    xSpeed,
                    ySpeed,
                    zSpeed,
                    this.sprites
            );

            return particle;
        }
    }
}