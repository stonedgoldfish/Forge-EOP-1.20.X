package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladium.util.property.StringArrayProperty;

import java.util.List;

public class AOECommandAbility extends Ability {

    public static final PalladiumProperty<Float> RADIUS = new FloatProperty("radius").configurable("Radius around the entity to affect");
    public static final PalladiumProperty<String[]> COMMANDS_ON_TARGETS = new StringArrayProperty("commands_on_targets").configurable("Commands to run as targets");
    public static final PalladiumProperty<String[]> COMMANDS_ON_ALLIES = new StringArrayProperty("commands_on_allies").configurable("Commands to run as allies");
    public static final PalladiumProperty<Boolean> CONE = new BooleanProperty("cone").configurable("If true, affected area turns into a cone");
    public static final PalladiumProperty<Float> CONE_ANGLE = new FloatProperty("cone_angle").configurable("Cone angle");
    public static final PalladiumProperty<Boolean> INCLUDE_CASTER = new BooleanProperty("include_caster").configurable("If true, commands_on_allies will also run on the caster");

    public AOECommandAbility() {
        this.withProperty(ICON, new ItemIcon(Items.COMMAND_BLOCK));
        this.withProperty(RADIUS, 8.0F);
        this.withProperty(COMMANDS_ON_TARGETS, new String[0]);
        this.withProperty(COMMANDS_ON_ALLIES, new String[0]);
        this.withProperty(CONE, false);
        this.withProperty(CONE_ANGLE, 90.0F);
        this.withProperty(INCLUDE_CASTER, false);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled || entity.level().isClientSide) {
            return;
        }

        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float radius = entry.getProperty(RADIUS);
        boolean cone = entry.getProperty(CONE);
        float coneAngle = entry.getProperty(CONE_ANGLE);
        boolean includeCaster = entry.getProperty(INCLUDE_CASTER);

        String[] targetCommands = entry.getProperty(COMMANDS_ON_TARGETS);
        String[] allyCommands = entry.getProperty(COMMANDS_ON_ALLIES);

        if (includeCaster) {
            runCommandsAs(
                    serverLevel,
                    entity,
                    allyCommands
            );
        }

        AABB area = entity.getBoundingBox().inflate(radius);

        List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                area,
                target -> target != entity && target.isAlive() && entity.distanceToSqr(target) <= radius * radius
        );

        for (LivingEntity target : nearbyEntities) {
            if (cone && !isInsideCone(entity, target, coneAngle)) {
                continue;
            }

            if (isAlly(entity, target)) {
                runCommandsAs(serverLevel, target, allyCommands);
            } else {
                runCommandsAs(serverLevel, target, targetCommands);
            }
        }
    }

    private static boolean isInsideCone(LivingEntity source, LivingEntity target, float coneAngleDegrees) {
        Vec3 look = source.getLookAngle().normalize();
        Vec3 directionToTarget = target.position()
                .subtract(source.position())
                .normalize();

        double dot = look.dot(directionToTarget);
        double requiredDot = Math.cos(Math.toRadians(coneAngleDegrees / 2.0F));

        return dot >= requiredDot;
    }

    private static boolean isAlly(LivingEntity source, LivingEntity target) {
        if (source.isAlliedTo(target) || target.isAlliedTo(source)) {
            return true;
        }

        if (target instanceof TamableAnimal tamableAnimal) {
            if (tamableAnimal.isOwnedBy(source)) {
                return true;
            }
        }

        if (source instanceof TamableAnimal sourcePet) {
            LivingEntity owner = sourcePet.getOwner();

            if (owner != null && target.isAlliedTo(owner)) {
                return true;
            }

            if (target instanceof TamableAnimal targetPet
                    && owner != null
                    && targetPet.isOwnedBy(owner)) {
                return true;
            }
        }

        return false;
    }

    private static void runCommandsAs(ServerLevel serverLevel, LivingEntity executor, String[] commands) {
        if (commands == null || commands.length == 0) {
            return;
        }

        CommandSourceStack sourceStack = executor
                .createCommandSourceStack()
                .withLevel(serverLevel)
                .withPermission(2)
                .withSuppressedOutput();

        for (String command : commands) {
            if (command == null || command.isBlank()) {
                continue;
            }

            String cleanedCommand = command.startsWith("/")
                    ? command.substring(1)
                    : command;

            serverLevel.getServer()
                    .getCommands()
                    .performPrefixedCommand(sourceStack, cleanedCommand);
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Runs commands as nearby entities around the caster";
    }
}