package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.BooleanProperty;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.minecraft.world.entity.player.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ForwardMotionAbility extends Ability {

    public static final PalladiumProperty<Float> MOTION_SCALE =
            new FloatProperty("motion_scale")
                    .configurable("How strongly the player is pushed in the direction they are facing.");

    public static final PalladiumProperty<Boolean> STOP_MOTION_ON_END =
            new BooleanProperty("stop_motion_on_end")
                    .configurable("Sets the caster's motion to 0 when the ability ends.");

    private static final Set<UUID> NO_BOB_PLAYERS = new HashSet<>();

    public ForwardMotionAbility() {
        this.withProperty(ICON, new ItemIcon(Items.FEATHER));
        this.withProperty(MOTION_SCALE, 1.0F);
        this.withProperty(STOP_MOTION_ON_END, false);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!enabled) {
            return;
        }
        if (entity instanceof Player player) {
            NO_BOB_PLAYERS.add(player.getUUID());
        }

        float scale = entry.getProperty(MOTION_SCALE);

        Vec3 newMotion = entity.getLookAngle().scale(scale);
        entity.setDeltaMovement(newMotion);

        syncMotion(entity);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!entry.getProperty(STOP_MOTION_ON_END)) {
            return;
        }
        if (entity instanceof Player player) {
            NO_BOB_PLAYERS.remove(player.getUUID());
        }

        entity.setDeltaMovement(Vec3.ZERO);
        syncMotion(entity);
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
        return "Constantly propels the entity in the direction it is facing.";
    }
}