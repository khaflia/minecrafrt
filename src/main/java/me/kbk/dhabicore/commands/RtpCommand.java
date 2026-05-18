package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class RtpCommand implements CommandExecutor {

    private final DhabiCore plugin;

    public RtpCommand(DhabiCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        World world = player.getWorld();
        int radius = plugin.getConfig().getInt("rtp.radius", 2000);

        Location destination = null;
        for (int tries = 0; tries < 20; tries++) {
            int x = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            int z = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            int y = world.getHighestBlockYAt(x, z);
            Location base = new Location(world, x + 0.5, y, z + 0.5);
            Block feet = base.getBlock();
            Block head = feet.getRelative(0, 1, 0);
            if (feet.getType().isAir() && head.getType().isAir()) {
                destination = base;
                break;
            }
        }

        if (destination == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cCouldn't find a safe RTP location."));
            return true;
        }

        player.teleport(destination);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b■ &fTeleported to a random location."));
        return true;
    }
}
