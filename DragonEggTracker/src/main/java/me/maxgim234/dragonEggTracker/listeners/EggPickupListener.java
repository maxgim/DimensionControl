package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.economy.EconomyHook;
import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggState;
import me.maxgim234.dragonEggTracker.tracking.EggStatsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class EggPickupListener implements Listener {

    private final JavaPlugin plugin;
    private final EggManager manager;
    private final EggStatsManager stats;

    public EggPickupListener(JavaPlugin plugin, EggManager manager, EggStatsManager stats) {
        this.plugin = plugin;
        this.manager = manager;
        this.stats = stats;
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack item = event.getItem().getItemStack();
        if (item.getType() != Material.DRAGON_EGG) return;

        manager.setOwner(
                player.getUniqueId(),
                player.getName()
        );

        manager.setEggLocation(
                player.getLocation(),
                EggState.PLAYER
        );

        if (plugin.getConfig().getBoolean("egg-pickup-announcement.sound-enabled", true)) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
        }

        if (plugin.getConfig().getBoolean("egg-pickup-announcement.enabled")) {
            String msg = plugin.getConfig()
                    .getString("egg-pickup-announcement.message", "")
                    .replace("%player%", player.getName());

            Bukkit.broadcastMessage(
                    ChatColor.translateAlternateColorCodes('&', msg)
            );
        }

        if (plugin.getConfig().getBoolean("economy-rewards.first-pickup.enabled", false)
                && EconomyHook.isEnabled()
                && !stats.hasReceivedFirstPickupReward(player.getUniqueId())) {

            stats.markFirstPickupRewarded(player.getUniqueId());

            double amount = plugin.getConfig().getDouble("economy-rewards.first-pickup.amount", 0);
            EconomyHook.deposit(player, amount);

            String msg = plugin.getConfig().getString(
                    "economy-rewards.first-pickup.message",
                    "&d&lYou earned &e$%amount% &d&lfor picking up the Dragon Egg for the first time!"
            ).replace("%amount%", String.valueOf(amount));

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }
}
