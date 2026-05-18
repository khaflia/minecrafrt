package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import me.kbk.dhabicore.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SetRankCommand implements CommandExecutor {
    private final DhabiCore plugin;
    public SetRankCommand(DhabiCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("dhabicore.admin")) {
            sender.sendMessage(plugin.msg("general.no-permission"));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(plugin.msg("ranks.usage-setrank"));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.msg("general.player-not-found").replace("{player}", args[0]));
            return true;
        }
        Rank rank = Rank.fromString(args[1]);
        plugin.getRankManager().setRank(target, rank);
        sender.sendMessage(plugin.msg("ranks.set-success")
            .replace("{player}", target.getName())
            .replace("{rank}", rank.getDisplayName()));
        target.sendMessage(plugin.msg("ranks.rank-updated")
            .replace("{rank}", rank.getDisplayName()));
        plugin.getScoreboardManager().updateScoreboard(target);
        return true;
    }
}
