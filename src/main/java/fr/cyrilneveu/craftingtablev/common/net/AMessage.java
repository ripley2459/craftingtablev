package fr.cyrilneveu.craftingtablev.common.net;

import com.google.common.base.Preconditions;
import fr.cyrilneveu.craftingtablev.CraftingTableV;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public abstract class AMessage<T extends AMessage<T>> implements IMessage, IMessageHandler<T, IMessage> {
    private static <T extends AMessage<T>> void enqueue(AMessage<T> message, MessageContext ctx) {
        IThreadListener thread = CraftingTableV.proxy.getThread(ctx);
        thread.addScheduledTask(() -> message.process(CraftingTableV.proxy.getPlayer(ctx), ctx.side));
    }

    public abstract void process(EntityPlayer player, Side side);

    protected abstract boolean isSideValid(Side side);

    protected boolean requiresMainThread() {
        return true;
    }

    @Override
    public final IMessage onMessage(T message, MessageContext ctx) {
        Preconditions.checkArgument(message.isSideValid(ctx.side));

        if (message.requiresMainThread())
            enqueue(message, ctx);
        else message.process(CraftingTableV.proxy.getPlayer(ctx), ctx.side);

        return null;
    }

    public static abstract class ServerToClientMessage<T extends AMessage<T>> extends AMessage<T> {
        @Override
        protected boolean isSideValid(Side side) {
            return side.isClient();
        }
    }

    public static abstract class ClientToServerMessage<T extends AMessage<T>> extends AMessage<T> {
        @Override
        protected boolean isSideValid(Side side) {
            return side.isServer();
        }
    }
}
