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
        if (!plugin.getTreeFellerManager().isToggled(player)) return;
        if (!player.isSneaking() && !plugin.getConfig().getBoolean("treefeller.always-active", false)) return;
        if (plugin.getTreeFellerManager().isWorldBlacklisted(player.getWorld())) return;
        if (plugin.getTreeFellerManager().isProcessing(player)) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!plugin.getTreeFellerManager().isAxe(tool.getType())) return;
        if (!plugin.getTreeFellerManager().isLog(event.getBlock().getType())) return;

        // Must be a natural tree with leaves
        if (!plugin.getTreeFellerManager().isNaturalTree(event.getBlock())) return;

        org.bukkit.block.Block block = event.getBlock();
        plugin.getServer().getScheduler().runTask(plugin, () ->
            plugin.getTreeFellerManager().fellTree(player, block));
    }
}
