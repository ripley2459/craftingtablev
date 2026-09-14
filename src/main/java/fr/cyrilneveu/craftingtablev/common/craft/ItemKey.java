package fr.cyrilneveu.craftingtablev.common.craft;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Comparator;
import java.util.Objects;

public final class ItemKey {
    public static final Comparator<ItemKey> COMPARATOR = Comparator
            .<ItemKey, String>comparing(key -> String.valueOf(key.item.getRegistryName()))
            .thenComparingInt(key -> key.meta);

    private final Item item;
    private final int meta;

    public ItemKey(Item item, int meta) {
        this.item = item;
        this.meta = item.getHasSubtypes() ? meta : 0;
    }

    public static ItemKey of(ItemStack stack) {
        return new ItemKey(stack.getItem(), stack.getMetadata());
    }

    public Item getItem() {
        return item;
    }

    public int getMeta() {
        return meta;
    }

    public ItemStack toStack(int count) {
        return new ItemStack(item, count, meta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ItemKey other))
            return false;
        return meta == other.meta && item == other.item;
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, meta);
    }
}
