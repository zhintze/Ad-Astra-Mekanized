package com.hecookin.adastramekanized.common.items;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.ImprovedOxygenDistributor;
import com.hecookin.adastramekanized.common.data.DistributorLinkData;
import com.hecookin.adastramekanized.common.menus.OxygenControllerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.hecookin.adastramekanized.common.blocks.WirelessControlButton;
import com.hecookin.adastramekanized.common.data.ButtonControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Handheld controller for managing multiple oxygen distributors wirelessly
 */
public class OxygenNetworkController extends Item implements MenuProvider {

    public OxygenNetworkController() {
        super(new Item.Properties()
            .stacksTo(1)
            .fireResistant());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockState state = level.getBlockState(pos);

        // Check if this is a wireless control button and player is sneaking
        if (player.isShiftKeyDown() && state.getBlock() instanceof WirelessControlButton) {
            if (!level.isClientSide) {
                // Bind controller to button
                bindToButton(level, pos, stack, player);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Check if this is a distributor
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ImprovedOxygenDistributor distributor) {
            if (!level.isClientSide) {
                // Toggle link on sneak + right-click
                if (player.isShiftKeyDown()) {
                    DistributorLinkData linkData = getOrCreateLinkData(stack);

                    if (linkData.isLinked(pos)) {
                        // Unlink
                        linkData.removeLink(pos);
                        saveLinkData(stack, linkData);
                        player.displayClientMessage(
                            Component.literal("Unlinked distributor at ")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal(String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ()))
                                    .withStyle(ChatFormatting.WHITE)),
                            true
                        );
                        AdAstraMekanized.LOGGER.info("Unlinked distributor at {} from controller", pos);
                    } else {
                        // Link
                        if (linkData.addLink(pos)) {
                            saveLinkData(stack, linkData);
                            player.displayClientMessage(
                                Component.literal("Linked distributor at ")
                                    .withStyle(ChatFormatting.GREEN)
                                    .append(Component.literal(String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ()))
                                        .withStyle(ChatFormatting.WHITE))
                                    .append(Component.literal(String.format(" (%d/%d)", linkData.getLinkCount(), 64))
                                        .withStyle(ChatFormatting.GRAY)),
                                true
                            );
                            AdAstraMekanized.LOGGER.info("Linked distributor at {} to controller", pos);
                        } else {
                            player.displayClientMessage(
                                Component.literal("Cannot link more distributors! Maximum 64 reached.")
                                    .withStyle(ChatFormatting.RED),
                                true
                            );
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Store the controller stack in the player's main hand for menu creation
            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("item.adastramekanized.oxygen_network_controller");
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player p) {
                    return new OxygenControllerMenu(containerId, playerInventory, stack);
                }
            });
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        DistributorLinkData linkData = getLinkData(stack);
        if (linkData != null) {
            int count = linkData.getLinkCount();
            tooltip.add(Component.literal("Linked Distributors: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(count + "/64")
                    .withStyle(count > 0 ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY)));

            if (count > 0 && flag.isAdvanced()) {
                tooltip.add(Component.literal("Right-click to manage")
                    .withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("Sneak+Right-click distributor to link/unlink")
                    .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        DistributorLinkData linkData = getLinkData(stack);
        return linkData != null && linkData.getLinkCount() > 0;
    }

    // Data management methods

    public static DistributorLinkData getOrCreateLinkData(ItemStack stack) {
        if (!(stack.getItem() instanceof OxygenNetworkController)) {
            return new DistributorLinkData();
        }

        DistributorLinkData data = new DistributorLinkData();

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.getUnsafe();
            if (tag != null && tag.contains("LinkData")) {
                data.fromNbt(tag.getCompound("LinkData"));
            }
        }

        return data;
    }

    public static DistributorLinkData getLinkData(ItemStack stack) {
        if (!(stack.getItem() instanceof OxygenNetworkController)) {
            return null;
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.getUnsafe();
            if (tag != null && tag.contains("LinkData")) {
                DistributorLinkData data = new DistributorLinkData();
                data.fromNbt(tag.getCompound("LinkData"));
                return data;
            }
        }

        return null;
    }

    public static void saveLinkData(ItemStack stack, DistributorLinkData data) {
        if (!(stack.getItem() instanceof OxygenNetworkController)) {
            return;
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.getUnsafe();
        if (tag == null) {
            tag = new CompoundTag();
        } else {
            tag = tag.copy();
        }
        tag.put("LinkData", data.toNbt());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // MenuProvider implementation

    @Override
    public Component getDisplayName() {
        return Component.translatable("item.adastramekanized.oxygen_network_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        ItemStack controller = findControllerInInventory(player);
        if (!controller.isEmpty()) {
            return new OxygenControllerMenu(containerId, playerInventory, controller);
        }
        return null;
    }

    private ItemStack findControllerInInventory(Player player) {
        // Check main hand
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof OxygenNetworkController) {
            return mainHand;
        }

        // Check off hand
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof OxygenNetworkController) {
            return offHand;
        }

        // Check inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof OxygenNetworkController) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Bind this controller to a button block
     */
    private void bindToButton(Level level, BlockPos pos, ItemStack stack, Player player) {
        // Check if another controller is already bound
        ButtonControllerManager.ControllerBinding existing = ButtonControllerManager.getBinding(level, pos);
        if (existing != null) {
            player.displayClientMessage(
                Component.literal("Replaced previous controller binding")
                    .withStyle(ChatFormatting.YELLOW),
                true
            );
        }

        // Store in level saved data
        ButtonControllerManager.bindController(level, pos, stack);

        DistributorLinkData linkData = getOrCreateLinkData(stack);
        player.displayClientMessage(
            Component.literal("Bound controller to button with ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(linkData.getLinkCount() + " distributors")
                    .withStyle(ChatFormatting.WHITE)),
            true
        );

        AdAstraMekanized.LOGGER.info("Bound oxygen controller to button at {}", pos);
    }

    /**
     * Update all linked distributors with current status from the level
     */
    public static void updateDistributorStatuses(Level level, DistributorLinkData linkData) {
        for (DistributorLinkData.LinkedDistributor link : linkData.getLinkedDistributors()) {
            BlockPos pos = link.getPos();
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof ImprovedOxygenDistributor distributor) {
                int energy = distributor.getEnergyStorage().getEnergyStored();
                long oxygen = distributor.getOxygenTank().getStored();
                float efficiency = distributor.getEfficiency();
                boolean online = distributor.isActive();

                link.updateStatus(energy, oxygen, efficiency, online);
            } else {
                // Distributor no longer exists or not loaded
                link.updateStatus(0, 0, 0, false);
            }
        }
    }
}