package fr.cyrilneveu.craftingtablev.server;

import fr.cyrilneveu.craftingtablev.common.ACommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
@Mod.EventBusSubscriber(Side.SERVER)
public final class ServerProxy extends ACommonProxy {
    // Nothing
}
