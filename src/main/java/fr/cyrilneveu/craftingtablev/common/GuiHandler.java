package fr.cyrilneveu.craftingtablev.common;

import fr.cyrilneveu.craftingtablev.common.tile.table.TableTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.jetbrains.annotations.Nullable;

public class GuiHandler implements IGuiHandler {
    public static final int CRAFTING_TABLE_V_GUI_ID = 0;

    @Override
    public @Nullable Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case CRAFTING_TABLE_V_GUI_ID:
                BlockPos pos = new BlockPos(x, y, z);
                TileEntity tileEntity = world.getTileEntity(pos);
                return tileEntity instanceof TableTile tile ? tile.createContainer(player) : null;
            default:
                return null;
        }
    }

    @Override
    public @Nullable Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case CRAFTING_TABLE_V_GUI_ID:
                BlockPos pos = new BlockPos(x, y, z);
                TileEntity tileEntity = world.getTileEntity(pos);
                return tileEntity instanceof TableTile tile ? tile.createGui(player) : null;
            default:
                return null;
        }
    }
}
