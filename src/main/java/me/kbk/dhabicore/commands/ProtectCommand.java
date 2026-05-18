package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ProtectCommand implements CommandExecutor {

    private final DhabiCore plugin;

    public ProtectCommand(DhabiCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (!player.hasPermission("dhabicore.zone")) {
            player.sendMessage(plugin.msg("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            player.getInventory().addItem(new ItemStack(Material.STICK, 1));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&b■ &fProtection wand given &7(Stick)."));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&7Left-click block = &bpos1&7, right-click block = &bpos2&7."));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&7Then run &b/protect create <name> &7to protect the area."));
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                player.sendMessage(plugin.msg("zone.usage-create"));
                return true;
            }
            String name = args[1];
            if (!plugin.getZoneManager().hasPos1(player) || !plugin.getZoneManager().hasPos2(player)) {
                player.sendMessage(plugin.msg("zone.need-positions"));
                return true;
            }
            boolean ok = plugin.getZoneManager().createZone(player, name);
            player.sendMessage(ok
                ? plugin.msg("zone.created").replace("{name}", name)
                : plugin.msg("zone.create-failed"));
            return true;
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&b■ &fUsage: &b/protect &7or &b/protect create <name>"));
        return true;
    }
}
