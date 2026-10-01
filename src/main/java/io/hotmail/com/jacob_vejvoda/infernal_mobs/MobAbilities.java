package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.TraderLlama;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Wolf;

public class MobAbilities {
	 private static infernal_mobs plugin;
	 
	 MobAbilities(infernal_mobs instance) {
		 plugin = instance;
	 }

    /**
     * Spawns baby minions when the "mama" ability triggers.
     */
    public static void doMamaPower(LivingEntity atc, boolean playerIsVictim, String ability) {
        if (!ability.equalsIgnoreCase("mama")) return;
        if (!plugin.isLegitVictim(atc, playerIsVictim, ability)) return;

        int amount = plugin.getConfig().getInt("mamaSpawnAmount", 3); // default 3 if not set

        EntityType type = atc.getType();

        for (int i = 0; i < amount; i++) {
            spawnBabyMinion(atc, type);
        }
    }

    /**
     * Spawns one baby of the given type at the parent's location.
     * Safe on old and new versions.
     */
//    private static void spawnBabyMinion(LivingEntity parent, EntityType type) {
//        World world = parent.getWorld();
//        Location loc = parent.getLocation();
//
//        try {
//            Entity spawned = world.spawnEntity(loc, type);
//
//            if (spawned instanceof Ageable) {
//                ((Ageable) spawned).setBaby();
//            }
//            // If it's not Ageable (e.g. some new mobs or old unsupported ones), just spawn normal
//
//        } catch (Exception e) {
//            // Extremely rare fallback - most EntityTypes are valid
//            world.spawnEntity(loc, type);
//        }
//    }
    
    /**
     * Spawns a baby minion of the same type as the parent,
     * preserving colors, variants, and special traits where possible.
     * Safe across old and new Minecraft versions.
     */
    private static void spawnBabyMinion(LivingEntity parent, EntityType type) {
        if (parent == null || type == null) return;

        World world = parent.getWorld();
        // Small random offset so babies don't all stack perfectly on top of each other
        Location loc = parent.getLocation().add(
                (Math.random() - 0.5) * 0.7,
                0.15,
                (Math.random() - 0.5) * 0.7
        );

        try {
            Entity entity = world.spawnEntity(loc, type);

            if ((entity instanceof Ageable ageable)) {
                // Make it a baby
                ageable.setBaby();
            }

            // ==================== Preserve special traits ====================

            if (parent instanceof Sheep && entity instanceof Sheep) {
                ((Sheep) entity).setColor(((Sheep) parent).getColor());
            }
            else if (parent instanceof Cat && entity instanceof Cat) {
                ((Cat) entity).setCatType(((Cat) parent).getCatType());
            }
            else if (parent instanceof Wolf && entity instanceof Wolf) {
                ((Wolf) entity).setCollarColor(((Wolf) parent).getCollarColor());
            }
            else if (parent instanceof MushroomCow && entity instanceof MushroomCow) {
                ((MushroomCow) entity).setVariant(((MushroomCow) parent).getVariant());
            }
            else if (parent instanceof Llama && entity instanceof Llama) {
                ((Llama) entity).setColor(((Llama) parent).getColor());
            }
            else if (parent instanceof TraderLlama && entity instanceof TraderLlama) {
                ((TraderLlama) entity).setColor(((TraderLlama) parent).getColor());
            }
            else if (parent instanceof Horse && entity instanceof Horse) {
                Horse p = (Horse) parent;
                Horse b = (Horse) entity;
                b.setColor(p.getColor());
                b.setStyle(p.getStyle());
            }
            else if (parent instanceof Fox && entity instanceof Fox) {
                ((Fox) entity).setFoxType(((Fox) parent).getFoxType());
            }
            else if (parent instanceof Panda && entity instanceof Panda) {
                Panda p = (Panda) parent;
                Panda b = (Panda) entity;
                b.setMainGene(p.getMainGene());
                b.setHiddenGene(p.getHiddenGene());
            }
            else if (parent instanceof Parrot && entity instanceof Parrot) {
                ((Parrot) entity).setVariant(((Parrot) parent).getVariant());
            }
            else if (parent instanceof Axolotl && entity instanceof Axolotl) {
                ((Axolotl) entity).setVariant(((Axolotl) parent).getVariant());
            }
            else if (parent instanceof Frog && entity instanceof Frog) {
                ((Frog) entity).setVariant(((Frog) parent).getVariant());
            }
            else if (parent instanceof Rabbit && entity instanceof Rabbit) {
                ((Rabbit) entity).setRabbitType(((Rabbit) parent).getRabbitType());
            }
            else if (parent instanceof TropicalFish && entity instanceof TropicalFish) {
                TropicalFish p = (TropicalFish) parent;
                TropicalFish b = (TropicalFish) entity;
                b.setPattern(p.getPattern());
                b.setPatternColor(p.getPatternColor());
                b.setBodyColor(p.getBodyColor());
            }

            // Future-proof: You can add more here later (e.g. new mobs in future updates)

        } catch (Exception e) {
            // Safe fallback if something fails (old server version, invalid cast, etc.)
            try {
                world.spawnEntity(loc, type);
            } catch (Exception ignored) {
                // Do nothing - better than crashing 
            }
        }
    }
}
