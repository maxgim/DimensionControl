package me.maxgim234.dimensioncontrol;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor {

    private final DimensionControl plugin;

    public Commands(DimensionControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("dimensioncontrol.admin")) {
            sender.sendMessage(plugin.getMsgNoPermission());
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getMsgUsageDc());
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "end-open":
                plugin.handleDimensionCommand(sender, "end", "open");
                break;
            case "end-close":
                plugin.handleDimensionCommand(sender, "end", "close");
                break;
            case "nether-open":
                plugin.handleDimensionCommand(sender, "nether", "open");
                break;
            case "nether-close":
                plugin.handleDimensionCommand(sender, "nether", "close");
                break;
            case "status":
                plugin.handleDimensionCommand(sender, "end",    "status");
                plugin.handleDimensionCommand(sender, "nether", "status");
                break;
            case "reload":
                plugin.loadConfigValues();
                sender.sendMessage(plugin.getMsgReloaded());
                break;
            default:
                sender.sendMessage(plugin.getMsgUsageDc());
                break;
        }

        return true;
    }
}
