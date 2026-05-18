package me.kbk.dhabicore.listeners;

import me.kbk.dhabicore.DhabiCore;
import me.kbk.dhabicore.ranks.Rank;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class ChatListener implements Listener {

    private final DhabiCore plugin;

    public ChatListener(DhabiCore plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Rank rank = plugin.getRankManager().getRank(player);

        String format = plugin.getConfig().getString("chat.format",
            "%rank% &7%player%&f: &r%message%");

        format = format
            .replace("%rank%", rank.getDisplayName())
            .replace("%player%", player.getName())
            .replace("%message%", "%2$s");

        event.setFormat(ChatColor.translateAlternateColorCodes('&', format));
    }
}
