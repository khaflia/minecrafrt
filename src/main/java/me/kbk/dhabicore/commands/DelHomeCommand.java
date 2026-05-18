package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class DelHomeCommand implements CommandExecutor {
    private final DhabiCore plugin;
    public DelHomeCommand(DhabiCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
        if (args.length == 0) {
            player.sendMessage(plugin.msg("homes.usage-delhome"));
            return true;
        }
        String name = args[0];
        boolean deleted = plugin.getHomeManager().deleteHome(player, name);
        if (deleted) {
            player.sendMessage(plugin.msg("homes.deleted").replace("{name}", name));
        } else {
            player.sendMessage(plugin.msg("homes.not-found").replace("{name}", name));
        }
        return true;
    }
}
