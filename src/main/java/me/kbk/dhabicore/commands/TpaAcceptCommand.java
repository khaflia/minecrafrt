package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TpaAcceptCommand implements CommandExecutor {
    private final DhabiCore plugin;
    public TpaAcceptCommand(DhabiCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
        if (!plugin.getTpaManager().hasPendingRequest(player)) {
            player.sendMessage(plugin.msg("tpa.no-requests"));
            return true;
        }
        plugin.getTpaManager().acceptRequest(player);
        return true;
    }
}
