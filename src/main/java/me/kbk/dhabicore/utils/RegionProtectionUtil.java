package me.kbk.dhabicore.utils;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class RegionProtectionUtil {

    private RegionProtectionUtil() {}

    public static boolean canBreak(Player player, Block block) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (!(plugin instanceof WorldGuardPlugin wg) || !plugin.isEnabled()) {
            return true;
        }

        try {
            Location loc = block.getLocation();
            return wg.canBuild(player, loc);
        } catch (Throwable ignored) {
            // Never hard-fail gameplay if WG API glitches; fallback to allow.
            return true;
        }
    }
}
