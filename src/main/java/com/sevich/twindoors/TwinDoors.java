package com.sevich.twindoors;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwinDoors implements ModInitializer {

    public static final String MOD_ID = "twindoors";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing TwinDoors mod for 1.21.10!");

        UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) -> {

            if (world.isClient() || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof DoorBlock
                    && state.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                pos = pos.down();
                state = world.getBlockState(pos);
            }

            Block block = state.getBlock();

            if (block instanceof DoorBlock) {
                DoorBlock doorBlock = (DoorBlock) block;

                if (!doorBlock.getBlockSetType().canOpenByHand()) {
                    return ActionResult.PASS;
                }

                DoorHinge hinge = state.get(DoorBlock.HINGE);
                Direction facing = state.get(DoorBlock.FACING);

                Direction neighborDirection = (hinge == DoorHinge.LEFT) ? facing.rotateYClockwise() : facing.rotateYCounterclockwise();
                BlockPos neighborPos = pos.offset(neighborDirection);
                BlockState neighborState = world.getBlockState(neighborPos);

                if (neighborState.getBlock() == block) {
                    if (neighborState.get(DoorBlock.FACING) == facing && neighborState.get(DoorBlock.HINGE) != hinge) {

                        boolean isOpen = state.get(DoorBlock.OPEN);
                        world.setBlockState(neighborPos, neighborState.with(DoorBlock.OPEN, !isOpen), Block.NOTIFY_ALL);
                    }
                }
            }

            return ActionResult.PASS;
        });
    }
}
