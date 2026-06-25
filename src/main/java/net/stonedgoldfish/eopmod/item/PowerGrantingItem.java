package net.stonedgoldfish.eopmod.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.stonedgoldfish.eopmod.power.EOPPowerGrantHandler;

public class PowerGrantingItem extends Item {

    private final String powerKey;
    private final double successRate;
    private final boolean consumeOnUse;

    public PowerGrantingItem(Properties properties, String powerKey, double successRate, boolean consumeOnUse) {
        super(properties);
        this.powerKey = powerKey;
        this.successRate = successRate;
        this.consumeOnUse = consumeOnUse;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {

            EOPPowerGrantHandler.GrantResult result =
                    EOPPowerGrantHandler.tryGrantPowerFromSource(
                            serverPlayer,
                            this.powerKey,
                            this.successRate
                    );

            EOPPowerGrantHandler.sendResultMessage(
                    serverPlayer,
                    result,
                    this.powerKey,
                    EOPPowerGrantHandler.GrantSource.ITEM
            );

            boolean shouldConsume = switch (result) {
                case SUCCESS, FAILED_CHANCE -> true;
                default -> false;
            };

            if (this.consumeOnUse && shouldConsume && !serverPlayer.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}