package me.maxgim234.dragonEggTracker.tracking;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EggStatsManager {

    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration data;

    private final Map<UUID, Long> heldSeconds = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();
    private final Set<UUID> rewarded = new HashSet<>();

    public EggStatsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "egg-stats.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public void addHeldSeconds(UUID uuid, String name, long seconds) {
        heldSeconds.merge(uuid, seconds, Long::sum);
        names.put(uuid, name);
    }

    public long getTotalHeldSeconds(UUID uuid) {
        return heldSeconds.getOrDefault(uuid, 0L);
    }

    public boolean hasReceivedFirstPickupReward(UUID uuid) {
        return rewarded.contains(uuid);
    }

    public void markFirstPickupRewarded(UUID uuid) {
        rewarded.add(uuid);
    }

    public String getName(UUID uuid) {
        return names.getOrDefault(uuid, "Unknown");
    }

    public List<Map.Entry<UUID, Long>> getTop(int count) {
        List<Map.Entry<UUID, Long>> entries = new ArrayList<>(heldSeconds.entrySet());
        entries.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        if (entries.size() > count) {
            return entries.subList(0, count);
        }
        return entries;
    }

    public static String formatDuration(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;

        if (h > 0) return h + "h " + m + "m " + s + "s";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    public void load() {
        heldSeconds.clear();
        names.clear();
        rewarded.clear();

        if (!file.exists()) return;

        ConfigurationSection players = data.getConfigurationSection("players");
        if (players != null) {
            for (String key : players.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    long seconds = players.getLong(key + ".seconds", 0L);
                    String name = players.getString(key + ".name", "Unknown");

                    heldSeconds.put(uuid, seconds);
                    names.put(uuid, name);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        for (String key : data.getStringList("first-pickup-rewarded")) {
            try {
                rewarded.add(UUID.fromString(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void save() {
        try {
            for (Map.Entry<UUID, Long> entry : heldSeconds.entrySet()) {
                String key = entry.getKey().toString();
                data.set("players." + key + ".seconds", entry.getValue());
                data.set("players." + key + ".name", names.getOrDefault(entry.getKey(), "Unknown"));
            }

            List<String> ids = new ArrayList<>();
            for (UUID uuid : rewarded) {
                ids.add(uuid.toString());
            }
            data.set("first-pickup-rewarded", ids);

            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save egg stats!");
            e.printStackTrace();
        }
    }
}
