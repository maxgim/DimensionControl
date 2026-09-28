package me.maxgim234.dragonEggTracker.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class TrackerRecipe {

    public static void register(JavaPlugin plugin) {
        ConfigurationSection section =
                plugin.getConfig().getConfigurationSection("tracker-compass-recipe");

        if (section == null || !section.getBoolean("enabled", true)) {
            plugin.getLogger().info("Tracker compass recipe is disabled.");
            return;
        }

        List<String> shape = section.getStringList("shape");
        if (shape.size() != 3) {
            plugin.getLogger().warning("Invalid tracker recipe shape (must be 3 rows).");
            return;
        }

        NamespacedKey key = new NamespacedKey(plugin, "dragon_egg_tracker");

        Bukkit.removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, TrackerCompass.create());

        recipe.shape(
                shape.get(0),
                shape.get(1),
                shape.get(2)
        );

        ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
        if (ingredients == null) {
            plugin.getLogger().warning("Tracker recipe ingredients missing.");
            return;
        }

        for (Map.Entry<String, Object> entry : ingredients.getValues(false).entrySet()) {
            char symbol = entry.getKey().charAt(0);
            String name = entry.getValue().toString();

            Material mat = Material.matchMaterial(name);
            if (mat == null) {
                plugin.getLogger().warning("Invalid material in recipe: " + name);
                continue;
            }

            recipe.setIngredient(symbol, mat);
        }

        Bukkit.addRecipe(recipe);
    }
}
