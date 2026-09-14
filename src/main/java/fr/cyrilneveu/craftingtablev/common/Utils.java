package fr.cyrilneveu.craftingtablev.common;

import fr.cyrilneveu.craftingtablev.common.craft.ItemKey;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public final class Utils {
    public static String localise(String localisationKey, Object... substitutions) {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER ? net.minecraft.util.text.translation.I18n.translateToLocalFormatted(localisationKey, substitutions) : net.minecraft.client.resources.I18n.format(localisationKey, substitutions);
    }

    public static void removeFromInventory(InventoryPlayer inventory, ItemKey key, int amount) {
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

    public static void giveOrDropItem(EntityPlayer player, ItemStack stack) {
        if (stack.isEmpty())
            return;

        ItemStack remaining = stack.copy();
        player.inventory.addItemStackToInventory(remaining);
        if (!remaining.isEmpty())
            player.dropItem(remaining, false);
    }
}
