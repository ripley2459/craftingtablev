package fr.cyrilneveu.craftingtablev.common;

import fr.cyrilneveu.craftingtablev.CraftingTableV;
import fr.cyrilneveu.craftingtablev.common.tile.table.TableBlock;
import fr.cyrilneveu.craftingtablev.common.tile.table.TableTile;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import static fr.cyrilneveu.craftingtablev.CraftingTableVTags.MODID;
import static net.minecraft.init.Blocks.*;
import static net.minecraft.init.Items.ENDER_EYE;

@Mod.EventBusSubscriber
public abstract class ACommonProxy {
    public static TableBlock tableBlock;
    public static ItemBlock tableItemBlock;

    @SubscribeEvent
    public static void syncConfigValues(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID))
            ConfigManager.sync(MODID, Config.Type.INSTANCE);
    }

    public static void createTile(Class<? extends TileEntity> clazz, String name) {
        GameRegistry.registerTileEntity(clazz, new ResourceLocation(MODID, name));
    }

    @SubscribeEvent
    public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> event) {
        GameRegistry.addShapedRecipe(new ResourceLocation(MODID, "crafting_table_v"),
                new ResourceLocation(MODID, "recipes"),
                new ItemStack(tableBlock),
                "EDE",
                "PCP",
                "ROR",
                'E', ENDER_EYE,
                'D', DIAMOND_BLOCK,
                'P', PISTON,
                'C', CRAFTING_TABLE,
                'R', RED_NETHER_BRICK,
                'O', OBSERVER
        );
    }

    public void construct(FMLConstructionEvent event) {
        // Nothing
    }

    public void preInit(FMLPreInitializationEvent event) {
        tableBlock = new TableBlock();
        tableBlock.setRegistryName("crafting_table_v");
        tableBlock.setTranslationKey(String.join(".", MODID, "crafting_table_v"));
        tableBlock.setCreativeTab(CreativeTabs.DECORATIONS);
        ForgeRegistries.BLOCKS.register(tableBlock);

        tableItemBlock = new ItemBlock(tableBlock);
        tableItemBlock.setRegistryName(tableBlock.getRegistryName());
        tableItemBlock.setTranslationKey(tableBlock.getTranslationKey());
        tableItemBlock.setCreativeTab(CreativeTabs.DECORATIONS);
        ForgeRegistries.ITEMS.register(tableItemBlock);

        createTile(TableTile.class, "CraftingTableVTile");
    }

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(CraftingTableV.instance, new GuiHandler());
    }

    public void postInit(FMLPostInitializationEvent event) {
        // Nothing
    }

    public EntityPlayer getPlayer(MessageContext context) {
        return context.getServerHandler().player;
    }

    public IThreadListener getThread(MessageContext context) {
        return context.getServerHandler().player.server;
    }
}
