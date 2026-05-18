package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import me.kbk.dhabicore.gui.TpaGui;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaCommand implements CommandExecutor {

    private final DhabiCore plugin;

    public TpaCommand(DhabiCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (args.length == 0) {
            // Open GUI showing active requests
            player.openInventory(TpaGui.build(plugin, player));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.msg("general.player-not-found").replace("{player}", args[0]));
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage(plugin.msg("tpa.cannot-self"));
            return true;
        }

        long cooldown = plugin.getTpaManager().getCooldownRemaining(player);
        if (cooldown > 0) {
            player.sendMessage(plugin.msg("tpa.cooldown").replace("{seconds}", String.valueOf(cooldown)));
            return true;
        }

        boolean sent = plugin.getTpaManager().sendRequest(player, target);
        if (sent) {
            player.sendMessage(plugin.msg("tpa.sent").replace("{player}", target.getName()));
            target.sendMessage(plugin.msg("tpa.received").replace("{player}", player.getName()));
        } else {
            player.sendMessage(plugin.msg("tpa.cooldown").replace("{seconds}", String.valueOf(
                plugin.getTpaManager().getCooldownRemaining(player))));
        }
        return true;
    }
}
