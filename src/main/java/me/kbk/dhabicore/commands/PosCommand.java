package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class PosCommand implements CommandExecutor {

    private final DhabiCore plugin;
    private final int posNum;

    public PosCommand(DhabiCore plugin, int posNum) {
        this.plugin = plugin;
        this.posNum = posNum;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }

        if (!player.hasPermission("dhabicore.zone")) {
            player.sendMessage(plugin.msg("general.no-permission"));
            return true;
        }

        Location loc = player.getLocation();
        String coords = "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";

        if (posNum == 1) {
            plugin.getZoneManager().setPos1(player, loc);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&b\u25a0 &fPosition 1 &7set to &b" + coords));
        } else {
            plugin.getZoneManager().setPos2(player, loc);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&b\u25a0 &fPosition 2 &7set to &b" + coords));
        }

        // Tell them if both are now set
        if (plugin.getZoneManager().hasPos1(player) && plugin.getZoneManager().hasPos2(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&7Both positions set \u2014 run &b/zone create <name> &7to protect."));
        }
        return true;
    }
}
