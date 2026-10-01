package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

public class VersionsHelper {

	/**
     * Gets the base maximum health of a LivingEntity.
     *
     * @param entity the LivingEntity
     * @return base max health, or 20.0 if not available
     */
    public double getMaxHealth(LivingEntity entity) {
        if (entity == null) return 20.0;

        // Paper 1.21.11 以降専用なので Attribute.MAX_HEALTH だけでよい(GENERIC_MAX_HEALTH は 1.21.2 で廃止)
        AttributeInstance attr = entity.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            return attr.getBaseValue();        // or .getValue() if you want modifiers applied
        }
        return 20.0;
    }

    /**
     * Sets the base maximum health of a LivingEntity.
     *
     * @param entity   the LivingEntity
     * @param newMaxHealth the new base max health value
     */
	public void setMaxHealth(LivingEntity entity, double newMaxHealth) {
        if (entity == null || newMaxHealth <= 0) return;

        AttributeInstance attr = entity.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(newMaxHealth);
            entity.setHealth(newMaxHealth);
            return;
        }
        System.out.println("[IM] Set Max HP Fail - " + newMaxHealth);
    }

}
