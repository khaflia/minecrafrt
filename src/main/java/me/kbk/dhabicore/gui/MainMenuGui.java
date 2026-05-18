package me.kbk.dhabicore.gui;

import me.kbk.dhabicore.DhabiCore;
import me.kbk.dhabicore.ranks.Rank;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MainMenuGui {

    public static final String TITLE = "&b&lKhaleeji SMP";

    public static Inventory build(DhabiCore plugin, Player player) {
        Inventory inv = GuiManager.createGui(TITLE, 54);
        GuiManager.fillBorder(inv);

        Rank rank = plugin.getRankManager().getRank(player);
        int homes = plugin.getHomeManager().getHomes(player).size();
        int maxHomes = plugin.getHomeManager().getMaxHomes(player);
        boolean vmOn = plugin.getVeinMinerManager().isToggled(player);
        boolean tfOn = plugin.getTreeFellerManager().isToggled(player);

        // Player head / profile — centre top
        inv.setItem(13, GuiManager.createPlayerHead(player.getName(),
            "&f" + player.getName(),
            "&7Rank: " + rank.getDisplayName(),
            "&7World: &f" + player.getWorld().getName(),
            "&7Ping: &b" + player.getPing() + "ms"));

        // Homes
        inv.setItem(19, GuiManager.createItem(Material.RED_BED,
            "&b\u25a0 &fHomes",
            "&7" + homes + " / " + maxHomes + " homes set",
            "",
            "&bClick &7to open homes"));

        // TPA
        inv.setItem(21, GuiManager.createItem(Material.ENDER_PEARL,
            "&b\u25a0 &fTeleport Requests",
            "&7View incoming TPA requests",
            "",
            "&bClick &7to open"));

        // VeinMiner toggle
        inv.setItem(23, GuiManager.createItem(
            vmOn ? Material.DIAMOND_PICKAXE : Material.IRON_PICKAXE,
            "&b\u25a0 &fVeinMiner",
            "&7Auto-mines connected ores",
            "",
            "&7Status: " + (vmOn ? "&aEnabled" : "&cDisabled"),
            "&bClick &7to toggle"));

        // TreeFeller toggle
        inv.setItem(25, GuiManager.createItem(
            tfOn ? Material.DIAMOND_AXE : Material.IRON_AXE,
            "&b\u25a0 &fTreeFeller",
            "&7Chops entire trees at once",
            "",
            "&7Status: " + (tfOn ? "&aEnabled" : "&cDisabled"),
            "&bClick &7to toggle"));

        // Zone info (if they have permission)
        if (player.hasPermission("dhabicore.zone")) {
            inv.setItem(31, GuiManager.createItem(Material.SHIELD,
                "&b\u25a0 &fZone Tool",
                "&7Left/right-click blocks with a &fStick",
                "&7to select corners, then:",
                "&b/zone create <name>",
                "",
                "&7\u2014 Staff+ only \u2014"));
        }

        // Server branding
        inv.setItem(49, GuiManager.createItem(Material.NETHER_STAR,
            "&f&lplay.&b&lkhaleeji.lol",
            "&7Dhabi's Minecraft Server",
            "&7by &bkbk"));

        return inv;
    }
}
