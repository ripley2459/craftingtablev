package fr.cyrilneveu.craftingtablev.common.tile.table;

import fr.cyrilneveu.craftingtablev.common.Utils;
import fr.cyrilneveu.craftingtablev.common.craft.Craftable;
import fr.cyrilneveu.craftingtablev.common.net.NetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

import static fr.cyrilneveu.craftingtablev.CraftingTableVTags.MODID;

public class TableScreen extends GuiContainer {
    public static final ResourceLocation BACKGROUND = new ResourceLocation(MODID, "textures/interfaces/elements/background.png");
    public static final ResourceLocation SEARCH_BAR = new ResourceLocation(MODID, "textures/interfaces/elements/search_bar.png");
    public static final ResourceLocation SCROLL_BAR = new ResourceLocation(MODID, "textures/interfaces/elements/scroll_bar.png");
    public static final ResourceLocation SCROLL_BAR_HANDLE = new ResourceLocation(MODID, "textures/interfaces/elements/scroll_bar_handle.png");
    public static final ResourceLocation SLOT = new ResourceLocation(MODID, "textures/interfaces/elements/slot.png");
    private static final int GRID_X = 7;
    private static final int GRID_Y = 34;
    private static final int GRID_COLUMNS = 8;
    private static final int GRID_ROWS = 6;
    private static final int SLOT_SIZE = 18;
    private static final int TRACK_X = 155;
    private static final int TRACK_Y = 34;
    private static final int TRACK_WIDTH = 14;
    private static final int TRACK_HEIGHT = 108;
    private static final int HANDLE_WIDTH = 12;
    private static final int HANDLE_HEIGHT = 15;
    private final TableTile owner;
    private int scrollRows = 0;
    private boolean draggingScrollbar = false;

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
        drawScrollBarHandle(mouseX, mouseY);
        drawMainGridBackground(partialTicks, mouseX, mouseY);
        drawMainGridItems(mouseX, mouseY);
        drawPlayerInventoryBackground(partialTicks, mouseX, mouseY);
        drawMainGridHighlight(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        drawScreenTitle(mouseX, mouseY);
        drawPlayerInventoryTitle(mouseX, mouseY);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        drawMainGridTooltip(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton != 0)
            return;

        if (isOverScrollTrack(mouseX, mouseY)) {
            draggingScrollbar = true;
            scrollToTrackPosition(mouseY);
            return;
        }

        Craftable target = gridEntryAt(mouseX - getGuiLeft(), mouseY - getGuiTop());
        if (target != null)
            NetManager.sendToServer(new CTablePacket(target.key()));
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (draggingScrollbar)
            scrollToTrackPosition(mouseY);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        draggingScrollbar = false;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int wheel = Mouse.getEventDWheel();
        if (wheel == 0)
            return;

        scrollRows = MathHelper.clamp(scrollRows - Integer.signum(wheel), 0, maxScrollRows());
    }

    private boolean isOverScrollTrack(int mouseX, int mouseY) {
        int posX = getGuiLeft() + TRACK_X;
        int posY = getGuiTop() + TRACK_Y;
        return mouseX >= posX && mouseX < posX + TRACK_WIDTH && mouseY >= posY && mouseY < posY + TRACK_HEIGHT;
    }

    private void scrollToTrackPosition(int mouseY) {
        int maxScroll = maxScrollRows();
        if (maxScroll == 0) {
            scrollRows = 0;
            return;
        }

        int usableTrack = TRACK_HEIGHT - HANDLE_HEIGHT - 2;
        int trackTop = getGuiTop() + TRACK_Y + 1 + HANDLE_HEIGHT / 2;
        int relative = MathHelper.clamp(mouseY - trackTop, 0, usableTrack);
        scrollRows = Math.round(relative / (float) usableTrack * maxScroll);
    }

    private int maxScrollRows() {
        List<Craftable> craftables = ((TableContainer) inventorySlots).getCraftables();
        int totalRows = (craftables.size() + GRID_COLUMNS - 1) / GRID_COLUMNS;
        return Math.max(0, totalRows - GRID_ROWS);
    }

    private Craftable gridEntryAt(int relX, int relY) {
        relX -= GRID_X;
        relY -= GRID_Y;
        if (relX < 0 || relY < 0)
            return null;

        int column = relX / SLOT_SIZE;
        int row = relY / SLOT_SIZE;
        if (column >= GRID_COLUMNS || row >= GRID_ROWS)
            return null;

        List<Craftable> craftables = ((TableContainer) inventorySlots).getCraftables();
        int index = (row + scrollRows) * GRID_COLUMNS + column;
        return index < craftables.size() ? craftables.get(index) : null;
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

    private void drawScrollBarHandle(int mouseX, int mouseY) {
        int maxScroll = maxScrollRows();
        int usableTrack = TRACK_HEIGHT - HANDLE_HEIGHT - 2;
        int handleOffset = maxScroll == 0 ? 0 : Math.round(scrollRows / (float) maxScroll * usableTrack);

        int posX = getGuiLeft() + TRACK_X + (TRACK_WIDTH - HANDLE_WIDTH) / 2;
        int posY = getGuiTop() + TRACK_Y + 1 + handleOffset;

        Minecraft.getMinecraft().getTextureManager().bindTexture(SCROLL_BAR_HANDLE);
        Gui.drawModalRectWithCustomSizedTexture(posX, posY, 0, 0, HANDLE_WIDTH, HANDLE_HEIGHT, HANDLE_WIDTH, HANDLE_HEIGHT);
    }

    private void drawMainGridBackground(float partialTicks, int mouseX, int mouseY) {
        int posX = getGuiLeft() + GRID_X;
        int posY = getGuiTop() + GRID_Y;
        Minecraft.getMinecraft().getTextureManager().bindTexture(SLOT);
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int column = 0; column < GRID_COLUMNS; column++) {
                Gui.drawModalRectWithCustomSizedTexture(posX + column * SLOT_SIZE, posY + row * SLOT_SIZE, 0, 0, SLOT_SIZE, SLOT_SIZE, 18, 18);
            }
        }
    }

    private void drawMainGridItems(int mouseX, int mouseY) {
        List<Craftable> craftables = ((TableContainer) inventorySlots).getCraftables();
        scrollRows = Math.min(scrollRows, maxScrollRows());

        int posX = getGuiLeft() + GRID_X;
        int posY = getGuiTop() + GRID_Y;

        RenderHelper.enableGUIStandardItemLighting();
        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int column = 0; column < GRID_COLUMNS; column++) {
                int index = (row + scrollRows) * GRID_COLUMNS + column;
                if (index >= craftables.size())
                    continue;

                Craftable craftable = craftables.get(index);
                ItemStack stack = craftable.key().toStack(Math.max(1, craftable.count()));
                int x = posX + column * SLOT_SIZE + 1;
                int y = posY + row * SLOT_SIZE + 1;
                itemRender.renderItemAndEffectIntoGUI(stack, x, y);
                itemRender.renderItemOverlayIntoGUI(fontRenderer, stack, x, y, null);
            }
        }
        RenderHelper.disableStandardItemLighting();
    }

    private void drawMainGridTooltip(int mouseX, int mouseY) {
        if (!Minecraft.getMinecraft().player.inventory.getItemStack().isEmpty())
            return;

        Craftable hovered = gridEntryAt(mouseX - getGuiLeft(), mouseY - getGuiTop());
        if (hovered == null)
            return;

        List<String> tooltip = getItemToolTip(hovered.key().toStack(Math.max(1, hovered.count())));
        if (hovered.failingItem() != null) {
            String itemName = hovered.failingItem().toStack(1).getDisplayName();
            tooltip.add(TextFormatting.RED + Utils.localise("tooltip." + MODID + ".will_fail", itemName));
        } else {
            tooltip.add(TextFormatting.YELLOW + Utils.localise("tooltip." + MODID + ".click_to_craft"));
        }
        drawHoveringText(tooltip, mouseX, mouseY);
    }

    private void drawMainGridHighlight(int mouseX, int mouseY) {
        int relX = mouseX - getGuiLeft();
        int relY = mouseY - getGuiTop();
        if (gridEntryAt(relX, relY) == null)
            return;

        int column = (relX - GRID_X) / SLOT_SIZE;
        int row = (relY - GRID_Y) / SLOT_SIZE;
        int x = getGuiLeft() + GRID_X + column * SLOT_SIZE + 1;
        int y = getGuiTop() + GRID_Y + row * SLOT_SIZE + 1;

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.colorMask(true, true, true, false);
        drawGradientRect(x, y, x + 16, y + 16, -2130706433, -2130706433);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
    }

    private void drawPlayerInventoryBackground(float partialTicks, int mouseX, int mouseY) {
        int posX = getGuiLeft() + 7;
        int posY = getGuiTop() + 158;
        Minecraft.getMinecraft().getTextureManager().bindTexture(SLOT);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++)
                Gui.drawModalRectWithCustomSizedTexture(posX + column * SLOT_SIZE, posY + row * SLOT_SIZE, 0, 0, SLOT_SIZE, SLOT_SIZE, 18, 18);
        }

        posY += 3 * SLOT_SIZE + 4; // BR
        for (int column = 0; column < 9; column++) {
            Gui.drawModalRectWithCustomSizedTexture(posX + column * SLOT_SIZE, posY, 0, 0, SLOT_SIZE, SLOT_SIZE, 18, 18);
        }
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
