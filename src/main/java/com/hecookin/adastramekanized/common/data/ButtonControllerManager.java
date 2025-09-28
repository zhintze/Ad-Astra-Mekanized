package com.hecookin.adastramekanized.common.data;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages button-controller bindings using level saved data
 */
public class ButtonControllerManager extends SavedData {

    private static final String DATA_NAME = "adastramekanized_button_controllers";
    private final Map<BlockPos, ControllerBinding> bindings = new HashMap<>();

    public static class ControllerBinding {
        public final CompoundTag controllerData;
        public final UUID playerId;
        public final int linkedCount;

        public ControllerBinding(CompoundTag controllerData, UUID playerId, int linkedCount) {
            this.controllerData = controllerData;
            this.playerId = playerId;
            this.linkedCount = linkedCount;
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.put("controllerData", controllerData);
            tag.putUUID("playerId", playerId);
            tag.putInt("linkedCount", linkedCount);
            return tag;
        }

        public static ControllerBinding load(CompoundTag tag) {
            return new ControllerBinding(
                tag.getCompound("controllerData"),
                tag.getUUID("playerId"),
                tag.getInt("linkedCount")
            );
        }
    }

    public ButtonControllerManager() {
        super();
    }

    public static ButtonControllerManager get(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new RuntimeException("ButtonControllerManager can only be accessed on the server");
        }

        DimensionDataStorage storage = serverLevel.getDataStorage();
        return storage.computeIfAbsent(
            new Factory<>(
                ButtonControllerManager::new,
                ButtonControllerManager::load
            ),
            DATA_NAME
        );
    }

    public static ButtonControllerManager load(CompoundTag tag, HolderLookup.Provider provider) {
        ButtonControllerManager manager = new ButtonControllerManager();
        ListTag list = tag.getList("bindings", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            BlockPos pos = NbtUtils.readBlockPos(entry, "pos").orElse(BlockPos.ZERO);
            ControllerBinding binding = ControllerBinding.load(entry.getCompound("binding"));
            manager.bindings.put(pos, binding);
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();

        for (Map.Entry<BlockPos, ControllerBinding> entry : bindings.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put("pos", NbtUtils.writeBlockPos(entry.getKey()));
            entryTag.put("binding", entry.getValue().save());
            list.add(entryTag);
        }

        tag.put("bindings", list);
        return tag;
    }

    /**
     * Bind a controller to a button at the given position
     */
    public static void bindController(Level level, BlockPos pos, ItemStack controllerStack) {
        if (level.isClientSide) return;

        ButtonControllerManager manager = get(level);

        // Extract controller data
        CustomData customData = controllerStack.get(DataComponents.CUSTOM_DATA);
        CompoundTag controllerData = customData != null ? customData.getUnsafe() : new CompoundTag();

        // Get link data to count distributors
        DistributorLinkData linkData = new DistributorLinkData();
        if (controllerData.contains("LinkData")) {
            linkData.fromNbt(controllerData.getCompound("LinkData"));
        }

        // Create binding
        ControllerBinding binding = new ControllerBinding(
            controllerData,
            UUID.randomUUID(), // We don't have player UUID here, generate one
            linkData.getLinkCount()
        );

        manager.bindings.put(pos, binding);
        manager.setDirty();

        AdAstraMekanized.LOGGER.info("Bound controller to button at {} with {} distributors", pos, linkData.getLinkCount());
    }

    /**
     * Get the controller binding for a button at the given position
     */
    public static ControllerBinding getBinding(Level level, BlockPos pos) {
        if (level.isClientSide) return null;
        return get(level).bindings.get(pos);
    }

    /**
     * Remove a controller binding at the given position
     */
    public static void removeBinding(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        ButtonControllerManager manager = get(level);
        manager.bindings.remove(pos);
        manager.setDirty();
    }

    /**
     * Update an existing controller binding
     */
    public static void updateBinding(Level level, BlockPos pos, ControllerBinding binding) {
        if (level.isClientSide) return;
        ButtonControllerManager manager = get(level);
        manager.bindings.put(pos, binding);
        manager.setDirty();
    }

    /**
     * Toggle all distributors linked to the controller bound to this button
     */
    public static void toggleDistributors(Level level, BlockPos buttonPos) {
        if (level.isClientSide) return;

        ControllerBinding binding = getBinding(level, buttonPos);
        if (binding == null) return;

        // Recreate link data from saved controller data
        DistributorLinkData linkData = new DistributorLinkData();
        if (binding.controllerData.contains("LinkData")) {
            linkData.fromNbt(binding.controllerData.getCompound("LinkData"));
        }

        // Toggle all distributors
        boolean anyEnabled = false;
        for (DistributorLinkData.LinkedDistributor link : linkData.getLinkedDistributors()) {
            if (link.isEnabled()) {
                anyEnabled = true;
                break;
            }
        }

        // Set all to opposite of current state
        boolean newState = !anyEnabled;
        for (DistributorLinkData.LinkedDistributor link : linkData.getLinkedDistributors()) {
            link.setEnabled(newState);
            
            // Apply to actual distributor blocks
            BlockPos distributorPos = link.getPos();
            if (level.isLoaded(distributorPos)) {
                var be = level.getBlockEntity(distributorPos);
                if (be instanceof com.hecookin.adastramekanized.common.blockentities.machines.ImprovedOxygenDistributor distributor) {
                    distributor.setManuallyDisabled(!newState);
                    AdAstraMekanized.LOGGER.info("Button toggle: distributor at {} set to {}", distributorPos, newState);
                }
            }
        }

        AdAstraMekanized.LOGGER.info("Button at {} toggled {} distributors to {}", buttonPos, linkData.getLinkCount(), newState);
    }
}