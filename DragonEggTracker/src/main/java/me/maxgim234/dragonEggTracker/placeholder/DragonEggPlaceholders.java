package me.maxgim234.dragonEggTracker.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggStatsManager;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DragonEggPlaceholders extends PlaceholderExpansion {

    private final JavaPlugin plugin;
    private final EggManager manager;
    private final EggStatsManager stats;

    public DragonEggPlaceholders(JavaPlugin plugin, EggManager manager, EggStatsManager stats) {
        this.plugin = plugin;
        this.manager = manager;
        this.stats = stats;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "dragoneggtracker";
    }

    @Override
    public @NotNull String getAuthor() {
        return "maxgim234";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        String param = params.toLowerCase();

        switch (param) {
            case "holder":
                return manager.getOwnerName() == null ? "None" : manager.getOwnerName();

            case "state":
                return manager.getEggState() == null ? "UNKNOWN" : manager.getEggState().name();

            case "world": {
                Location loc = manager.getEggLocation();
                return (loc == null || loc.getWorld() == null) ? "None" : loc.getWorld().getName();
            }

            case "x": {
                Location loc = manager.getEggLocation();
                return loc == null ? "0" : String.valueOf(loc.getBlockX());
            }

            case "y": {
                Location loc = manager.getEggLocation();
                return loc == null ? "0" : String.valueOf(loc.getBlockY());
            }

            case "z": {
                Location loc = manager.getEggLocation();
                return loc == null ? "0" : String.valueOf(loc.getBlockZ());
            }

            case "online": {
                OfflinePlayer owner = manager.getOwner();
                return (owner != null && owner.isOnline()) ? "Yes" : "No";
            }

            default:
                break;
        }

        if (param.startsWith("top_")) {
            return resolveTop(param);
        }

        return null;
    }

    private String resolveTop(String key) {
        String[] parts = key.split("_");
        if (parts.length < 2) return null;

        int rank;
        try {
            rank = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
        if (rank < 1) return null;

        boolean wantTime = parts.length >= 3 && parts[2].equals("time");

        List<Map.Entry<UUID, Long>> top = stats.getTop(rank);
        if (top.size() < rank) {
            return wantTime ? "0s" : "None";
        }

        Map.Entry<UUID, Long> entry = top.get(rank - 1);

        return wantTime
                ? EggStatsManager.formatDuration(entry.getValue())
                : stats.getName(entry.getKey());
    }
}
