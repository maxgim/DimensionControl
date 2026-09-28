package me.maxgim234.dragonEggTracker.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyHook {

    private static Economy economy;

    private EconomyHook() {
    }

    public static boolean setup(JavaPlugin plugin) {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            economy = null;
            return false;
        }

        RegisteredServiceProvider<Economy> provider =
                Bukkit.getServicesManager().getRegistration(Economy.class);

        if (provider == null) {
            economy = null;
            return false;
        }

        economy = provider.getProvider();
        return economy != null;
    }

    public static boolean isEnabled() {
        return economy != null;
    }

    public static void deposit(OfflinePlayer player, double amount) {
        if (economy == null) return;
        if (amount <= 0) return;

        economy.depositPlayer(player, amount);
    }
}
