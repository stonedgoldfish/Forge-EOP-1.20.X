package net.stonedgoldfish.eopmod.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.stonedgoldfish.eopmod.power.ability.CustomFlightAbility;

public class EOPFlightSound extends AbstractTickableSoundInstance {

    private final Player player;
    private int time;
    public boolean stop;

    private static final float MAX_VOLUME = 0.28F;

    public EOPFlightSound(Player player) {
        super(SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());

        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0F;
    }

    @Override
    public void tick() {
        if (this.stop) {
            this.stop();
            return;
        }

        ++this.time;

        boolean sprintFlying = this.player.isAlive()
                && CustomFlightAbility.hasCustomFlight(this.player)
                && CustomFlightAbility.isFlying(this.player)
                && this.player.isSprinting();

        if (sprintFlying) {
            this.x = (float) this.player.getX();
            this.y = (float) this.player.getY();
            this.z = (float) this.player.getZ();

            float movementVolume = (float) this.player.getDeltaMovement().lengthSqr() / 4F;

            if ((double) movementVolume >= 1.0E-7D) {
                this.volume = Mth.clamp(movementVolume / 4.0F, 0.0F, MAX_VOLUME);
            } else {
                this.volume = 0.0F;
            }

            // Fade in like Palladium
            if (this.time < 20) {
                this.volume = 0.0F;
            } else if (this.time < 40) {
                this.volume = this.volume * ((this.time - 20) / 20.0F);
            }

            float pitchThreshold = MAX_VOLUME * 0.8F;

            if (this.volume > pitchThreshold) {
                this.pitch = 1.0F + (this.volume - pitchThreshold);
            } else {
                this.pitch = 1.0F;
            }

        } else {
            this.stop();
            EOPFlightSoundHandler.clear();
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}