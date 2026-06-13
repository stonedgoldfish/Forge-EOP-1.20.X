package net.stonedgoldfish.eopmod.power.condition;

import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.condition.*;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.ability.AbilityConfiguration;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.context.DataContextType;
import net.threetag.palladium.util.property.*;

public class ToggleCondition extends KeyCondition {

    private final int ticks;
    private final int energy;
    private final int energyInterval;
    private final boolean consumeEnergyRepeatedly;
    private final String energyProperty;

    public ToggleCondition(
            int ticks,
            int cooldown,
            int energy,
            int energyInterval,
            boolean consumeEnergyRepeatedly,
            String energyProperty,
            AbilityConfiguration.KeyType type,
            boolean needsEmptyHand
    ) {
        super(cooldown, type, needsEmptyHand, true);

        this.ticks = ticks;
        this.energy = energy;
        this.energyInterval = energyInterval;
        this.consumeEnergyRepeatedly = consumeEnergyRepeatedly;
        this.energyProperty = energyProperty;
    }

    @Override
    public boolean active(DataContext context) {
        LivingEntity entity = context.getLivingEntity();
        AbilityInstance entry = context.get(DataContextType.ABILITY);

        if (entity == null || entry == null || !entry.keyPressed) {
            return false;
        }

        if (this.ticks > 0 && entry.activationTimer <= 0) {
            stopToggle(entity, entry);
            return false;
        }

        if (this.consumeEnergyRepeatedly
                && this.energyInterval > 0
                && this.energy > 0) {

            int elapsed = this.ticks > 0
                    ? this.ticks - entry.activationTimer
                    : entity.tickCount;

            if (elapsed > 0 && elapsed % this.energyInterval == 0) {
                if (!consumeEnergy(entity)) {
                    sendNotEnoughEnergy(entity);
                    stopToggle(entity, entry);
                    return false;
                }
            }
        }

        return true;
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

        if (entry.keyPressed) {
            stopToggle(entity, entry);
            return;
        }

        if (this.energy > 0 && !consumeEnergy(entity)) {
            sendNotEnoughEnergy(entity);
            return;
        }

        entry.keyPressed = true;
        if (this.ticks > 0) {
            entry.startActivationTimer(entity, this.ticks);
        } else {
            entry.activationTimer = 1;
        }
    }

    @Override
    public void onKeyReleased(
            LivingEntity entity,
            AbilityInstance entry,
            Power power,
            IPowerHolder holder
    ) {
        // Do nothing. Toggle only changes on key press.
    }

    private void stopToggle(LivingEntity entity, AbilityInstance entry) {
        entry.keyPressed = false;
        entry.activationTimer = 0;

        if (this.cooldown > 0) {
            entry.startCooldown(entity, this.cooldown);
        }
    }

    private boolean consumeEnergy(LivingEntity entity) {
        if (this.energy <= 0 || this.energyProperty == null || this.energyProperty.isBlank()) {
            return true;
        }

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

        return success[0];
    }

    private void sendNotEnoughEnergy(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

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

    @Override
    public AbilityConfiguration.KeyPressType getKeyPressType() {
        return AbilityConfiguration.KeyPressType.TOGGLE;
    }

    @Override
    public ConditionSerializer getSerializer() {
        return EOPConditions.TOGGLE.get();
    }

    public static class Serializer extends ConditionSerializer {

        public static final PalladiumProperty<Integer> TICKS =
                new IntegerProperty("ticks")
                        .configurable("Maximum amount of ticks the ability can stay active after toggled on");

        public static final PalladiumProperty<Integer> ENERGY =
                new IntegerProperty("energy")
                        .configurable("Energy consumed by the toggle ability");

        public static final PalladiumProperty<Integer> ENERGY_INTERVAL =
                new IntegerProperty("energy_interval")
                        .configurable("If repeated energy is enabled, consumes energy every X ticks while toggled on");

        public static final PalladiumProperty<Boolean> CONSUME_ENERGY_REPEATEDLY =
                new BooleanProperty("consume_energy_repeatedly")
                        .configurable("If true, consumes energy every energy_interval ticks while toggled on. If false, only consumes once on activation.");

        public static final PalladiumProperty<String> PROPERTY =
                new StringProperty("property")
                        .configurable("Name of the integer Palladium property used as the energy source");

        public Serializer() {
            this.withProperty(net.threetag.palladium.condition.ActionCondition.Serializer.COOLDOWN, 0);
            this.withProperty(TICKS, 60);
            this.withProperty(ENERGY, 0);
            this.withProperty(ENERGY_INTERVAL, 20);
            this.withProperty(CONSUME_ENERGY_REPEATEDLY, false);
            this.withProperty(PROPERTY, "");
            this.withProperty(KeyCondition.KEY_TYPE_WITHOUT_SCROLLING, AbilityConfiguration.KeyType.KEY_BIND);
            this.withProperty(KeyCondition.NEEDS_EMPTY_HAND, false);
        }

        @Override
        public Condition make(JsonObject json) {
            return new ToggleCondition(
                    this.getProperty(json, TICKS),
                    this.getProperty(json, net.threetag.palladium.condition.ActionCondition.Serializer.COOLDOWN),
                    this.getProperty(json, ENERGY),
                    this.getProperty(json, ENERGY_INTERVAL),
                    this.getProperty(json, CONSUME_ENERGY_REPEATEDLY),
                    this.getProperty(json, PROPERTY),
                    this.getProperty(json, KeyCondition.KEY_TYPE_WITHOUT_SCROLLING),
                    this.getProperty(json, KeyCondition.NEEDS_EMPTY_HAND)
            );
        }

        @Override
        public ConditionEnvironment getContextEnvironment() {
            return ConditionEnvironment.DATA;
        }

        @Override
        public String getDocumentationDescription() {
            return "Toggles an ability on/off with a key press, with optional duration, cooldown, and repeated energy consumption.";
        }
    }
}