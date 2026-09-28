package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import me.maxgim234.dragonEggTracker.util.ContainerUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryBlockListener implements Listener {

    private final JavaPlugin plugin;
    private final EggManager manager;

    public InventoryBlockListener(JavaPlugin plugin, EggManager manager) {
        this.plugin = plugin;
        this.manager = manager;

        ContainerUtil.load(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        boolean hasEgg = ContainerUtil.containsDragonEgg(current)
                || ContainerUtil.containsDragonEgg(cursor);

        if (!hasEgg) return;

        InventoryType type = event.getInventory().getType();

        if (ContainerUtil.isAllBlocked()
                && type != InventoryType.PLAYER
                && type != InventoryType.CRAFTING) {
            event.setCancelled(true);
            return;
        }

        if (type == InventoryType.HOPPER) {
            event.setCancelled(true);
            return;
        }

        if (!plugin.getConfig().getBoolean("allow-enderchest-egg")
                && type == InventoryType.ENDER_CHEST) {
            event.setCancelled(true);
            return;
        }

        if (ContainerUtil.isBlocked(type)) {
            event.setCancelled(true);
            return;
        }

        if (event.getInventory().getLocation() != null) {
            manager.setEggLocation(
                    event.getInventory().getLocation(),
                    EggState.CONTAINER
            );
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        ItemStack dragged = event.getOldCursor();
        if (!ContainerUtil.containsDragonEgg(dragged)) return;

        InventoryType type = event.getInventory().getType();

        if (type == InventoryType.PLAYER || type == InventoryType.CRAFTING) return;

        boolean blocked = (ContainerUtil.isAllBlocked() && type != InventoryType.PLAYER)
                || type == InventoryType.HOPPER
                || (!plugin.getConfig().getBoolean("allow-enderchest-egg") && type == InventoryType.ENDER_CHEST)
                || ContainerUtil.isBlocked(type);

        if (blocked) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryType type = event.getInventory().getType();

        if (type == InventoryType.PLAYER || type == InventoryType.CRAFTING) return;
        if (!ContainerUtil.isAllBlocked() && !ContainerUtil.isBlocked(type)
                && (plugin.getConfig().getBoolean("allow-enderchest-egg") || type != InventoryType.ENDER_CHEST)
                && type != InventoryType.HOPPER) return;

        if (event.getInventory().getLocation() == null) return;

        for (int i = 0; i < event.getInventory().getSize(); i++) {
            ItemStack item = event.getInventory().getItem(i);
            if (ContainerUtil.containsDragonEgg(item)) {
                event.getInventory().setItem(i, null);
                event.getInventory().getLocation().getWorld().dropItemNaturally(
                        event.getInventory().getLocation(), item
                );
            }
        }
    }
}
