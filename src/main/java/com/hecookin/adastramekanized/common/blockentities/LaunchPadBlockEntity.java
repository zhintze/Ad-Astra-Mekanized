package com.hecookin.adastramekanized.common.blockentities;

import com.hecookin.adastramekanized.common.blocks.LaunchPadBlock;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Launch pad block entity that acts as a pass-through for pipes to fuel rockets directly
 */
public class LaunchPadBlockEntity extends BlockEntity {

    public LaunchPadBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.LAUNCH_PAD.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LaunchPadBlockEntity entity) {
        // No tick logic needed - fluid transfers happen immediately on pipe insertion
    }

    /**
     * Get fluid handler for a specific side (for pipe connections)
     * Returns a pass-through handler that directly fills the rocket
     */
    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return new IFluidHandler() {
            @Override
            public int getTanks() {
                Rocket rocket = findRocket();
                return rocket != null ? rocket.fluidContainer().getTanks() : 1;
            }

            @Override
            public @NotNull FluidStack getFluidInTank(int tank) {
                Rocket rocket = findRocket();
                return rocket != null ? rocket.fluidContainer().getFluidInTank(tank) : FluidStack.EMPTY;
            }

            @Override
            public int getTankCapacity(int tank) {
                Rocket rocket = findRocket();
                return rocket != null ? rocket.fluidContainer().getTankCapacity(tank) : 0;
            }

            @Override
            public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
                Rocket rocket = findRocket();
                return rocket != null && rocket.fluidContainer().isFluidValid(tank, stack);
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                Rocket rocket = findRocket();
                if (rocket != null) {
                    return rocket.fluidContainer().fill(resource, action);
                }
                return 0;
            }

            @Override
            public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
                Rocket rocket = findRocket();
                if (rocket != null) {
                    return rocket.fluidContainer().drain(resource, action);
                }
                return FluidStack.EMPTY;
            }

            @Override
            public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
                Rocket rocket = findRocket();
                if (rocket != null) {
                    return rocket.fluidContainer().drain(maxDrain, action);
                }
                return FluidStack.EMPTY;
            }

            @Nullable
            private Rocket findRocket() {
                if (level == null) return null;
                AABB searchBox = new AABB(worldPosition).inflate(1.5, 2, 1.5);
                List<Rocket> rockets = level.getEntitiesOfClass(Rocket.class, searchBox);
                return rockets.isEmpty() ? null : rockets.get(0);
            }
        };
    }
}
