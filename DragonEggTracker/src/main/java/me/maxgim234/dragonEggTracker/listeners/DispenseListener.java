package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.DragonEggTracker;
import me.maxgim234.dragonEggTracker.util.ContainerUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;

public class DispenseListener implements Listener {

    public DispenseListener(DragonEggTracker plugin) {
    }

    @EventHandler
    public void onDispense(BlockDispenseEvent event) {
        if (ContainerUtil.containsDragonEgg(event.getItem())) {
            event.setCancelled(true);
        }
    }
}
