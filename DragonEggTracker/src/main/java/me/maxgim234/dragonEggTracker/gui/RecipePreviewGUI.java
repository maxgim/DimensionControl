package me.maxgim234.dragonEggTracker.gui;

import me.maxgim234.dragonEggTracker.items.TrackerCompass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class RecipePreviewGUI {

    public static final String TITLE = "§5Dragon Egg Tracker Recipe";

    public static Inventory create(JavaPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 36, TITLE);

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        ConfigurationSection section =
                plugin.getConfig().getConfigurationSection("tracker-compass-recipe");

        if (section == null) return inv;

        List<String> shape = section.getStringList("shape");
        if (shape.size() != 3) return inv;

        ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
        if (ingredients == null) return inv;

        int[][] slots = {
                {10, 11, 12},
                {19, 20, 21},
                {28, 29, 30}
        };

        for (int row = 0; row < 3; row++) {
            String line = shape.get(row);
            for (int col = 0; col < Math.min(line.length(), 3); col++) {
                char symbol = line.charAt(col);

                if (symbol == ' ') continue;

                String name = ingredients.getString(String.valueOf(symbol));
                if (name == null) continue;

                Material mat = Material.matchMaterial(name);
                if (mat != null) {
                    inv.setItem(slots[row][col], new ItemStack(mat));
                }
            }
        }

        inv.setItem(25, TrackerCompass.create());

        return inv;
    }
}
