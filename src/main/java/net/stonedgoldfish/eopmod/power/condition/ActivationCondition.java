package net.stonedgoldfish.eopmod.power.condition;

import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.condition.ActionCondition;
import net.threetag.palladium.condition.Condition;
import net.threetag.palladium.condition.ConditionEnvironment;
import net.threetag.palladium.condition.ConditionSerializer;
import net.threetag.palladium.condition.KeyCondition;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.ability.AbilityConfiguration;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.EntityPropertyHandler;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.StringProperty;

public class ActivationCondition extends KeyCondition {

    private final int ticks;
    private final boolean canToggleOff;
    private final int energy;
    private final String energyProperty;

    public ActivationCondition(
            int ticks,
            int cooldown,
            int energy,
            String energyProperty,
            boolean canToggleOff,
            AbilityConfiguration.KeyType type,
            boolean needsEmptyHand,
            boolean allowScrollingWhenCrouching
    ) {
        super(cooldown, type, needsEmptyHand, allowScrollingWhenCrouching);

        this.ticks = ticks;
        this.energy = energy;
        this.energyProperty = energyProperty;
        this.canToggleOff = canToggleOff;
    }

    @Override
    public boolean active(DataContext context) {
        LivingEntity entity = context.getLivingEntity();
        AbilityInstance entry = context.getAbility();

        if (entity == null || entry == null) {
            return false;
        }

        if (this.cooldown != 0 && entry.activationTimer == 1) {
            entry.startCooldown(entity, this.cooldown);
        }

        return entry.activationTimer > 0;
    }

    @Override
    public void onKeyPressed(
            LivingEntity entity,
            AbilityInstance entry,
            Power power,
            IPowerHolder holder
    ) {
        if (entry.cooldown > 0) {
            return;
        }

        if (entry.activationTimer > 0) {
            if (this.canToggleOff) {
                entry.activationTimer = 0;

                if (this.cooldown > 0) {
                    entry.startCooldown(entity, this.cooldown);
                }
            }

            return;
        }

        if (this.energy > 0 && this.energyProperty != null && !this.energyProperty.isBlank()) {

            boolean[] success = {false};

            EntityPropertyHandler.getHandler(entity).ifPresent(handler -> {
                PalladiumProperty<?> property = handler.getPropertyByName(this.energyProperty);

                if (property instanceof IntegerProperty integerProperty) {
                    int currentEnergy = handler.get(integerProperty);

                    if (currentEnergy >= this.energy) {
                        handler.set(integerProperty, Math.max(currentEnergy - this.energy, 0));
                        success[0] = true;
                    }
                }
            });

            if (!success[0]) {
                if (entity instanceof ServerPlayer player) {
                    player.displayClientMessage(
                            Component.literal("Not enough energy!")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );

                    player.level().playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(),
                            SoundSource.PLAYERS,
                            1.0F,
                            0.5F
                    );
                }

                return;
            }
        }

        entry.startActivationTimer(entity, this.ticks);
    }

    @Override
    public ConditionSerializer getSerializer() {
        return EOPConditions.ACTIVATION.get();
    }

    @Override
    public AbilityConfiguration.KeyPressType getKeyPressType() {
        return AbilityConfiguration.KeyPressType.ACTIVATION;
    }

    public static class Serializer extends ConditionSerializer {

        public static final PalladiumProperty<Integer> TICKS = new IntegerProperty("ticks").configurable("The amount of ticks the ability will stay active for");
        public static final PalladiumProperty<Integer> ENERGY = new IntegerProperty("energy").configurable("Energy consumed when the activation starts");
        public static final PalladiumProperty<String> PROPERTY = new StringProperty("property").configurable("Name of the integer Palladium property used as the energy source");
        public static final PalladiumProperty<Boolean> TOGGLE_MODE = new BooleanProperty("toggle_mode").configurable("If true, pressing again while active disables the ability early");

        public Serializer() {
            this.withProperty(ActionCondition.Serializer.COOLDOWN, 0);
            this.withProperty(TICKS, 60);
            this.withProperty(ENERGY, 0);
            this.withProperty(PROPERTY, "");
            this.withProperty(TOGGLE_MODE, false);
            this.withProperty(KeyCondition.KEY_TYPE_WITH_SCROLLING, AbilityConfiguration.KeyType.KEY_BIND);
            this.withProperty(KeyCondition.NEEDS_EMPTY_HAND, false);
            this.withProperty(KeyCondition.ALLOW_SCROLLING_DURING_CROUCHING, true);
        }

        @Override
        public Condition make(JsonObject json) {
            return new ActivationCondition(
                    this.getProperty(json, TICKS),
                    this.getProperty(json, ActionCondition.Serializer.COOLDOWN),
                    this.getProperty(json, ENERGY),
                    this.getProperty(json, PROPERTY),
                    this.getProperty(json, TOGGLE_MODE),
                    this.getProperty(json, KeyCondition.KEY_TYPE_WITH_SCROLLING),
                    this.getProperty(json, KeyCondition.NEEDS_EMPTY_HAND),
                    this.getProperty(json, KeyCondition.ALLOW_SCROLLING_DURING_CROUCHING)
            );
        }

        @Override
        public ConditionEnvironment getContextEnvironment() {
            return ConditionEnvironment.DATA;
        }

        @Override
        public String getDocumentationDescription() {
            return "Activates an ability for a set duration and optionally consumes a configurable integer Palladium property.";
        }
    }
}