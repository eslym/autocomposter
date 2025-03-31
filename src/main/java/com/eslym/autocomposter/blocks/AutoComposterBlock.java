package com.eslym.autocomposter.blocks;

import com.eslym.autocomposter.blocks.entities.AutoComposterBlockEntity;
import com.eslym.autocomposter.menus.AutoComposterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AutoComposterBlock extends Block implements EntityBlock {

    protected static final BlockBehaviour.Properties PROPERTIES =
            BlockBehaviour.Properties
                    .of()
                    .sound(SoundType.METAL)
                    .strength(1.0f)
                    .noOcclusion();

    public static final String BLOCK_ID = "autocomposter";

    public AutoComposterBlock() {
        super(PROPERTIES);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.LEVEL_COMPOSTER, 0));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new AutoComposterBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> definition) {
        definition.add(BlockStateProperties.LEVEL_COMPOSTER);
    }
    
    
    @SuppressWarnings({"deprecation", "NullableProblems"})
    @Override
    public InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                 @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AutoComposterBlockEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, createMenu(level, pos), pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    protected MenuProvider createMenu(Level world, BlockPos pos){
        return new SimpleMenuProvider(
                (windowId, inv, player) -> new AutoComposterMenu(windowId, world, pos, inv, player),
                Component.translatable("block.autocomposter.autocomposter")
        ); 
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        return state.getValue(BlockStateProperties.LEVEL_COMPOSTER);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState oldBlock, @NotNull Level level, @NotNull BlockPos pos, BlockState newBlock, boolean someBool) {
        if (!oldBlock.is(newBlock.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AutoComposterBlockEntity) {
                Containers.dropContents(level, pos, ((AutoComposterBlockEntity) be).getContents());
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(oldBlock, level, pos, newBlock, someBool);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        } else {
            return (l, p, s, t) -> {
                if (t instanceof AutoComposterBlockEntity) {
                    ((AutoComposterBlockEntity) t).serverTick(l, p);
                }
            };
        }
    }
}
