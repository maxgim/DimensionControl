package me.maxgim234.dragonEggTracker.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EggDestructionListener implements Listener {

    private final JavaPlugin plugin;

    public EggDestructionListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isEnabled() {
        return plugin.getConfig().getBoolean("prevent-egg-destruction", true);
    }

    private boolean isDragonEggItem(Entity entity) {
        return entity instanceof Item item
                && item.getItemStack().getType() == Material.DRAGON_EGG;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggItemDamage(EntityDamageEvent event) {
        if (!isEnabled()) return;
        if (!isDragonEggItem(event.getEntity())) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggDespawn(ItemDespawnEvent event) {
        if (!isEnabled()) return;
        if (event.getEntity().getItemStack().getType() != Material.DRAGON_EGG) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEggMerge(ItemMergeEvent event) {
        if (!isEnabled()) return;
        if (event.getEntity().getItemStack().getType() != Material.DRAGON_EGG) return;

        event.setCancelled(true);
    }
}
