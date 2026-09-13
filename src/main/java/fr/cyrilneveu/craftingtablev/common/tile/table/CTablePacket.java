package fr.cyrilneveu.craftingtablev.common.tile.table;

import fr.cyrilneveu.craftingtablev.common.net.AMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

public class CTablePacket extends AMessage.ClientToServerMessage<CTablePacket> {
    private TableTile instigator;
    private ByteBuf data;

    public CTablePacket() {
        // Nothing
    }

    public CTablePacket(TableTile instigator) {
        this.instigator = instigator;
    }

    @Override
    public void process(EntityPlayer player, Side side) {
        // TODO
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // TODO
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // TODO
    }
}
