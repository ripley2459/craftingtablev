package fr.cyrilneveu.craftingtablev.common.tile.table;

import fr.cyrilneveu.craftingtablev.common.craft.Craftable;
import fr.cyrilneveu.craftingtablev.common.craft.ItemKey;
import fr.cyrilneveu.craftingtablev.common.net.AMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class STablePacket extends AMessage.ServerToClientMessage<STablePacket> {
    private List<Craftable> craftables = Collections.emptyList();

    public STablePacket() {
        // Nothing
    }

    public STablePacket(List<Craftable> craftables) {
        this.craftables = craftables;
    }

    @Override
    public void process(EntityPlayer player, Side side) {
        if (player.openContainer instanceof TableContainer table)
            table.setCraftables(craftables);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        int count = buffer.readVarInt();
        List<Craftable> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Item item = Item.REGISTRY.getObject(new ResourceLocation(buffer.readString(256)));
            int meta = buffer.readVarInt();
            int amount = buffer.readVarInt();
            if (item != null)
                list.add(new Craftable(new ItemKey(item, meta), amount));
        }
        craftables = list;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeVarInt(craftables.size());
        for (Craftable craftable : craftables) {
            buffer.writeString(craftable.key().getItem().getRegistryName().toString());
            buffer.writeVarInt(craftable.key().getMeta());
            buffer.writeVarInt(craftable.count());
        }
    }
}
