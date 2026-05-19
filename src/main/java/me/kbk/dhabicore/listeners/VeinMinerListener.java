package me.kbk.dhabicore.listeners;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VeinMinerListener implements Listener {

    private final DhabiCore plugin;

    public VeinMinerListener(DhabiCore plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (plugin.getConfig().getBoolean("veinminer.require-permission", false) && !player.hasPermission("dhabicore.veinminer.use")) return;
        if (plugin.getConfig().getBoolean("veinminer.require-sneak", false) && !player.isSneaking()) return;
        if (!plugin.getVeinMinerManager().isToggled(player)) return;
        if (plugin.getVeinMinerManager().isWorldBlacklisted(player.getWorld())) return;
        if (plugin.getVeinMinerManager().isProcessing(player)) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!plugin.getVeinMinerManager().isMiningTool(tool.getType())) return;
        if (!plugin.getVeinMinerManager().isOre(event.getBlock().getType())) return;

        org.bukkit.block.Block block = event.getBlock();
        org.bukkit.Material targetType = block.getType();

        if (plugin.getVeinMinerManager().getVein(block, targetType, player).size() <= 1) return;
        if (plugin.getZoneManager().isProtected(block.getLocation()) && !player.hasPermission("dhabicore.zone.bypass") && !player.isOp()) return;
        if (!me.kbk.dhabicore.utils.RegionProtectionUtil.canBreak(player, block)) return;

        event.setCancelled(true);
        if (plugin.getConfig().getBoolean("debug.blockbreak", false)) {
            plugin.getLogger().info("[DEBUG] VeinMiner cancelling vanilla break at "
                + block.getX() + "," + block.getY() + "," + block.getZ() + " type=" + targetType);
        }
        plugin.getServer().getScheduler().runTask(plugin, () ->
            plugin.getVeinMinerManager().mineVein(player, block, targetType));
    }
}
