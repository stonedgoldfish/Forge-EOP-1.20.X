package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.StringProperty;

public class ExtinguishFireAbility extends Ability {

    public static final PalladiumProperty<Boolean> PLAY_EFFECTS =
            new BooleanProperty("play_effects")
                    .configurable("Whether an effect should play when fire is extinguished.");

    public static final PalladiumProperty<String> SOUND =
            new StringProperty("sound")
                    .configurable("Sound played when fire is extinguished.");

    public static final PalladiumProperty<Float> SOUND_VOLUME =
            new FloatProperty("sound_volume")
                    .configurable("Volume of the extinguish sound.");

    public static final PalladiumProperty<Float> SOUND_PITCH =
            new FloatProperty("sound_pitch")
                    .configurable("Pitch of the extinguish sound.");

    public ExtinguishFireAbility() {
        this.withProperty(ICON, new ItemIcon(Items.WATER_BUCKET));

        this.withProperty(PLAY_EFFECTS, true);
        this.withProperty(SOUND, "minecraft:block.fire.extinguish");
        this.withProperty(SOUND_VOLUME, 1.0F);
        this.withProperty(SOUND_PITCH, 1.0F);
    }

    private static final java.util.Map<java.util.UUID, Integer> SAFE_TICKS = new java.util.HashMap<>();

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide || !enabled) {
            return;
        }

        java.util.UUID uuid = entity.getUUID();

        boolean standingInFire = entity.fireImmune()
                || entity.level().getBlockState(entity.blockPosition()).is(net.minecraft.world.level.block.Blocks.FIRE)
                || entity.level().getBlockState(entity.blockPosition()).is(net.minecraft.world.level.block.Blocks.SOUL_FIRE);

        if (standingInFire) {
            SAFE_TICKS.put(uuid, 0);
            return;
        }

        int safeTicks = SAFE_TICKS.getOrDefault(uuid, 0) + 1;
        SAFE_TICKS.put(uuid, safeTicks);

        if (safeTicks < 5) {
            return;
        }

        if (!entity.isOnFire()) {
            return;
        }

        entity.clearFire();

        if (entry.getProperty(PLAY_EFFECTS)) {
            playSound(
                    entity,
                    entry.getProperty(SOUND),
                    entry.getProperty(SOUND_VOLUME),
                    entry.getProperty(SOUND_PITCH)
            );
        }

        SAFE_TICKS.remove(uuid);
    }

    private static void playSound(LivingEntity entity, String soundId, float volume, float pitch) {
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

        if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {

            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.POOF,
                    entity.getX(),
                    entity.getY() + 0.5D,
                    entity.getZ(),
                    12,
                    0.3D,
                    0.3D,
                    0.3D,
                    0.05D
            );
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Extinguishes the entity when they are on fire, with optional sound effects.";
    }
}