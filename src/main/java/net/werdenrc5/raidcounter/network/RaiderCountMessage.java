package net.werdenrc5.raidcounter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.werdenrc5.raidcounter.client.ClientRaiderCountData;

import java.util.Map;
import java.util.function.Supplier;

public class RaiderCountMessage {
    private final Map<String, Integer> raiderMap;
    private final boolean raidActive;
    private final int waveNumber;
    private final int totalWaves;

    public RaiderCountMessage(Map<String, Integer> raiderMap, boolean raidActive, int waveNumber, int totalWaves) {
        this.raiderMap = raiderMap;
        this.raidActive = raidActive;
        this.waveNumber = waveNumber;
        this.totalWaves = totalWaves;
    }

    public RaiderCountMessage(FriendlyByteBuf buf) {
        this.raiderMap = buf.readMap(
            FriendlyByteBuf::readUtf,
            FriendlyByteBuf::readInt
        );
        this.raidActive = buf.readBoolean();
        this.waveNumber = buf.readInt();
        this.totalWaves = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(raiderMap, 
            FriendlyByteBuf::writeUtf,
            FriendlyByteBuf::writeInt
        );
        buf.writeBoolean(raidActive);
        buf.writeInt(waveNumber);
        buf.writeInt(totalWaves);
    }

    public static RaiderCountMessage decode(FriendlyByteBuf buf) {
        return new RaiderCountMessage(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientRaiderCountData.setRaiderMap(raiderMap);
            ClientRaiderCountData.setRaidActive(raidActive);
            ClientRaiderCountData.setWaveNumber(waveNumber);
            ClientRaiderCountData.setTotalWaves(totalWaves);
        });
        ctx.get().setPacketHandled(true);
    }
}