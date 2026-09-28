package me.maxgim234.dragonEggTracker.commands;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class EggLocateCommand implements CommandExecutor {

    private final EggManager manager;

    public EggLocateCommand(EggManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {

        if (!sender.hasPermission("dragoneggtracker.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        Location loc = manager.getEggLocation();
        EggState state = manager.getEggState();

        if (loc == null || state == EggState.UNKNOWN) {
            sender.sendMessage("§cDragon Egg location is currently unknown.");
            return true;
        }

        sender.sendMessage("§dDragon Egg Location");
        sender.sendMessage("§7• World: §f" + loc.getWorld().getName());
        sender.sendMessage("§7• X: §f" + loc.getBlockX());
        sender.sendMessage("§7• Y: §f" + loc.getBlockY());
        sender.sendMessage("§7• Z: §f" + loc.getBlockZ());
        sender.sendMessage("§7• State: §f" + state.name());

        return true;
    }
}
