package fr.cyrilneveu.craftingtablev.common.tile.table;

import fr.cyrilneveu.craftingtablev.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import static fr.cyrilneveu.craftingtablev.CraftingTableVTags.MODID;

public class TableScreen extends GuiContainer {
    public static final ResourceLocation BACKGROUND = new ResourceLocation(MODID, "textures/interfaces/elements/background.png");
    public static final ResourceLocation SEARCH_BAR = new ResourceLocation(MODID, "textures/interfaces/elements/search_bar.png");
    public static final ResourceLocation SCROLL_BAR = new ResourceLocation(MODID, "textures/interfaces/elements/scroll_bar.png");
    public static final ResourceLocation SLOT = new ResourceLocation(MODID, "textures/interfaces/elements/slot.png");

    private final TableTile owner;

    public TableScreen(TableTile owner, Container container) {
        super(container);
        this.owner = owner;
        this.xSize = 176;
        this.ySize = 241;
    }

    private static void renderText(String text, int posX, int posY, int color, float scale, boolean dropShadow, boolean center) {
        GlStateManager.pushMatrix();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        double scaledTextWidth = center ? fontRenderer.getStringWidth(text) * scale : 0.0;
        GlStateManager.translate(posX - scaledTextWidth / 2.0, posY, 0.0f);
        GlStateManager.scale(scale, scale, scale);
        fontRenderer.drawString(text, 0, 0, color, dropShadow);
        GlStateManager.popMatrix();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawBackground(partialTicks, mouseX, mouseY);
        drawSearchBarBackground(partialTicks, mouseX, mouseY);
        drawScrollBarBackground(partialTicks, mouseX, mouseY);
        drawMainGridBackground(partialTicks, mouseX, mouseY);
        drawPlayerInventoryBackground(partialTicks, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        drawScreenTitle(mouseX, mouseY);
        drawPlayerInventoryTitle(mouseX, mouseY);
    }

    private void drawBackground(float partialTicks, int mouseX, int mouseY) {
        int posX = getGuiLeft();
        int posY = getGuiTop();
        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
        Gui.drawModalRectWithCustomSizedTexture(posX, posY, 0, 0, 176, 241, 176, 241);
    }

    private void drawSearchBarBackground(float partialTicks, int mouseX, int mouseY) {
        int posX = getGuiLeft() + 7;
        int posY = getGuiTop() + 18;
        Minecraft.getMinecraft().getTextureManager().bindTexture(SEARCH_BAR);
        Gui.drawModalRectWithCustomSizedTexture(posX, posY, 0, 0, 90, 12, 90, 12);
    }

    private void drawScrollBarBackground(float partialTicks, int mouseX, int mouseY) {
        int posX = getGuiLeft() + 155;
        int posY = getGuiTop() + 34;
        Minecraft.getMinecraft().getTextureManager().bindTexture(SCROLL_BAR);
        Gui.drawModalRectWithCustomSizedTexture(posX, posY, 0, 0, 14, 108, 14, 108);
    }

    private void drawMainGridBackground(float partialTicks, int mouseX, int mouseY) {
        int posX = getGuiLeft() + 7;
        int posY = getGuiTop() + 34;
        Minecraft.getMinecraft().getTextureManager().bindTexture(SLOT);
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 8; column++) {
                Gui.drawModalRectWithCustomSizedTexture(posX + column * 18, posY + row * 18, 0, 0, 18, 18, 18, 18);
            }
        }
    }

    private void drawPlayerInventoryBackground(float partialTicks, int mouseX, int mouseY) {
        int posX = getGuiLeft() + 7;
        int posY = getGuiTop() + 158;
        Minecraft.getMinecraft().getTextureManager().bindTexture(SLOT);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                Gui.drawModalRectWithCustomSizedTexture(posX + column * 18, posY + row * 18, 0, 0, 18, 18, 18, 18);
            }
        }

        posY += 3 * 18 + 4; // BR
        for (int column = 0; column < 9; column++)
            Gui.drawModalRectWithCustomSizedTexture(posX + column * 18, posY, 0, 0, 18, 18, 18, 18);
    }

    private void drawPlayerInventoryTitle(int mouseX, int mouseY) {
        renderText(Utils.localise("tile." + MODID + ".crafting_table_v.name"), 7, 146, 4210752, 1.0f, false, false);
    }

    private void drawScreenTitle(int mouseX, int mouseY) {
        int posX = 88;
        int posY = 6;
        renderText(Utils.localise("container.inventory"), posX, posY, 4210752, 1.0f, false, true);
    }
}
