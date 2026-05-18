package me.kbk.dhabicore.scoreboard;

import me.kbk.dhabicore.DhabiCore;
import me.kbk.dhabicore.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final DhabiCore plugin;
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();
    private final Map<UUID, Objective> playerObjectives = new HashMap<>();

    private static final String[] PADDING = {
        ChatColor.WHITE + "" + ChatColor.BLACK + ChatColor.RESET,
        ChatColor.BLACK + "" + ChatColor.WHITE + ChatColor.RESET,
        ChatColor.DARK_BLUE + "" + ChatColor.RESET,
        ChatColor.DARK_GREEN + "" + ChatColor.RESET,
        ChatColor.DARK_AQUA + "" + ChatColor.RESET,
        ChatColor.DARK_RED + "" + ChatColor.RESET,
        ChatColor.DARK_PURPLE + "" + ChatColor.RESET,
        ChatColor.GOLD + "" + ChatColor.RESET,
        ChatColor.GRAY + "" + ChatColor.RESET,
        ChatColor.DARK_GRAY + "" + ChatColor.RESET,
        ChatColor.BLUE + "" + ChatColor.RESET,
        ChatColor.GREEN + "" + ChatColor.RESET,
        ChatColor.AQUA + "" + ChatColor.RESET,
        ChatColor.RED + "" + ChatColor.RESET,
        ChatColor.LIGHT_PURPLE + "" + ChatColor.RESET,
        ChatColor.YELLOW + "" + ChatColor.RESET
    };

    public ScoreboardManager(DhabiCore plugin) {
        this.plugin = plugin;
        int refreshTicks = plugin.getConfig().getInt("scoreboard.refresh-ticks", 40);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) updateScoreboard(p);
        }, 20L, refreshTicks);
    }

    public void setupScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager bm = Bukkit.getScoreboardManager();
        Scoreboard board = bm.getNewScoreboard();
        String title = ChatColor.translateAlternateColorCodes('&',
            plugin.getConfig().getString("scoreboard.title", "&f&lKhaleej&bSMP"));
        Objective obj = board.registerNewObjective("dhabicore", "dummy", title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        playerBoards.put(player.getUniqueId(), board);
        playerObjectives.put(player.getUniqueId(), obj);
        player.setScoreboard(board);
        applyPlayerVisuals(player);
        updateScoreboard(player);
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        Objective obj = playerObjectives.get(player.getUniqueId());
        if (board == null || obj == null) { setupScoreboard(player); return; }

        Rank rank = plugin.getRankManager().getRank(player);
        int online = Bukkit.getOnlinePlayers().size();
        int ping = player.getPing();
        String world = formatWorldName(player.getWorld().getName());

        for (String entry : board.getEntries()) board.resetScores(entry);

        int slot = 10;
        setLine(board, obj, PADDING[slot],   c("&b&lplay.&f&lkhaleeji.lol"), slot--);
        setLine(board, obj, PADDING[slot],   "", slot--);
        setLine(board, obj, PADDING[slot],   c("&f Rank &8\u00bb ") + rank.getDisplayName(), slot--);
        setLine(board, obj, PADDING[slot],   c("&f World &8\u00bb &b") + world, slot--);
        setLine(board, obj, PADDING[slot],   c("&f Online &8\u00bb &b") + online, slot--);
        setLine(board, obj, PADDING[slot],   c("&f Ping &8\u00bb ") + pingColor(ping) + ping + "ms", slot--);
        setLine(board, obj, PADDING[slot],   "", slot--);
        setLine(board, obj, PADDING[slot],   c("&b khaleeji.lol"), slot);

        applyPlayerVisuals(player);
    }

    public void refreshAllPlayerVisuals() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            applyPlayerVisuals(online);
        }
    }

    public void applyPlayerVisuals(Player player) {
        Rank rank = plugin.getRankManager().getRank(player);
        String teamName = String.format("r%02d_%s", 99 - rank.getWeight(), rank.name().toLowerCase());

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard board = viewer.getScoreboard();
            if (board == null) {
                continue;
            }

            Team team = board.getTeam(teamName);
            if (team == null) {
                team = board.registerNewTeam(teamName);
            }
            team.setPrefix(rank.getPrefix() + ChatColor.WHITE + " ");
            team.setColor(ChatColor.WHITE);

            for (Team t : board.getTeams()) {
                if (t.getName().startsWith("r") && t.hasEntry(player.getName()) && !t.getName().equals(teamName)) {
                    t.removeEntry(player.getName());
                }
            }
            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }
        }

        player.setPlayerListName(rank.getPrefix() + ChatColor.WHITE + " " + player.getName());
        player.setCustomName(rank.getPrefix() + ChatColor.WHITE + " " + player.getName());
        player.setCustomNameVisible(true);
        player.setPlayerListHeaderFooter(
            c("&b&lKhaleeji SMP\n&fplay.khaleeji.lol"),
            c("\n&fDiscord: &b/gnty &7| &b/dhabi")
        );
    }

    private void setLine(Scoreboard board, Objective obj, String entry, Object value, int score) {
        String teamName = "sb_line_" + score;
        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);
        if (!team.hasEntry(entry)) team.addEntry(entry);
        team.setPrefix(value.toString());
        obj.getScore(entry).setScore(score);
    }

    private String pingColor(int ping) {
        if (ping < 80)  return ChatColor.GREEN.toString();
        if (ping < 150) return ChatColor.YELLOW.toString();
        return ChatColor.RED.toString();
    }

    private String formatWorldName(String name) {
        return switch (name.toLowerCase()) {
            case "world"        -> "Overworld";
            case "world_nether" -> "Nether";
            case "world_the_end", "world_end" -> "The End";
            default -> capitalize(name);
        };
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public void removeScoreboard(Player player) {
        playerBoards.remove(player.getUniqueId());
        playerObjectives.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
