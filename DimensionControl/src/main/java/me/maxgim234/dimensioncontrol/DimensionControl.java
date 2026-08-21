package me.maxgim234.dimensioncontrol;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import org.bstats.bukkit.Metrics;
import org.bukkit.Location;
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
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DimensionControl extends JavaPlugin implements Listener {

    private final Set<String> closed = new HashSet<>();
    private final List<String> actions = List.of("open", "close", "status");
    private final List<String> dcCmds = List.of("end-open", "end-close", "nether-open", "nether-close", "status", "reload");

    private File dataFile;
    private FileConfiguration data;

    private String endMsg;
    private String netherMsg;
    private String openedMsg;
    private String closedMsg;
    private String statusLine;
    private String noPerm;
    private String reloaded;
    private String usageEnd;
    private String usageNether;
    private String usageDc;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new Metrics(this, 31795);

        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create data.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        loadConfig();

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("end").setExecutor(this);
        getCommand("end").setTabCompleter(this);
        getCommand("nether").setExecutor(this);
        getCommand("nether").setTabCompleter(this);
        getCommand("dimensioncontrol").setExecutor(this);
        getCommand("dimensioncontrol").setTabCompleter(this);

        getLogger().info("DimensionControl has been enabled!");
    }

    @Override
    public void onDisable() {
        saveData();
        getLogger().info("DimensionControl has been disabled!");
    }

    public void loadConfig() {
        reloadConfig();
        FileConfiguration cfg = getConfig();

        endMsg = color(cfg.getString("messages.end-closed", "&cThe End is currently closed!"));
        netherMsg = color(cfg.getString("messages.nether-closed", "&cThe Nether is currently closed!"));
        openedMsg = color(cfg.getString("messages.opened", "&aThe {dimension} has been opened!"));
        closedMsg = color(cfg.getString("messages.closed", "&aThe {dimension} has been closed!"));
        statusLine = color(cfg.getString("messages.status-line", "&eThe {dimension} is currently {state}&e."));
        noPerm = color(cfg.getString("messages.no-permission", "&cYou don't have permission to use this command!"));
        reloaded = color(cfg.getString("messages.reloaded", "&aConfig reloaded!"));
        usageEnd = color(cfg.getString("messages.usage-end", "&eUsage: /end <open|close|status>"));
        usageNether = color(cfg.getString("messages.usage-nether", "&eUsage: /nether <open|close|status>"));
        usageDc = color(cfg.getString("messages.usage-dc", "&eUsage: /dimensioncontrol <nether-open|nether-close|end-open|end-close|status|reload>"));

        data = YamlConfiguration.loadConfiguration(dataFile);
        closed.clear();
        closed.addAll(data.getStringList("closed-dimensions"));
    }

    public void saveData() {
        data.set("closed-dimensions", new ArrayList<>(closed));
        try {
            data.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cmdName = cmd.getName().toLowerCase();

        if (cmdName.equals("dimensioncontrol")) {
            if (!sender.hasPermission("dimensioncontrol.admin")) {
                sender.sendMessage(noPerm);
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(usageDc);
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "end-open" -> handleCommand(sender, "end", "open");
                case "end-close" -> handleCommand(sender, "end", "close");
                case "nether-open" -> handleCommand(sender, "nether", "open");
                case "nether-close" -> handleCommand(sender, "nether", "close");
                case "status" -> {
                    handleCommand(sender, "end", "status");
                    handleCommand(sender, "nether", "status");
                }
                case "reload" -> {
                    loadConfig();
                    sender.sendMessage(reloaded);
                }
                default -> sender.sendMessage(usageDc);
            }
            return true;
        }

        String dim = cmdName.equals("end") ? "end" : cmdName.equals("nether") ? "nether" : null;
        if (dim == null) {
            return false;
        }
        if (!sender.hasPermission("dimensioncontrol." + dim)) {
            sender.sendMessage(noPerm);
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(dim.equals("end") ? usageEnd : usageNether);
            return true;
        }
        handleCommand(sender, dim, args[0]);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }

        List<String> options = cmd.getName().equalsIgnoreCase("dimensioncontrol") ? dcCmds : actions;
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(args[0].toLowerCase())) {
                matches.add(option);
            }
        }
        return matches;
    }

    public void handleCommand(CommandSender sender, String dim, String action) {
        switch (action.toLowerCase()) {
            case "open" -> {
                closed.remove(dim);
                saveData();
                sender.sendMessage(openedMsg.replace("{dimension}", capitalize(dim)));
            }
            case "close" -> {
                closed.add(dim);
                saveData();
                sender.sendMessage(closedMsg.replace("{dimension}", capitalize(dim)));
            }
            case "status" -> {
                boolean isClosed = closed.contains(dim);
                String state = color(isClosed ? "&cclosed" : "&aopen");
                sender.sendMessage(statusLine
                        .replace("{dimension}", capitalize(dim))
                        .replace("{state}", state));
            }
            default -> sender.sendMessage(dim.equals("end") ? usageEnd : usageNether);
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location to = event.getTo();
        if (to == null || to.getWorld() == null) {
            return;
        }

        Player player = event.getPlayer();
        String dim = dimensionOf(to.getWorld().getEnvironment());
        if (dim != null && closed.contains(dim) && !canBypass(player, dim)) {
            event.setCancelled(true);
            player.sendMessage(dim.equals("end") ? endMsg : netherMsg);
        }
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        Location to = event.getTo();
        if (to == null || to.getWorld() == null) {
            return;
        }

        String dim = dimensionOf(to.getWorld().getEnvironment());
        if (dim == null || !closed.contains(dim)) {
            return;
        }

        event.setCancelled(true);
        String msg = dim.equals("end") ? endMsg : netherMsg;
        for (Entity passenger : event.getEntity().getPassengers()) {
            if (passenger instanceof Player rider) {
                rider.sendMessage(msg);
            }
        }
    }

    @EventHandler
    public void onEntityPortalReady(EntityPortalReadyEvent event) {
        World world = event.getTargetWorld();
        if (world == null) {
            return;
        }

        String dim = dimensionOf(world.getEnvironment());
        if (dim == null || !closed.contains(dim)) {
            return;
        }

        Entity entity = event.getEntity();
        Player player = entity instanceof Player p ? p : findRider(entity);
        if (player == null || canBypass(player, dim)) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(dim.equals("end") ? endMsg : netherMsg);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String dim = dimensionOf(player.getWorld().getEnvironment());
        if (dim == null || !closed.contains(dim) || canBypass(player, dim)) {
            return;
        }

        Location fallback = event.getFrom().getSpawnLocation();
        player.getScheduler().run(this, task -> {
            player.teleport(fallback);
            player.sendMessage(dim.equals("end") ? endMsg : netherMsg);
        }, null);
    }

    private Player findRider(Entity entity) {
        for (Entity passenger : entity.getPassengers()) {
            if (passenger instanceof Player rider) {
                return rider;
            }
        }
        return null;
    }

    private String dimensionOf(World.Environment env) {
        if (env == World.Environment.THE_END) return "end";
        if (env == World.Environment.NETHER) return "nether";
        return null;
    }

    private boolean canBypass(Player player, String dim) {
        return player.isOp()
                || player.hasPermission("dimensioncontrol.bypass")
                || player.hasPermission("dimensioncontrol.bypass." + dim);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String color(String msg) {
        return msg == null ? "" : msg.replace("&", "§");
    }
}
