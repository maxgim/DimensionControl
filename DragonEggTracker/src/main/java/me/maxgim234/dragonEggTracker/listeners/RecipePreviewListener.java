package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.gui.RecipePreviewGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RecipePreviewListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(RecipePreviewGUI.TITLE)) {
            event.setCancelled(true);
        }
    }
}
