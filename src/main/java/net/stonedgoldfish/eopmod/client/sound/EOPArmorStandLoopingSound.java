package net.stonedgoldfish.eopmod.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.decoration.ArmorStand;

public class EOPArmorStandLoopingSound extends AbstractTickableSoundInstance {

    private final ArmorStand armorStand;

    public EOPArmorStandLoopingSound(
            ArmorStand armorStand,
            ResourceLocation sound,
            float volume,
            float pitch
    ) {
        super(
                SoundEvent.createVariableRangeEvent(sound),
                SoundSource.PLAYERS,
                SoundInstance.createUnseededRandom()
        );

        this.armorStand = armorStand;
        this.looping = true;
        this.delay = 0;
        this.volume = volume;
        this.pitch = pitch;

        this.x = armorStand.getX();
        this.y = armorStand.getY();
        this.z = armorStand.getZ();
    }

    @Override
    public void tick() {
        if (armorStand == null || !armorStand.isAlive() || armorStand.isRemoved()) {
            this.stop();
            return;
        }

        this.x = armorStand.getX();
        this.y = armorStand.getY();
        this.z = armorStand.getZ();
    }
}