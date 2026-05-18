package me.kbk.dhabicore.managers;

import me.kbk.dhabicore.DhabiCore;
import me.kbk.dhabicore.ranks.Rank;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RankManager {

    private final DhabiCore plugin;
    private final Map<UUID, Rank> playerRanks = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public RankManager(DhabiCore plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "ranks.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataConfig.getConfigurationSection("players") != null) {
            for (String key : dataConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String rankName = dataConfig.getString("players." + key, "DEFAULT");
                    Rank rank = Rank.fromString(rankName);
                    playerRanks.put(uuid, rank);
                } catch (Exception ignored) {}
            }
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, Rank> entry : playerRanks.entrySet()) {
            dataConfig.set("players." + entry.getKey().toString(), entry.getValue().name());
        }
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public Rank getRank(Player player) {
        return playerRanks.getOrDefault(player.getUniqueId(), Rank.MEMBER);
    }

    public void setRank(Player player, Rank rank) {
        playerRanks.put(player.getUniqueId(), rank);
        dataConfig.set("players." + player.getUniqueId().toString(), rank.name());
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public String getPrefix(Player player) {
        return getRank(player).getPrefix();
    }

    public String getColoredName(Player player) {
        Rank rank = getRank(player);
        return rank.getColor() + player.getName();
    }
}
