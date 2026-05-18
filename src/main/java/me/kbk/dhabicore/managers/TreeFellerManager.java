package me.kbk.dhabicore.managers;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TreeFellerManager {

    private final DhabiCore plugin;
    private final Set<UUID> toggledOn = new HashSet<>();
    private final Set<UUID> processing = new HashSet<>();

    private File dataFile;
    private FileConfiguration dataConfig;

    private static final Set<Material> DEFAULT_LOG_TYPES = EnumSet.of(
        Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
        Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
        Material.MANGROVE_LOG, Material.CHERRY_LOG,
        Material.CRIMSON_STEM, Material.WARPED_STEM,
        Material.OAK_WOOD, Material.SPRUCE_WOOD, Material.BIRCH_WOOD,
        Material.JUNGLE_WOOD, Material.ACACIA_WOOD, Material.DARK_OAK_WOOD,
        Material.MANGROVE_WOOD, Material.CHERRY_WOOD,
        Material.CRIMSON_HYPHAE, Material.WARPED_HYPHAE
    );

    private static final Set<Material> LEAF_TYPES = EnumSet.of(
        Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES,
        Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES,
        Material.MANGROVE_LEAVES, Material.CHERRY_LEAVES, Material.AZALEA_LEAVES,
        Material.FLOWERING_AZALEA_LEAVES, Material.NETHER_WART_BLOCK, Material.WARPED_WART_BLOCK,
        Material.SHROOMLIGHT
    );

    private static final Set<Material> DEFAULT_AXE_TYPES = EnumSet.of(
        Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
        Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE
    );

    public TreeFellerManager(DhabiCore plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "treefeller.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        List<String> enabled = dataConfig.getStringList("toggled-on");
        for (String s : enabled) {
            try { toggledOn.add(UUID.fromString(s)); } catch (Exception ignored) {}
        }
    }

    public void saveAll() {
        List<String> list = new ArrayList<>();
        for (UUID uuid : toggledOn) list.add(uuid.toString());
        dataConfig.set("toggled-on", list);
        try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean isToggled(Player player) {
        return plugin.getConfig().getBoolean("treefeller.force-enabled", true);
    }

    public void toggle(Player player) {
        // Force-enabled mode: ignore toggles so behavior stays on 24/7.
    }

    public boolean isLog(Material m) {
        if (m == null) return false;
        if (DEFAULT_LOG_TYPES.contains(m)) return true;
        for (String name : plugin.getConfig().getStringList("treefeller.custom-logs")) {
            if (m.name().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public boolean isLeaf(Material m) { return LEAF_TYPES.contains(m); }

    public boolean isAxe(Material m) {
        if (!plugin.getConfig().getBoolean("treefeller.require-axe", true)) return true;
        if (m == null) return false;
        if (DEFAULT_AXE_TYPES.contains(m)) return true;
        for (String name : plugin.getConfig().getStringList("treefeller.allowed-axes")) {
            if (m.name().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public boolean isWorldBlacklisted(World world) {
        List<String> blacklist = plugin.getConfig().getStringList("treefeller.world-blacklist");
        return blacklist.contains(world.getName());
    }

    public boolean isProcessing(Player player) { return processing.contains(player.getUniqueId()); }

    public boolean isNaturalTree(Block base) {
        if (!isLog(base.getType())) return false;
        int maxScan = Math.min(plugin.getConfig().getInt("treefeller.max-blocks", 128), 512);
        int minLeaves = Math.max(1, plugin.getConfig().getInt("treefeller.min-leaves", 3));
        boolean diagonal = plugin.getConfig().getBoolean("treefeller.diagonal-detection", true);

        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(base);
        visited.add(base);

        int leafCount = 0;
        int count = 0;

        while (!queue.isEmpty() && count < maxScan) {
            Block current = queue.poll();
            count++;

            int radius = diagonal ? 1 : 0;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block neighbor = current.getRelative(dx, 1, dz);
                    if (visited.contains(neighbor)) continue;
                    if (isLog(neighbor.getType())) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    } else if (isLeaf(neighbor.getType())) {
                        leafCount++;
                    }
                }
            }
        }
        return leafCount >= minLeaves;
    }

    public List<Block> getTree(Block base) {
        int maxBlocks = Math.min(plugin.getConfig().getInt("treefeller.max-blocks", 128), 512);
        boolean diagonal = plugin.getConfig().getBoolean("treefeller.diagonal-detection", true);

        List<Block> tree = new ArrayList<>();
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();

        queue.add(base);
        visited.add(base);

        while (!queue.isEmpty() && tree.size() < maxBlocks) {
            Block current = queue.poll();
            if (!isLog(current.getType())) continue;
            tree.add(current);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        if (!diagonal && Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 1) continue;
                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (!visited.contains(neighbor) && isLog(neighbor.getType())) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
        return tree;
    }

    public void fellTree(Player player, Block base) {
        if (isProcessing(player)) return;
        if (plugin.getZoneManager().isProtected(base.getLocation()) && !player.hasPermission("dhabicore.zone.bypass") && !player.isOp()) return;
        processing.add(player.getUniqueId());

        try {
            List<Block> tree = getTree(base);
            ItemStack tool = player.getInventory().getItemInMainHand();

            for (Block b : tree) {
                if (b.equals(base)) continue;
                if (plugin.getZoneManager().isProtected(b.getLocation()) && !player.hasPermission("dhabicore.zone.bypass") && !player.isOp()) continue;
                b.breakNaturally(tool);
            }
        } finally {
            processing.remove(player.getUniqueId());
        }
    }
}
