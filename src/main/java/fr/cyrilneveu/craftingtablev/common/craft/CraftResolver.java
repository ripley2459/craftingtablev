package fr.cyrilneveu.craftingtablev.common.craft;

import com.github.bsideup.jabel.Desugar;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;

import java.util.*;

public final class CraftResolver {
    private static final int MAX_DEPTH = 16;

    private CraftResolver() {
        // Nothing
    }

    public static Map<ItemKey, Integer> snapshot(InventoryPlayer inventory) {
        Map<ItemKey, Integer> counts = new HashMap<>();
        for (ItemStack stack : inventory.mainInventory)
            if (!stack.isEmpty())
                counts.merge(ItemKey.of(stack), stack.getCount(), Integer::sum);
        return counts;
    }

    public static CraftResult craft(ItemKey target, Map<ItemKey, Integer> snapshot) {
        Map<ItemKey, Integer> realStock = new HashMap<>(snapshot);
        Map<ItemKey, Integer> credit = new HashMap<>();

        if (!tryRecipes(target, realStock, credit, new HashSet<>(), 0, new ArrayList<>()))
            return null;

        Map<ItemKey, Integer> removals = new HashMap<>();
        for (Map.Entry<ItemKey, Integer> entry : snapshot.entrySet()) {
            int used = entry.getValue() - realStock.getOrDefault(entry.getKey(), 0);
            if (used > 0)
                removals.put(entry.getKey(), used);
        }

        Map<ItemKey, Integer> surplus = new HashMap<>();
        for (Map.Entry<ItemKey, Integer> entry : credit.entrySet())
            if (entry.getValue() > 0)
                surplus.put(entry.getKey(), entry.getValue());

        return new CraftResult(removals, surplus);
    }

    private static boolean resolve(ItemKey key, Map<ItemKey, Integer> realStock, Map<ItemKey, Integer> credit, Set<ItemKey> ancestors, int depth, List<Undo> undo) {
        int fromCredit = credit.getOrDefault(key, 0);
        if (fromCredit >= 1) {
            set(credit, key, fromCredit - 1, undo);
            creditContainerItem(key, credit, undo);
            return true;
        }

        int fromStock = realStock.getOrDefault(key, 0);
        if (fromStock >= 1) {
            set(realStock, key, fromStock - 1, undo);
            creditContainerItem(key, credit, undo);
            return true;
        }

        if (depth >= MAX_DEPTH || ancestors.contains(key))
            return false;

        return tryRecipes(key, realStock, credit, ancestors, depth, undo);
    }

    private static void creditContainerItem(ItemKey key, Map<ItemKey, Integer> credit, List<Undo> undo) {
        ItemStack probe = key.toStack(1);
        if (!probe.getItem().hasContainerItem(probe))
            return;

        ItemStack container = probe.getItem().getContainerItem(probe);
        if (container.isEmpty())
            return;

        ItemKey containerKey = ItemKey.of(container);
        set(credit, containerKey, credit.getOrDefault(containerKey, 0) + container.getCount(), undo);
    }

    private static boolean tryRecipes(ItemKey key, Map<ItemKey, Integer> realStock, Map<ItemKey, Integer> credit, Set<ItemKey> ancestors, int depth, List<Undo> undo) {
        List<IRecipe> recipes = RecipeIndex.recipesFor(key);
        if (recipes.isEmpty())
            return false;

        ancestors.add(key);
        try {
            for (IRecipe recipe : recipes) {
                int checkpoint = undo.size();
                boolean ok = true;

                for (Ingredient ingredient : recipe.getIngredients()) {
                    if (ingredient.getMatchingStacks().length == 0)
                        continue;

                    if (!resolveAny(ingredient, realStock, credit, ancestors, depth + 1, undo)) {
                        ok = false;
                        break;
                    }
                }

                if (ok) {
                    set(credit, key, credit.getOrDefault(key, 0) + RecipeIndex.outputCount(recipe), undo);
                    return true;
                }

                rollback(undo, checkpoint);
            }
            return false;
        } finally {
            ancestors.remove(key);
        }
    }

    private static boolean resolveAny(Ingredient ingredient, Map<ItemKey, Integer> realStock, Map<ItemKey, Integer> credit, Set<ItemKey> ancestors, int depth, List<Undo> undo) {
        for (ItemStack alternative : ingredient.getMatchingStacks()) {
            int checkpoint = undo.size();
            if (resolve(ItemKey.of(alternative), realStock, credit, ancestors, depth, undo))
                return true;

            rollback(undo, checkpoint);
        }
        return false;
    }

    private static void set(Map<ItemKey, Integer> ledger, ItemKey key, int value, List<Undo> undo) {
        undo.add(new Undo(ledger, key, ledger.getOrDefault(key, 0)));
        ledger.put(key, value);
    }

    private static void rollback(List<Undo> undo, int checkpoint) {
        for (int i = undo.size() - 1; i >= checkpoint; i--)
            undo.remove(i).restore();
    }

    @Desugar
    private record Undo(Map<ItemKey, Integer> ledger, ItemKey key, int previousValue) {
        void restore() {
            ledger.put(key, previousValue);
        }
    }
}
