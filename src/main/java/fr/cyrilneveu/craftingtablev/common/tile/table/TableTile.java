package fr.cyrilneveu.craftingtablev.common.tile.table;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

public class TableTile extends TileEntity {
    public TableTile() {
        // Nothing
    }

    public Container createContainer(EntityPlayer player) {
        return new TableContainer(this, player);
    }

    public GuiContainer createGui(EntityPlayer player) {
        return new TableScreen(this, createContainer(player));
    }
}
