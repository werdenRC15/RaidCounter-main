package net.werdenrc5.raidcounter.util;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraftforge.registries.ForgeRegistries;
import net.werdenrc5.raidcounter.config.ModConfig;

import static com.mojang.text2speech.Narrator.LOGGER;

public class RaiderTypeUtil {
    // Constants for raider types
    public static final int TYPE_ILLAGER = 0;       // Abstract Illager class
    public static final int TYPE_RAIDER = 1;        // Raider class (not an Illager)
    public static final int TYPE_SPELLCASTER = 2;   // SpellcasterIllager class
    
    /**
     * Determines the type of raider based on the entity ID
     * @param entityId The entity ID string
     * @return One of the TYPE_* constants
     */
    public static int getRaiderType(String entityId) {
    ResourceLocation res = ResourceLocation.tryParse(entityId);
    if (res == null) return TYPE_ILLAGER;
    
    EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(res);
    if (entityType == null) return TYPE_ILLAGER;

    try {

        // Check for entity instance
        Entity entity = entityType.create(Minecraft.getInstance().level);
        if (entity != null) {
            if (entity instanceof SpellcasterIllager) {
                return TYPE_SPELLCASTER;
            } else if (entity instanceof Raider && !(entity instanceof AbstractIllager)) {
                return TYPE_RAIDER;
            } else if (entity instanceof AbstractIllager) {
                return TYPE_ILLAGER;
            }
        }
    } catch (Exception e) {
        LOGGER.debug("Error during class-based raider detection, falling back to string-based detection: {}", e.getMessage());
    }
    
    // If all else fails, try string-based detection
    String path = res.getPath().toLowerCase();
    if (path.contains("evoker") || path.contains("illusioner")) {
        return TYPE_SPELLCASTER;
    } else if (path.contains("ravager") || path.contains("witch")) {
        return TYPE_RAIDER;
    }
    
    // Default to regular illager
    return TYPE_ILLAGER;
}
    
    /**
     * Gets the appropriate color for a raider type
     * @param entityId The entity ID string
     * @param useColorCoding Whether color coding is enabled
     * @return The color as an integer
     */
    public static int getRaiderColor(String entityId, boolean useColorCoding) {
    if (!useColorCoding) return ModConfig.ILLAGER_COLOR.get();
    
    int type = getRaiderType(entityId);
    
    //debug logging
    LOGGER.debug("Entity {} has type {} which maps to color {}", 
        entityId, type, getColorForType(type));
    
    return getColorForType(type);
}

private static int getColorForType(int type) {
    return switch (type) {
        case TYPE_RAIDER -> ModConfig.RAIDER_COLOR.get();
        case TYPE_SPELLCASTER -> ModConfig.SPELLCASTER_COLOR.get();
        default -> ModConfig.ILLAGER_COLOR.get();
    };
}
    
    /**
     * Determines if an entity is a SpellcasterIllager based on class hierarchy
     * @param entityClass The entity class
     * @return true if the entity is a SpellcasterIllager
     */
    public static boolean isSpellcasterIllager(Class<?> entityClass) {
        return SpellcasterIllager.class.isAssignableFrom(entityClass);
    }
    
    /**
     * Determines if an entity is a regular Illager (AbstractIllager but not SpellcasterIllager)
     * @param entityClass The entity class
     * @return true if the entity is a regular Illager
     */
    public static boolean isRegularIllager(Class<?> entityClass) {
        return AbstractIllager.class.isAssignableFrom(entityClass) && 
               !SpellcasterIllager.class.isAssignableFrom(entityClass);
    }
    
    /**
     * Determines if an entity is a non-Illager Raider
     * @param entityClass The entity class
     * @return true if the entity is a Raider but not an Illager
     */
    public static boolean isNonIllagerRaider(Class<?> entityClass) {
        return Raider.class.isAssignableFrom(entityClass) && 
               !AbstractIllager.class.isAssignableFrom(entityClass);
    }
}