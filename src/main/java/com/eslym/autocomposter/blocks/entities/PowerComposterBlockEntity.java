package com.eslym.autocomposter.blocks.entities;

import com.eslym.autocomposter.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

public class PowerComposterBlockEntity extends AutoComposterBlockEntity implements ContainerData {

    protected static final String TAG_ENERGY = "energy";

    protected static final String TAG_FLUID = "water";

    public static final int ENERGY_CAPACITY = 5000;

    public static final int WATER_CAPACITY = 5000;

    public static final int ENERGY_TRANSFER_RATE = 50;

    public static final int CONSUME_COST = 5;

    public static final int COMPOST_COST = 10;

    public static final int WATER_COST = 5;

    protected EnergyStorage energy = new EnergyStorage(ENERGY_CAPACITY, ENERGY_TRANSFER_RATE, ENERGY_TRANSFER_RATE);

    protected LazyOptional<IEnergyStorage> lazyEnergy = LazyOptional.of(() -> energy);

    protected FluidTank fluid = new FluidTank(WATER_CAPACITY, f -> f.getFluid().isSame(Fluids.WATER)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    protected LazyOptional<IFluidHandler> lazyFluid = LazyOptional.of(() -> fluid);

    public PowerComposterBlockEntity(BlockPos pos, BlockState state) {
        super(Registries.BlockEntities.POWER_COMPOSTER.get(), pos, state);
    }

    @Override
    protected int getConsumeCoolDown() {
        if(energy.getEnergyStored() >= CONSUME_COST){
            energy.extractEnergy(CONSUME_COST, false);
            return 4;
        }
        return super.getConsumeCoolDown();
    }

    @Override
    protected int getCompostCoolDown() {
        if(energy.getEnergyStored() >= COMPOST_COST){
            energy.extractEnergy(COMPOST_COST, false);
            return 10;
        }
        return super.getCompostCoolDown();
    }

    @Override
    protected float getChance(Item item) {
        float chance = super.getChance(item);
        if(fluid.getFluidAmount() >= WATER_COST){
            fluid.drain(WATER_COST, IFluidHandler.FluidAction.EXECUTE);
            chance *= 1.1F;
        }
        return chance;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if(cap == ForgeCapabilities.ENERGY){
            return lazyEnergy.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER){
            return lazyFluid.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_ENERGY, energy.getEnergyStored());
        tag.putInt(TAG_FLUID, fluid.getFluidAmount());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.deserializeNBT(tag.get(TAG_ENERGY));
        fluid.setFluid(new FluidStack(Fluids.WATER, tag.getInt(TAG_FLUID)));
    }

    @Override
    public int get(int index) {
        return switch (index) {
            case 0 -> energy.getEnergyStored();
            case 1 -> fluid.getFluidAmount();
            default -> throw new IndexOutOfBoundsException();
        };
    }

    @Override
    public void set(int index, int value) {
        switch (index) {
            case 0 -> energy.deserializeNBT(IntTag.valueOf(value));
            case 1 -> fluid.setFluid(new FluidStack(Fluids.WATER, value));
            default -> throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
