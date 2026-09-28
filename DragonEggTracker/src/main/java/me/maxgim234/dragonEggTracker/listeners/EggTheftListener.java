package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class EggTheftListener implements Listener {

    private final JavaPlugin plugin;
    private final EggManager manager;

    public EggTheftListener(JavaPlugin plugin, EggManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.getConfig().getBoolean("egg-theft-announcement.enabled", true)) return;

        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;

        OfflinePlayer owner = manager.getOwner();
        if (owner == null || !victim.getUniqueId().equals(owner.getUniqueId())) return;

        boolean dropsEgg = false;
        for (ItemStack drop : event.getDrops()) {
            if (drop != null && drop.getType() == Material.DRAGON_EGG) {
                dropsEgg = true;
                break;
            }
        }
        if (!dropsEgg) return;

        String msg = plugin.getConfig().getString(
                "egg-theft-announcement.message",
                "&c&lThe Dragon Egg was stolen from &f%victim% &c&lby &f%killer%&c&l!"
        ).replace("%victim%", victim.getName())
         .replace("%killer%", killer.getName());

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }
}
