package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.tracking.EggManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

public class EggBuffListener implements Listener {

    private final JavaPlugin plugin;
    private final EggManager manager;

    private final Set<String> buffed = new HashSet<>();

    public EggBuffListener(JavaPlugin plugin, EggManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public boolean hasEgg(Player player) {
        for (ItemStack item : player.getInventory()) {
            if (item != null && item.getType() == Material.DRAGON_EGG) {
                return true;
            }
        }
        return false;
    }

    public void applyBuffs(Player player) {
        if (!plugin.getConfig().getBoolean("egg-holder-buffs.enabled")) return;
        if (!hasEgg(player)) return;

        int hearts = plugin.getConfig().getInt(
                "egg-holder-buffs.extra-hearts", 0
        );

        if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20 + hearts * 2);
        }

        ConfigurationSection effects =
                plugin.getConfig().getConfigurationSection(
                        "egg-holder-buffs.potion-effects"
                );

        if (effects == null) return;

        for (String key : effects.getKeys(false)) {
            PotionEffectType type = PotionEffectType.getByName(key);
            if (type == null) continue;

            int duration = effects.getInt(key + ".duration");
            int amplifier = effects.getInt(key + ".amplifier");

            player.addPotionEffect(
                    new PotionEffect(type, duration, amplifier, true, false)
            );
        }

        buffed.add(player.getName());
    }

    public void clearBuffs(Player player) {
        if (!buffed.contains(player.getName())) return;

        if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
        }

        ConfigurationSection effects =
                plugin.getConfig().getConfigurationSection(
                        "egg-holder-buffs.potion-effects"
                );

        if (effects != null) {
            for (String key : effects.getKeys(false)) {
                PotionEffectType type = PotionEffectType.getByName(key);
                if (type != null) {
                    player.removePotionEffect(type);
                }
            }
        }

        buffed.remove(player.getName());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            applyBuffs(event.getPlayer());
        }, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clearBuffs(event.getPlayer());
    }

    public void checkAllPlayers() {
        if (!plugin.getConfig().getBoolean("egg-holder-buffs.enabled")) return;

        OfflinePlayer owner = manager.getOwner();
        if (owner == null || !owner.isOnline()) return;

        Player player = owner.getPlayer();
        if (player == null) return;

        if (hasEgg(player)) {
            applyBuffs(player);
        } else if (buffed.contains(player.getName())) {
            clearBuffs(player);
        }
    }
}
