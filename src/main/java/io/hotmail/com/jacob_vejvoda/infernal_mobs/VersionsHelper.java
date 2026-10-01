package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

public class VersionsHelper {

	/**
     * Gets the base maximum health of a LivingEntity.
     * Works on both old and new Spigot versions.
     *
     * @param entity the LivingEntity
     * @return base max health, or 20.0 if not available
     */
    @SuppressWarnings("deprecation")
    public double getMaxHealth(LivingEntity entity) {
        if (entity == null) return 20.0;

        // Try modern attribute name (1.21+)
    	Attribute a = Attribute.valueOf("MAX_HEALTH");
        AttributeInstance attr = entity.getAttribute(a);
        if (attr != null) {
            return attr.getBaseValue();        // or .getValue() if you want modifiers applied
        }

        // Fallback for older versions (pre-1.21)
        try {
        	a = Attribute.valueOf("GENERIC_MAX_HEALTH");
            attr = entity.getAttribute(a);
            if (attr != null) {
                return attr.getBaseValue();
            }
        } catch (IllegalArgumentException ignored) {
            // Attribute.GENERIC_MAX_HEALTH doesn't exist in this version
        }

        // Final fallback: use the deprecated method (still works on most versions)
        return entity.getMaxHealth();
    }

    /**
     * Sets the base maximum health of a LivingEntity.
     * Works on both old and new Spigot versions.
     *
     * @param entity   the LivingEntity
     * @param newMaxHealth the new base max health value
     */
    @SuppressWarnings("deprecation")
	public void setMaxHealth(LivingEntity entity, double newMaxHealth) {
        if (entity == null || newMaxHealth <= 0) return;

        // Try modern attribute
    	Attribute a = Attribute.valueOf("MAX_HEALTH");
        AttributeInstance attr = entity.getAttribute(a);
        if (attr != null) {
            attr.setBaseValue(newMaxHealth);
            entity.setHealth(newMaxHealth);
            //System.out.println("[IM] Set Max HP Pass 1 - " + newMaxHealth);
            return;
        }

        // Try old attribute name
        try {
        	a = Attribute.valueOf("GENERIC_MAX_HEALTH");
            attr = entity.getAttribute(a);
            if (attr != null) {
                attr.setBaseValue(newMaxHealth);
                entity.setHealth(newMaxHealth);
                //System.out.println("[IM] Set Max HP Pass 2 - " + newMaxHealth);
                return;
            }
        } catch (IllegalArgumentException ignored) {}

        // Final fallback using deprecated method
        try {
            entity.setMaxHealth(newMaxHealth);
            entity.setHealth(newMaxHealth);
            //System.out.println("[IM] Set Max HP Pass 3 - " + newMaxHealth);
        } catch (Exception ignored) {
            // If even this fails, do nothing
        	System.out.println("[IM] Set Max HP Fail - " + newMaxHealth);
        }
    }
	
}
