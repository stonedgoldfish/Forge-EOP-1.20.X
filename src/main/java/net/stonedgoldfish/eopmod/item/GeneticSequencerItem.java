package net.stonedgoldfish.eopmod.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.stonedgoldfish.eopmod.client.screen.GeneticSequencerClient;

public class GeneticSequencerItem extends Item {

    public GeneticSequencerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT,
                    () -> GeneticSequencerClient::openScreen
            );
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}