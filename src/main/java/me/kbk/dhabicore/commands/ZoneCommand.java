package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import me.kbk.dhabicore.managers.ZoneManager.ProtectedZone;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ZoneCommand implements CommandExecutor {

    private final DhabiCore plugin;

    public ZoneCommand(DhabiCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (!player.hasPermission("dhabicore.zone")) {
            player.sendMessage(plugin.msg("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "create" -> {
                if (args.length < 2) { player.sendMessage(plugin.msg("zone.usage-create")); return true; }
                String name = args[1];
                if (!plugin.getZoneManager().hasPos1(player) || !plugin.getZoneManager().hasPos2(player)) {
                    player.sendMessage(plugin.msg("zone.need-positions"));
                    return true;
                }
                boolean ok = plugin.getZoneManager().createZone(player, name);
                if (ok) {
                    player.sendMessage(plugin.msg("zone.created").replace("{name}", name));
                } else {
                    player.sendMessage(plugin.msg("zone.create-failed"));
                }
            }

            case "delete", "remove" -> {
                if (args.length < 2) { player.sendMessage(plugin.msg("zone.usage-delete")); return true; }
                String name = args[1];
                boolean deleted = plugin.getZoneManager().deleteZone(name);
                if (deleted) {
                    player.sendMessage(plugin.msg("zone.deleted").replace("{name}", name));
                } else {
                    player.sendMessage(plugin.msg("zone.not-found").replace("{name}", name));
                }
            }

            case "list" -> {
                Collection<ProtectedZone> zones = plugin.getZoneManager().getAllZones();
                if (zones.isEmpty()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&b&lZones &8\u00bb &7No protected zones exist."));
                    return true;
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b&l\u25a0 Protected Zones &8\u00bb"));
                for (ProtectedZone z : zones) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&8 \u25ba &b" + z.getName() +
                        " &7(" + z.getWorld().getName() + ") &8\u2014 &f" + z.getSizeString()));
                }
            }

            case "info" -> {
                if (args.length < 2) {
                    // Check zone at feet
                    ProtectedZone zone = plugin.getZoneManager().getZoneAt(player.getLocation());
                    if (zone == null) {
                        player.sendMessage(plugin.msg("zone.not-standing-in"));
                        return true;
                    }
                    sendZoneInfo(player, zone);
                } else {
                    ProtectedZone zone = plugin.getZoneManager().getZone(args[1]);
                    if (zone == null) {
                        player.sendMessage(plugin.msg("zone.not-found").replace("{name}", args[1]));
                        return true;
                    }
                    sendZoneInfo(player, zone);
                }
            }

            default -> sendHelp(player);
        }
        return true;
    }

    private void sendZoneInfo(Player player, ProtectedZone z) {
        String c = "&b";
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&b&l\u25a0 Zone: &f" + z.getName()));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            " &7World: " + c + z.getWorld().getName()));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            " &7Corner 1: " + c + z.getMinX() + ", " + z.getMinY() + ", " + z.getMinZ()));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            " &7Corner 2: " + c + z.getMaxX() + ", " + z.getMaxY() + ", " + z.getMaxZ()));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            " &7Size: " + c + z.getSizeString()));
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&b&l\u25a0 Zone Commands &8\u00bb"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            " &b/pos1 &7\u2014 Set first corner (or left-click with stick)"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            " &b/pos2 &7\u2014 Set second corner (or right-click with stick)"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            " &b/zone create <name> &7\u2014 Create zone from selection"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            " &b/zone delete <name> &7\u2014 Remove a zone"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            " &b/zone list &7\u2014 List all zones"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            " &b/zone info [name] &7\u2014 Zone info (defaults to zone you're in)"));
    }
}
