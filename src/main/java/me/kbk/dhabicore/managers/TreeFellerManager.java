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

    private static final Set<Material> LOG_TYPES = EnumSet.of(
        Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
        Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
        Material.MANGROVE_LOG, Material.CHERRY_LOG,
        Material.OAK_WOOD, Material.SPRUCE_WOOD, Material.BIRCH_WOOD,
        Material.JUNGLE_WOOD, Material.ACACIA_WOOD, Material.DARK_OAK_WOOD,
        Material.MANGROVE_WOOD, Material.CHERRY_WOOD
    );

    private static final Set<Material> LEAF_TYPES = EnumSet.of(
        Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES,
        Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES,
        Material.MANGROVE_LEAVES, Material.CHERRY_LEAVES, Material.AZALEA_LEAVES,
        Material.FLOWERING_AZALEA_LEAVES
    );

    private static final Set<Material> AXE_TYPES = EnumSet.of(
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
        return toggledOn.contains(player.getUniqueId());
    }

    public void toggle(Player player) {
        if (toggledOn.contains(player.getUniqueId())) {
            toggledOn.remove(player.getUniqueId());
        } else {
            toggledOn.add(player.getUniqueId());
        }
        saveAll();
    }

    public boolean isLog(Material m) { return LOG_TYPES.contains(m); }
    public boolean isLeaf(Material m) { return LEAF_TYPES.contains(m); }
    public boolean isAxe(Material m) { return AXE_TYPES.contains(m); }

    public boolean isWorldBlacklisted(World world) {
        List<String> blacklist = plugin.getConfig().getStringList("treefeller.world-blacklist");
        return blacklist.contains(world.getName());
    }

    public boolean isProcessing(Player player) {
        return processing.contains(player.getUniqueId());
    }

    /**
     * Detects if the broken block is truly part of a natural tree.
     * A tree must have logs connected upward to leaves.
     */
    public boolean isNaturalTree(Block base) {
        Material logType = base.getType();
        if (!isLog(logType)) return false;

        // Scan upward to find if there are leaves nearby at the top
        int maxScan = plugin.getConfig().getInt("treefeller.max-blocks", 50);
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(base);
        visited.add(base);

        boolean foundLeaves = false;
        int count = 0;

        while (!queue.isEmpty() && count < maxScan) {
            Block current = queue.poll();
            count++;
            // Check up and diagonally up for logs
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Block neighbor = current.getRelative(dx, 1, dz);
                    if (!visited.contains(neighbor)) {
                        if (neighbor.getType() == logType) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        } else if (isLeaf(neighbor.getType())) {
                            foundLeaves = true;
                        }
                    }
                }
            }
        }
        return foundLeaves;
    }

    /**
     * Collects all logs connected to the base block via BFS.
     */
    /**
     * Collects ONLY logs connected to the base block via BFS.
     * Leaves are intentionally left alone — they'll decay naturally.
     */
    public List<Block> getTree(Block base) {
        Material logType = base.getType();
        int maxBlocks = plugin.getConfig().getInt("treefeller.max-blocks", 50);
        List<Block> tree = new ArrayList<>();
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();

        queue.add(base);
        visited.add(base);

        while (!queue.isEmpty() && tree.size() < maxBlocks) {
            Block current = queue.poll();
            if (current.getType() != logType) continue;
            tree.add(current);

            // Scan 3x3x3 cube for more logs of the same type
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (!visited.contains(neighbor) && neighbor.getType() == logType) {
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
        processing.add(player.getUniqueId());

        try {
            // Logs ONLY — leaves decay naturally on their own
            List<Block> tree = getTree(base);
            ItemStack tool = player.getInventory().getItemInMainHand();

            for (Block b : tree) {
                if (b.equals(base)) continue; // already broken by the event
                b.breakNaturally(tool);
            }
        } finally {
            processing.remove(player.getUniqueId());
        }
    }
}
