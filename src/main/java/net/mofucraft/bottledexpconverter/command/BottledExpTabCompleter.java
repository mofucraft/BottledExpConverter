package net.mofucraft.bottledexpconverter.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BottledExpTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("convert", "reload", "status");

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            for (String subCommand : SUBCOMMANDS) {
                if (subCommand.startsWith(input)) {
                    // 権限チェック
                    if (subCommand.equals("reload") && !sender.hasPermission("bottledexpconverter.reload")) {
                        continue;
                    }
                    if ((subCommand.equals("convert") || subCommand.equals("status"))
                            && !sender.hasPermission("bottledexpconverter.convert")) {
                        continue;
                    }
                    completions.add(subCommand);
                }
            }

            return completions;
        }

        return List.of();
    }
}
