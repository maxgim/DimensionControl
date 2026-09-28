package me.maxgim234.dragonEggTracker;

import dev.faststats.bukkit.BukkitContext;
import me.maxgim234.dragonEggTracker.commands.DragonCommand;
import me.maxgim234.dragonEggTracker.economy.EconomyHook;
import me.maxgim234.dragonEggTracker.items.TrackerCompass;
import me.maxgim234.dragonEggTracker.items.TrackerRecipe;
import me.maxgim234.dragonEggTracker.listeners.*;
import me.maxgim234.dragonEggTracker.placeholder.DragonEggPlaceholders;
import me.maxgim234.dragonEggTracker.tracking.CompassTrackerTask;
import me.maxgim234.dragonEggTracker.tracking.EggBuffTask;
import me.maxgim234.dragonEggTracker.tracking.EggHoldTrackerTask;
import me.maxgim234.dragonEggTracker.tracking.EggManager;
import me.maxgim234.dragonEggTracker.tracking.EggStatsManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

public final class DragonEggTracker extends JavaPlugin {

    private static DragonEggTracker instance;
    private EggManager manager;
    private EggStatsManager stats;

    private final BukkitContext context = new BukkitContext.Factory(this, "168688ab401d457188de2487dfa34ff3")
            .metrics(dev.faststats.Metrics.Factory::create)
            .create();

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        manager = new EggManager(this);
        stats = new EggStatsManager(this);

        if (EconomyHook.setup(this)) {
            getLogger().info("Vault economy hook enabled.");
        }

        int pluginId = 31633;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(
                new SimplePie("chart_id", () -> "My value")
        );

        getServer().getPluginManager().registerEvents(
                new EggPickupListener(this, manager, stats), this
        );
        getServer().getPluginManager().registerEvents(
                new InventoryBlockListener(this, manager), this
        );
        getServer().getPluginManager().registerEvents(
                new HopperMoveListener(this), this
        );
        getServer().getPluginManager().registerEvents(
                new DispenseListener(this), this
        );
        getServer().getPluginManager().registerEvents(
                new EggBlockListener(manager, this), this
        );
        getServer().getPluginManager().registerEvents(
                new RecipePreviewListener(), this
        );
        getServer().getPluginManager().registerEvents(
                new RecipeCraftListener(this), this
        );
        getServer().getPluginManager().registerEvents(
                new EggDropListener(manager), this
        );
        getServer().getPluginManager().registerEvents(
                new HopperPickupListener(), this
        );
        getServer().getPluginManager().registerEvents(
                new EggDestructionListener(this), this
        );
        getServer().getPluginManager().registerEvents(
                new GuiRestrictionListener(this), this
        );
        getServer().getPluginManager().registerEvents(
                new EggTheftListener(this, manager), this
        );
        getServer().getPluginManager().registerEvents(
                new EggVoidListener(this, manager), this );

        EggBuffListener eggBuffListener = new EggBuffListener(this, manager);
        getServer().getPluginManager().registerEvents(eggBuffListener, this);

        DragonCommand dragonCommand = new DragonCommand(this, manager, stats);
        getCommand("dragon").setExecutor(dragonCommand);
        getCommand("dragon").setTabCompleter(dragonCommand);

        TrackerCompass.init(this);
        TrackerRecipe.register(this);

        long interval = getConfig().getLong(
                "compass-update-interval", 40L
        );

        getServer().getScheduler().runTaskTimer(
                this,
                new CompassTrackerTask(this, manager),
                20L,
                interval
        );

        if (getConfig().getBoolean("egg-holder-buffs.enabled")) {
            EggBuffTask buffTask = new EggBuffTask(this, manager);
            buffTask.setBuffListener(eggBuffListener);

            getServer().getScheduler().runTaskTimer(
                    this,
                    buffTask,
                    20L,
                    40L
            );
        }

        getServer().getScheduler().runTaskTimer(
                this,
                new EggHoldTrackerTask(this, manager, stats),
                20L,
                20L
        );

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new DragonEggPlaceholders(this, manager, stats).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }

        long autoSaveInterval = getConfig().getLong("auto-save-interval", 6000L);
        if (autoSaveInterval > 0) {
            getServer().getScheduler().runTaskTimer(
                    this,
                    () -> {
                        manager.saveData();
                        stats.save();
                    },
                    autoSaveInterval,
                    autoSaveInterval
            );
        }

        context.ready();

        getLogger().info("DragonEggTracker enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.saveData();
        }
        if (stats != null) {
            stats.save();
        }

        context.shutdown();

        getLogger().info("DragonEggTracker disabled.");
    }

    public static DragonEggTracker getInstance() {
        return instance;
    }

    public EggManager getEggManager() {
        return manager;
    }

    public EggStatsManager getStatsManager() {
        return stats;
    }
}
