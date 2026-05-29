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
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.EntityPropertyHandler;
import net.threetag.palladium.util.property.IntegerProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.StringProperty;

public class HeldCondition extends KeyCondition {

    private final int ticks;
    private final int energy;
    private final String energyProperty;

    public HeldCondition(
            int ticks,
            int cooldown,
            int energy,
            String energyProperty,
            AbilityConfiguration.KeyType type,
            boolean needsEmptyHand
    ) {
        super(cooldown, type, needsEmptyHand, true);

        this.ticks = ticks;
        this.energy = energy;
        this.energyProperty = energyProperty;
    }

    @Override
    public boolean active(DataContext context) {
        LivingEntity entity = context.getLivingEntity();
        AbilityInstance entry = context.get(DataContextType.ABILITY);

        if (entity == null || entry == null) {
            return false;
        }

        if (!entry.keyPressed) {
            return false;
        }

        if (entry.activationTimer <= 0) {
            entry.keyPressed = false;

            if (this.cooldown > 0) {
                entry.startCooldown(entity, this.cooldown);
            }

            return false;
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
        if (entry.cooldown > 0 || entry.keyPressed) {
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

        entry.keyPressed = true;
        entry.startActivationTimer(entity, this.ticks);
    }

    @Override
    public void onKeyReleased(
            LivingEntity entity,
            AbilityInstance entry,
            Power power,
            IPowerHolder holder
    ) {
        if (!entry.keyPressed) {
            return;
        }

        entry.keyPressed = false;
        entry.activationTimer = 0;

        if (this.cooldown > 0) {
            entry.startCooldown(entity, this.cooldown);
        }
    }

    @Override
    public AbilityConfiguration.KeyPressType getKeyPressType() {
        return AbilityConfiguration.KeyPressType.HOLD;
    }

    @Override
    public ConditionSerializer getSerializer() {
        return EOPConditions.HELD.get();
    }

    public static class Serializer extends ConditionSerializer {

        public static final PalladiumProperty<Integer> TICKS =
                new IntegerProperty("ticks")
                        .configurable("Maximum amount of ticks the ability can stay active while held.");

        public static final PalladiumProperty<Integer> ENERGY =
                new IntegerProperty("energy")
                        .configurable("Energy consumed when the held ability starts.");

        public static final PalladiumProperty<String> PROPERTY =
                new StringProperty("property")
                        .configurable("Name of the integer Palladium property used as the energy source.");

        public Serializer() {
            this.withProperty(net.threetag.palladium.condition.ActionCondition.Serializer.COOLDOWN, 0);
            this.withProperty(TICKS, 60);
            this.withProperty(ENERGY, 0);
            this.withProperty(PROPERTY, "");
            this.withProperty(KeyCondition.KEY_TYPE_WITHOUT_SCROLLING, AbilityConfiguration.KeyType.KEY_BIND);
            this.withProperty(KeyCondition.NEEDS_EMPTY_HAND, false);
        }

        @Override
        public Condition make(JsonObject json) {
            return new HeldCondition(
                    this.getProperty(json, TICKS),
                    this.getProperty(json, net.threetag.palladium.condition.ActionCondition.Serializer.COOLDOWN),
                    this.getProperty(json, ENERGY),
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
            return "Allows an ability to stay active while holding a key and optionally consumes a configurable integer Palladium property when activated.";
        }
    }
}