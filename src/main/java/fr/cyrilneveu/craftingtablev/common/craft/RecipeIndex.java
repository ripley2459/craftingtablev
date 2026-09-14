package fr.cyrilneveu.craftingtablev.common.craft;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;

import java.util.*;


public final class RecipeIndex {
    private static Map<ItemKey, List<IRecipe>> byOutput;

    private RecipeIndex() {
        // Nothing
    }

    public static List<IRecipe> recipesFor(ItemKey key) {
        return byOutput().getOrDefault(key, Collections.emptyList());
    }

    public static Set<ItemKey> allOutputs() {
        return byOutput().keySet();
    }

    public static int outputCount(IRecipe recipe) {
        return recipe.getRecipeOutput().getCount();
    }

    public static synchronized void invalidate() {
        byOutput = null;
    }

    private static Map<ItemKey, List<IRecipe>> byOutput() {
        if (byOutput == null)
            build();
        return byOutput;
    }

    private static synchronized void build() {
        if (byOutput != null)
            return;

        Map<ItemKey, List<IRecipe>> outputs = new LinkedHashMap<>();
        for (IRecipe recipe : CraftingManager.REGISTRY) {
            ItemStack output = recipe.getRecipeOutput();
            if (output.isEmpty() || !hasUsableIngredients(recipe))
                continue;

            outputs.computeIfAbsent(ItemKey.of(output), unused -> new ArrayList<>()).add(recipe);
        }

        byOutput = outputs;
    }

    private static boolean hasUsableIngredients(IRecipe recipe) {
        for (Ingredient ingredient : recipe.getIngredients())
            if (ingredient.getMatchingStacks().length > 0)
                return true;
        return false;
    }
}
