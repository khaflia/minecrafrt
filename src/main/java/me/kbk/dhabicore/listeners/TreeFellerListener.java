package me.kbk.dhabicore.listeners;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TreeFellerListener implements Listener {

    private final DhabiCore plugin;

    public TreeFellerListener(DhabiCore plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (plugin.getConfig().getBoolean("treefeller.require-permission", false) && !player.hasPermission("dhabicore.treefeller.use")) return;
        if (plugin.getConfig().getBoolean("treefeller.require-sneak", false) && !player.isSneaking()) return;
        if (!plugin.getTreeFellerManager().isToggled(player)) return;
        if (plugin.getTreeFellerManager().isWorldBlacklisted(player.getWorld())) return;
        if (plugin.getTreeFellerManager().isProcessing(player)) return;

        if (!plugin.getTreeFellerManager().isLog(event.getBlock().getType())) return;

        // Must be a natural tree with leaves
        if (!plugin.getTreeFellerManager().isNaturalTree(event.getBlock())) return;

        org.bukkit.block.Block block = event.getBlock();
        if (plugin.getTreeFellerManager().getTree(block).size() <= 1) return;
        if (plugin.getZoneManager().isProtected(block.getLocation()) && !player.hasPermission("dhabicore.zone.bypass") && !player.isOp()) return;
        if (!me.kbk.dhabicore.utils.RegionProtectionUtil.canBreak(player, block)) return;

        event.setCancelled(true);
        if (plugin.getConfig().getBoolean("debug.blockbreak", false)) {
            plugin.getLogger().info("[DEBUG] TreeFeller cancelling vanilla break at "
                + block.getX() + "," + block.getY() + "," + block.getZ() + " type=" + block.getType());
        }
        plugin.getServer().getScheduler().runTask(plugin, () ->
            plugin.getTreeFellerManager().fellTree(player, block));
    }
}
