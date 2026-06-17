package net.stonedgoldfish.eopmod.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class GlitchParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    private final float baseXOffset;
    private final float baseYOffset;
    private final float baseZOffset;

    protected GlitchParticle(
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

        this.lifetime = 12 + this.random.nextInt(10);

        this.gravity = 0.0F;
        this.friction = 0.85F;
        this.hasPhysics = false;

        this.quadSize = 0.18F + this.random.nextFloat() * 0.35F;

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.baseXOffset = (this.random.nextFloat() - 0.5F) * 0.25F;
        this.baseYOffset = (this.random.nextFloat() - 0.5F) * 0.25F;
        this.baseZOffset = (this.random.nextFloat() - 0.5F) * 0.25F;

        randomizeColor();

        this.setSpriteFromAge(sprites);
    }

    private double renderJoltX;
    private double renderJoltY;
    private double renderJoltZ;

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        double oldX = this.x;
        double oldY = this.y;
        double oldZ = this.z;

        double oldXo = this.xo;
        double oldYo = this.yo;
        double oldZo = this.zo;

        this.x += this.renderJoltX;
        this.y += this.renderJoltY;
        this.z += this.renderJoltZ;

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        super.render(buffer, camera, 0.0F);

        this.x = oldX;
        this.y = oldY;
        this.z = oldZ;

        this.xo = oldXo;
        this.yo = oldYo;
        this.zo = oldZo;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.move(this.xd, this.yd, this.zd);

        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;

        if (this.age % 2 == 0) {
            this.renderJoltX = (this.random.nextDouble() - 0.5D) * 1.8D;
            this.renderJoltY = (this.random.nextDouble() - 0.5D) * 0.8D;
            this.renderJoltZ = (this.random.nextDouble() - 0.5D) * 1.8D;

            randomizeColor();
        } else {
            this.renderJoltX = 0.0D;
            this.renderJoltY = 0.0D;
            this.renderJoltZ = 0.0D;
        }

        if (this.age % 3 == 0) {
            this.quadSize = 0.12F + this.random.nextFloat() * 0.55F;
        }

        this.setSpriteFromAge(this.sprites);
    }

    private void randomizeColor() {
        int choice = this.random.nextInt(7);

        switch (choice) {
            case 0 -> setColor(1.0F, 0.0F, 0.0F);
            case 1 -> setColor(0.0F, 0.2F, 1.0F);
            case 2 -> setColor(0.0F, 1.0F, 0.0F);
            case 3 -> setColor(1.0F, 1.0F, 0.0F);
            case 4 -> setColor(0.6F, 0.0F, 1.0F);
            case 5 -> setColor(1.0F, 1.0F, 1.0F);
            default -> setColor(0.0F, 0.0F, 0.0F);
        }
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

        @Nullable
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
            return new GlitchParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}