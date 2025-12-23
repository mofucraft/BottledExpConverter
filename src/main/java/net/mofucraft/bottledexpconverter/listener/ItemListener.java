package net.mofucraft.bottledexpconverter.listener;

import net.mofucraft.bottledexpconverter.BottledExpConverter;
import net.mofucraft.bottledexpconverter.ConvertResult;
import net.mofucraft.bottledexpconverter.ExpBottleConverter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * アイテム関連イベントでの自動変換処理
 * auto-convert: true の場合のみ動作
 */
public class ItemListener implements Listener {

    private static final String PREFIX = "§7[§aBottledExpConverter§7] ";
    private final BottledExpConverter plugin;

    public ItemListener(BottledExpConverter plugin) {
        this.plugin = plugin;
    }

    /**
     * エンダーチェストを開いた時の処理
     * エンダーチェスト内のアイテムをスキャンして変換
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!plugin.isAutoConvert()) {
            return;
        }

        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        Inventory inventory = event.getInventory();
        if (inventory.getType() != InventoryType.ENDER_CHEST) {
            return;
        }

        ExpBottleConverter converter = plugin.getConverter();
        int convertedCount = 0;
        long totalExp = 0;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir() && converter.isLegacyBottledExp(item)) {
                ConvertResult result = converter.convert(item);
                if (result.success()) {
                    convertedCount++;
                    totalExp += result.storedExp();
                    if (plugin.isDebug()) {
                        plugin.getLogger().info("[Debug] " + player.getName() +
                                " のエンダーチェスト スロット " + i + " のBottledExpを変換: Exp = " + result.storedExp());
                    }
                }
            }
        }

        if (convertedCount > 0 && plugin.isNotifyPlayer()) {
            player.sendMessage(PREFIX + "§aエンダーチェスト内の" + convertedCount + "個のBottledExpを自動変換しました");
            player.sendMessage("  §7合計経験値: §f" + totalExp);
        }
    }

    /**
     * アイテムを拾った時の処理
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!plugin.isAutoConvert()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack item = event.getItem().getItemStack();
        tryConvert(player, item, "拾得");
    }

    /**
     * インベントリでアイテムをクリックした時の処理
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.isAutoConvert()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // クリックしたアイテムを変換
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null && !currentItem.getType().isAir()) {
            tryConvert(player, currentItem, "クリック");
        }

        // カーソル上のアイテムも変換
        ItemStack cursorItem = event.getCursor();
        if (cursorItem != null && !cursorItem.getType().isAir()) {
            tryConvert(player, cursorItem, "カーソル");
        }
    }

    /**
     * アイテムを使用しようとした時の処理
     * 旧形式のBottledExpを投げようとした場合はキャンセルして変換
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.isAutoConvert()) {
            return;
        }

        // 右クリック（投擲動作）のみ処理
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType().isAir()) {
            return;
        }

        ExpBottleConverter converter = plugin.getConverter();

        // 旧形式のBottledExpでない場合は通常通り投げられる
        if (!converter.isLegacyBottledExp(item)) {
            return;
        }

        // 旧形式のBottledExpを投げようとした場合
        // イベントをキャンセルして変換
        event.setCancelled(true);

        ConvertResult result = converter.convert(item);

        if (result.success()) {
            if (plugin.isDebug()) {
                plugin.getLogger().info("[Debug] " + player.getName() +
                        " のBottledExpを投擲前に変換: Exp = " + result.storedExp());
            }

            if (plugin.isNotifyPlayer()) {
                player.sendMessage(PREFIX + "§aBottledExpを変換しました。もう一度投げてください (Exp: " + result.storedExp() + ")");
            }
        } else {
            player.sendMessage(PREFIX + "§c変換に失敗しました: " + result.getMessage());
        }
    }

    /**
     * アイテムの変換を試みる
     */
    private void tryConvert(Player player, ItemStack item, String trigger) {
        ExpBottleConverter converter = plugin.getConverter();

        if (!converter.isLegacyBottledExp(item)) {
            return;
        }

        ConvertResult result = converter.convert(item);

        if (result.success()) {
            if (plugin.isDebug()) {
                plugin.getLogger().info("[Debug] " + player.getName() +
                        " のBottledExpを自動変換 (" + trigger + "): Exp = " + result.storedExp());
            }

            if (plugin.isNotifyPlayer()) {
                player.sendMessage(PREFIX + "§aBottledExpを自動変換しました (Exp: " + result.storedExp() + ")");
            }
        }
    }
}
