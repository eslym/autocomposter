package com.eslym.autocomposter.blocks;

import com.eslym.autocomposter.blocks.entities.AutoComposterBlockEntity;
import com.eslym.autocomposter.menus.AutoComposterMenu;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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

import java.util.List;

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

//    @SuppressWarnings({"deprecation", "NullableProblems"})
//    @Override
//    public InteractionResult use(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
//        if (!world.isClientSide) {
//            BlockEntity be = world.getBlockEntity(pos);
//            if (be instanceof AutoComposterBlockEntity) {
//                NetworkHooks.openGui((ServerPlayer) player, createMenu(world, pos), pos);
//            }
//        }
//        return InteractionResult.SUCCESS;
//    }

    protected MenuProvider createMenu(Level world, BlockPos pos){
        return new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
//                return new TranslatableContents("block.autocomposter.autocomposter", null, TranslatableContents.NO_ARGS);
                //TODO: TEMP FOR FIRST TIME RUNNING
                return new Component() {
                    @Override
                    public Style getStyle() {
                        return null;
                    }

                    @Override
                    public ComponentContents getContents() {
                        return null;
                    }

                    @Override
                    public List<Component> getSiblings() {
                        return List.of();
                    }

                    @Override
                    public FormattedCharSequence getVisualOrderText() {
                        return null;
                    }
                };
            }

            @Override
            public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inv, @NotNull Player p) {
                return new AutoComposterMenu(windowId, world, pos, inv, p);
            }
        };
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
