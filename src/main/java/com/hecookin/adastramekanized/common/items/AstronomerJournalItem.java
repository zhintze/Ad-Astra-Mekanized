package com.hecookin.adastramekanized.common.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import vazkii.patchouli.api.PatchouliAPI;

public class AstronomerJournalItem extends Item {

    private static final ResourceLocation BOOK_ID = ResourceLocation.fromNamespaceAndPath("adastramekanized", "journal");

    public AstronomerJournalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);

        if (user instanceof ServerPlayer player && ModList.get().isLoaded("patchouli")) {
            PatchouliAPI.get().openBookGUI(player, BOOK_ID);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
