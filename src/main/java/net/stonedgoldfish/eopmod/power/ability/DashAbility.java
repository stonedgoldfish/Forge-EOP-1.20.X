package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.StringProperty;
import net.threetag.palladium.util.property.BooleanProperty;

public class DashAbility extends Ability {

    public static final PalladiumProperty<Float> STRENGTH = new FloatProperty("strength").configurable("How strong the dash is");
    public static final PalladiumProperty<Boolean> ADD_PITCH = new BooleanProperty("add_pitch").configurable("If true, dashes follow the player's pitch");
    public static final PalladiumProperty<String> SOUND = new StringProperty("sound").configurable("Sound played when the dash activates");
    public static final PalladiumProperty<String> PARTICLE = new StringProperty("particle").configurable("Particle spawned when the dash activates");
    public static final PalladiumProperty<Float> SOUND_VOLUME = new FloatProperty("sound_volume").configurable("Volume of the dash activation sound");
    public static final PalladiumProperty<Float> SOUND_PITCH = new FloatProperty("sound_pitch").configurable("Pitch of the dash activation sound");
    public static final PalladiumProperty<Integer> PARTICLE_AMOUNT = new IntegerProperty("particle_amount").configurable("Amount of particles spawned when dashing");
    public static final PalladiumProperty<Float> PARTICLE_SPEED = new FloatProperty("particle_speed").configurable("Speed of the particles spawned when dashing");

    public DashAbility() {
        this.withProperty(ICON, new ItemIcon(Items.FEATHER));
        this.withProperty(STRENGTH, 1.5F);
        this.withProperty(ADD_PITCH, false);
        this.withProperty(SOUND, "minecraft:entity.player.attack.sweep");
        this.withProperty(PARTICLE, "minecraft:cloud");
        this.withProperty(SOUND_VOLUME, 1.0F);
        this.withProperty(SOUND_PITCH, 1.0F);
        this.withProperty(PARTICLE_AMOUNT, 12);
        this.withProperty(PARTICLE_SPEED, 0.05F);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled) {
            return;
        }

        if (entity.level().isClientSide) {
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                    net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> net.stonedgoldfish.eopmod.client.EOPClientDashHelper.dash(entry.getProperty(STRENGTH),entry.getProperty(ADD_PITCH))
            );

            return;
        }

        playDashSound(
                entity,
                entry.getProperty(SOUND),
                entry.getProperty(SOUND_VOLUME),
                entry.getProperty(SOUND_PITCH)
        );

        spawnDashParticles(
                entity,
                entry.getProperty(PARTICLE),
                entry.getProperty(PARTICLE_AMOUNT),
                entry.getProperty(PARTICLE_SPEED)
        );
    }

    private static void playDashSound(LivingEntity entity, String soundId, float volume, float pitch) {
        ResourceLocation soundLocation = ResourceLocation.tryParse(soundId);

        if (soundLocation == null) {
            return;
        }

        SoundEvent sound = SoundEvent.createVariableRangeEvent(soundLocation);

        entity.level().playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                sound,
                SoundSource.PLAYERS,
                volume,
                pitch
        );
    }

    private static void spawnDashParticles(
            LivingEntity entity,
            String particleId,
            int amount,
            float speed
    ) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ResourceLocation particleLocation = ResourceLocation.tryParse(particleId);

        if (particleLocation == null) {
            return;
        }

        var particleType = BuiltInRegistries.PARTICLE_TYPE.get(particleLocation);

        if (!(particleType instanceof net.minecraft.core.particles.SimpleParticleType simpleParticleType)) {
            return;
        }

        serverLevel.sendParticles(
                simpleParticleType,
                entity.getX(),
                entity.getY() + 1.0D,
                entity.getZ(),
                amount,
                0.25D,
                0.25D,
                0.25D,
                speed
        );
    }

    @Override
    public String getDocumentationDescription() {
        return "Dashes the entity.";
    }
}