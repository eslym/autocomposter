package com.eslym.autocomposter.screens;

import com.eslym.autocomposter.menus.AutoComposterMenu;
import com.eslym.autocomposter.menus.PowerComposterMenu;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.eslym.autocomposter.AutoComposterMod.MODID;
import static com.eslym.autocomposter.blocks.entities.PowerComposterBlockEntity.ENERGY_CAPACITY;
import static com.eslym.autocomposter.blocks.entities.PowerComposterBlockEntity.WATER_CAPACITY;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PowerComposterScreen extends AutoComposterScreen {
    protected Component energyTooltip = 
             Component.translatable("tooltip.autocomposter.energy");
    protected Component waterTooltip =
            Component.translatable("tooltip.autocomposter.water");

    Optional<TooltipComponent> tooltipComponent = Optional.empty();
    

    public PowerComposterScreen(AutoComposterMenu menu, Inventory inv, Component name) {
        super(menu, inv, name);
        GUI = new ResourceLocation(MODID, "textures/gui/container/powercomposter.png");
    }

    
    public void render(@NotNull GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    
    protected void renderTooltip(@NotNull GuiGraphics matrixStack, int mouseX, int mouseY) {
        super.renderTooltip(matrixStack, mouseX, mouseY);
        if(menu instanceof PowerComposterMenu power){
            if (mouseY >= (startY + 26) && mouseY < (startY + 56)){
                if (mouseX >= (startX + 11) && mouseX < (startX + 21)){
                    Component energyText = Component.literal(String.format("%d / %d", power.getEnergyAmount(), ENERGY_CAPACITY));
                    matrixStack.renderTooltip(
                            this.font,
                            Lists.newArrayList(energyTooltip, energyText),
                            tooltipComponent,
                            mouseX, mouseY
                    );
                } else if (mouseX >= (startX + 25) && mouseX < (startX + 35)){
                    Component waterText = Component.literal(String.format("%d / %d", power.getWaterAmount(), WATER_CAPACITY));
                    matrixStack.renderTooltip(
                            this.font,
                            Lists.newArrayList(waterTooltip, waterText),
                            Optional.empty(),
                            mouseX, mouseY
                    );
                }
            }
        }
    }

    
    protected void renderBg(@NotNull GuiGraphics matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        if(menu instanceof PowerComposterMenu power){
            renderBar(matrixStack, 11, 26, 0, 189, power.getEnergyAmount(), ENERGY_CAPACITY);
            renderBar(matrixStack, 25, 26, 10, 189, power.getWaterAmount(), WATER_CAPACITY);
        }
    }

    
    protected void renderCompostLevel(@NotNull GuiGraphics matrixStack, int x, int y) {
        super.renderCompostLevel(matrixStack, 138, y);
    }

    protected void renderBar(@NotNull GuiGraphics matrixStack, int x, int y, int resX, int resY, int amount, int max){
        x += startX;
        y += startY;
        if(amount > 0){
            int height = (int) Math.ceil(30.0f * amount / max);
            int offset = 30 - height;
            matrixStack.blit(GUI, x, y + offset, resX, resY, 10, height);
        }
        matrixStack.blit(GUI, x, y, 20, 189, 10, 30);
    }
}
