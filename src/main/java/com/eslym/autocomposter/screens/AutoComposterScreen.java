package com.eslym.autocomposter.screens;

import com.eslym.autocomposter.menus.AutoComposterMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import static com.eslym.autocomposter.AutoComposterMod.MODID;

public class AutoComposterScreen extends AbstractContainerScreen<AutoComposterMenu> {

    protected ResourceLocation GUI;

    protected int startX = 0;
    protected int startY = 0;

    public AutoComposterScreen(AutoComposterMenu menu, Inventory inv, Component name) {
        super(menu, inv, name);
        this.imageHeight = 165;
        this.inventoryLabelY = this.imageHeight - 94;
        GUI = new ResourceLocation(MODID, "textures/gui/container/autocomposter.png");
    }
    
    @Override
    public void render(@NotNull GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        startX = (this.width - this.imageWidth) / 2;
        startY = (this.height - this.imageHeight) / 2;

        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI);
        matrixStack.blit(GUI, startX, startY, 0, 0, this.imageWidth, this.imageHeight);
        this.renderCompostLevel(matrixStack, 128, 29);
    }
    
    protected void renderCompostLevel(@NotNull GuiGraphics matrixStack, int x, int y) {
        x += startX;
        y += startY;
        int level = menu.getCompostLevel();
        if (level == 8) {
            matrixStack.blit(GUI, x, y, 24, 165, 24, 24);
        } else if (level > 0) {
            int height = (int) (24.0f * level / 7);
            int offset = 24 - height;
            int resOffset = 165 + offset;
            matrixStack.blit(GUI, x, y + offset, 0, resOffset, 24, height);
        }
    }
}
