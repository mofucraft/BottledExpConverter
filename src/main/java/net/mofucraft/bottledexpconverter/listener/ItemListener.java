package net.mofucraft.bottledexpconverter.listener;

import net.mofucraft.bottledexpconverter.BottledExpConverter;
import net.mofucraft.bottledexpconverter.ConvertResult;
import net.mofucraft.bottledexpconverter.ExpBottleConverter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.isAutoConvert()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && !item.getType().isAir()) {
            tryConvert(player, item, "使用");
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
