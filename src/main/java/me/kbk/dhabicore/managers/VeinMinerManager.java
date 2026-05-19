package me.kbk.dhabicore.managers;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import me.kbk.dhabicore.utils.RegionProtectionUtil;

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
        return plugin.getConfig().getBoolean("veinminer.force-enabled", true);
    }

    public void toggle(Player player) {
        // Force-enabled mode: ignore toggles so behavior stays on 24/7.
    }

    public boolean isOre(Material m) {
        if (ORE_BLOCKS.contains(m)) return true;
        for (String name : plugin.getConfig().getStringList("veinminer.custom-ores")) {
            if (m.name().equalsIgnoreCase(name)) return true;
        }
        return false;
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
        int maxBlocks = Math.min(plugin.getConfig().getInt("veinminer.max-blocks", 64), 512);
        boolean diagonal = plugin.getConfig().getBoolean("veinminer.diagonal-detection", true);
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
                        if (!diagonal && Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 1) continue;
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

    public void mineVein(Player player, Block origin, Material targetMaterial) {
        if (isProcessing(player)) return;
        if (plugin.getZoneManager().isProtected(origin.getLocation()) && !player.hasPermission("dhabicore.zone.bypass") && !player.isOp()) return;
        if (!RegionProtectionUtil.canBreak(player, origin)) return;
        processing.add(player.getUniqueId());

        try {
            List<Block> vein = getVein(origin, targetMaterial, player);
            ItemStack tool = player.getInventory().getItemInMainHand();

            for (Block b : vein) {
                if (plugin.getZoneManager().isProtected(b.getLocation()) && !player.hasPermission("dhabicore.zone.bypass") && !player.isOp()) continue;
                if (!RegionProtectionUtil.canBreak(player, b)) continue;
                breakWithDrops(player, b, tool);
            }
        } finally {
            processing.remove(player.getUniqueId());
        }
    }
    private void breakWithDrops(Player player, Block block, ItemStack tool) {
        if (block.getType() == Material.AIR) return;

        Collection<ItemStack> drops = (tool == null || tool.getType() == Material.AIR)
            ? block.getDrops()
            : block.getDrops(tool, player);
        block.setType(Material.AIR, false);
        for (ItemStack drop : drops) {
            block.getWorld().dropItemNaturally(block.getLocation(), drop);
        }

        if (tool != null && tool.getType().getMaxDurability() > 0 && tool.getItemMeta() instanceof Damageable meta) {
            int next = meta.getDamage() + 1;
            if (next >= tool.getType().getMaxDurability()) {
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            } else {
                meta.setDamage(next);
                tool.setItemMeta(meta);
            }
        }
    }

}
