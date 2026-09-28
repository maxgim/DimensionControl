package me.maxgim234.dragonEggTracker.tracking;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class EggManager {

    private final JavaPlugin plugin;

    private UUID ownerId;
    private String ownerName;
    private Location loc;
    private EggState state;
    private final File file;
    private final YamlConfiguration data;

    public EggManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.state = EggState.UNKNOWN;

        this.file = new File(plugin.getDataFolder(), "egg-data.yml");
        this.data = YamlConfiguration.loadConfiguration(file);

        loadData();
    }

    public void setOwner(UUID uuid, String name) {
        this.ownerId = uuid;
        this.ownerName = name;
        saveData();
    }

    public OfflinePlayer getOwner() {
        return ownerId == null ? null : Bukkit.getOfflinePlayer(ownerId);
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setEggLocation(Location location, EggState state) {
        this.loc = location;
        this.state = state;
    }

    public Location getEggLocation() {
        return loc;
    }

    public EggState getEggState() {
        return state;
    }

    public void saveData() {
        try {
            if (ownerId != null) {
                data.set("owner.uuid", ownerId.toString());
                data.set("owner.name", ownerName);
            }

            if (loc != null) {
                data.set("location.world", loc.getWorld().getName());
                data.set("location.x", loc.getX());
                data.set("location.y", loc.getY());
                data.set("location.z", loc.getZ());
                data.set("location.state", state.name());
            }

            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save egg data!");
            e.printStackTrace();
        }
    }

    private void loadData() {
        if (!file.exists()) return;

        if (data.contains("owner.uuid")) {
            ownerId = UUID.fromString(data.getString("owner.uuid"));
            ownerName = data.getString("owner.name");
        }

        if (data.contains("location.world")) {
            World world = Bukkit.getWorld(data.getString("location.world"));
            if (world != null) {
                loc = new Location(
                        world,
                        data.getDouble("location.x"),
                        data.getDouble("location.y"),
                        data.getDouble("location.z")
                );
                state = EggState.valueOf(
                        data.getString("location.state", "UNKNOWN")
                );
            }
        }
    }
}
