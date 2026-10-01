package io.hotmail.com.jacob_vejvoda.infernal_mobs;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 原作が使っている § 付きの文字列と Adventure の Component を相互に変換する。
 * 推奨されない ChatColor や setDisplayName(String) などの代わりに使う。
 */
final class LegacyText {

    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.legacySection();

    // ChatColor.translateAlternateColorCodes で変換の対象になる文字
    private static final String CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    private LegacyText() {
    }

    // ChatColor.translateAlternateColorCodes('&', text) と同じ処理
    static String color(String text) {
        char[] b = text.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && CODES.indexOf(b[i + 1]) > -1) {
                b[i] = '§';
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    // § 付きの文字列を Component にする(チャット、MOB の名前、本のページ用。null なら null)
    static Component toComponent(String text) {
        return (text == null) ? null : SECTION.deserialize(text);
    }

    // アイテムの名前・説明文用。Minecraft は何も指定しないと斜体で表示するので、斜体をオフにする
    static Component toItemComponent(String text) {
        return (text == null) ? null : SECTION.deserialize(text).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    static List<Component> toItemComponents(List<String> lines) {
        List<Component> list = new ArrayList<>();
        for (String line : lines) {
            list.add(toItemComponent(line));
        }
        return list;
    }

    // Component を § 付きの文字列にする(null なら null)
    static String toLegacy(Component component) {
        return (component == null) ? null : SECTION.serialize(component);
    }

    // ItemMeta.getDisplayName() の代わり。名前がなければ ""。
    // 名前の比較は Component 同士ではなくこの文字列で行う(更新前に作られたアイテムとも一致させるため)
    static String displayName(ItemMeta meta) {
        Component name = meta.displayName();
        return (name == null) ? "" : SECTION.serialize(name);
    }
}
