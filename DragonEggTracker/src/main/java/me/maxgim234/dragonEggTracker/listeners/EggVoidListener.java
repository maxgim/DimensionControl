package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class EggVoidListener implements Listener {

    private final JavaPlugin plugin;
    private final EggManager manager;

    public EggVoidListener(JavaPlugin plugin, EggManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {

        Entity entity = event.getEntity();

        if (entity instanceof Item item) {

            if (item.getItemStack().getType() != Material.DRAGON_EGG) {
                return;
            }

            if (!isTrackedEgg(item.getLocation())) {
                return;
            }

            monitorEgg(item);
            return;
        }

        if (entity instanceof FallingBlock fallingBlock) {

            if (fallingBlock.getBlockData().getMaterial()
                    != Material.DRAGON_EGG) {
                return;
            }

            if (!isTrackedEgg(fallingBlock.getLocation())) {
                return;
            }

            monitorEgg(fallingBlock);
        }
    }

    private boolean isTrackedEgg(Location location) {

        Location tracked = manager.getEggLocation();

        if (tracked == null) {
            return false;
        }

        if (tracked.getWorld() == null
                || location.getWorld() == null) {
            return false;
        }

        if (!tracked.getWorld().equals(location.getWorld())) {
            return false;
        }

        return tracked.distanceSquared(location) <= 4.0;
    }

    private void monitorEgg(Entity entity) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!entity.isValid()) {
                    cancel();
                    return;
                }

                World world = entity.getWorld();
                Location location = entity.getLocation();

                if (location.getY() <= world.getMinHeight() - 5) {

                    rescueEgg(entity);

                    cancel();
                    return;
                }

                if (entity instanceof Item item) {

                    if (item.getVelocity().getY() >= 0
                            && location.getY() > world.getMinHeight()) {
                        cancel();
                    }
                }
            }

        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void rescueEgg(Entity entity) {

        World world = entity.getWorld();

        Location oldLoc = entity.getLocation().clone();

        Location safe = findSafeLocation(
                world,
                oldLoc
        );

        entity.remove();

        Bukkit.getScheduler().runTask(plugin, () -> {

            if (!safe.getChunk().isLoaded()) {
                safe.getChunk().load();
            }

            safe.getBlock().setType(Material.DRAGON_EGG);

            Location actual = safe.getBlock().getLocation();

            manager.setEggLocation(
                    actual,
                    EggState.DROPPED
            );

            manager.saveData();

            sendRescueMessage(
                    world,
                    actual
            );
        });
    }

    private void sendRescueMessage(
            World world,
            Location safe
    ) {

        String msg = plugin.getConfig().getString(
                "egg-void-rescue.message",
                "&d&lThe Dragon Egg &r&dfell into the void and was rescued at "
                        + "&e%world% &7[&f%x%&7, &f%y%&7, &f%z%&7]&d!"
        );

        msg = msg
                .replace("%world%", world.getName())
                .replace("%x%", String.valueOf(safe.getBlockX()))
                .replace("%y%", String.valueOf(safe.getBlockY()))
                .replace("%z%", String.valueOf(safe.getBlockZ()));

        Bukkit.broadcastMessage(
                ChatColor.translateAlternateColorCodes('&', msg)
        );
    }

    private Location findSafeLocation(
            World world,
            Location near
    ) {

        int x = near.getBlockX();
        int z = near.getBlockZ();

        int top = world.getHighestBlockYAt(x, z);

        if (top <= world.getMinHeight()) {

            Location spawn = world.getSpawnLocation();

            return new Location(
                    world,
                    spawn.getBlockX(),
                    spawn.getBlockY() + 1,
                    spawn.getBlockZ()
            );
        }

        return new Location(
                world,
                x,
                top + 1,
                z
        );
    }
}
