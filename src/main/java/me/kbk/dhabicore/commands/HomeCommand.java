package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import me.kbk.dhabicore.gui.HomeGui;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {
    private final DhabiCore plugin;
    public HomeCommand(DhabiCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }

        if (args.length == 0) {
            // Open GUI if multiple homes, or teleport to "home" if only one
            if (plugin.getHomeManager().getHomes(player).size() <= 1
                && plugin.getHomeManager().hasHome(player, "home")) {
                teleportWithDelay(player, "home");
            } else {
                player.openInventory(HomeGui.build(plugin, player));
            }
            return true;
        }

        String name = args[0];
        if (!plugin.getHomeManager().hasHome(player, name)) {
            player.sendMessage(plugin.msg("homes.not-found").replace("{name}", name));
            return true;
        }
        teleportWithDelay(player, name);
        return true;
    }

    private void teleportWithDelay(Player player, String name) {
        int delay = plugin.getConfig().getInt("homes.teleport-delay-seconds", 3);
        Location startLoc = player.getLocation();

        player.sendMessage(plugin.msg("homes.teleporting")
            .replace("{name}", name)
            .replace("{seconds}", String.valueOf(delay)));

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Movement cancel check
            if (player.isOnline()) {
                Location now = player.getLocation();
                double dist = now.distanceSquared(startLoc);
                if (dist > 0.5 && plugin.getConfig().getBoolean("homes.cancel-on-move", true)) {
                    player.sendMessage(plugin.msg("homes.teleport-cancelled"));
                    return;
                }
                Location dest = plugin.getHomeManager().getHome(player, name);
                if (dest != null) {
                    player.teleport(dest);
                    player.sendMessage(plugin.msg("homes.teleported").replace("{name}", name));
                }
            }
        }, delay * 20L);
    }
}
