package net.werdenrc5.raidcounter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.werdenrc5.raidcounter.client.ClientRaiderCountData;

import java.util.Map;
import java.util.function.Supplier;

public class RaiderCountMessage {
    private final Map<String, Integer> raiderMap;
    private final boolean raidActive;

    public RaiderCountMessage(Map<String, Integer> raiderMap, boolean raidActive) {
        this.raiderMap = raiderMap;
        this.raidActive = raidActive;
    }

    public RaiderCountMessage(FriendlyByteBuf buf) {
        this.raiderMap = buf.readMap(
            FriendlyByteBuf::readUtf,
            FriendlyByteBuf::readInt
        );
        this.raidActive = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(raiderMap, 
            FriendlyByteBuf::writeUtf,
            FriendlyByteBuf::writeInt
        );
        buf.writeBoolean(raidActive);
    }

    public static RaiderCountMessage decode(FriendlyByteBuf buf) {
        return new RaiderCountMessage(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientRaiderCountData.setRaiderMap(raiderMap);
            ClientRaiderCountData.setRaidActive(raidActive);
        });
        ctx.get().setPacketHandled(true);
    }
}