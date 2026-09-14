package fr.cyrilneveu.craftingtablev.common.craft;

import java.util.Map;

public final class CraftResult {
    private final Map<ItemKey, Integer> surplus;

    CraftResult(Map<ItemKey, Integer> surplus) {
        this.surplus = surplus;
    }

    public int amountOf(ItemKey key) {
        return surplus.getOrDefault(key, 0);
    }
}
