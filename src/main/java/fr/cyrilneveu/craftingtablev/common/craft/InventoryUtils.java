package fr.cyrilneveu.craftingtablev.common.craft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public final class InventoryUtils {
    private InventoryUtils() {
        // Nothing
    }

    public static void remove(InventoryPlayer inventory, ItemKey key, int amount) {
        int remaining = amount;
        for (int i = 0; i < inventory.mainInventory.size() && remaining > 0; i++) {
            ItemStack stack = inventory.mainInventory.get(i);
            if (stack.isEmpty() || !ItemKey.of(stack).equals(key))
                continue;

            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
        }
    }

    public static void giveOrDrop(EntityPlayer player, ItemKey key, int amount) {
        int remaining = amount;
        int maxStackSize = key.toStack(1).getMaxStackSize();

        while (remaining > 0) {
            int chunk = Math.min(remaining, maxStackSize);
            ItemStack stack = key.toStack(chunk);
            player.inventory.addItemStackToInventory(stack);
            if (!stack.isEmpty())
                player.dropItem(stack, false);
            remaining -= chunk;
        }
    }
}
