package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {
    private final DhabiCore plugin;
    public SetHomeCommand(DhabiCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
        String name = args.length > 0 ? args[0] : "home";
        if (name.length() > 16) {
            player.sendMessage(plugin.msg("homes.name-too-long"));
            return true;
        }
        boolean set = plugin.getHomeManager().setHome(player, name);
        if (set) {
            player.sendMessage(plugin.msg("homes.set").replace("{name}", name));
        } else {
            int max = plugin.getHomeManager().getMaxHomes(player);
            player.sendMessage(plugin.msg("homes.limit-reached").replace("{max}", String.valueOf(max)));
        }
        return true;
    }
}
