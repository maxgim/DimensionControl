package me.maxgim234.dragonEggTracker.commands;

import me.maxgim234.dragonEggTracker.gui.RecipePreviewGUI;
import me.maxgim234.dragonEggTracker.items.TrackerRecipe;
import me.maxgim234.dragonEggTracker.util.ContainerUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DragonTrackerCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public DragonTrackerCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("dragoneggtracker.admin")) {
                sender.sendMessage("§cNo permission.");
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("recipe")) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can use this.");
                    return true;
                }

                player.openInventory(RecipePreviewGUI.create(plugin));
                return true;
            }

            plugin.reloadConfig();
            ContainerUtil.load(plugin);
            TrackerRecipe.register(plugin);

            sender.sendMessage("§aDragonEggTracker reloaded.");
            return true;
        }

        sender.sendMessage("§cUsage: /dragontracker reload");
        return true;
    }
}
