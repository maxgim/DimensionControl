package me.maxgim234.dragonEggTracker.tracking;

import me.maxgim234.dragonEggTracker.items.TrackerCompass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class CompassTrackerTask implements Runnable {

    private final JavaPlugin plugin;
    private final EggManager manager;

    public CompassTrackerTask(JavaPlugin plugin, EggManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public void run() {
        // The egg's stored location is only set once, at pickup time.
        // While the egg is being carried, keep it in sync with the holder's
        // current position so the compass follows them instead of pointing
        // at the spot they picked it up from.
        if (manager.getEggState() == EggState.PLAYER) {
            OfflinePlayer owner = manager.getOwner();
            if (owner != null && owner.isOnline()) {
                Player holder = owner.getPlayer();
                if (holder != null) {
                    manager.setEggLocation(holder.getLocation(), EggState.PLAYER);
                }
            }
        }

        Location loc = manager.getEggLocation();
        if (loc == null) return;

        if (plugin.getConfig().getBoolean("disable-tracking-if-owner-offline")) {
            if (manager.getOwner() != null && !manager.getOwner().isOnline()) {
                return;
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (!TrackerCompass.isTracker(item)) continue;

                TrackerCompass.setTarget(item, loc);
            }
        }
    }
}