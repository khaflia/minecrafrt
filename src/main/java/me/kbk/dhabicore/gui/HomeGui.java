package me.kbk.dhabicore.gui;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public class HomeGui {

    public static final String TITLE = "&a&lYour Homes";

    public static Inventory build(DhabiCore plugin, Player player) {
        Map<String, Location> homes = plugin.getHomeManager().getHomes(player);
        int max = plugin.getHomeManager().getMaxHomes(player);

        int rows = Math.max(3, (int) Math.ceil((homes.size() + 9) / 9.0) + 1);
        rows = Math.min(rows, 6);
        int size = rows * 9;

        Inventory inv = GuiManager.createGui(TITLE, size);
        GuiManager.fillBorder(inv);

        int slot = 10;
        for (Map.Entry<String, Location> entry : homes.entrySet()) {
            if (slot >= size - 9) break;
            Location loc = entry.getValue();
            String name = entry.getKey();
            String worldName = loc.getWorld() != null ? loc.getWorld().getName() : "unknown";

            inv.setItem(slot, GuiManager.createItem(Material.RED_BED,
                "&a" + capitalize(name),
                "&7World: &f" + worldName,
                "&7X: &f" + (int) loc.getX() +
                " &7Y: &f" + (int) loc.getY() +
                " &7Z: &f" + (int) loc.getZ(),
                "",
                "&eLeft-Click &7to teleport",
                "&cRight-Click &7to delete"));

            slot++;
            if (slot % 9 == 8) slot += 2; // skip border slots
        }

        // Info item in last row
        inv.setItem(size - 5, GuiManager.createItem(Material.BOOK,
            "&eHome Info",
            "&7Homes: &f" + homes.size() + " / " + max,
            "&7Use &e/sethome <name> &7to add more"));

        return inv;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
