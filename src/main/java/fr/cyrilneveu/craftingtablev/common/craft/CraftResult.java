package fr.cyrilneveu.craftingtablev.common.craft;

import net.minecraft.entity.player.EntityPlayer;

import java.util.Map;

public final class CraftResult {
    private final Map<ItemKey, Integer> removals;
    private final Map<ItemKey, Integer> surplus;

    CraftResult(Map<ItemKey, Integer> removals, Map<ItemKey, Integer> surplus) {
        this.removals = removals;
        this.surplus = surplus;
    }

    public int amountOf(ItemKey key) {
        return surplus.getOrDefault(key, 0);
    }

    public void applyTo(EntityPlayer player) {
        for (Map.Entry<ItemKey, Integer> entry : removals.entrySet())
            InventoryUtils.remove(player.inventory, entry.getKey(), entry.getValue());

        for (Map.Entry<ItemKey, Integer> entry : surplus.entrySet())
            InventoryUtils.giveOrDrop(player, entry.getKey(), entry.getValue());
    }
}
