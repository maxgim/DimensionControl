package me.maxgim234.dimensioncontrol;

import org.bstats.bukkit.Metrics;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public final class DimensionControl extends JavaPlugin implements Listener {

    private Set<String> closedDimensions;

    private File dataFile;
    private FileConfiguration dataConfig;

    // Block messages
    private String endClosedMsg;
    private String netherClosedMsg;

    // Command response messages
    private String msgOpened;
    private String msgClosed;
    private String msgStatusLine;
    private String msgNoPermission;
    private String msgReloaded;
    private String msgUsageEnd;
    private String msgUsageNether;
    private String msgUsageDc;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        int pluginId = 31795;
        Metrics metrics = new Metrics(this, pluginId);

        // data.yml — closed-dimensions state only
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create data.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        closedDimensions = new HashSet<>();
        loadConfigValues();

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("end").setExecutor(this);
        getCommand("end").setTabCompleter(new TabCompleter());

        getCommand("nether").setExecutor(this);
        getCommand("nether").setTabCompleter(new TabCompleter());

        getCommand("dimensioncontrol").setExecutor(new Commands(this));
        getCommand("dimensioncontrol").setTabCompleter(new TabCompleter());

        getLogger().info("DimensionControl has been enabled!");
    }

    @Override
    public void onDisable() {
        saveData();
        getLogger().info("DimensionControl has been disabled!");
    }

    public void loadConfigValues() {
        // Reload messages from config.yml (never written back)
        reloadConfig();
        FileConfiguration cfg = getConfig();

        endClosedMsg    = color(cfg.getString("messages.end-closed",    "&cThe End is currently closed!"));
        netherClosedMsg = color(cfg.getString("messages.nether-closed", "&cThe Nether is currently closed!"));
        msgOpened       = color(cfg.getString("messages.opened",        "&aThe {dimension} has been opened!"));
        msgClosed       = color(cfg.getString("messages.closed",        "&aThe {dimension} has been closed!"));
        msgStatusLine   = color(cfg.getString("messages.status-line",   "&eThe {dimension} is currently {state}&e."));
        msgNoPermission = color(cfg.getString("messages.no-permission", "&cYou don't have permission to use this command!"));
        msgReloaded     = color(cfg.getString("messages.reloaded",      "&aConfig reloaded!"));
        msgUsageEnd     = color(cfg.getString("messages.usage-end",     "&eUsage: /end <open|close|status>"));
        msgUsageNether  = color(cfg.getString("messages.usage-nether",  "&eUsage: /nether <open|close|status>"));
        msgUsageDc      = color(cfg.getString("messages.usage-dc",      "&eUsage: /dimensioncontrol <nether-open|nether-close|end-open|end-close|status|reload>"));

        // Reload closed-dimensions from data.yml
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        closedDimensions = new HashSet<>(dataConfig.getStringList("closed-dimensions"));
    }

    public void saveData() {
        dataConfig.set("closed-dimensions", closedDimensions.stream().toList());
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("end")) {
            if (!sender.hasPermission("dimensioncontrol.end")) {
                sender.sendMessage(msgNoPermission);
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(msgUsageEnd);
                return true;
            }
            handleDimensionCommand(sender, "end", args[0]);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("nether")) {
            if (!sender.hasPermission("dimensioncontrol.nether")) {
                sender.sendMessage(msgNoPermission);
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(msgUsageNether);
                return true;
            }
            handleDimensionCommand(sender, "nether", args[0]);
            return true;
        }

        return false;
    }

    public void handleDimensionCommand(CommandSender sender, String dimension, String action) {
        switch (action.toLowerCase()) {
            case "open":
                closedDimensions.remove(dimension);
                saveData();
                sender.sendMessage(msgOpened.replace("{dimension}", capitalize(dimension)));
                break;
            case "close":
                closedDimensions.add(dimension);
                saveData();
                sender.sendMessage(msgClosed.replace("{dimension}", capitalize(dimension)));
                break;
            case "status":
                boolean isClosed = closedDimensions.contains(dimension);
                String state = isClosed ? color("&cclosed") : color("&aopen");
                sender.sendMessage(
                    msgStatusLine
                        .replace("{dimension}", capitalize(dimension))
                        .replace("{state}", state)
                );
                break;
            default:
                sender.sendMessage(dimension.equals("end") ? msgUsageEnd : msgUsageNether);
                break;
        }
    }

    public boolean isDimensionClosed(String dimension) {
        return closedDimensions.contains(dimension);
    }

    public String getMsgUsageDc()      { return msgUsageDc; }
    public String getMsgNoPermission() { return msgNoPermission; }
    public String getMsgReloaded()     { return msgReloaded; }
    public String getMsgStatusLine()   { return msgStatusLine; }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String color(String msg) {
        return msg == null ? "" : msg.replace("&", "§");
    }

    // --- Portal Events ---

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getTo() == null || event.getTo().getWorld() == null) return;

        Player player = event.getPlayer();
        World.Environment toEnvironment = event.getTo().getWorld().getEnvironment();

        boolean hasBypass = player.hasPermission("dimensioncontrol.bypass") || player.isOp();

        if (toEnvironment == World.Environment.THE_END && closedDimensions.contains("end")) {
            if (!hasBypass && !player.hasPermission("dimensioncontrol.bypass.end")) {
                event.setCancelled(true);
                player.sendMessage(endClosedMsg);
            }
        } else if (toEnvironment == World.Environment.NETHER && closedDimensions.contains("nether")) {
            if (!hasBypass && !player.hasPermission("dimensioncontrol.bypass.nether")) {
                event.setCancelled(true);
                player.sendMessage(netherClosedMsg);
            }
        }
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        if (event.getTo() == null || event.getTo().getWorld() == null) return;

        World.Environment toEnvironment = event.getTo().getWorld().getEnvironment();

        if (toEnvironment == World.Environment.THE_END && closedDimensions.contains("end")) {
            event.setCancelled(true);
            for (Entity passenger : event.getEntity().getPassengers()) {
                if (passenger instanceof Player rider) {
                    rider.sendMessage(endClosedMsg);
                }
            }
        } else if (toEnvironment == World.Environment.NETHER && closedDimensions.contains("nether")) {
            event.setCancelled(true);
            for (Entity passenger : event.getEntity().getPassengers()) {
                if (passenger instanceof Player rider) {
                    rider.sendMessage(netherClosedMsg);
                }
            }
        }
    }

    @EventHandler
    public void onEntityPortalReady(EntityPortalReadyEvent event) {
        World destWorld = event.getTargetWorld();
        if (destWorld == null) return;

        String dimension = destWorld.getEnvironment() == World.Environment.THE_END ? "end"
                : destWorld.getEnvironment() == World.Environment.NETHER ? "nether"
                : null;
        if (dimension == null || !closedDimensions.contains(dimension)) return;

        Entity entity = event.getEntity();
        Player player = entity instanceof Player p ? p
                : entity.getPassengers().stream()
                .filter(pa -> pa instanceof Player)
                .map(pa -> (Player) pa)
                .findFirst().orElse(null);
        if (player == null) return;

        boolean hasBypass = player.hasPermission("dimensioncontrol.bypass")
                || player.isOp()
                || player.hasPermission("dimensioncontrol.bypass." + dimension);
        if (hasBypass) return;

        event.setCancelled(true);
        player.sendMessage(dimension.equals("end") ? endClosedMsg : netherClosedMsg);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World.Environment env = player.getWorld().getEnvironment();
        String dimension = env == World.Environment.THE_END ? "end"
                : env == World.Environment.NETHER ? "nether"
                : null;
        if (dimension == null || !closedDimensions.contains(dimension)) return;

        boolean hasBypass = player.hasPermission("dimensioncontrol.bypass")
                || player.isOp()
                || player.hasPermission("dimensioncontrol.bypass." + dimension);
        if (hasBypass) return;

        Location fallback = event.getFrom().getSpawnLocation();
        player.getScheduler().run(this, task -> {
            player.teleport(fallback);
            player.sendMessage(dimension.equals("end") ? endClosedMsg : netherClosedMsg);
        }, null);
    }
}
