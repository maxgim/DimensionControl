package me.maxgim234.dragonEggTracker.commands;

import me.maxgim234.dragonEggTracker.gui.RecipePreviewGUI;
import me.maxgim234.dragonEggTracker.items.TrackerRecipe;
import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import me.maxgim234.dragonEggTracker.tracking.EggStatsManager;
import me.maxgim234.dragonEggTracker.util.ContainerUtil;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DragonCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final EggManager manager;
    private final EggStatsManager stats;

    public DragonCommand(JavaPlugin plugin, EggManager manager, EggStatsManager stats) {
        this.plugin = plugin;
        this.manager = manager;
        this.stats = stats;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help":
                sendHelp(sender);
                return true;

            case "owner":
                return handleOwner(sender);

            case "top":
                return handleTop(sender);

            case "placeholders":
                return handlePlaceholders(sender);

            case "locate":
                return handleLocate(sender);

            case "recipe":
                return handleRecipe(sender);

            case "reload":
                return handleReload(sender);

            default:
                sender.sendMessage("§cUnknown subcommand. Use §e/" + label + " help §cfor a list of commands.");
                return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(Arrays.asList("help", "owner", "recipe", "top", "placeholders"));

            if (sender.hasPermission("dragoneggtracker.admin")) {
                options.add("locate");
                options.add("reload");
            }

            return options.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("§d§lDragon Egg Tracker §7- §fby maxgim234");
        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("");
        sender.sendMessage("§e/dragon help §7- §fShows all commands");
        sender.sendMessage("§e/dragon owner §7- §fShows who owns the Dragon Egg");
        sender.sendMessage("§e/dragon top §7- §fShows the top 3 longest-holding players");
        sender.sendMessage("§e/dragon placeholders §7- §fLists available PlaceholderAPI placeholders");
        sender.sendMessage("§e/dragon recipe §7- §fDisplays the Tracker Compass recipe");

        if (sender.hasPermission("dragoneggtracker.admin")) {
            sender.sendMessage("§e/dragon locate §7- §fShows the Dragon Egg location §c(Admin)");
            sender.sendMessage("§e/dragon reload §7- §fReloads the plugin config §c(Admin)");
        }

        sender.sendMessage("");
        sender.sendMessage("§7You can also use §e/dragoneggtracker §7or §e/det §7instead of §e/dragon");
        sender.sendMessage("§d§l═══════════════════════════════");
    }

    private boolean handleTop(CommandSender sender) {
        List<Map.Entry<UUID, Long>> top = stats.getTop(3);

        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("§d§lDragon Egg Top Holders");
        sender.sendMessage("§d§l═══════════════════════════════");

        if (top.isEmpty()) {
            sender.sendMessage("§7No data yet.");
        } else {
            String[] labels = {"§6#1", "§7#2", "§c#3"};
            for (int i = 0; i < top.size(); i++) {
                Map.Entry<UUID, Long> entry = top.get(i);
                sender.sendMessage(labels[i] + " §f" + stats.getName(entry.getKey())
                        + " §7- §f" + EggStatsManager.formatDuration(entry.getValue()));
            }
        }

        sender.sendMessage("§d§l═══════════════════════════════");
        return true;
    }

    private boolean handlePlaceholders(CommandSender sender) {
        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("§d§lAvailable Placeholders §7(requires PlaceholderAPI)");
        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("§e%dragoneggtracker_holder% §7- Current holder's name");
        sender.sendMessage("§e%dragoneggtracker_state% §7- Egg state (PLAYER/DROPPED/CONTAINER/etc.)");
        sender.sendMessage("§e%dragoneggtracker_world% §7- World the egg is in");
        sender.sendMessage("§e%dragoneggtracker_x%§7/§e%dragoneggtracker_y%§7/§e%dragoneggtracker_z% §7- Egg coordinates");
        sender.sendMessage("§e%dragoneggtracker_online% §7- Whether the holder is online");
        sender.sendMessage("§e%dragoneggtracker_top_1%§7/§e_2%§7/§e_3% §7- Top 3 holders' names (all-time)");
        sender.sendMessage("§e%dragoneggtracker_top_1_time%§7/§e_2_time%§7/§e_3_time% §7- Their total hold time");
        sender.sendMessage("§d§l═══════════════════════════════");
        return true;
    }

    private boolean handleOwner(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        OfflinePlayer owner = manager.getOwner();

        if (owner == null || manager.getOwnerName() == null) {
            sender.sendMessage("§cNo one has picked up the Dragon Egg yet.");
            return true;
        }

        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("§d§lDragon Egg Owner");
        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("§7• Player: §f" + manager.getOwnerName());
        sender.sendMessage("§7• Online: " + (owner.isOnline() ? "§a✓ Yes" : "§c✗ No"));
        sender.sendMessage("§d§l═══════════════════════════════");

        return true;
    }

    private boolean handleLocate(CommandSender sender) {
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

        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("§d§lDragon Egg Location");
        sender.sendMessage("§d§l═══════════════════════════════");
        sender.sendMessage("§7• World: §f" + loc.getWorld().getName());
        sender.sendMessage("§7• X: §f" + loc.getBlockX());
        sender.sendMessage("§7• Y: §f" + loc.getBlockY());
        sender.sendMessage("§7• Z: §f" + loc.getBlockZ());
        sender.sendMessage("§7• State: §f" + state.name());
        sender.sendMessage("§d§l═══════════════════════════════");

        return true;
    }

    private boolean handleRecipe(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can view the recipe.");
            return true;
        }

        player.openInventory(RecipePreviewGUI.create(plugin));
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("dragoneggtracker.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        try {
            plugin.reloadConfig();
            ContainerUtil.load(plugin);
            TrackerRecipe.register(plugin);

            sender.sendMessage("§a§l✓ §aDragonEggTracker configuration reloaded successfully!");
        } catch (Exception e) {
            sender.sendMessage("§c§l✗ §cError reloading configuration: " + e.getMessage());
            plugin.getLogger().severe("Error reloading config: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}
