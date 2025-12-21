package net.mofucraft.bottledexpconverter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * 旧形式のBottledExpを新形式（CMI互換）に変換するコンバーター
 *
 * 旧形式: custom_data に StoredBottledExp: <value> が直接格納
 * 新形式: custom_data に PublicBukkitValues: { "cmilib:storedbottledexp": <int> } が格納
 */
public class ExpBottleConverter {

    // CMIが使用するNamespacedKey（名前空間は "cmilib" が正しい）
    private static final NamespacedKey CMI_STORED_EXP_KEY = new NamespacedKey("cmilib", "storedbottledexp");

    private final boolean enabled;

    public ExpBottleConverter(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 変換が有効かどうか
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * アイテムが旧形式のBottledExpかどうか判定
     *
     * @param item 判定対象のアイテム
     * @return 旧形式の場合true
     */
    public boolean isLegacyBottledExp(ItemStack item) {
        if (item == null || item.getType() != Material.EXPERIENCE_BOTTLE) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // 既にCMI形式に変換済みの場合はスキップ
        if (pdc.has(CMI_STORED_EXP_KEY, PersistentDataType.LONG) ||
            pdc.has(CMI_STORED_EXP_KEY, PersistentDataType.INTEGER)) {
            return false;
        }

        // Loreに "Stored Exp:" が含まれているかチェック（旧形式の特徴）
        if (!meta.hasLore()) {
            return false;
        }

        List<Component> lore = meta.lore();
        if (lore == null || lore.isEmpty()) {
            return false;
        }

        // Loreを確認して旧形式かどうか判定
        for (Component line : lore) {
            String plainText = getPlainText(line);
            if (plainText.contains("Stored Exp:")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 旧形式のBottledExpを新形式に変換
     *
     * @param item 変換対象のアイテム
     * @return 変換結果
     */
    public ConvertResult convert(ItemStack item) {
        if (!enabled) {
            return ConvertResult.notEnabled();
        }

        if (item == null || item.getType() != Material.EXPERIENCE_BOTTLE) {
            return ConvertResult.failure("アイテムが経験値ボトルではありません");
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return ConvertResult.failure("アイテムのメタデータを取得できません");
        }

        // 旧形式のExp値をLoreから取得
        long storedExp = extractExpFromLore(meta);
        if (storedExp < 0) {
            return ConvertResult.failure("Stored Exp値を取得できません");
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // 既に変換済みかチェック
        if (pdc.has(CMI_STORED_EXP_KEY, PersistentDataType.LONG) ||
            pdc.has(CMI_STORED_EXP_KEY, PersistentDataType.INTEGER)) {
            return ConvertResult.alreadyConverted(storedExp);
        }

        try {
            // 新形式でExp値を保存（CMI互換 - INTEGERで保存）
            pdc.set(CMI_STORED_EXP_KEY, PersistentDataType.INTEGER, (int) storedExp);

            // Loreを更新して新しいExp値を反映
            updateLore(meta, storedExp);

            // メタデータを適用
            if (!item.setItemMeta(meta)) {
                return ConvertResult.failure("メタデータの適用に失敗しました");
            }

            return ConvertResult.success(storedExp);
        } catch (Exception e) {
            return ConvertResult.failure("変換中にエラーが発生しました: " + e.getMessage());
        }
    }

    /**
     * Loreから Stored Exp の値を抽出
     */
    private long extractExpFromLore(ItemMeta meta) {
        if (!meta.hasLore()) {
            return -1;
        }

        List<Component> lore = meta.lore();
        if (lore == null) {
            return -1;
        }

        for (Component line : lore) {
            String plainText = getPlainText(line);
            if (plainText.contains("Stored Exp:")) {
                // "Stored Exp: 12345" の形式から数値を抽出
                String[] parts = plainText.split(":");
                if (parts.length >= 2) {
                    String expPart = parts[1].trim().replaceAll("[^0-9]", "");
                    try {
                        return Long.parseLong(expPart);
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Loreを更新してExp値を新しい形式で表示
     */
    private void updateLore(ItemMeta meta, long storedExp) {
        List<Component> lore = meta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        List<Component> newLore = new ArrayList<>();

        for (Component line : lore) {
            String plainText = getPlainText(line);
            if (plainText.contains("Stored Exp:")) {
                // Loreを新しい形式で再作成
                Component newLine = Component.text("")
                        .append(Component.text("Stored Exp: ")
                                .color(NamedTextColor.YELLOW)
                                .decoration(TextDecoration.BOLD, false)
                                .decoration(TextDecoration.ITALIC, false))
                        .append(Component.text(String.valueOf(storedExp))
                                .color(NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false));
                newLore.add(newLine);
            } else {
                newLore.add(line);
            }
        }

        meta.lore(newLore);
    }

    /**
     * Componentからプレーンテキストを取得
     */
    private String getPlainText(Component component) {
        StringBuilder sb = new StringBuilder();
        appendPlainText(sb, component);
        return sb.toString();
    }

    private void appendPlainText(StringBuilder sb, Component component) {
        if (component instanceof net.kyori.adventure.text.TextComponent textComponent) {
            sb.append(textComponent.content());
        }
        for (Component child : component.children()) {
            appendPlainText(sb, child);
        }
    }
}
