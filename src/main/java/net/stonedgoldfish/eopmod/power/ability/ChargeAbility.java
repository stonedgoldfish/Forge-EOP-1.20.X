package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import java.util.*;

public class ChargeAbility extends Ability {

    public static final PalladiumProperty<Float> ACCELERATION = new FloatProperty("acceleration").configurable("Speed gained every tick while charging");
    public static final PalladiumProperty<Float> MAX_SPEED = new FloatProperty("max_speed").configurable("Maximum charge speed");
    public static final PalladiumProperty<Boolean> STOP_MOTION_ON_END = new BooleanProperty("stop_motion_on_end").configurable("Sets motion to 0 when the charge ends");
    private static final UUID STEP_HEIGHT_UUID = UUID.fromString("64e76d3d-b28d-45e8-b38a-846b6eb3c802");
    private static final Map<UUID, Float> CURRENT_SPEED = new HashMap<>();
    private static final Set<UUID> NO_BOB_PLAYERS = new HashSet<>();

    public ChargeAbility() {
        this.withProperty(ICON, new ItemIcon(Items.IRON_BOOTS));
        this.withProperty(ACCELERATION, 0.02F);
        this.withProperty(MAX_SPEED, 0.4F);
        this.withProperty(STOP_MOTION_ON_END, true);
    }

    public static boolean isCharging(Player player) {
        return CURRENT_SPEED.containsKey(player.getUUID());
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled) {
            return;
        }

        UUID uuid = entity.getUUID();

        if (entity instanceof Player player) {
            NO_BOB_PLAYERS.add(uuid);
            player.setSprinting(false);
        }

        applyStepHeight(entity);

        float acceleration = entry.getProperty(ACCELERATION);
        float maxSpeed = entry.getProperty(MAX_SPEED);

        Vec3 currentMotion = entity.getDeltaMovement();
        float currentHorizontalSpeed = (float) Math.sqrt(
                currentMotion.x * currentMotion.x
                        + currentMotion.z * currentMotion.z
        );

        float currentSpeed = CURRENT_SPEED.getOrDefault(uuid, currentHorizontalSpeed);
        currentSpeed = Math.min(maxSpeed, currentSpeed + acceleration);

        CURRENT_SPEED.put(uuid, currentSpeed);

        float yawRad = entity.getYRot() * ((float) Math.PI / 180.0F);

        Vec3 direction = new Vec3(
                -Math.sin(yawRad),
                0.0D,
                Math.cos(yawRad)
        ).normalize();

        entity.setDeltaMovement(
                direction.x * currentSpeed,
                currentMotion.y,
                direction.z * currentSpeed
        );

        syncMotion(entity);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        reset(entity, entry);
    }

    private static void applyStepHeight(LivingEntity entity) {
        var attribute = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());

        if (attribute == null) {
            return;
        }

        attribute.removeModifier(STEP_HEIGHT_UUID);

        attribute.addTransientModifier(
                new AttributeModifier(
                        STEP_HEIGHT_UUID,
                        "eop_charge_step_height",
                        1.0D,
                        AttributeModifier.Operation.ADDITION
                )
        );
    }

    private static void removeStepHeight(LivingEntity entity) {
        var attribute = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());

        if (attribute != null) {
            attribute.removeModifier(STEP_HEIGHT_UUID);
        }
    }

    private static void reset(LivingEntity entity, AbilityInstance entry) {
        boolean wasCharging = CURRENT_SPEED.containsKey(entity.getUUID());

        CURRENT_SPEED.remove(entity.getUUID());
        removeStepHeight(entity);

        if (entity instanceof Player player) {
            NO_BOB_PLAYERS.remove(player.getUUID());
            player.setSprinting(false);
        }

        if (wasCharging && entry.getProperty(STOP_MOTION_ON_END)) {
            entity.setDeltaMovement(Vec3.ZERO);
            syncMotion(entity);
        }
    }

    public static boolean disablesCameraBobbing(Player player) {
        return NO_BOB_PLAYERS.contains(player.getUUID());
    }

    private static void syncMotion(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetEntityMotionPacket(entity));
        }
    }

    @Override
    public String getDocumentationDescription() {
        return "Forces the entity to walk forward horizontally, starting from current speed and gradually accelerating.";
    }
}