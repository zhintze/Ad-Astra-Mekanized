package com.hecookin.adastramekanized.common.commands;

import com.hecookin.adastramekanized.common.blockentities.machines.MekanismBasedOxygenDistributor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class OxygenDebugCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("oxygendebug")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("target")
                .executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    var level = player.level();

                    // Get the block the player is looking at
                    HitResult result = player.pick(10, 0, false);
                    if (result instanceof BlockHitResult blockHit) {
                        BlockPos pos = blockHit.getBlockPos();
                        BlockEntity be = level.getBlockEntity(pos);

                        if (be instanceof MekanismBasedOxygenDistributor distributor) {
                            long oxygen = distributor.getChemicalInTank(0).getAmount();
                            long energy = distributor.getEnergyForDebug(0);
                            boolean active = distributor.isActive();

                            player.sendSystemMessage(Component.literal(String.format(
                                "Target Oxygen Distributor: Oxygen: %d/4000 mB, Energy: %d/40000 FE, Active: %s",
                                oxygen, energy, active)));
                            return 1;
                        } else {
                            player.sendSystemMessage(Component.literal("Target block at " + pos + " is not an oxygen distributor"));
                            if (be != null) {
                                player.sendSystemMessage(Component.literal("It is: " + be.getClass().getSimpleName()));
                            }
                        }
                    } else {
                        player.sendSystemMessage(Component.literal("Not looking at a block"));
                    }
                    return 0;
                }))
            .then(Commands.literal("check")
                .executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    var level = player.level();
                    // Check the block at player's feet level (the block they're standing on)
                    var pos = player.blockPosition();

                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof MekanismBasedOxygenDistributor distributor) {
                        long oxygen = distributor.getChemicalInTank(0).getAmount();
                        long energy = distributor.getEnergyForDebug(0);
                        boolean active = distributor.isActive();

                        player.sendSystemMessage(Component.literal(String.format(
                            "Oxygen: %d/4000 mB, Energy: %d/40000 FE, Active: %s",
                            oxygen, energy, active)));
                        return 1;
                    }
                    player.sendSystemMessage(Component.literal("No oxygen distributor below you"));
                    return 0;
                }))
            .then(Commands.literal("fill")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 4000))
                    .executes(context -> {
                        var player = context.getSource().getPlayerOrException();
                        var level = player.level();
                        int amount = IntegerArgumentType.getInteger(context, "amount");

                        // Get the block the player is looking at
                        HitResult result = player.pick(10, 0, false);
                        if (!(result instanceof BlockHitResult blockHit)) {
                            player.sendSystemMessage(Component.literal("Not looking at a block"));
                            return 0;
                        }

                        BlockPos pos = blockHit.getBlockPos();
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof MekanismBasedOxygenDistributor distributor) {
                            // Try to fill with oxygen directly
                            try {
                                // Get Mekanism's oxygen chemical from registry
                                Class<?> chemicalsClass = Class.forName("mekanism.common.registries.MekanismChemicals");
                                java.lang.reflect.Field oxygenField = chemicalsClass.getField("OXYGEN");
                                Object oxygenHolder = oxygenField.get(null);

                                // The field should be a DeferredHolder<Chemical, Chemical>
                                if (oxygenHolder != null) {
                                    // Get the actual chemical from the holder
                                    java.lang.reflect.Method getMethod = oxygenHolder.getClass().getMethod("get");
                                    Object oxygenChemical = getMethod.invoke(oxygenHolder);

                                    if (oxygenChemical instanceof mekanism.api.chemical.Chemical chemical) {
                                        // Get the holder from the chemical
                                        var holder = chemical.getAsHolder();
                                        ChemicalStack stack = new ChemicalStack(holder, amount);
                                        ChemicalStack remainder = distributor.insertChemical(0, stack, Action.EXECUTE);
                                        long inserted = amount - remainder.getAmount();

                                        player.sendSystemMessage(Component.literal(String.format(
                                            "Inserted %d mB of oxygen (remainder: %d mB)", inserted, remainder.getAmount())));
                                        return 1;
                                    } else {
                                        player.sendSystemMessage(Component.literal("Oxygen is not a Chemical instance: " +
                                            (oxygenChemical != null ? oxygenChemical.getClass().getName() : "null")));
                                    }
                                } else {
                                    player.sendSystemMessage(Component.literal("Could not find OXYGEN field"));
                                }
                            } catch (Exception e) {
                                player.sendSystemMessage(Component.literal("Error: " + e.getClass().getSimpleName() + ": " + e.getMessage()));
                                e.printStackTrace();
                            }
                        } else {
                            player.sendSystemMessage(Component.literal("Target block is not an oxygen distributor"));
                        }
                        return 0;
                    })))
            .then(Commands.literal("energy")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 40000))
                    .executes(context -> {
                        var player = context.getSource().getPlayerOrException();
                        var level = player.level();
                        int amount = IntegerArgumentType.getInteger(context, "amount");

                        // Get the block the player is looking at
                        HitResult result = player.pick(10, 0, false);
                        if (!(result instanceof BlockHitResult blockHit)) {
                            player.sendSystemMessage(Component.literal("Not looking at a block"));
                            return 0;
                        }

                        BlockPos pos = blockHit.getBlockPos();
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof MekanismBasedOxygenDistributor distributor) {
                            int received = distributor.receiveEnergy(amount, false);
                            player.sendSystemMessage(Component.literal(String.format(
                                "Added %d FE to distributor", received)));
                            return 1;
                        } else {
                            player.sendSystemMessage(Component.literal("Target block is not an oxygen distributor"));
                        }
                        return 0;
                    }))));
    }
}