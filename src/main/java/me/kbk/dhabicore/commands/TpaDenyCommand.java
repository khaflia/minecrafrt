package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TpaDenyCommand implements CommandExecutor {
    private final DhabiCore plugin;
    public TpaDenyCommand(DhabiCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
        if (!plugin.getTpaManager().hasPendingRequest(player)) {
            player.sendMessage(plugin.msg("tpa.no-requests"));
            return true;
        }
        plugin.getTpaManager().denyRequest(player);
        return true;
    }
}
