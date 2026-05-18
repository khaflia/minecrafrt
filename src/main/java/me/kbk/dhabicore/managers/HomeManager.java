package me.kbk.dhabicore.managers;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HomeManager {

    private final DhabiCore plugin;
    // UUID -> (homeName -> Location)
    private final Map<UUID, Map<String, Location>> homes = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public HomeManager(DhabiCore plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "homes.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataConfig.getConfigurationSection("homes") == null) return;

        for (String uuidStr : dataConfig.getConfigurationSection("homes").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Map<String, Location> playerHomes = new HashMap<>();
                var section = dataConfig.getConfigurationSection("homes." + uuidStr);
                if (section == null) continue;
                for (String homeName : section.getKeys(false)) {
                    String worldName = dataConfig.getString("homes." + uuidStr + "." + homeName + ".world");
                    double x = dataConfig.getDouble("homes." + uuidStr + "." + homeName + ".x");
                    double y = dataConfig.getDouble("homes." + uuidStr + "." + homeName + ".y");
                    double z = dataConfig.getDouble("homes." + uuidStr + "." + homeName + ".z");
                    float yaw = (float) dataConfig.getDouble("homes." + uuidStr + "." + homeName + ".yaw");
                    float pitch = (float) dataConfig.getDouble("homes." + uuidStr + "." + homeName + ".pitch");
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        playerHomes.put(homeName, new Location(world, x, y, z, yaw, pitch));
                    }
                }
                homes.put(uuid, playerHomes);
            } catch (Exception ignored) {}
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, Map<String, Location>> entry : homes.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (Map.Entry<String, Location> homeEntry : entry.getValue().entrySet()) {
                Location loc = homeEntry.getValue();
                String path = "homes." + uuidStr + "." + homeEntry.getKey();
                dataConfig.set(path + ".world", loc.getWorld().getName());
                dataConfig.set(path + ".x", loc.getX());
                dataConfig.set(path + ".y", loc.getY());
                dataConfig.set(path + ".z", loc.getZ());
                dataConfig.set(path + ".yaw", loc.getYaw());
                dataConfig.set(path + ".pitch", loc.getPitch());
            }
        }
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private void savePlayer(UUID uuid) {
        Map<String, Location> playerHomes = homes.getOrDefault(uuid, new HashMap<>());
        // Clear existing entries for this player
        dataConfig.set("homes." + uuid.toString(), null);
        for (Map.Entry<String, Location> homeEntry : playerHomes.entrySet()) {
            Location loc = homeEntry.getValue();
            String path = "homes." + uuid + "." + homeEntry.getKey();
            dataConfig.set(path + ".world", loc.getWorld().getName());
            dataConfig.set(path + ".x", loc.getX());
            dataConfig.set(path + ".y", loc.getY());
            dataConfig.set(path + ".z", loc.getZ());
            dataConfig.set(path + ".yaw", loc.getYaw());
            dataConfig.set(path + ".pitch", loc.getPitch());
        }
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public int getMaxHomes(Player player) {
        if (player.hasPermission("dhabicore.homes.unlimited")) return 99;
        if (player.hasPermission("dhabicore.homes.admin")) return 10;
        if (player.hasPermission("dhabicore.homes.mvp")) return 5;
        if (player.hasPermission("dhabicore.homes.vip")) return 3;
        return plugin.getConfig().getInt("homes.default-max", 2);
    }

    public boolean setHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        Map<String, Location> playerHomes = homes.computeIfAbsent(uuid, k -> new HashMap<>());
        if (!playerHomes.containsKey(name) && playerHomes.size() >= getMaxHomes(player)) {
            return false; // limit reached
        }
        playerHomes.put(name.toLowerCase(), player.getLocation());
        savePlayer(uuid);
        return true;
    }

    public boolean deleteHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        Map<String, Location> playerHomes = homes.get(uuid);
        if (playerHomes == null || !playerHomes.containsKey(name.toLowerCase())) return false;
        playerHomes.remove(name.toLowerCase());
        savePlayer(uuid);
        return true;
    }

    public Location getHome(Player player, String name) {
        Map<String, Location> playerHomes = homes.get(player.getUniqueId());
        if (playerHomes == null) return null;
        return playerHomes.get(name.toLowerCase());
    }

    public Map<String, Location> getHomes(Player player) {
        return homes.getOrDefault(player.getUniqueId(), new HashMap<>());
    }

    public boolean hasHome(Player player, String name) {
        Map<String, Location> playerHomes = homes.get(player.getUniqueId());
        return playerHomes != null && playerHomes.containsKey(name.toLowerCase());
    }
}
