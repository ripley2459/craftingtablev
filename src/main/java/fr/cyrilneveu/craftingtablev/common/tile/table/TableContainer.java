package fr.cyrilneveu.craftingtablev.common.tile.table;

import fr.cyrilneveu.craftingtablev.common.craft.*;
import fr.cyrilneveu.craftingtablev.common.net.NetManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import java.util.*;

public class TableContainer extends Container {
    private final TableTile owner;
    private final EntityPlayer player;
    private final ItemStack[] lastSnapshot = new ItemStack[36];

    private List<Craftable> craftables = Collections.emptyList();

    public TableContainer(TableTile owner, EntityPlayer playerIn) {
        this.owner = owner;
        this.player = playerIn;
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

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (!player.world.isRemote && inventoryChanged())
            refreshCraftables();
    }

    public List<Craftable> getCraftables() {
        return craftables;
    }

    public void setCraftables(List<Craftable> craftables) {
        this.craftables = craftables;
    }

    public void tryCraft(ItemKey target) {
        if (player.world.isRemote || !(player instanceof EntityPlayerMP mp))
            return;

        boolean success = craftables.stream().anyMatch(craftable -> craftable.key().equals(target)) && CraftExecutor.execute(target, mp);
        playCraftSound(mp, success);

        if (success)
            refreshCraftables();
    }

    private void playCraftSound(EntityPlayerMP mp, boolean success) {
        SoundEvent sound = success ? SoundEvents.ENTITY_ITEM_PICKUP : SoundEvents.ENTITY_VILLAGER_NO;
        mp.connection.sendPacket(new SPacketSoundEffect(sound, SoundCategory.PLAYERS, mp.posX, mp.posY, mp.posZ, 0.5F, 1.0F));
    }

    private boolean inventoryChanged() {
        boolean changed = false;
        for (int i = 0; i < lastSnapshot.length; i++) {
            ItemStack current = player.inventory.mainInventory.get(i);
            ItemStack previous = lastSnapshot[i];

            if (previous == null || previous.getItem() != current.getItem() || previous.getMetadata() != current.getMetadata() || previous.getCount() != current.getCount()) {
                changed = true;
                lastSnapshot[i] = current.isEmpty() ? ItemStack.EMPTY : current.copy();
            }
        }
        return changed;
    }

    private void refreshCraftables() {
        Map<ItemKey, Integer> snapshot = CraftResolver.snapshot(player.inventory);
        List<Craftable> updated = new ArrayList<>();
        for (ItemKey candidate : RecipeIndex.allOutputs()) {
            CraftResult result = CraftResolver.craft(candidate, snapshot);
            if (result != null)
                updated.add(new Craftable(candidate, result.amountOf(candidate), CraftExecutor.predictFailure(candidate, player)));
        }
        updated.sort(Comparator.comparing(Craftable::key, ItemKey.COMPARATOR));

        if (!updated.equals(craftables)) {
            craftables = updated;
            if (player instanceof EntityPlayerMP mp)
                NetManager.sendTo(new STablePacket(craftables), mp);
        }
    }
}
