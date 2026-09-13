package fr.cyrilneveu.craftingtablev.client;

import fr.cyrilneveu.craftingtablev.common.ACommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static fr.cyrilneveu.craftingtablev.CraftingTableVTags.MODID;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public final class ClientProxy extends ACommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        ModelResourceLocation tableItemBlockRL = new ModelResourceLocation(MODID + ":crafting_table_v", "inventory");
        ModelLoader.setCustomModelResourceLocation(ACommonProxy.tableItemBlock, 0, tableItemBlockRL);
    }

    @Override
    public EntityPlayer getPlayer(MessageContext context) {
        return context.side.isClient() ? Minecraft.getMinecraft().player : super.getPlayer(context);
    }

    @Override
    public IThreadListener getThread(MessageContext context) {
        return context.side.isClient() ? Minecraft.getMinecraft() : super.getThread(context);
    }
}
