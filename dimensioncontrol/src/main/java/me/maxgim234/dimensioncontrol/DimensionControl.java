package me.maxgim234.dimensioncontrol;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class DimensionControl extends JavaPlugin implements Listener {

    private Set<String> closedDimensions;
    private String endClosedMsg;
    private String netherClosedMsg;

    @Override
    public void onEnable() {
        closedDimensions = new HashSet<>();
        saveDefaultConfig();
        reloadConfig();
        closedDimensions.addAll(getConfig().getStringList("closed-dimensions"));
        endClosedMsg = color(getConfig().getString("messages.end-closed", "&cThe End is currently closed!"));
        netherClosedMsg = color(getConfig().getString("messages.nether-closed", "&cThe Nether is currently closed!"));

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("DimensionControl has been enabled!");
    }

    @Override
    public void onDisable() {
        getConfig().set("closed-dimensions", closedDimensions.stream().toList());
        getLogger().info("DimensionControl has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("end")) {
            if (!sender.hasPermission("dimensioncontrol.end")) {
                sender.sendMessage("§cYou don't have permission to use this command!");
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage("§eUsage: /end <open|close|status>");
                return true;
            }

            handleDimensionCommand(sender, "end", args[0]);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("nether")) {
            if (!sender.hasPermission("dimensioncontrol.nether")) {
                sender.sendMessage("§cYou don't have permission to use this command!");
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage("§eUsage: /nether <open|close|status>");
                return true;
            }

            handleDimensionCommand(sender, "nether", args[0]);
            return true;
        }

        return false;
    }

    private void handleDimensionCommand(CommandSender sender, String dimension, String action) {
        switch (action.toLowerCase()) {
            case "open":
                closedDimensions.remove(dimension);
                sender.sendMessage("§aThe " + dimension + " has been opened!");
                break;
            case "close":
                closedDimensions.add(dimension);
                sender.sendMessage("§aThe " + dimension + " has been closed!");
                break;
            case "status":
                boolean isClosed = closedDimensions.contains(dimension);
                sender.sendMessage(
                        "§eThe " + dimension + " is currently " + (isClosed ? "§cclosed" : "§aopen") + "§e.");
                break;
            default:
                sender.sendMessage("§eUsage: /" + dimension + " <open|close|status>");
                break;
        }
    }

    private String color(String msg) {
        return msg == null ? "" : msg.replace("&", "§");
    }

    // bypass
    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        World.Environment toEnvironment = event.getTo().getWorld().getEnvironment();

        boolean hasBypass = player.hasPermission("dimensioncontrol.bypass") ||
                player.isOp();

        // End dimension check
        if (toEnvironment == World.Environment.THE_END && closedDimensions.contains("end")) {
            if (!hasBypass && !player.hasPermission("dimensioncontrol.bypass.end")) {
                event.setCancelled(true);
                player.sendMessage(endClosedMsg);
            }
        }
        // Nether dimension check
        else if (toEnvironment == World.Environment.NETHER && closedDimensions.contains("nether")) {
            if (!hasBypass && !player.hasPermission("dimensioncontrol.bypass.nether")) {
                event.setCancelled(true);
                player.sendMessage(netherClosedMsg);
            }
        }
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        World.Environment toEnvironment = event.getTo().getWorld().getEnvironment();

        // End dimension check
        if (toEnvironment == World.Environment.THE_END && closedDimensions.contains("end")) {
            event.setCancelled(true);
            for (Entity passenger : event.getEntity().getPassengers()) {
                if (passenger instanceof Player rider) {
                    rider.sendMessage(endClosedMsg);
                }
            }
        }
        // Nether dimension check
        else if (toEnvironment == World.Environment.NETHER && closedDimensions.contains("nether")) {
            event.setCancelled(true);
            for (Entity passenger : event.getEntity().getPassengers()) {
                if (passenger instanceof Player rider) {
                    rider.sendMessage(netherClosedMsg);
                }
            }
        }
    }
}
