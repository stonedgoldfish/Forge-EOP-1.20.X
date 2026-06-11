package net.stonedgoldfish.eopmod.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class FallingBloodParticle extends TextureSheetParticle {

    private final Fluid type;

    protected FallingBloodParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            Fluid type
    ) {
        super(level, x, y, z);

        this.type = type;
        this.setSize(0.01F, 0.01F);
        this.gravity = 0.06F;
        this.lifetime = (int)(64.0D / (Math.random() * 0.8D + 0.2D));

        this.setColor(0.65F, 0.0F, 0.0F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.lifetime-- <= 0) {
            this.remove();
            return;
        }

        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);

        if (this.onGround) {
            this.remove();
            this.level.addParticle(
                    EOPParticles.LANDING_BLOOD.get(),
                    this.x,
                    this.y,
                    this.z,
                    0.0D,
                    0.0D,
                    0.0D
            );
            return;
        }

        this.xd *= 0.98D;
        this.yd *= 0.98D;
        this.zd *= 0.98D;

        if (this.type != Fluids.EMPTY) {
            BlockPos blockPos = BlockPos.containing(this.x, this.y, this.z);
            FluidState fluidState = this.level.getFluidState(blockPos);

            if (fluidState.getType() == this.type
                    && this.y < blockPos.getY() + fluidState.getHeight(this.level, blockPos)) {
                this.remove();
            }
        }
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
            FallingBloodParticle particle = new FallingBloodParticle(
                    level,
                    x,
                    y,
                    z,
                    Fluids.WATER
            );

            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}