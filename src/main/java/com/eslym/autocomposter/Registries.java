package com.eslym.autocomposter;

import com.eslym.autocomposter.blocks.AutoComposterBlock;
import com.eslym.autocomposter.blocks.PowerComposterBlock;
import com.eslym.autocomposter.blocks.entities.AutoComposterBlockEntity;
import com.eslym.autocomposter.blocks.entities.PowerComposterBlockEntity;
import com.eslym.autocomposter.menus.AutoComposterMenu;
import com.eslym.autocomposter.menus.PowerComposterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static com.eslym.autocomposter.AutoComposterMod.MODID;

public final class Registries {
    
    public static void register(IEventBus eventBus){
        Items.ITEM_DEFERRED_REGISTER.register(eventBus);
        Blocks.BLOCK_DEFERRED_REGISTER.register(eventBus);
        BlockEntities.BLOCK_ENTITY_TYPE_DEFERRED_REGISTER.register(eventBus);
        Menus.MENU_TYPE_DEFERRED_REGISTER.register(eventBus);
        CreativeTabs.CREATIVE_MODE_TAB_DEFERRED_REGISTER.register(eventBus);
    }

    public static final class Items {
        
        private static final DeferredRegister<Item> ITEM_DEFERRED_REGISTER = DeferredRegister.
                create(ForgeRegistries.ITEMS, MODID);
        
        public static final RegistryObject<Item> AUTO_COMPOSTER = ITEM_DEFERRED_REGISTER.register(
                AutoComposterBlock.BLOCK_ID, 
                () -> new BlockItem(Blocks.AUTO_COMPOSTER.get(), new Item.Properties())
        );

        public static final RegistryObject<Item> POWER_COMPOSTER = ITEM_DEFERRED_REGISTER.register(
                PowerComposterBlock.BLOCK_ID,
                () -> new BlockItem(Blocks.POWER_COMPOSTER.get(), new Item.Properties())
        );
        
    }
    
    public static final class Blocks {

        private static final DeferredRegister<Block> BLOCK_DEFERRED_REGISTER = DeferredRegister.
                create(ForgeRegistries.BLOCKS, MODID);
        
        public static final RegistryObject<Block> AUTO_COMPOSTER = BLOCK_DEFERRED_REGISTER.register(
                AutoComposterBlock.BLOCK_ID, 
                AutoComposterBlock::new
        );
        public static final RegistryObject<Block> POWER_COMPOSTER = BLOCK_DEFERRED_REGISTER.register(
                PowerComposterBlock.BLOCK_ID, 
                PowerComposterBlock::new
        );
    }

    @SuppressWarnings("ConstantConditions")
    public static final class BlockEntities {
        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE_DEFERRED_REGISTER = DeferredRegister.
                create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

        private static <T extends BlockEntity> Supplier<BlockEntityType<T>> buildType(BlockEntityType.BlockEntitySupplier<T> supplier, RegistryObject<Block> blockRegistry){
            return () -> BlockEntityType.Builder.of(supplier, blockRegistry.get()).build(null);
        }

        public static final RegistryObject<BlockEntityType<AutoComposterBlockEntity>> AUTO_COMPOSTER =
                BLOCK_ENTITY_TYPE_DEFERRED_REGISTER.register(
                        AutoComposterBlock.BLOCK_ID, 
                        buildType(AutoComposterBlockEntity::new, Blocks.AUTO_COMPOSTER)
                );

        public static final RegistryObject<BlockEntityType<PowerComposterBlockEntity>> POWER_COMPOSTER =
                BLOCK_ENTITY_TYPE_DEFERRED_REGISTER.register(
                        PowerComposterBlock.BLOCK_ID, 
                        buildType(PowerComposterBlockEntity::new, Blocks.POWER_COMPOSTER)
                );
    }

    public static final class Menus {

        private static final DeferredRegister<MenuType<?>> MENU_TYPE_DEFERRED_REGISTER = DeferredRegister.
                create(ForgeRegistries.MENU_TYPES, MODID);

        public static final RegistryObject<MenuType<AutoComposterMenu>> AUTO_COMPOSTER = MENU_TYPE_DEFERRED_REGISTER
                .register(AutoComposterBlock.BLOCK_ID, () -> IForgeMenuType.create(
                        (containerId, playerInv, extraData) ->{
                            BlockPos pos = extraData.readBlockPos();
                            Level world = playerInv.player.getCommandSenderWorld();
                            return new AutoComposterMenu(containerId, world, pos, playerInv, playerInv.player);
                        }
                        ));

        public static final RegistryObject<MenuType<PowerComposterMenu>> POWER_COMPOSTER = MENU_TYPE_DEFERRED_REGISTER
                .register(PowerComposterBlock.BLOCK_ID, 
                        () -> IForgeMenuType.create((containerId, 
                                                     playerInv, 
                                                     extraData) -> {
                                    BlockPos pos = extraData.readBlockPos();
                                    Level world = playerInv.player.getCommandSenderWorld();
                                    return new PowerComposterMenu(containerId, world, pos, playerInv, playerInv.player);
                        }
                ));

    }
    
    public static final class CreativeTabs {
        private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB_DEFERRED_REGISTER  = DeferredRegister.
                create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MODID);

        public static final RegistryObject<CreativeModeTab> AUTOCOMPOSTER_TAB = CREATIVE_MODE_TAB_DEFERRED_REGISTER
                .register(MODID + "tab",
                () -> CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.autocomposter"))
                        .icon(() -> new ItemStack(Items.AUTO_COMPOSTER.get()))
                        .displayItems((parameters, output) -> {
                            output.accept(Items.AUTO_COMPOSTER.get());
                            output.accept(Items.POWER_COMPOSTER.get());
                        }).build());
    }
}


