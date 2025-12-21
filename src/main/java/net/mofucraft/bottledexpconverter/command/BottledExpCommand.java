package net.mofucraft.bottledexpconverter.command;

import net.mofucraft.bottledexpconverter.BottledExpConverter;
import net.mofucraft.bottledexpconverter.ConvertResult;
import net.mofucraft.bottledexpconverter.ExpBottleConverter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BottledExpCommand implements CommandExecutor {

    private static final String PREFIX = "§7[§aBottledExpConverter§7] ";
    private final BottledExpConverter plugin;

    public BottledExpCommand(BottledExpConverter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "convert" -> handleConvert(sender);
            case "reload" -> handleReload(sender);
            case "status" -> handleStatus(sender);
            default -> sendUsage(sender);
        }

        return true;
    }

    private void handleConvert(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + "§cこのコマンドはプレイヤーのみ実行できます");
            return;
        }

        if (!player.hasPermission("bottledexpconverter.convert")) {
            player.sendMessage(PREFIX + "§c権限がありません");
            return;
        }

        ExpBottleConverter converter = plugin.getConverter();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            player.sendMessage(PREFIX + "§c手にアイテムを持ってください");
            return;
        }

        if (!converter.isLegacyBottledExp(item)) {
            player.sendMessage(PREFIX + "§cこのアイテムは旧形式のBottledExpではありません");
            return;
        }

        ConvertResult result = converter.convert(item);

        if (result.success()) {
            player.sendMessage(PREFIX + "§a変換成功!");
            player.sendMessage("  §7Stored Exp: §f" + result.storedExp());
            player.sendMessage("  §7形式: §fStoredBottledExp → cmilib:storedbottledexp");
        } else {
            player.sendMessage(PREFIX + "§c変換失敗");
            player.sendMessage("  §7理由: §f" + result.getMessage());
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("bottledexpconverter.reload")) {
            sender.sendMessage(PREFIX + "§c権限がありません");
            return;
        }

        try {
            plugin.loadConfiguration();
            sender.sendMessage(PREFIX + "§a設定を再読み込みしました");
            sender.sendMessage("  §7自動変換: " + (plugin.isAutoConvert() ? "§a有効" : "§c無効"));
        } catch (Exception e) {
            sender.sendMessage(PREFIX + "§c設定の再読み込みに失敗しました: " + e.getMessage());
        }
    }

    private void handleStatus(CommandSender sender) {
        if (!sender.hasPermission("bottledexpconverter.convert")) {
            sender.sendMessage(PREFIX + "§c権限がありません");
            return;
        }

        sender.sendMessage(PREFIX + "§eステータス:");
        sender.sendMessage("  §7自動変換: " + (plugin.isAutoConvert() ? "§a有効" : "§c無効"));
        sender.sendMessage("  §7プレイヤー通知: " + (plugin.isNotifyPlayer() ? "§a有効" : "§c無効"));
        sender.sendMessage("  §7デバッグモード: " + (plugin.isDebug() ? "§a有効" : "§c無効"));

        if (plugin.isAutoConvert()) {
            sender.sendMessage("");
            sender.sendMessage("  §7自動変換タイミング:");
            sender.sendMessage("    §8- ログイン時");
            sender.sendMessage("    §8- アイテム拾得時");
            sender.sendMessage("    §8- インベントリクリック時");
            sender.sendMessage("    §8- アイテム使用時");
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(PREFIX + "§e使用方法:");
        sender.sendMessage("  §7/bexpconvert convert §f- 手持ちのBottledExpを変換");
        sender.sendMessage("  §7/bexpconvert reload §f- 設定を再読み込み");
        sender.sendMessage("  §7/bexpconvert status §f- ステータスを表示");
    }
}
