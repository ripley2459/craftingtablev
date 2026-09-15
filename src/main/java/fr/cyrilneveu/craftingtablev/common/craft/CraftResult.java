package fr.cyrilneveu.craftingtablev.common.craft;

import java.util.Map;

public final class CraftResult {
    private final Map<ItemKey, Integer> surplus;
    private final boolean touchesContainer;

    CraftResult(Map<ItemKey, Integer> surplus, boolean touchesContainer) {
        this.surplus = surplus;
        this.touchesContainer = touchesContainer;
    }

    public int amountOf(ItemKey key) {
        return surplus.getOrDefault(key, 0);
    }

    public boolean touchesContainer() {
        return touchesContainer;
    }
}
