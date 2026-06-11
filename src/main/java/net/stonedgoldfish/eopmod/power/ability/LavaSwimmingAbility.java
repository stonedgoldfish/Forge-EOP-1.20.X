package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.FloatProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LavaSwimmingAbility extends Ability {

    public static final PalladiumProperty<Float> LAVA_FOG_DISTANCE = new FloatProperty("lava_fog_distance").configurable("How far the player can see in lava");
    private static final Map<UUID, Float> ACTIVE = new HashMap<>();

    public LavaSwimmingAbility() {
        this.withProperty(ICON, new ItemIcon(Items.LAVA_BUCKET));
        this.withProperty(LAVA_FOG_DISTANCE, 16.0F);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (!(entity instanceof Player player)) {
            return;
        }

        if (!enabled) {
            ACTIVE.remove(player.getUUID());
            return;
        }

        ACTIVE.put(player.getUUID(), entry.getProperty(LAVA_FOG_DISTANCE));

        if (player.isInLava()) {
            player.setSwimming(player.isSprinting());
            player.fallDistance = 0.0F;
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity instanceof Player player) {
            ACTIVE.remove(player.getUUID());
            player.setSwimming(false);
        }
    }

    public static boolean hasLavaSwimming(LivingEntity entity) {
        return ACTIVE.containsKey(entity.getUUID());
    }

    public static float getLavaFogDistance(Player player) {
        return ACTIVE.getOrDefault(player.getUUID(), 0.0F);
    }

    @Override
    public String getDocumentationDescription() {
        return "Allows the player to swim through lava like water.";
    }
}