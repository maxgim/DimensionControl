package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class EggDropListener implements Listener {

    private final EggManager manager;

    public EggDropListener(EggManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() != Material.DRAGON_EGG) return;

        manager.setEggLocation(
                event.getItemDrop().getLocation(),
                EggState.DROPPED
        );
    }
}
