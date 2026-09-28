package me.maxgim234.dragonEggTracker.tracking;

import me.maxgim234.dragonEggTracker.listeners.EggBuffListener;
import org.bukkit.plugin.java.JavaPlugin;

public class EggBuffTask implements Runnable {

    private final JavaPlugin plugin;
    private final EggManager manager;
    private EggBuffListener buffListener;

    public EggBuffTask(JavaPlugin plugin, EggManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void setBuffListener(EggBuffListener buffListener) {
        this.buffListener = buffListener;
    }

    @Override
    public void run() {
        if (buffListener != null) {
            buffListener.checkAllPlayers();
        }
    }
}
