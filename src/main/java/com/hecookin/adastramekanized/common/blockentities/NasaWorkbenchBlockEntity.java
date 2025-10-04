package com.hecookin.adastramekanized.common.blockentities;

import com.hecookin.adastramekanized.common.menus.NasaWorkbenchMenu;
import com.hecookin.adastramekanized.common.recipes.NasaWorkbenchRecipe;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NasaWorkbenchBlockEntity extends BlockEntity implements Container, MenuProvider, RecipeInput {

    private static final int SLOT_COUNT = 15;
    private static final int INPUT_SLOT_COUNT = 14;
    private static final int OUTPUT_SLOT = 14;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final RecipeManager.CachedCheck<RecipeInput, NasaWorkbenchRecipe> quickCheck =
            RecipeManager.createCheck(ModRecipeTypes.NASA_WORKBENCH.get());

    @Nullable
    private NasaWorkbenchRecipe currentRecipe;

    public NasaWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.NASA_WORKBENCH.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NasaWorkbenchBlockEntity entity) {
        if (level.isClientSide()) return;

        boolean hasItems = false;
        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            if (!entity.getItem(i).isEmpty()) {
                hasItems = true;
                break;
            }
        }

        if (hasItems && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5,
                    pos.getY() + 1.5,
                    pos.getZ() + 0.5,
                    3,
                    0.12, 0.12, 0.12,
                    0.15
            );
        }

        entity.updateRecipe();

        if (entity.currentRecipe != null && entity.canCraft()) {
            entity.setItem(OUTPUT_SLOT, entity.currentRecipe.result());
        } else {
            entity.setItem(OUTPUT_SLOT, ItemStack.EMPTY);
        }
    }

    private void updateRecipe() {
        if (level == null || level.isClientSide()) return;
        currentRecipe = quickCheck.getRecipeFor(this, level)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    public boolean canCraft() {
        return currentRecipe != null && currentRecipe.matches(this, level);
    }

    public void craft() {
        if (!canCraft() || level == null) return;

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.TOTEM_OF_UNDYING,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.5,
                    worldPosition.getZ() + 0.5,
                    100,
                    0.1, 0.1, 0.1,
                    0.7
            );
            serverLevel.playSound(null, worldPosition, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
        }

        Containers.dropItemStack(level,
                worldPosition.getX(),
                worldPosition.getY() + 1,
                worldPosition.getZ(),
                getItem(OUTPUT_SLOT).copy());

        setItem(OUTPUT_SLOT, ItemStack.EMPTY);
        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            getItem(i).shrink(1);
        }
        currentRecipe = null;
        setChanged();
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack stack = items.get(slot);
        if (!stack.isEmpty()) {
            ItemStack result = stack.split(amount);
            setChanged();
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public int size() {
        return INPUT_SLOT_COUNT;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                items.get(i).save(registries, slotTag);
                itemsTag.put(String.valueOf(i), slotTag);
            }
        }
        tag.put("Items", itemsTag);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items")) {
            CompoundTag itemsTag = tag.getCompound("Items");
            for (String key : itemsTag.getAllKeys()) {
                int slot = Integer.parseInt(key);
                if (slot >= 0 && slot < items.size()) {
                    items.set(slot, ItemStack.parseOptional(registries, itemsTag.getCompound(key)));
                }
            }
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.adastramekanized.nasa_workbench");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return NasaWorkbenchMenu.create(containerId, playerInventory, this);
    }
}
