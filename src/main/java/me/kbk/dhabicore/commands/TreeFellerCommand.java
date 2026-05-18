package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TreeFellerCommand implements CommandExecutor {
    private final DhabiCore plugin;
    public TreeFellerCommand(DhabiCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
        plugin.getTreeFellerManager().toggle(player);
        boolean on = plugin.getTreeFellerManager().isToggled(player);
        player.sendMessage(plugin.msg("treefeller.toggled")
            .replace("{status}", on ? "&aEnabled" : "&cDisabled"));
        plugin.getScoreboardManager().updateScoreboard(player);
        return true;
    }
}
