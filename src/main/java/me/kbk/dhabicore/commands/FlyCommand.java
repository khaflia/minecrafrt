package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import me.kbk.dhabicore.ranks.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {

    private final DhabiCore plugin;

    public FlyCommand(DhabiCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Rank rank = plugin.getRankManager().getRank(player);
        if (!(player.isOp() || rank.isAtLeast(Rank.STAFF) || player.hasPermission("dhabicore.staff"))) {
            player.sendMessage(plugin.msg("general.no-permission"));
            return true;
        }

        boolean enable = !player.getAllowFlight();
        player.setAllowFlight(enable);
        if (!enable && player.isFlying()) {
            player.setFlying(false);
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            enable ? "&b■ &fFlight enabled." : "&b■ &fFlight disabled."));
        return true;
    }
}
