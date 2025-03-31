package com.eslym.autocomposter.blocks.entities;

import com.eslym.autocomposter.Registries;
import com.eslym.autocomposter.utils.ExtractLockItemStackHandlerWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassEscapesDefinedScope")
public class AutoComposterBlockEntity extends BlockEntity {

    protected static final String TAG_CONSUME_CD = "consumeCoolDown";
    protected static final String TAG_COMPOST_CD = "compostCoolDown";
    protected static final String TAG_TRANSFER_CD = "transferCoolDown";

    protected ContentStack contents = new ContentStack();

    private final ExtractLockItemStackHandlerWrapper wrappedHandler = new ExtractLockItemStackHandlerWrapper(contents, i -> i < 5);
    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> wrappedHandler);

    private int consumeCoolDown = 8;
    private int compostCoolDown = 20;

    private int transferCoolDown = 7;

    public AutoComposterBlockEntity(BlockPos pos, BlockState state) {
        super(Registries.BlockEntities.AUTO_COMPOSTER.get(), pos, state);
    }

    protected AutoComposterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void serverTick(Level world, BlockPos pos) {
        this.tickPull(world, pos);
        this.tickComposter(world, pos);
        this.tickPush(world, pos);
        if (transferCoolDown == 0) {
            transferCoolDown = getTransferCoolDown();
        } else {
            transferCoolDown--;
        }
    }

    protected void tickComposter(Level world, BlockPos pos) {
        int level = getBlockState().getValue(BlockStateProperties.LEVEL_COMPOSTER);
        if (level == 8) {
            if (transferCoolDown == 0) {
                for (int i = 5; i < 10; i++) {
                    ItemStack stack = contents.getStackInSlot(i);
                    if (stack.isEmpty() || stack.getCount() < contents.getSlotLimit(i)) {
                        ItemStack newItem = new ItemStack(Items.BONE_MEAL, stack.getCount() + 1);
                        contents.setStackInSlot(i, newItem);
                        world.playSound(null, pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        BlockState state = getBlockState().setValue(BlockStateProperties.LEVEL_COMPOSTER, 0);
                        world.setBlockAndUpdate(pos, state);
                        break;
                    }
                }
            }
            return;
        } else if (level == 7) {
            if (compostCoolDown > 0) {
                compostCoolDown--;
                return;
            }
            world.playSound(null, pos, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 1.0F, 1.0F);
            BlockState state = getBlockState().setValue(BlockStateProperties.LEVEL_COMPOSTER, 8);
            world.setBlockAndUpdate(pos, state);
            return;
        }
        if (consumeCoolDown > 0) {
            consumeCoolDown--;
            return;
        }
        for (int i = 0; i < 5; i++) {
            ItemStack stack = contents.extractItem(i, 1, false);
            if (stack.isEmpty()) {
                continue;
            }
            float chance = getChance(stack.getItem());
            if (world.getRandom().nextDouble() < chance) {
                BlockState state = getBlockState().setValue(BlockStateProperties.LEVEL_COMPOSTER, level + 1);
                world.setBlockAndUpdate(pos, state);
                world.levelEvent(1500, pos, 1);
            } else {
                world.levelEvent(1500, pos, 0);
            }
            consumeCoolDown = getConsumeCoolDown();
            if (level == 6) { // The next level is full
                compostCoolDown = getCompostCoolDown();
            }
            break;
        }
    }

    protected void tickPull(Level world, BlockPos pos) {
        if (transferCoolDown > 0) return;
        BlockEntity be = world.getBlockEntity(pos.above());
        if (be == null) return;
        LazyOptional<IItemHandler> cap = be.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN);
        cap.ifPresent((handler) -> {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack extracted = handler.extractItem(i, 1, true);
                if (extracted.isEmpty() || !ComposterBlock.COMPOSTABLES.containsKey(extracted.getItem()))
                    continue;
                ItemStack rest = ItemHandlerHelper.insertItem(contents, extracted, false);
                if (rest.getCount() == 0) {
                    handler.extractItem(i, 1, false);
                    break;
                }
            }
        });
    }

    protected void tickPush(Level world, BlockPos pos) {
        if (transferCoolDown > 0) return;
        BlockEntity be = world.getBlockEntity(pos.below());
        if (be == null) return;
        LazyOptional<IItemHandler> cap = be.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN);
        cap.ifPresent((handler) -> {
            for (int i = 5; i < 10; i++) {
                ItemStack extracted = contents.extractItem(i, 1, true);
                ItemStack rest = ItemHandlerHelper.insertItem(handler, extracted, false);
                if (rest.getCount() == 0) {
                    contents.extractItem(i, 1, false);
                    break;
                }
            }
        });
    }

    protected int getConsumeCoolDown() {
        return 8;
    }

    protected int getTransferCoolDown() {
        return 7;
    }

    protected int getCompostCoolDown() {
        return 20;
    }

    protected float getChance(Item item) {
        return ComposterBlock.COMPOSTABLES.getFloat(item);
    }

    @Override
    public void load(CompoundTag tag) {
        contents.deserializeNBT(tag.getCompound(ContentStack.TAG_NAME));
        compostCoolDown = tag.getInt(TAG_COMPOST_CD);
        consumeCoolDown = tag.getInt(TAG_CONSUME_CD);
        transferCoolDown = tag.getInt(TAG_TRANSFER_CD);
        super.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put(ContentStack.TAG_NAME, contents.serializeNBT());
        tag.putInt(TAG_COMPOST_CD, compostCoolDown);
        tag.putInt(TAG_CONSUME_CD, consumeCoolDown);
        tag.putInt(TAG_TRANSFER_CD, transferCoolDown);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public NonNullList<ItemStack> getContents() {
        return contents.getStacks();
    }

    public IItemHandler getItemHandler() {
        return contents;
    }

    class ContentStack extends ItemStackHandler {
        public static final String TAG_NAME = "inv";

        private ContentStack() {
            super(10);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            super.onContentsChanged(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < 5 && ComposterBlock.COMPOSTABLES.containsKey(stack.getItem());
        }

        public NonNullList<ItemStack> getStacks() {
            return stacks;
        }
    }
}
