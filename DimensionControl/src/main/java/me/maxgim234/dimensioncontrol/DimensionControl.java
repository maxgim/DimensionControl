package me.maxgim234.dimensioncontrol;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import org.bstats.bukkit.Metrics;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DimensionControl extends JavaPlugin implements Listener {

    private final Set<String> closed = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastPortalDeny = new ConcurrentHashMap<>();
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
        if (data == null) {
            return;
        }
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        Location loc = event.getLocation();
        World world = loc != null ? loc.getWorld() : event.getEntity().getWorld();
        if (world == null) {
            return;
        }

        PortalType portalType = event.getPortalType();
        World.Environment env = world.getEnvironment();
        String dim = null;

        if (portalType == PortalType.ENDER) {
            if (env != World.Environment.THE_END) {
                dim = "end";
            }
        } else if (portalType == PortalType.NETHER) {
            if (env != World.Environment.NETHER) {
                dim = "nether";
            }
        } else if (loc != null) {
            Material type = loc.getBlock().getType();
            if (type == Material.END_PORTAL && env != World.Environment.THE_END) {
                dim = "end";
            } else if (type == Material.NETHER_PORTAL && env != World.Environment.NETHER) {
                dim = "nether";
            }
        }

        if (dim == null || !closed.contains(dim)) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            if (canBypass(player, dim)) {
                return;
            }
            event.setCancelled(true);
            player.setPortalCooldown(20);
            denyWithCooldown(player, dim);
        } else {
            Player rider = findRider(entity);
            if (rider != null && canBypass(rider, dim)) {
                return;
            }
            event.setCancelled(true);
            setPortalCooldownRecursive(entity, 20);
            if (rider != null) {
                denyWithCooldown(rider, dim);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPortalReady(EntityPortalReadyEvent event) {
        World world = event.getTargetWorld();
        String dim = world != null ? dimensionOf(world.getEnvironment()) : null;
        if (dim == null) {
            dim = switch (event.getPortalType()) {
                case ENDER -> "end";
                case NETHER -> "nether";
                default -> null;
            };
        }

        if (dim == null || !closed.contains(dim)) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            if (canBypass(player, dim)) {
                return;
            }
            event.setCancelled(true);
            player.setPortalCooldown(20);
            denyWithCooldown(player, dim);
        } else {
            Player rider = findRider(entity);
            if (rider != null && canBypass(rider, dim)) {
                return;
            }
            event.setCancelled(true);
            setPortalCooldownRecursive(entity, 20);
            if (rider != null) {
                denyWithCooldown(rider, dim);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        String dim = to != null && to.getWorld() != null
                ? dimensionOf(to.getWorld().getEnvironment())
                : null;
        if (dim == null) {
            dim = switch (event.getCause()) {
                case END_PORTAL -> "end";
                case NETHER_PORTAL -> "nether";
                default -> null;
            };
        }

        if (dim != null && closed.contains(dim) && !canBypass(player, dim)) {
            event.setCancelled(true);
            player.setPortalCooldown(20);
            denyWithCooldown(player, dim);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event instanceof PlayerPortalEvent) {
            return;
        }

        Location to = event.getTo();
        if (to == null || to.getWorld() == null) {
            return;
        }

        Location from = event.getFrom();
        if (from.getWorld() != null && from.getWorld().equals(to.getWorld())) {
            return;
        }

        Player player = event.getPlayer();
        String dim = dimensionOf(to.getWorld().getEnvironment());
        if (dim != null && closed.contains(dim) && !canBypass(player, dim)) {
            event.setCancelled(true);
            player.setPortalCooldown(20);
            denyWithCooldown(player, dim);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {
        Location to = event.getTo();
        String dim = to != null && to.getWorld() != null
                ? dimensionOf(to.getWorld().getEnvironment())
                : null;
        if (dim == null) {
            dim = switch (event.getPortalType()) {
                case ENDER -> "end";
                case NETHER -> "nether";
                default -> null;
            };
        }

        if (dim == null || !closed.contains(dim)) {
            return;
        }

        Entity entity = event.getEntity();
        Player rider = findRider(entity);
        if (rider != null && canBypass(rider, dim)) {
            return;
        }

        event.setCancelled(true);
        setPortalCooldownRecursive(entity, 20);
        if (rider != null) {
            denyWithCooldown(rider, dim);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (event instanceof EntityPortalEvent) {
            return;
        }

        Location to = event.getTo();
        if (to == null || to.getWorld() == null) {
            return;
        }

        Location from = event.getFrom();
        if (from.getWorld() != null && from.getWorld().equals(to.getWorld())) {
            return;
        }

        String dim = dimensionOf(to.getWorld().getEnvironment());
        if (dim == null || !closed.contains(dim)) {
            return;
        }

        Player rider = findRider(event.getEntity());
        if (rider != null && canBypass(rider, dim)) {
            return;
        }

        event.setCancelled(true);
        setPortalCooldownRecursive(event.getEntity(), 20);
        if (rider != null) {
            denyWithCooldown(rider, dim);
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String dim = dimensionOf(player.getWorld().getEnvironment());
        if (dim == null || !closed.contains(dim) || canBypass(player, dim)) {
            return;
        }
        Location fallback = event.getFrom().getSpawnLocation();
        if (fallback == null) {
            return;
        }
        player.getScheduler().runDelayed(this, task -> {
            player.teleportAsync(fallback);
            denyWithCooldown(player, dim);
        }, null, 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastPortalDeny.remove(event.getPlayer().getUniqueId());
    }

    private void denyWithCooldown(Player player, String dim) {
        long now = System.currentTimeMillis();
        UUID id = player.getUniqueId();
        Long last = lastPortalDeny.get(id);
        if (last != null && now - last < 1500L) {
            return;
        }
        lastPortalDeny.put(id, now);
        player.sendMessage(dim.equals("end") ? endMsg : netherMsg);
    }

    private void setPortalCooldownRecursive(Entity entity, int cooldown) {
        entity.setPortalCooldown(cooldown);
        for (Entity passenger : entity.getPassengers()) {
            setPortalCooldownRecursive(passenger, cooldown);
        }
    }

    private Player findRider(Entity entity) {
        for (Entity passenger : entity.getPassengers()) {
            if (passenger instanceof Player rider) {
                return rider;
            }
            Player nestedRider = findRider(passenger);
            if (nestedRider != null) {
                return nestedRider;
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
