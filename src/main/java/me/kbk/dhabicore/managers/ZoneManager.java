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

public class ZoneManager {

    private final DhabiCore plugin;
    private final Map<String, ProtectedZone> zones = new LinkedHashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    // Players mid-selection: step 1 = pos1 set, step 2 = pos2 set
    private final Map<UUID, Location> pos1 = new HashMap<>();
    private final Map<UUID, Location> pos2 = new HashMap<>();

    public ZoneManager(DhabiCore plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "zones.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        var section = dataConfig.getConfigurationSection("zones");
        if (section == null) return;

        for (String name : section.getKeys(false)) {
            String path = "zones." + name;
            String worldName = dataConfig.getString(path + ".world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            int minX = dataConfig.getInt(path + ".minX");
            int minY = dataConfig.getInt(path + ".minY");
            int minZ = dataConfig.getInt(path + ".minZ");
            int maxX = dataConfig.getInt(path + ".maxX");
            int maxY = dataConfig.getInt(path + ".maxY");
            int maxZ = dataConfig.getInt(path + ".maxZ");

            zones.put(name.toLowerCase(), new ProtectedZone(name, world, minX, minY, minZ, maxX, maxY, maxZ));
        }
    }

    public void saveAll() {
        dataConfig.set("zones", null);
        for (ProtectedZone zone : zones.values()) {
            String path = "zones." + zone.getName();
            dataConfig.set(path + ".world", zone.getWorld().getName());
            dataConfig.set(path + ".minX", zone.getMinX());
            dataConfig.set(path + ".minY", zone.getMinY());
            dataConfig.set(path + ".minZ", zone.getMinZ());
            dataConfig.set(path + ".maxX", zone.getMaxX());
            dataConfig.set(path + ".maxY", zone.getMaxY());
            dataConfig.set(path + ".maxZ", zone.getMaxZ());
        }
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Selection ──────────────────────────────────────────────────

    public void setPos1(Player player, Location loc) {
        pos1.put(player.getUniqueId(), loc);
    }

    public void setPos2(Player player, Location loc) {
        pos2.put(player.getUniqueId(), loc);
    }

    public boolean hasPos1(Player player) { return pos1.containsKey(player.getUniqueId()); }
    public boolean hasPos2(Player player) { return pos2.containsKey(player.getUniqueId()); }
    public Location getPos1(Player player) { return pos1.get(player.getUniqueId()); }
    public Location getPos2(Player player) { return pos2.get(player.getUniqueId()); }

    public void clearSelection(Player player) {
        pos1.remove(player.getUniqueId());
        pos2.remove(player.getUniqueId());
    }

    // ── Zone CRUD ─────────────────────────────────────────────────

    public boolean createZone(Player player, String name) {
        if (!hasPos1(player) || !hasPos2(player)) return false;
        Location p1 = pos1.get(player.getUniqueId());
        Location p2 = pos2.get(player.getUniqueId());

        if (!p1.getWorld().equals(p2.getWorld())) return false;

        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int minY = Math.min(p1.getBlockY(), p2.getBlockY());
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX());
        int maxY = Math.max(p1.getBlockY(), p2.getBlockY());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());

        ProtectedZone zone = new ProtectedZone(name, p1.getWorld(), minX, minY, minZ, maxX, maxY, maxZ);
        zones.put(name.toLowerCase(), zone);
        clearSelection(player);
        saveAll();
        return true;
    }

    public boolean deleteZone(String name) {
        if (!zones.containsKey(name.toLowerCase())) return false;
        zones.remove(name.toLowerCase());
        saveAll();
        return true;
    }

    public ProtectedZone getZone(String name) {
        return zones.get(name.toLowerCase());
    }

    public Collection<ProtectedZone> getAllZones() {
        return zones.values();
    }

    /** Returns the first zone that contains the given location, or null */
    public ProtectedZone getZoneAt(Location loc) {
        for (ProtectedZone zone : zones.values()) {
            if (zone.contains(loc)) return zone;
        }
        return null;
    }

    public boolean isProtected(Location loc) {
        return getZoneAt(loc) != null;
    }

    // ── Inner model ───────────────────────────────────────────────

    public static class ProtectedZone {
        private final String name;
        private final World world;
        private final int minX, minY, minZ, maxX, maxY, maxZ;

        public ProtectedZone(String name, World world,
                             int minX, int minY, int minZ,
                             int maxX, int maxY, int maxZ) {
            this.name  = name;
            this.world = world;
            this.minX = minX; this.minY = minY; this.minZ = minZ;
            this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
        }

        public boolean contains(Location loc) {
            if (!loc.getWorld().equals(world)) return false;
            int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
        }

        public String getName()  { return name; }
        public World  getWorld() { return world; }
        public int getMinX() { return minX; } public int getMaxX() { return maxX; }
        public int getMinY() { return minY; } public int getMaxY() { return maxY; }
        public int getMinZ() { return minZ; } public int getMaxZ() { return maxZ; }

        public String getSizeString() {
            return (maxX - minX + 1) + "x" + (maxY - minY + 1) + "x" + (maxZ - minZ + 1);
        }
    }
}
