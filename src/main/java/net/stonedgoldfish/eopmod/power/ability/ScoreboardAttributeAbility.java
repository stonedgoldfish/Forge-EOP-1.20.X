package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.*;

import java.util.UUID;

public class ScoreboardAttributeAbility extends Ability {

    public static final PalladiumProperty<String> ATTRIBUTE =
            new StringProperty("attribute")
                    .configurable("Attribute ID to modify. Example: minecraft:generic.attack_damage");

    public static final PalladiumProperty<String> SCOREBOARD =
            new StringProperty("scoreboard")
                    .configurable("Scoreboard objective name used for scaling.");

    public static final PalladiumProperty<Float> VALUE_PER_SCORE =
            new FloatProperty("value_per_score")
                    .configurable("Modifier value added per scoreboard point.");

    public static final PalladiumProperty<String> OPERATION =
            new StringProperty("operation")
                    .configurable("Attribute operation: addition, multiply_base, or multiply_total.");

    private static final UUID MODIFIER_UUID =
            UUID.fromString("5f38e27a-bd2f-49e6-a52f-bb0f037a7d71");

    public ScoreboardAttributeAbility() {
        this.withProperty(ICON, new ItemIcon(Items.COMMAND_BLOCK));

        this.withProperty(ATTRIBUTE, "minecraft:generic.attack_damage");
        this.withProperty(SCOREBOARD, "score");
        this.withProperty(VALUE_PER_SCORE, 1.0F);
        this.withProperty(OPERATION, "addition");
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide) {
            return;
        }

        if (!enabled) {
            removeModifier(entity);
            return;
        }

        Attribute attribute = getAttribute(entry.getProperty(ATTRIBUTE));

        if (attribute == null || !entity.getAttributes().hasAttribute(attribute)) {
            removeModifier(entity);
            return;
        }

        int score = getScore(entity, entry.getProperty(SCOREBOARD));

        double value = score * entry.getProperty(VALUE_PER_SCORE);

        var instance = entity.getAttribute(attribute);

        if (instance == null) {
            return;
        }

        instance.removeModifier(MODIFIER_UUID);

        instance.addTransientModifier(
                new AttributeModifier(
                        MODIFIER_UUID,
                        "eop_scoreboard_attribute",
                        value,
                        getOperation(entry.getProperty(OPERATION))
                )
        );
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        removeModifier(entity);
    }

    private static int getScore(LivingEntity entity, String objectiveName) {
        if (!(entity instanceof ServerPlayer player)) {
            return 0;
        }

        if (objectiveName == null || objectiveName.isBlank()) {
            return 0;
        }

        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);

        if (objective == null) {
            return 0;
        }

        return scoreboard.getOrCreatePlayerScore(
                player.getScoreboardName(),
                objective
        ).getScore();
    }

    private static Attribute getAttribute(String attributeId) {
        ResourceLocation id = ResourceLocation.tryParse(attributeId);

        if (id == null) {
            return null;
        }

        return BuiltInRegistries.ATTRIBUTE.get(id);
    }

    private static AttributeModifier.Operation getOperation(String operation) {
        if (operation == null) {
            return AttributeModifier.Operation.ADDITION;
        }

        return switch (operation.toLowerCase()) {
            case "multiply_base", "multiply_base_value", "multiply_base_addition" ->
                    AttributeModifier.Operation.MULTIPLY_BASE;
            case "multiply_total", "multiply_total_value", "multiply_total_multiplicative" ->
                    AttributeModifier.Operation.MULTIPLY_TOTAL;
            default ->
                    AttributeModifier.Operation.ADDITION;
        };
    }

    private static void removeModifier(LivingEntity entity) {
        for (Attribute attribute : BuiltInRegistries.ATTRIBUTE) {
            var instance = entity.getAttribute(attribute);

            if (instance != null) {
                instance.removeModifier(MODIFIER_UUID);
            }
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Applies an attribute modifier that scales with a configurable scoreboard value.";
    }
}