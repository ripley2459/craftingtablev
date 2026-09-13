package fr.cyrilneveu.craftingtablev.common.net;

import fr.cyrilneveu.craftingtablev.CraftingTableVTags;
import fr.cyrilneveu.craftingtablev.common.tile.table.CTablePacket;
import fr.cyrilneveu.craftingtablev.common.tile.table.STablePacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class NetManager {
    private static final SimpleNetworkWrapper NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(CraftingTableVTags.MODID);
    private static int ID = 0;

    public static void registerPackets() {
        // To server/On client
        registerMessage(CTablePacket.class);

        // To clients/On server
        registerMessage(STablePacket.class);
    }

    private static <T extends AMessage<T> & IMessageHandler<T, IMessage>> void registerMessage(Class<T> clazz) {
        if (AMessage.ServerToClientMessage.class.isAssignableFrom(clazz))
            NETWORK_WRAPPER.registerMessage(clazz, clazz, ID++, Side.CLIENT);
        else if (AMessage.ClientToServerMessage.class.isAssignableFrom(clazz))
            NETWORK_WRAPPER.registerMessage(clazz, clazz, ID++, Side.SERVER);
        else throw new IllegalArgumentException("The given packet does not match any side.");
    }

    public static void sendTo(IMessage message, EntityPlayerMP player) {
        NETWORK_WRAPPER.sendTo(message, player);
    }

    public static void sendToServer(IMessage message) {
        NETWORK_WRAPPER.sendToServer(message);
    }
}
