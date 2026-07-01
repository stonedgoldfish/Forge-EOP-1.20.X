package net.stonedgoldfish.eopmod.power.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.util.icon.ItemIcon;
import net.threetag.palladium.util.property.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID)
public class DamageReductionAbility extends Ability {

    public static final PalladiumProperty<Float> REDUCTION = new FloatProperty("reduction").configurable("Damage reduction percentage. 0.5 = 50% reduction");
    public static final PalladiumProperty<Boolean> SPECIFIC_DAMAGE = new BooleanProperty("specific_damage").configurable("Only reduce selected damage types");
    public static final PalladiumProperty<String[]> DAMAGE_TYPES = new StringArrayProperty("damage_types").configurable("Accepted damage type IDs");

    private static final Map<UUID, Map<Integer, Settings>> ACTIVE_REDUCTIONS = new HashMap<>();

    public record Settings(
            float reduction,
            boolean specificDamage,
            String[] damageTypes
    ) {}

    public DamageReductionAbility() {
        this.withProperty(ICON, new ItemIcon(Items.SHIELD));
        this.withProperty(REDUCTION, 0.5F);
        this.withProperty(SPECIFIC_DAMAGE, false);
        this.withProperty(DAMAGE_TYPES, new String[0]);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        if (entity.level().isClientSide) {
            return;
        }

        UUID entityId = entity.getUUID();
        int instanceId = System.identityHashCode(entry);

        if (enabled) {
            ACTIVE_REDUCTIONS
                    .computeIfAbsent(entityId, id -> new HashMap<>())
                    .put(instanceId, new Settings(
                            clamp01(entry.getProperty(REDUCTION)),
                            entry.getProperty(SPECIFIC_DAMAGE),
                            entry.getProperty(DAMAGE_TYPES)
                    ));
        } else {
            removeInstance(entityId, instanceId);
        }
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance entry, IPowerHolder holder, boolean enabled) {
        removeInstance(entity.getUUID(), System.identityHashCode(entry));
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide) {
            return;
        }

        Map<Integer, Settings> reductions = ACTIVE_REDUCTIONS.get(entity.getUUID());

        if (reductions == null || reductions.isEmpty()) {
            return;
        }

        ResourceLocation damageType = event.getSource()
                .typeHolder()
                .unwrapKey()
                .map(key -> key.location())
                .orElse(null);

        if (damageType == null) {
            return;
        }

        float strongestReduction = 0.0F;

        for (Settings settings : reductions.values()) {
            if (!matchesDamage(settings, damageType)) {
                continue;
            }

            strongestReduction = Math.max(strongestReduction, settings.reduction());
        }

        if (strongestReduction <= 0.0F) {
            return;
        }

        float newDamage = event.getAmount() * (1.0F - strongestReduction);
        event.setAmount(Math.max(0.0F, newDamage));
    }

    private static boolean matchesDamage(Settings settings, ResourceLocation damageType) {
        if (!settings.specificDamage()) {
            return true;
        }

        String id = damageType.toString();

        for (String allowed : settings.damageTypes()) {
            if (allowed == null || allowed.isBlank()) {
                continue;
            }

            if (allowed.equals(id)) {
                return true;
            }
        }

        return false;
    }

    private static void removeInstance(UUID entityId, int instanceId) {
        Map<Integer, Settings> reductions = ACTIVE_REDUCTIONS.get(entityId);

        if (reductions == null) {
            return;
        }

        reductions.remove(instanceId);

        if (reductions.isEmpty()) {
            ACTIVE_REDUCTIONS.remove(entityId);
        }
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    @Override
    public String getDocumentationDescription() {
        return "Reduces incoming damage. Can optionally only apply to specific damage types.";
    }
}