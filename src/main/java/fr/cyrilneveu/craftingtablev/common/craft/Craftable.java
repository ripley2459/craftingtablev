package fr.cyrilneveu.craftingtablev.common.craft;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record Craftable(ItemKey key, int count) {
}
