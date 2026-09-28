package me.maxgim234.dragonEggTracker.tracking;

import me.maxgim234.dragonEggTracker.economy.EconomyHook;
import me.maxgim234.dragonEggTracker.util.EggItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class EggHoldTrackerTask implements Runnable {

    private static final long HOUR_SECONDS = 3600L;

    private final JavaPlugin plugin;
    private final EggManager manager;
    private final EggStatsManager stats;

    private UUID lastOwner;
    private long streak = 0;

    public EggHoldTrackerTask(JavaPlugin plugin, EggManager manager, EggStatsManager stats) {
        this.plugin = plugin;
        this.manager = manager;
        this.stats = stats;
    }

    @Override
    public void run() {
        OfflinePlayer owner = manager.getOwner();
        if (owner == null || !owner.isOnline()) {
            resetStreak();
            return;
        }

        Player player = owner.getPlayer();
        if (player == null || !EggItemUtil.hasDragonEggAnywhere(player)) {
            resetStreak();
            return;
        }

        UUID uuid = player.getUniqueId();

        if (!uuid.equals(lastOwner)) {
            lastOwner = uuid;
            streak = 0;
        }

        stats.addHeldSeconds(uuid, player.getName(), 1);
        streak++;

        if (streak >= HOUR_SECONDS) {
            streak = 0;
            payHourlyReward(player);
        }
    }

    private void resetStreak() {
        lastOwner = null;
        streak = 0;
    }

    private void payHourlyReward(Player player) {
        if (!plugin.getConfig().getBoolean("economy-rewards.hourly-holding.enabled", false)) return;
        if (!EconomyHook.isEnabled()) return;

        double amount = plugin.getConfig().getDouble("economy-rewards.hourly-holding.amount", 0);
        EconomyHook.deposit(player, amount);

        String msg = plugin.getConfig().getString(
                "economy-rewards.hourly-holding.message",
                "&d&lYou earned &e$%amount% &d&lfor holding the Dragon Egg for an hour!"
        ).replace("%amount%", String.valueOf(amount));

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }
}
