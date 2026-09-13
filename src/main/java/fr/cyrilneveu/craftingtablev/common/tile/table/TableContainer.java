package fr.cyrilneveu.craftingtablev.common.tile.table;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class TableContainer extends Container {
    private final TableTile owner;

    public TableContainer(TableTile owner, EntityPlayer playerIn) {
        this.owner = owner;
        initPlayerSlots(playerIn);
    }

    private void initPlayerSlots(EntityPlayer playerIn) {
        int posX = 7 + 1;
        int posY = 158 + 1;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(new Slot(playerIn.inventory, column + row * 9 + 9, posX + column * 18, posY + row * 18));
            }
        }

        posY += 3 * 18 + 4; // BR
        for (int column = 0; column < 9; column++)
            addSlotToContainer(new Slot(playerIn.inventory, column, posX + column * 18, posY));
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return !owner.isInvalid() && playerIn.getDistanceSq(owner.getPos().add(0.5D, 0.5D, 0.5D)) <= 64D;
    }
}
