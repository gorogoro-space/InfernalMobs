package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionType;

/**
 * 1.21 未満向けの config.yml / loot.yml に残っている古い名前を、1.21 以降の名前に置き換える。
 * <ul>
 *   <li>何かを置き換えたときだけ、元のファイルを *.pre1.21.bak に残してから保存する</li>
 *   <li>今の名前として有効なものは変えない。変換表にない無効な名前は書き換えず、警告だけ出す</li>
 *   <li>何度実行しても結果は変わらない(起動時と /im reload のときに実行する)</li>
 * </ul>
 */
class LegacyConfigConverter {

    // MOB の名前(1.16 で PIG_ZOMBIE、1.20.5 で MUSHROOM_COW と SNOWMAN が改名)
    private static final Map<String, String> ENTITY_NAMES = Map.of(
            "PIG_ZOMBIE", "ZOMBIFIED_PIGLIN",
            "MUSHROOM_COW", "MOOSHROOM",
            "SNOWMAN", "SNOW_GOLEM");

    // 効果の名前(1.20.5 で改名)
    private static final Map<String, String> EFFECT_NAMES = Map.ofEntries(
            Map.entry("SLOW", "SLOWNESS"),
            Map.entry("FAST_DIGGING", "HASTE"),
            Map.entry("SLOW_DIGGING", "MINING_FATIGUE"),
            Map.entry("INCREASE_DAMAGE", "STRENGTH"),
            Map.entry("HEAL", "INSTANT_HEALTH"),
            Map.entry("HARM", "INSTANT_DAMAGE"),
            Map.entry("JUMP", "JUMP_BOOST"),
            Map.entry("CONFUSION", "NAUSEA"),
            Map.entry("DAMAGE_RESISTANCE", "RESISTANCE"));

    // ポーションの種類(1.20.5 で改名。HEAL は旧同梱ファイルにあった誤った名前)
    private static final Map<String, String> POTION_TYPE_NAMES = Map.of(
            "INSTANT_HEAL", "HEALING",
            "HEAL", "HEALING",
            "INSTANT_DAMAGE", "HARMING",
            "JUMP", "LEAPING",
            "SPEED", "SWIFTNESS",
            "REGEN", "REGENERATION");

    // アイテムの名前(1.13 より前の名前と、1.20.3・1.20.5 の改名)
    private static final Map<String, String> MATERIAL_NAMES = Map.ofEntries(
            Map.entry("WOOD_SWORD", "WOODEN_SWORD"),
            Map.entry("WOOD_AXE", "WOODEN_AXE"),
            Map.entry("WOOD_PICKAXE", "WOODEN_PICKAXE"),
            Map.entry("WOOD_SPADE", "WOODEN_SHOVEL"),
            Map.entry("WOOD_HOE", "WOODEN_HOE"),
            Map.entry("STONE_SPADE", "STONE_SHOVEL"),
            Map.entry("IRON_SPADE", "IRON_SHOVEL"),
            Map.entry("DIAMOND_SPADE", "DIAMOND_SHOVEL"),
            Map.entry("GOLD_SWORD", "GOLDEN_SWORD"),
            Map.entry("GOLD_AXE", "GOLDEN_AXE"),
            Map.entry("GOLD_PICKAXE", "GOLDEN_PICKAXE"),
            Map.entry("GOLD_SPADE", "GOLDEN_SHOVEL"),
            Map.entry("GOLD_HOE", "GOLDEN_HOE"),
            Map.entry("GOLD_HELMET", "GOLDEN_HELMET"),
            Map.entry("GOLD_CHESTPLATE", "GOLDEN_CHESTPLATE"),
            Map.entry("GOLD_LEGGINGS", "GOLDEN_LEGGINGS"),
            Map.entry("GOLD_BOOTS", "GOLDEN_BOOTS"),
            Map.entry("GRASS", "SHORT_GRASS"),
            Map.entry("SCUTE", "TURTLE_SCUTE"));

    // エンチャントの名前(昔の Bukkit の名前と、1.20.5 の改名)。loot.yml では大文字・小文字どちらでもよい
    private static final Map<String, String> ENCHANTMENT_NAMES = Map.ofEntries(
            Map.entry("PROTECTION_ENVIRONMENTAL", "PROTECTION"),
            Map.entry("PROTECTION_FIRE", "FIRE_PROTECTION"),
            Map.entry("PROTECTION_FALL", "FEATHER_FALLING"),
            Map.entry("PROTECTION_EXPLOSIONS", "BLAST_PROTECTION"),
            Map.entry("PROTECTION_PROJECTILE", "PROJECTILE_PROTECTION"),
            Map.entry("OXYGEN", "RESPIRATION"),
            Map.entry("WATER_WORKER", "AQUA_AFFINITY"),
            Map.entry("DAMAGE_ALL", "SHARPNESS"),
            Map.entry("DAMAGE_UNDEAD", "SMITE"),
            Map.entry("DAMAGE_ARTHROPODS", "BANE_OF_ARTHROPODS"),
            Map.entry("LOOT_BONUS_MOBS", "LOOTING"),
            Map.entry("SWEEPING", "SWEEPING_EDGE"),
            Map.entry("DIG_SPEED", "EFFICIENCY"),
            Map.entry("DURABILITY", "UNBREAKING"),
            Map.entry("LOOT_BONUS_BLOCKS", "FORTUNE"),
            Map.entry("ARROW_DAMAGE", "POWER"),
            Map.entry("ARROW_KNOCKBACK", "PUNCH"),
            Map.entry("ARROW_FIRE", "FLAME"),
            Map.entry("ARROW_INFINITE", "INFINITY"),
            Map.entry("LUCK", "LUCK_OF_THE_SEA"));

    // config.yml の中で MOB の名前を並べているリスト
    private static final String[] ENTITY_LISTS = {"enabledmobs", "enabledMounts", "enabledRiders", "disabledBabyMobs"};

    private final infernal_mobs plugin;
    private final List<String> changes = new ArrayList<>();

    LegacyConfigConverter(infernal_mobs plugin) {
        this.plugin = plugin;
    }

    void convert() {
        convertConfig();
        convertLoot();
    }

    private void convertConfig() {
        FileConfiguration cfg = plugin.getConfig();
        changes.clear();
        for (String path : ENTITY_LISTS) {
            // isSet は jar 内の既定値を見ないので、ファイルに書かれているものだけを対象にできる
            if (!cfg.isSet(path)) continue;
            List<String> before = cfg.getStringList(path);
            LinkedHashSet<String> after = new LinkedHashSet<>();
            for (int i = 0; i < before.size(); i++) {
                String v = before.get(i);
                String fixed = fix("config.yml", path + "[" + i + "]", v, LegacyConfigConverter::isEntity, ENTITY_NAMES);
                after.add(fixed != null ? fixed : v);
            }
            // 置き換えで同じ名前が 2 つになったら 1 つにまとめる(例: PIG_ZOMBIE と ZOMBIFIED_PIGLIN)
            if (!new ArrayList<>(after).equals(before)) {
                if (after.size() < before.size()) {
                    changes.add("config.yml " + path + ": 重複した名前を 1 つにまとめました");
                }
                cfg.set(path, new ArrayList<>(after));
            }
        }
        // mobChances はキーが MOB の名前
        if (cfg.isSet("mobChances") && cfg.isConfigurationSection("mobChances")) {
            ConfigurationSection sec = cfg.getConfigurationSection("mobChances");
            for (String key : new ArrayList<>(sec.getKeys(false))) {
                if (isEntity(key)) continue;
                String renamed = ENTITY_NAMES.get(key.toUpperCase(Locale.ROOT));
                if (renamed != null && !sec.contains(renamed)) {
                    sec.set(renamed, sec.get(key));
                    sec.set(key, null);
                    changes.add("config.yml mobChances." + key + " → mobChances." + renamed);
                }
            }
        }
        if (changes.isEmpty()) return;
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (backup(file)) {
            plugin.saveConfig();
            logChanges();
        }
    }

    private void convertLoot() {
        YamlConfiguration loot = plugin.lootFile;
        changes.clear();
        // 能力として付く効果(potionEffects.<id>.potion)
        ConfigurationSection pe = loot.getConfigurationSection("potionEffects");
        if (pe != null) {
            for (String id : pe.getKeys(false)) {
                fixValue(loot, "potionEffects." + id + ".potion", LegacyConfigConverter::isEffect, EFFECT_NAMES);
            }
        }
        // 食べたときの効果(consumeEffects.<id>.potionEffects の「名前:レベル:秒数」)
        ConfigurationSection ce = loot.getConfigurationSection("consumeEffects");
        if (ce != null) {
            for (String id : ce.getKeys(false)) {
                String path = "consumeEffects." + id + ".potionEffects";
                if (!loot.isList(path)) continue;
                List<String> list = loot.getStringList(path);
                boolean changed = false;
                for (int i = 0; i < list.size(); i++) {
                    String[] split = list.get(i).split(":", 2);
                    // fertility はこのプラグイン独自の効果
                    if (split[0].equalsIgnoreCase("fertility")) continue;
                    String fixed = fix("loot.yml", path + "[" + i + "]", split[0], LegacyConfigConverter::isEffect, EFFECT_NAMES);
                    if (fixed != null) {
                        list.set(i, split.length > 1 ? fixed + ":" + split[1] : fixed);
                        changed = true;
                    }
                }
                if (changed) loot.set(path, list);
            }
        }
        // 戦利品(loot.<id>)
        ConfigurationSection items = loot.getConfigurationSection("loot");
        if (items != null) {
            for (String id : items.getKeys(false)) {
                String base = "loot." + id;
                fixValue(loot, base + ".item", LegacyConfigConverter::isMaterial, MATERIAL_NAMES);
                fixValue(loot, base + ".potion", LegacyConfigConverter::isPotionType, POTION_TYPE_NAMES);
                ConfigurationSection ench = loot.getConfigurationSection(base + ".enchantments");
                if (ench == null) continue;
                for (String n : ench.getKeys(false)) {
                    fixValue(loot, base + ".enchantments." + n + ".enchantment", LegacyConfigConverter::isEnchantment, ENCHANTMENT_NAMES);
                }
            }
        }
        if (changes.isEmpty()) return;
        File file = new File(plugin.getDataFolder(), "loot.yml");
        if (backup(file)) {
            try {
                loot.save(file);
                logChanges();
            } catch (IOException e) {
                plugin.getLogger().severe("loot.yml の保存に失敗しました: " + e.getMessage());
            }
        }
    }

    private void fixValue(YamlConfiguration yml, String path, Predicate<String> valid, Map<String, String> renames) {
        if (!yml.isString(path)) return;
        String fixed = fix("loot.yml", path, yml.getString(path), valid, renames);
        if (fixed != null) yml.set(path, fixed);
    }

    /**
     * 名前が今のバージョンで使えなければ、変換表で置き換えた名前を返す。
     * 置き換えが不要か、置き換えられないときは null を返す(置き換えられないときは警告を出す)。
     */
    private String fix(String file, String path, String value, Predicate<String> valid, Map<String, String> renames) {
        if (value == null || valid.test(value)) return null;
        String upper = value.toUpperCase(Locale.ROOT);
        String renamed = renames.getOrDefault(upper, upper);
        if (valid.test(renamed)) {
            changes.add(file + " " + path + ": " + value + " → " + renamed);
            return renamed;
        }
        plugin.getLogger().warning(file + " " + path + ": 「" + value + "」はこのバージョンでは使えない名前です。手で直してください。");
        return null;
    }

    /** 元のファイルを *.pre1.21.bak に残す。すでにあるときは日時を付けた名前にして、前の控えを上書きしない。 */
    private boolean backup(File file) {
        if (!file.exists()) return true;
        File bak = new File(file.getParentFile(), file.getName() + ".pre1.21.bak");
        if (bak.exists()) {
            String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            bak = new File(file.getParentFile(), file.getName() + ".pre1.21-" + stamp + ".bak");
        }
        try {
            Files.copy(file.toPath(), bak.toPath());
            plugin.getLogger().info(file.getName() + " の古い名前を 1.21 以降の名前に置き換えます。元のファイルは " + bak.getName() + " に残しました。");
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe(file.getName() + " の控えを作れなかったため、変換を中止しました: " + e.getMessage());
            return false;
        }
    }

    private void logChanges() {
        for (String c : changes) {
            plugin.getLogger().info("  " + c);
        }
    }

    // 以下は、各値を実際に読み込む処理(infernal_mobs / EventListener)と同じ方法で有効かを判定する

    // enabledmobs などは EntityType#name() と文字列で比べている
    private static boolean isEntity(String s) {
        try {
            EntityType.valueOf(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // infernal_mobs.getEffectType で読んでいる
    private static boolean isEffect(String s) {
        try {
            return infernal_mobs.getEffectType(s) != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    // PotionType.valueOf(大文字) で読んでいる
    private static boolean isPotionType(String s) {
        try {
            PotionType.valueOf(s.toUpperCase(Locale.ROOT));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Material.valueOf で読んでいる(LEGACY_ で始まる古い名前は使えないものとして扱う)
    @SuppressWarnings("deprecation")
    private static boolean isMaterial(String s) {
        try {
            return !Material.valueOf(s).isLegacy();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // 小文字にしてエンチャントのレジストリから読んでいる
    private static boolean isEnchantment(String s) {
        NamespacedKey key = NamespacedKey.fromString(s.toLowerCase(Locale.ROOT));
        return key != null && RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(key) != null;
    }
}
