package com.hecookin.adastramekanized.common.blocks;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.ImprovedOxygenDistributor;
import com.hecookin.adastramekanized.common.data.ButtonControllerManager;
import com.hecookin.adastramekanized.common.data.DistributorLinkData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * A special button that can be linked to an Oxygen Network Controller
 * to remotely toggle oxygen distributors
 */
public class WirelessControlButton extends ButtonBlock {

    public WirelessControlButton(Properties properties) {
        super(BlockSetType.IRON, 30, properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Check if button is already pressed
        if (state.getValue(POWERED)) {
            return InteractionResult.CONSUME;
        }

        // Check if we have a controller bound
        ButtonControllerManager.ControllerBinding binding = ButtonControllerManager.getBinding(level, pos);
        if (binding == null) {
            player.displayClientMessage(
                Component.literal("No controller linked to this button")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.CONSUME;
        }

        // Press the button
        this.press(state, level, pos, player);
        this.playSound(player, level, pos, true);

        // Toggle distributors
        toggleDistributors(level, pos, binding, player);

        return InteractionResult.SUCCESS;
    }

    private void toggleDistributors(Level level, BlockPos buttonPos, ButtonControllerManager.ControllerBinding binding, Player player) {
        // Recreate link data from saved controller data
        DistributorLinkData linkData = new DistributorLinkData();
        if (binding.controllerData.contains("LinkData")) {
            linkData.fromNbt(binding.controllerData.getCompound("LinkData"));
        }

        // Count current state
        int enabledCount = 0;
        int totalCount = linkData.getLinkCount();
        
        for (DistributorLinkData.LinkedDistributor link : linkData.getLinkedDistributors()) {
            if (link.isEnabled()) {
                enabledCount++;
            }
        }

        // Determine new state (if any are on, turn all off; if all are off, turn all on)
        boolean newState = enabledCount == 0;
        int successCount = 0;

        // Apply new state to all distributors
        for (DistributorLinkData.LinkedDistributor link : linkData.getLinkedDistributors()) {
            link.setEnabled(newState);
            
            BlockPos distributorPos = link.getPos();
            if (level.isLoaded(distributorPos)) {
                BlockEntity be = level.getBlockEntity(distributorPos);
                if (be instanceof ImprovedOxygenDistributor distributor) {
                    distributor.setManuallyDisabled(!newState);
                    successCount++;
                    AdAstraMekanized.LOGGER.info("Button at {} toggled distributor at {} to {}", 
                        buttonPos, distributorPos, newState ? "ON" : "OFF");
                }
            }
        }

        // Update the stored controller data with new states
        binding.controllerData.put("LinkData", linkData.toNbt());
        ButtonControllerManager.updateBinding(level, buttonPos, binding);

        // Send feedback to player
        String action = newState ? "Enabled" : "Disabled";
        player.displayClientMessage(
            Component.literal(action + " ")
                .withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED)
                .append(Component.literal(successCount + "/" + totalCount + " distributors")
                    .withStyle(ChatFormatting.WHITE)),
            true
        );
    }

    protected void playSound(@javax.annotation.Nullable Player player, Level level, BlockPos pos, boolean pressed) {
        level.playSound(
            pressed ? player : null,
            pos,
            pressed ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF,
            SoundSource.BLOCKS,
            0.3F,
            pressed ? 0.6F : 0.5F
        );
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Remove controller binding when button is broken
            if (!level.isClientSide) {
                ButtonControllerManager.removeBinding(level, pos);
                AdAstraMekanized.LOGGER.info("Removed controller binding from button at {}", pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.adastramekanized.wireless_control_button.tooltip")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("block.adastramekanized.wireless_control_button.tooltip2")
            .withStyle(ChatFormatting.DARK_GRAY));
    }
}