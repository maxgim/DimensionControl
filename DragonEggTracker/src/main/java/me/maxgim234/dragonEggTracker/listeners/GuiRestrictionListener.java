package me.maxgim234.dragonEggTracker.listeners;

import me.maxgim234.dragonEggTracker.util.ContainerUtil;
import me.maxgim234.dragonEggTracker.util.EggItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;

public class GuiRestrictionListener implements Listener {

    private final JavaPlugin plugin;

    public GuiRestrictionListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean guiBlockEnabled() {
        return plugin.getConfig().getBoolean("gui-restriction.block-egg-in-other-guis", false);
    }

    private boolean isVirtualGui(Inventory inventory) {
        InventoryType type = inventory.getType();

        if (type == InventoryType.PLAYER || type == InventoryType.CRAFTING) return false;
        if (type == InventoryType.SHULKER_BOX) return false;
        if (inventory.getLocation() != null) return false;

        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!guiBlockEnabled()) return;
        if (!isVirtualGui(event.getInventory())) return;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (ContainerUtil.containsDragonEgg(current) || ContainerUtil.containsDragonEgg(cursor)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!guiBlockEnabled()) return;
        if (!isVirtualGui(event.getInventory())) return;

        if (ContainerUtil.containsDragonEgg(event.getOldCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        List<String> blocked = plugin.getConfig().getStringList("gui-restriction.blocked-sell-commands");
        if (blocked.isEmpty()) return;

        Player player = event.getPlayer();
        if (!EggItemUtil.hasDragonEggInMainHand(player)) return;

        String cmd = event.getMessage().substring(1).toLowerCase(Locale.ROOT);

        for (String entry : blocked) {
            String needle = entry.toLowerCase(Locale.ROOT);

            if (cmd.equals(needle) || cmd.startsWith(needle + " ")) {
                event.setCancelled(true);
                player.sendMessage("§cYou can't sell the Dragon Egg while holding it!");
                return;
            }
        }
    }
}
