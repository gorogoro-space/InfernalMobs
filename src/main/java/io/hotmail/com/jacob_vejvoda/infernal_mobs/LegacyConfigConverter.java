package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
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
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
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
            Map.entry("DAMAGE_RESISTANCE", "RESISTANCE"),
            // ポーションの種類の名前を効果の欄に書いたもの
            Map.entry("INSTANT_HEAL", "INSTANT_HEALTH"));

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

    // 名前の種類(警告の文面と、似た名前の候補を出すのに使う)
    private enum Kind {
        ENTITY("MOB の名前"),
        EFFECT("効果の名前"),
        POTION_TYPE("ポーションの種類"),
        MATERIAL("アイテムの名前"),
        ENCHANTMENT("エンチャントの名前");

        final String label;

        Kind(String label) {
            this.label = label;
        }

        // その種類で使える名前の一覧(警告を出すときだけ作る)
        List<String> validNames() {
            List<String> names = new ArrayList<>();
            switch (this) {
                case ENTITY:
                    for (EntityType t : EntityType.values())
                        if (t != EntityType.UNKNOWN) names.add(t.name());
                    break;
                case EFFECT:
                    for (PotionEffectType t : Registry.MOB_EFFECT)
                        names.add(t.getKey().getKey().toUpperCase(Locale.ROOT));
                    break;
                case POTION_TYPE:
                    for (PotionType t : PotionType.values()) names.add(t.name());
                    break;
                case MATERIAL:
                    for (Material m : Material.values())
                        if (!m.isLegacy()) names.add(m.name());
                    break;
                case ENCHANTMENT:
                    for (Enchantment e : RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT))
                        names.add(e.getKey().getKey());
                    break;
            }
            return names;
        }
    }

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
                String fixed = fix("config.yml", path + "[" + i + "]", v, v, Kind.ENTITY, LegacyConfigConverter::isEntity, ENTITY_NAMES);
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
                fixValue(loot, "potionEffects." + id + ".potion", Kind.EFFECT, LegacyConfigConverter::isEffect, EFFECT_NAMES);
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
                    String fixed = fix("loot.yml", path + "[" + i + "]", split[0], list.get(i), Kind.EFFECT, LegacyConfigConverter::isEffect, EFFECT_NAMES);
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
                fixValue(loot, base + ".item", Kind.MATERIAL, LegacyConfigConverter::isMaterial, MATERIAL_NAMES);
                fixValue(loot, base + ".potion", Kind.POTION_TYPE, LegacyConfigConverter::isPotionType, POTION_TYPE_NAMES);
                ConfigurationSection ench = loot.getConfigurationSection(base + ".enchantments");
                if (ench == null) continue;
                for (String n : ench.getKeys(false)) {
                    fixValue(loot, base + ".enchantments." + n + ".enchantment", Kind.ENCHANTMENT, LegacyConfigConverter::isEnchantment, ENCHANTMENT_NAMES);
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

    private void fixValue(YamlConfiguration yml, String path, Kind kind, Predicate<String> valid, Map<String, String> renames) {
        if (!yml.isString(path)) return;
        String value = yml.getString(path);
        String fixed = fix("loot.yml", path, value, value, kind, valid, renames);
        if (fixed != null) yml.set(path, fixed);
    }

    /**
     * 名前が今のバージョンで使えなければ、変換表で置き換えた名前を返す。
     * 置き換えが不要か、置き換えられないときは null を返す(置き換えられないときは警告を出す)。
     * shown は警告でファイルの中の値として見せる文字列(consumeEffects の「名前:レベル:秒数」など)。
     */
    private String fix(String file, String path, String value, String shown, Kind kind, Predicate<String> valid, Map<String, String> renames) {
        if (value == null || valid.test(value)) return null;
        String upper = value.toUpperCase(Locale.ROOT);
        // 1.13 以降のサーバーで古い作りのプラグインが保存すると、アイテムの名前の頭に LEGACY_ が付く
        if (kind == Kind.MATERIAL && upper.startsWith("LEGACY_")) {
            upper = upper.substring("LEGACY_".length());
        }
        String renamed = renames.getOrDefault(upper, upper);
        if (valid.test(renamed)) {
            changes.add(file + " " + path + ": " + value + " → " + renamed);
            return renamed;
        }
        warnInvalid(file, path, value, shown, kind);
        return null;
    }

    // 使えない名前を、ファイルの中の場所(YAML の形)と似た名前の候補を付けて知らせる
    private void warnInvalid(String file, String path, String value, String shown, Kind kind) {
        List<String> lines = new ArrayList<>();
        lines.add(file + " に、このバージョンの Minecraft では使えない" + kind.label + "があります。この項目は読み込まれません。");
        // 例: potionEffects.13.potion → potionEffects: / '13': / potion: 値、enabledmobs[3] → enabledmobs: / - 値(上から 4 番目)
        String[] keys = path.split("\\.");
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String indent = "  ".repeat(i + 1);
            int bracket = key.indexOf('[');
            String name = bracket >= 0 ? key.substring(0, bracket) : key;
            String yamlKey = name.chars().allMatch(Character::isDigit) ? "'" + name + "'" : name;
            if (i < keys.length - 1) {
                lines.add(indent + yamlKey + ":");
            } else if (bracket >= 0) {
                int index = Integer.parseInt(key.substring(bracket + 1, key.length() - 1));
                lines.add(indent + yamlKey + ":");
                lines.add(indent + "  - " + shown + "   ← ここ(上から " + (index + 1) + " 番目)");
            } else {
                lines.add(indent + yamlKey + ": " + shown + "   ← ここ");
            }
        }
        List<String> candidates = similarNames(value, kind.validNames());
        if (!candidates.isEmpty()) {
            lines.add("  候補: " + String.join(", ", candidates));
        }
        String folder = plugin.getDataFolder().getPath().replace('\\', '/');
        lines.add("  " + folder + "/" + file + " のこの名前を、正しい名前に書き換えてから /im reload を実行してください。");
        for (String line : lines) {
            plugin.getLogger().warning(line);
        }
    }

    // 綴りが近い名前を最大 3 つ返す(片方がもう片方を含むものを優先し、次に編集距離の小さい順)
    private static List<String> similarNames(String value, List<String> names) {
        String v = value.toUpperCase(Locale.ROOT);
        if (v.startsWith("LEGACY_")) v = v.substring("LEGACY_".length());
        record Scored(String name, boolean contains, int distance) {}
        int limit = Math.max(2, v.length() / 3);
        List<Scored> scored = new ArrayList<>();
        for (String name : names) {
            String n = name.toUpperCase(Locale.ROOT);
            boolean contains = n.contains(v) || v.contains(n);
            int distance = editDistance(v, n);
            if (contains || distance <= limit) {
                scored.add(new Scored(name, contains, distance));
            }
        }
        scored.sort(Comparator.comparing((Scored s) -> !s.contains())
                .thenComparingInt(Scored::distance)
                .thenComparing(Scored::name));
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(3, scored.size()); i++) {
            result.add(scored.get(i).name());
        }
        return result;
    }

    private static int editDistance(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] cur = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] t = prev;
            prev = cur;
            cur = t;
        }
        return prev[b.length()];
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
