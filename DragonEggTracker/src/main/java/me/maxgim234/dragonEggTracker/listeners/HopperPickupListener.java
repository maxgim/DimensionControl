package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.util.ContainerUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;

public class HopperPickupListener implements Listener {

    @EventHandler
    public void onHopperPickup(InventoryPickupItemEvent event) {
        if (!ContainerUtil.containsDragonEgg(event.getItem().getItemStack())) return;

        if (event.getInventory().getType() == InventoryType.HOPPER) {
            event.setCancelled(true);
        }
    }
}
