package com.sevich.twindoors;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwinDoors implements ModInitializer {

    public static final String MOD_ID = "twindoors";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing TwinDoors mod for 1.21.1!");

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if (world.isClient() || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (!(state.getBlock() instanceof DoorBlock doorBlock)) {
                return ActionResult.PASS;
            }

            boolean isSneaking = player.isSneaking();
            boolean hasItem = !player.getStackInHand(hand).isEmpty();

            if (isSneaking && hasItem) {
                return ActionResult.PASS;
            }

            if (!doorBlock.getBlockSetType().canOpenByHand()) {
                return ActionResult.PASS;
            }

            if (state.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                pos = pos.down();
                state = world.getBlockState(pos);
                if (!(state.getBlock() instanceof DoorBlock)) {
                    return ActionResult.PASS;
                }
            }

            boolean isOpen = state.get(DoorBlock.OPEN);
            Direction facing = state.get(DoorBlock.FACING);
            DoorHinge hinge = state.get(DoorBlock.HINGE);

            Direction neighborDirection = (hinge == DoorHinge.LEFT)
                    ? facing.rotateYClockwise()
                    : facing.rotateYCounterclockwise();

            BlockPos neighborPos = pos.offset(neighborDirection);
            BlockState neighborState = world.getBlockState(neighborPos);

            if (neighborState.getBlock() == state.getBlock()
                    && neighborState.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER
                    && neighborState.get(DoorBlock.FACING) == facing
                    && neighborState.get(DoorBlock.HINGE) != hinge
                    && neighborState.get(DoorBlock.OPEN) == isOpen) {

                boolean targetOpen = !isOpen;
                world.setBlockState(neighborPos, neighborState.with(DoorBlock.OPEN, targetOpen), 10);
            }

            return ActionResult.PASS;
        });
    }
}
