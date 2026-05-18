package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class VeinMinerCommand implements CommandExecutor {
    private final DhabiCore plugin;
    public VeinMinerCommand(DhabiCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
        player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
            "&b■ &fVeinMiner is always enabled on this server."));
        plugin.getScoreboardManager().updateScoreboard(player);
        return true;
    }
}
