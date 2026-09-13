package fr.cyrilneveu.craftingtablev.common.tile.table;

import fr.cyrilneveu.craftingtablev.CraftingTableV;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static fr.cyrilneveu.craftingtablev.common.GuiHandler.CRAFTING_TABLE_V_GUI_ID;

public class TableBlock extends Block implements ITileEntityProvider {
    public TableBlock() {
        super(Material.WOOD);
        setHardness(2.5F);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote)
            return true;

        if (worldIn.getTileEntity(pos) instanceof TableTile && !playerIn.isSneaking()) {
            playerIn.openGui(CraftingTableV.instance, CRAFTING_TABLE_V_GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }

        return false;
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TableTile();
    }
}
