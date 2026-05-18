package me.kbk.dhabicore.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
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

        try {
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            return query.testState(BukkitAdapter.adapt(block.getLocation()), localPlayer, Flags.BLOCK_BREAK, Flags.BUILD);
        } catch (Throwable ignored) {
            return true;
        }
    }
}
