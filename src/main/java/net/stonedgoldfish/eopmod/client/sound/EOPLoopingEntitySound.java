package net.stonedgoldfish.eopmod.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;

public class EOPLoopingEntitySound extends AbstractTickableSoundInstance {

    private final LivingEntity entity;
    private final float baseVolume;

    public EOPLoopingEntitySound(
            LivingEntity entity,
            ResourceLocation sound,
            float volume,
            float pitch
    ) {
        super(
                SoundEvent.createVariableRangeEvent(sound),
                entity.getSoundSource(),
                SoundInstance.createUnseededRandom()
        );

        this.entity = entity;
        this.baseVolume = volume;
        this.pitch = pitch;

        this.looping = true;
        this.delay = 0;

        this.x = entity.getX();
        this.y = entity.getY() + entity.getBbHeight() * 0.5D;
        this.z = entity.getZ();
        this.volume = baseVolume;
    }

    @Override
    public void tick() {
        if (entity == null || !entity.isAlive() || entity.isRemoved()) {
            this.stop();
            return;
        }

        this.x = entity.getX();
        this.y = entity.getY() + entity.getBbHeight() * 0.5D;
        this.z = entity.getZ();

        this.volume = baseVolume;
    }

    public void forceStop() {
        this.stop();
    }
}