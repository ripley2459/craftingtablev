package fr.cyrilneveu.craftingtablev.common.tile.table;

import fr.cyrilneveu.craftingtablev.common.craft.ItemKey;
import fr.cyrilneveu.craftingtablev.common.net.AMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

public class CTablePacket extends AMessage.ClientToServerMessage<CTablePacket> {
    private ResourceLocation itemName;
    private int meta;

    public CTablePacket() {
        // Nothing
    }

    public CTablePacket(ItemKey target) {
        this.itemName = target.getItem().getRegistryName();
        this.meta = target.getMeta();
    }

    @Override
    public void process(EntityPlayer player, Side side) {
        Item item = itemName == null ? null : Item.REGISTRY.getObject(itemName);
        if (item == null || !(player.openContainer instanceof TableContainer table))
            return;

        table.tryCraft(new ItemKey(item, meta));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        itemName = new ResourceLocation(buffer.readString(256));
        meta = buffer.readVarInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeString(itemName.toString());
        buffer.writeVarInt(meta);
    }
}
