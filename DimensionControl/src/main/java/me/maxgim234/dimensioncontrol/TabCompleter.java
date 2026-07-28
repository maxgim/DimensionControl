package me.maxgim234.dimensioncontrol;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {

    private static final List<String> END_NETHER_ACTIONS = List.of("open", "close", "status");
    private static final List<String> DC_ACTIONS = List.of("end-open", "end-close", "nether-open", "nether-close", "status", "reload");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length != 1) return List.of();

        String partial = args[0].toLowerCase();
        List<String> options = cmd.getName().equalsIgnoreCase("dimensioncontrol")
                ? DC_ACTIONS
                : END_NETHER_ACTIONS;

        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(partial)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
