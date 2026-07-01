package net.stonedgoldfish.eopmod.power.condition;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.stonedgoldfish.eopmod.EOPMod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = EOPMod.MOD_ID)
public class EOPDamageTracker {

    private static final Map<UUID, DamageInfo> DAMAGE = new HashMap<>();

    private static final int DAMAGE_VALID_TICKS = 5;

    public record DamageInfo(
            float amount,
            ResourceLocation damageType,
            long gameTime
    ) {}

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide) {
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

        DAMAGE.put(
                entity.getUUID(),
                new DamageInfo(
                        event.getAmount(),
                        damageType,
                        entity.level().getGameTime()
                )
        );
    }

    public static DamageInfo getDamage(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide) {
            return null;
        }

        DamageInfo info = DAMAGE.get(entity.getUUID());

        if (info == null) {
            return null;
        }

        long currentTime = entity.level().getGameTime();

        if (currentTime - info.gameTime() > DAMAGE_VALID_TICKS) {
            DAMAGE.remove(entity.getUUID());
            return null;
        }

        return info;
    }

    public static void clearOldDamage(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide) {
            return;
        }

        long currentTime = entity.level().getGameTime();

        Iterator<Map.Entry<UUID, DamageInfo>> iterator = DAMAGE.entrySet().iterator();

        while (iterator.hasNext()) {
            DamageInfo info = iterator.next().getValue();

            if (currentTime - info.gameTime() > DAMAGE_VALID_TICKS) {
                iterator.remove();
            }
        }
    }
}