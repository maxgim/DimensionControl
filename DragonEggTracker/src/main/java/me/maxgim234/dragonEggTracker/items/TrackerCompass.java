package me.maxgim234.dragonEggTracker.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class TrackerCompass {

    private static NamespacedKey key;

    public static void init(JavaPlugin plugin) {
        key = new NamespacedKey(plugin, "dragon_egg_tracker");
    }

    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName("§dDragon Egg Tracker");
        meta.setLore(List.of(
                "§7Points to the Dragon Egg"
        ));

        meta.getPersistentDataContainer().set(
                key,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isTracker(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        return item.getItemMeta()
                .getPersistentDataContainer()
                .has(key, PersistentDataType.BYTE);
    }

    public static void setTarget(ItemStack item, Location location) {
        if (!(item.getItemMeta() instanceof CompassMeta meta)) return;

        meta.setLodestoneTracked(false);
        meta.setLodestone(location);

        item.setItemMeta(meta);
    }
}
