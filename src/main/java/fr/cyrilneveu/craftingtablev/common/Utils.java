package fr.cyrilneveu.craftingtablev.common;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public final class Utils {
    public static String localise(String localisationKey, Object... substitutions) {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER ? net.minecraft.util.text.translation.I18n.translateToLocalFormatted(localisationKey, substitutions) : net.minecraft.client.resources.I18n.format(localisationKey, substitutions);
    }
}
