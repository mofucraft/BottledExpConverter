package net.mofucraft.bottledexpconverter;

import net.mofucraft.bottledexpconverter.command.BottledExpCommand;
import net.mofucraft.bottledexpconverter.command.BottledExpTabCompleter;
import net.mofucraft.bottledexpconverter.listener.ItemListener;
import net.mofucraft.bottledexpconverter.listener.LoginListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class BottledExpConverter extends JavaPlugin {

    private static BottledExpConverter instance;
    private ExpBottleConverter converter;
    private boolean autoConvert;
    private boolean debug;
    private boolean notifyPlayer;

    @Override
    public void onEnable() {
        instance = this;

        // 設定ファイルの保存
        saveDefaultConfig();

        // 設定の読み込み
        loadConfiguration();

        // コマンドの登録
        PluginCommand command = getCommand("bexpconvert");
        if (command != null) {
            BottledExpCommand commandExecutor = new BottledExpCommand(this);
            command.setExecutor(commandExecutor);
            command.setTabCompleter(new BottledExpTabCompleter());
        }

        // リスナーの登録
        getServer().getPluginManager().registerEvents(new LoginListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);

        getLogger().info("BottledExpConverter が有効になりました");
        getLogger().info("自動変換: " + (autoConvert ? "有効" : "無効"));
    }

    @Override
    public void onDisable() {
        getLogger().info("BottledExpConverter が無効になりました");
    }

    public void loadConfiguration() {
        reloadConfig();

        autoConvert = getConfig().getBoolean("auto-convert", false);
        debug = getConfig().getBoolean("debug", false);
        notifyPlayer = getConfig().getBoolean("notify-player", true);

        // コンバーターは常に有効（コマンド用）
        converter = new ExpBottleConverter(true);

        if (debug) {
            getLogger().info("[Debug] 設定を読み込みました");
            getLogger().info("[Debug] auto-convert: " + autoConvert);
            getLogger().info("[Debug] notify-player: " + notifyPlayer);
        }
    }

    public static BottledExpConverter getInstance() {
        return instance;
    }

    public ExpBottleConverter getConverter() {
        return converter;
    }

    public boolean isAutoConvert() {
        return autoConvert;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isNotifyPlayer() {
        return notifyPlayer;
    }
}
