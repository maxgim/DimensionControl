package me.maxgim234.dragonEggTracker.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EggItemUtil {

    private EggItemUtil() {
    }

    public static boolean hasDragonEggAnywhere(Player player) {
        for (ItemStack item : player.getInventory()) {
            if (item != null && item.getType() == Material.DRAGON_EGG) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasDragonEggInMainHand(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        return hand.getType() == Material.DRAGON_EGG;
    }
}
