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

public class VeinMinerManager {

    private final DhabiCore plugin;
    private final Set<UUID> toggledOn = new HashSet<>();
    // Players currently mid-vein to prevent recursion
    private final Set<UUID> processing = new HashSet<>();

    private File dataFile;
    private FileConfiguration dataConfig;

    private static final Set<Material> ORE_BLOCKS = EnumSet.of(
        Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
        Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
        Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
        Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
        Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
        Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
        Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
        Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
        Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
        Material.ANCIENT_DEBRIS
    );

    private static final Set<Material> MINING_TOOLS = EnumSet.of(
        Material.WOODEN_PICKAXE, Material.STONE_PICKAXE,
        Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE,
        Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE
    );

    public VeinMinerManager(DhabiCore plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "veinminer.yml");
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

    public boolean isOre(Material m) {
        return ORE_BLOCKS.contains(m);
    }

    public boolean isMiningTool(Material m) {
        return MINING_TOOLS.contains(m);
    }

    public boolean isWorldBlacklisted(World world) {
        List<String> blacklist = plugin.getConfig().getStringList("veinminer.world-blacklist");
        return blacklist.contains(world.getName());
    }

    public boolean isProcessing(Player player) {
        return processing.contains(player.getUniqueId());
    }

    /**
     * Performs BFS vein mining. Returns list of blocks to break.
     * All safety checks included.
     */
    public List<Block> getVein(Block origin, Material targetMaterial, Player player) {
        int maxBlocks = plugin.getConfig().getInt("veinminer.max-blocks", 64);
        List<Block> vein = new ArrayList<>();
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();

        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty() && vein.size() < maxBlocks) {
            Block current = queue.poll();
            if (current.getType() != targetMaterial) continue;
            vein.add(current);

            // Check 26 neighbors (3x3x3 cube)
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (!visited.contains(neighbor) && neighbor.getType() == targetMaterial) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
        return vein;
    }

    public void mineVein(Player player, Block block) {
        if (isProcessing(player)) return;
        processing.add(player.getUniqueId());

        try {
            List<Block> vein = getVein(block, block.getType(), player);
            ItemStack tool = player.getInventory().getItemInMainHand();

            for (Block b : vein) {
                if (b.equals(block)) continue; // Already broken by the event
                // Damage tool durability
                if (tool != null && tool.getType() != Material.AIR) {
                    damageToolBy(player, tool, 1);
                    if (tool.getType() == Material.AIR) break; // Tool broke
                }
                b.breakNaturally(tool);
            }
        } finally {
            processing.remove(player.getUniqueId());
        }
    }

    @SuppressWarnings("deprecation")
    private void damageToolBy(Player player, ItemStack tool, int amount) {
        if (tool == null) return;
        short newDurability = (short) (tool.getDurability() + amount);
        short maxDurability = tool.getType().getMaxDurability();
        if (newDurability >= maxDurability) {
            player.getInventory().setItemInMainHand(null);
        } else {
            tool.setDurability(newDurability);
        }
    }
}
