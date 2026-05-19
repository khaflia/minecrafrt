package me.kbk.dhabicore.listeners;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class DebugBlockBreakListener implements Listener {

    private final DhabiCore plugin;

    public DebugBlockBreakListener(DhabiCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onBreakLowest(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("debug.blockbreak", false)) return;
        plugin.getLogger().info("[DEBUG][LOWEST] break player=" + event.getPlayer().getName()
            + " block=" + event.getBlock().getType()
            + " cancelled=" + event.isCancelled()
            + " dropItems=" + event.isDropItems());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBreakMonitor(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("debug.blockbreak", false)) return;
        plugin.getLogger().info("[DEBUG][MONITOR] break player=" + event.getPlayer().getName()
            + " block=" + event.getBlock().getType()
            + " cancelled=" + event.isCancelled()
            + " dropItems=" + event.isDropItems());
    }
}
