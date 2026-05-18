package me.kbk.dhabicore.listeners;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;

public class PlayerJoinQuitListener implements Listener {

    private final DhabiCore plugin;

    public PlayerJoinQuitListener(DhabiCore plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Assign MEMBER rank on very first join
        if (!player.hasPlayedBefore()) {
            plugin.getRankManager().setRank(player, me.kbk.dhabicore.ranks.Rank.MEMBER);
        }

        plugin.getScoreboardManager().setupScoreboard(player);
        plugin.getScoreboardManager().refreshAllPlayerVisuals();

        String joinMsg = plugin.getConfig().getString("chat.join-message", "&e{player} &ajoined the server!")
            .replace("{player}", plugin.getRankManager().getRank(player).getDisplayName() + " " + player.getName());
        event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', joinMsg));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getTpaManager().cancelAllFor(player);
        plugin.getScoreboardManager().removeScoreboard(player);
        plugin.getScoreboardManager().refreshAllPlayerVisuals();

        String quitMsg = plugin.getConfig().getString("chat.quit-message", "&e{player} &cleft the server.")
            .replace("{player}", player.getName());
        event.setQuitMessage(ChatColor.translateAlternateColorCodes('&', quitMsg));
    }
}
