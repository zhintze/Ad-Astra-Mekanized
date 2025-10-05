package com.hecookin.adastramekanized.common.items.vehicles;

import com.hecookin.adastramekanized.common.blocks.LaunchPadBlock;
import com.hecookin.adastramekanized.common.blocks.properties.LaunchPadPartProperty;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Supplier;

public class RocketItem extends Item {

    private final Supplier<? extends EntityType<?>> type;

    public RocketItem(Supplier<? extends EntityType<?>> type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public EntityType<?> type() {
        return type.get();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.PASS;
        var pos = context.getClickedPos();
        var stack = context.getItemInHand();
        var state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof LaunchPadBlock)) return InteractionResult.PASS;
        if (state.hasProperty(LaunchPadBlock.PART) && state.getValue(LaunchPadBlock.PART) != LaunchPadPartProperty.CENTER) {
            return InteractionResult.PASS;
        }

        level.playSound(null, pos, SoundEvents.NETHERITE_BLOCK_PLACE, SoundSource.BLOCKS, 1, 1);
        var vehicle = type().create(level);
        if (vehicle == null) return InteractionResult.PASS;
        vehicle.setPos(pos.getX() + 0.5, pos.getY() + 0.125f, pos.getZ() + 0.5);
        vehicle.setYRot(context.getHorizontalDirection().getOpposite().toYRot());

        // Restore fuel from item
        if (vehicle instanceof Rocket rocket) {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();
            if (tag.contains("Fluid")) {
                FluidStack fluid = FluidStack.parseOptional(level.registryAccess(), tag.getCompound("Fluid"));
                rocket.fluidContainer().fill(fluid, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            }
        }

        level.addFreshEntity(vehicle);

        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (!tag.isEmpty() && tag.contains("Fluid")) {
            FluidStack fluid = FluidStack.parseOptional(context.registries(), tag.getCompound("Fluid"));
            if (!fluid.isEmpty()) {
                // Get fuel capacity (3000 mB for all rockets)
                long capacity = 3000;
                int percentage = capacity > 0 ? (int)((fluid.getAmount() * 100L) / capacity) : 0;

                // Get fluid color
                int color = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);

                tooltipComponents.add(Component.literal("Fuel: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(percentage + "%").withStyle(style ->
                        style.withColor(net.minecraft.network.chat.TextColor.fromRgb(color)))));
            }
        }
    }
}
