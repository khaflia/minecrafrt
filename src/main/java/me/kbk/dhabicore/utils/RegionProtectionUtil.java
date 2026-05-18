package me.kbk.dhabicore.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class RegionProtectionUtil {

    private RegionProtectionUtil() {}

    public static boolean canBreak(Player player, Block block) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (!(plugin instanceof WorldGuardPlugin) || !plugin.isEnabled()) {
            return true;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(block.getWorld()));
        if (regionManager == null) {
            return true;
        }

        ApplicableRegionSet set = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation()));
        if (set.size() == 0) {
            return true;
        }

        return set.testState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.BLOCK_BREAK);
    }
}
