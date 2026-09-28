package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.util.ContainerUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

public class HopperMoveListener implements Listener {

    public HopperMoveListener(JavaPlugin plugin) {
        ContainerUtil.load(plugin);
    }

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent event) {
        if (!ContainerUtil.containsDragonEgg(event.getItem())) return;

        if (event.getSource().getType() == InventoryType.HOPPER
                || event.getDestination().getType() == InventoryType.HOPPER
                || ContainerUtil.isBlocked(event.getSource().getType())
                || ContainerUtil.isBlocked(event.getDestination().getType())) {
            event.setCancelled(true);
        }
    }
}
