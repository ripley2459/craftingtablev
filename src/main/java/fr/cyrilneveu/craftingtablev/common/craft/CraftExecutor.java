package fr.cyrilneveu.craftingtablev.common.craft;

import com.github.bsideup.jabel.Desugar;
import fr.cyrilneveu.craftingtablev.common.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;

import java.util.*;

public final class CraftExecutor {
    private static final int MAX_DEPTH = 16;
    private static final int MAX_STEPS = 5_000;

    private CraftExecutor() {
        // Nothing
    }

    public static boolean execute(ItemKey target, EntityPlayer player) {
        Resolution resolution = resolveTree(target, player);
        if (!resolution.success)
            return false;

        resolution.state.commit(player);
        return true;
    }

    public static ItemKey predictFailure(ItemKey target, EntityPlayer player) {
        Resolution resolution = resolveTree(target, player);
        return resolution.success ? null : resolution.state.lastContainerFailure;
    }

    private static Resolution resolveTree(ItemKey target, EntityPlayer player) {
        State state = new State(player.inventory);
        boolean success = tryRecipes(target, state, new HashSet<>(), 0, new ArrayList<>(), new int[1]);
        return new Resolution(success, state);
    }

    private static boolean resolve(ItemKey key, State state, Set<ItemKey> ancestors, int depth, List<Runnable> undo, int[] steps) {
        if (++steps[0] > MAX_STEPS)
            return false;

        ItemStack fromCredit = state.pollCredit(key, undo);
        if (fromCredit != null)
            return processContainer(fromCredit, state, undo);

        ItemStack fromStock = state.pollStock(key, undo);
        if (fromStock != null)
            return processContainer(fromStock, state, undo);

        if (depth >= MAX_DEPTH || ancestors.contains(key))
            return false;

        if (!tryRecipes(key, state, ancestors, depth, undo, steps))
            return false;

        ItemStack fromNewCredit = state.pollCredit(key, undo);
        return fromNewCredit != null && processContainer(fromNewCredit, state, undo);
    }

    private static boolean processContainer(ItemStack consumed, State state, List<Runnable> undo) {
        if (!consumed.getItem().hasContainerItem(consumed))
            return true;

        ItemStack container = consumed.getItem().getContainerItem(consumed);
        if (container == null || container.isEmpty()) {
            state.lastContainerFailure = ItemKey.of(consumed);
            return false;
        }

        state.pushCredit(ItemKey.of(container), container, undo);
        return true;
    }

    private static boolean tryRecipes(ItemKey key, State state, Set<ItemKey> ancestors, int depth, List<Runnable> undo, int[] steps) {
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

                    if (!resolveAny(ingredient, state, ancestors, depth + 1, undo, steps)) {
                        ok = false;
                        break;
                    }
                }

                if (ok) {
                    state.pushCraftedOutput(key, recipe.getRecipeOutput(), undo);
                    return true;
                }

                rollback(undo, checkpoint);

                if (steps[0] > MAX_STEPS)
                    return false;
            }
            return false;
        } finally {
            ancestors.remove(key);
        }
    }

    private static boolean resolveAny(Ingredient ingredient, State state, Set<ItemKey> ancestors, int depth, List<Runnable> undo, int[] steps) {
        for (ItemStack alternative : ingredient.getMatchingStacks()) {
            int checkpoint = undo.size();
            if (resolve(ItemKey.of(alternative), state, ancestors, depth, undo, steps))
                return true;

            rollback(undo, checkpoint);

            if (steps[0] > MAX_STEPS)
                return false;
        }
        return false;
    }

    private static void rollback(List<Runnable> undo, int checkpoint) {
        for (int i = undo.size() - 1; i >= checkpoint; i--)
            undo.remove(i).run();
    }

    private static List<ItemStack> units(ItemStack stack) {
        List<ItemStack> units = new ArrayList<>(stack.getCount());
        for (int i = 0; i < stack.getCount(); i++) {
            ItemStack unit = stack.copy();
            unit.setCount(1);
            units.add(unit);
        }
        return units;
    }

    @Desugar
    private record Resolution(boolean success, State state) {
    }

    private static final class State {
        private final InventoryPlayer inventory;
        private final Map<ItemKey, Deque<ItemStack>> stock = new HashMap<>();
        private final Map<ItemKey, Integer> drawn = new HashMap<>();
        private final Map<ItemKey, Deque<ItemStack>> credit = new HashMap<>();
        private ItemKey lastContainerFailure;

        public State(InventoryPlayer inventory) {
            this.inventory = inventory;
        }

        public ItemStack pollCredit(ItemKey key, List<Runnable> undo) {
            Deque<ItemStack> deque = credit.get(key);
            if (deque == null || deque.isEmpty())
                return null;

            ItemStack stack = deque.pollFirst();
            undo.add(() -> deque.addFirst(stack));
            return stack;
        }

        public void pushCredit(ItemKey key, ItemStack stack, List<Runnable> undo) {
            Deque<ItemStack> deque = credit.computeIfAbsent(key, unused -> new ArrayDeque<>());
            deque.addFirst(stack);
            undo.add(deque::pollFirst);
        }

        public void pushCraftedOutput(ItemKey key, ItemStack output, List<Runnable> undo) {
            for (ItemStack unit : units(output))
                pushCredit(key, unit, undo);
        }

        public ItemStack pollStock(ItemKey key, List<Runnable> undo) {
            Deque<ItemStack> deque = stock.computeIfAbsent(key, this::materialize);
            if (deque.isEmpty())
                return null;

            ItemStack stack = deque.pollFirst();
            undo.add(() -> deque.addFirst(stack));
            return stack;
        }

        private Deque<ItemStack> materialize(ItemKey key) {
            Deque<ItemStack> deque = new ArrayDeque<>();
            for (ItemStack stack : inventory.mainInventory) {
                if (stack.isEmpty() || !ItemKey.of(stack).equals(key))
                    continue;

                deque.addAll(units(stack));
            }
            drawn.put(key, deque.size());
            return deque;
        }

        public void commit(EntityPlayer player) {
            for (Map.Entry<ItemKey, Integer> entry : drawn.entrySet()) {
                int consumed = entry.getValue() - stock.get(entry.getKey()).size();
                if (consumed > 0)
                    Utils.removeFromInventory(inventory, entry.getKey(), consumed);
            }

            for (Deque<ItemStack> deque : credit.values())
                for (ItemStack stack : deque)
                    Utils.giveOrDropItem(player, stack);
        }
    }
}
