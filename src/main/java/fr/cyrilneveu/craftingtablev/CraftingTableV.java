package fr.cyrilneveu.craftingtablev;

import fr.cyrilneveu.craftingtablev.common.ACommonProxy;
import fr.cyrilneveu.craftingtablev.common.net.NetManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = CraftingTableVTags.MODID, version = CraftingTableVTags.VERSION, name = CraftingTableVTags.MODNAME, acceptedMinecraftVersions = "[1.12.2]", dependencies = "after:crafttweaker")
public final class CraftingTableV {

    public static final Logger LOGGER = LogManager.getLogger(CraftingTableVTags.MODID);

    @Mod.Instance
    public static CraftingTableV instance;

    @SidedProxy(clientSide = "fr.cyrilneveu.craftingtablev.client.ClientProxy", serverSide = "fr.cyrilneveu.craftingtablev.server.ServerProxy")
    public static ACommonProxy proxy;

    public CraftingTableV() {
        instance = this;
    }

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        proxy.construct(event);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        NetManager.registerPackets();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
