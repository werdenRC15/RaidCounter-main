package net.werdenrc5.raidcounter.client;

import java.util.HashMap;
import java.util.Map;

public class ClientRaiderCountData {
    private static Map<String, Integer> raiderMap = new HashMap<>();
    private static boolean raidActive = false;
    private static int waveNumber = 0;
    private static int totalWaves = 0;
    
    public static void setRaiderMap(Map<String, Integer> map) {
        raiderMap = new HashMap<>(map);
    }
    
    public static Map<String, Integer> getRaiderMap() {
        return raiderMap;
    }
    
    public static void setRaidActive(boolean active) {
        raidActive = active;
    }
    
    public static boolean isRaidActive() {
        return raidActive;
    }
    
    public static void setWaveNumber(int wave) {
        waveNumber = wave;
    }
    
    public static int getWaveNumber() {
        return waveNumber;
    }
    
    public static void setTotalWaves(int waves) {
        totalWaves = waves;
    }
    
    public static int getTotalWaves() {
        return totalWaves;
    }
}