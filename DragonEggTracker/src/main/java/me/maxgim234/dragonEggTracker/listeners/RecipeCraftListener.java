package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.items.TrackerCompass;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class RecipeCraftListener implements Listener {

    private final JavaPlugin plugin;

    public RecipeCraftListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getRecipe() == null ? null : event.getRecipe().getResult();
        if (!TrackerCompass.isTracker(result)) return;

        if (event.getView().getPlayer() instanceof Player player) {
            String perm = plugin.getConfig().getString(
                    "tracker-compass-recipe.permission", ""
            );

            if (!perm.isEmpty() && !player.hasPermission(perm)) {
                event.getInventory().setResult(null);
            }
        }
    }
}
