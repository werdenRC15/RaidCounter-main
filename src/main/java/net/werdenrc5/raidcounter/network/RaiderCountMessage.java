package net.werdenrc5.raidcounter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.werdenrc5.raidcounter.client.ClientRaiderCountData;

import java.util.HashMap;
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
        int mapSize = buf.readInt();
        this.raiderMap = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            String key = buf.readUtf();
            Integer value = buf.readInt();
            this.raiderMap.put(key, value);
        }
        this.raidActive = buf.readBoolean();
        this.waveNumber = buf.readInt();
        this.totalWaves = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(raiderMap.size());
        for (Map.Entry<String, Integer> entry : raiderMap.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeInt(entry.getValue());
        }
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