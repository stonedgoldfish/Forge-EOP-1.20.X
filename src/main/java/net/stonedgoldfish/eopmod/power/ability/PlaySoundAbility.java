package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.stonedgoldfish.eopmod.client.sound.EOPLoopingEntitySound;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.ResourceLocationProperty;
import net.threetag.palladium.util.property.BooleanProperty;

import java.util.HashMap;
import java.util.Map;

public class PlaySoundAbility extends Ability {

    public static final PalladiumProperty<ResourceLocation> SOUND =
            new ResourceLocationProperty("sound")
                    .configurable("Sound ID that is played.");

    public static final PalladiumProperty<Float> VOLUME =
            new FloatProperty("volume")
                    .configurable("Sound volume. Higher values also increase audible range.");

    public static final PalladiumProperty<Float> PITCH =
            new FloatProperty("pitch")
                    .configurable("Sound pitch.");

    public static final PalladiumProperty<Boolean> PLAY_SELF =
            new BooleanProperty("play_self")
                    .configurable("If true, only the caster hears the sound.");

    public PlaySoundAbility() {
        this.withProperty(ICON, new ItemIcon(Items.NOTE_BLOCK));
        this.withProperty(SOUND, ResourceLocation.parse("minecraft:block.beacon.ambient"));
        this.withProperty(VOLUME, 1.0F);
        this.withProperty(PITCH, 1.0F);
        this.withProperty(PLAY_SELF, false);
    }

    @Override
    public boolean isEffect() {
        return true;
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled) {
            return;
        }

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientSoundHandler.start(
                        entity,
                        getSoundKey(entity, entry),
                        entry.getProperty(SOUND),
                        entry.getProperty(VOLUME),
                        entry.getProperty(PITCH),
                        entry.getProperty(PLAY_SELF)
                )
        );
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientSoundHandler.stop(getSoundKey(entity, entry))
        );
    }

    private static String getSoundKey(LivingEntity entity, AbilityInstance entry) {
        return entity.getId() + ":" + entry.getReference();
    }

    private static class ClientSoundHandler {

        private static final Map<String, EOPLoopingEntitySound> ACTIVE_SOUNDS = new HashMap<>();

        private static void start(
                LivingEntity entity,
                String key,
                ResourceLocation sound,
                float volume,
                float pitch,
                boolean playSelf
        ) {
            if (playSelf) {
                var player = net.minecraft.client.Minecraft.getInstance().player;

                if (player == null || player != entity) {
                    return;
                }
            }

            stop(key);

            EOPLoopingEntitySound soundInstance =
                    new EOPLoopingEntitySound(entity, sound, volume, pitch);

            ACTIVE_SOUNDS.put(key, soundInstance);

            net.minecraft.client.Minecraft.getInstance()
                    .getSoundManager()
                    .play(soundInstance);
        }

        private static void stop(String key) {
            EOPLoopingEntitySound sound = ACTIVE_SOUNDS.remove(key);

            if (sound != null) {
                sound.forceStop();
            }
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Plays a looping sound that follows the entity and stops when the ability ends.";
    }
}