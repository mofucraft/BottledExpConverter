package net.mofucraft.bottledexpconverter.listener;

import net.mofucraft.bottledexpconverter.BottledExpConverter;
import net.mofucraft.bottledexpconverter.ConvertResult;
import net.mofucraft.bottledexpconverter.ExpBottleConverter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * プレイヤーログイン時のインベントリスキャン処理
 * auto-convert: true の場合のみ動作
 */
public class LoginListener implements Listener {

    private static final String PREFIX = "§7[§aBottledExpConverter§7] ";
    private final BottledExpConverter plugin;

    public LoginListener(BottledExpConverter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.isAutoConvert()) {
            return;
        }

        Player player = event.getPlayer();

        // 遅延実行でインベントリが完全にロードされるのを待つ
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            scanAndConvertInventory(player);
        }, 20L); // 1秒後に実行
    }

    private void scanAndConvertInventory(Player player) {
        if (!player.isOnline()) {
            return;
        }

        ExpBottleConverter converter = plugin.getConverter();
        PlayerInventory inventory = player.getInventory();
        int convertedCount = 0;
        long totalExp = 0;

        // メインインベントリをスキャン
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir() && converter.isLegacyBottledExp(item)) {
                ConvertResult result = converter.convert(item);
                if (result.success()) {
                    convertedCount++;
                    totalExp += result.storedExp();
                    if (plugin.isDebug()) {
                        plugin.getLogger().info("[Debug] " + player.getName() +
                                " のスロット " + i + " のBottledExpを変換: Exp = " + result.storedExp());
                    }
                }
            }
        }

        // オフハンドもスキャン
        ItemStack offhand = inventory.getItemInOffHand();
        if (!offhand.getType().isAir() && converter.isLegacyBottledExp(offhand)) {
            ConvertResult result = converter.convert(offhand);
            if (result.success()) {
                convertedCount++;
                totalExp += result.storedExp();
                if (plugin.isDebug()) {
                    plugin.getLogger().info("[Debug] " + player.getName() +
                            " のオフハンドのBottledExpを変換: Exp = " + result.storedExp());
                }
            }
        }

        if (convertedCount > 0 && plugin.isNotifyPlayer()) {
            player.sendMessage(PREFIX + "§a" + convertedCount + "個のBottledExpを自動変換しました");
            player.sendMessage("  §7合計経験値: §f" + totalExp);
        }
    }
}
