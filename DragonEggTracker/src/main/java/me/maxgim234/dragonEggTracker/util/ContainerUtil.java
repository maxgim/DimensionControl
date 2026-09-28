package me.maxgim234.dragonEggTracker.util;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContainerUtil {

    private static final Set<InventoryType> blocked = new HashSet<>();
    private static boolean blockAll = false;

    public static void load(JavaPlugin plugin) {
        blocked.clear();
        blockAll = plugin.getConfig().getBoolean("prevent-all-containers", false);

        List<String> names = plugin.getConfig().getStringList("prevent-containers");
        for (String name : names) {
            try {
                blocked.add(InventoryType.valueOf(name));
            } catch (IllegalArgumentException e) {
                if (name.equals("HOPPER_MINECART")) {
                    plugin.getLogger().info("[DragonEggTracker] Note: HOPPER_MINECART is not a valid InventoryType. Hopper minecarts use the HOPPER type, which is already blocked by the code.");
                } else {
                    plugin.getLogger().warning("[DragonEggTracker] Invalid container type in config: " + name);
                }
            }
        }
    }

    public static boolean isAllBlocked() {
        return blockAll;
    }

    public static boolean isBlocked(InventoryType type) {
        return blocked.contains(type);
    }

    public static boolean containsDragonEgg(ItemStack item) {
        if (item == null) return false;
        if (item.getType() == Material.DRAGON_EGG) return true;

        if (item.getType() == Material.BUNDLE && item.getItemMeta() instanceof BundleMeta meta) {
            for (ItemStack inner : meta.getItems()) {
                if (inner != null && inner.getType() == Material.DRAGON_EGG) {
                    return true;
                }
            }
        }
        return false;
    }
}
