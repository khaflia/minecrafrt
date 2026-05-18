package me.kbk.dhabicore.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class GuiManager {

    public static Inventory createGui(String title, int size) {
        return Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', title));
    }

    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(Arrays.stream(lore)
            .map(l -> ChatColor.translateAlternateColorCodes('&', l))
            .collect(java.util.stream.Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(lore.stream()
            .map(l -> ChatColor.translateAlternateColorCodes('&', l))
            .collect(java.util.stream.Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createPlayerHead(String playerName, String displayName, String... lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(playerName);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        meta.setLore(Arrays.stream(lore)
            .map(l -> ChatColor.translateAlternateColorCodes('&', l))
            .collect(java.util.stream.Collectors.toList()));
        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack createFillerPane() {
        return createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
    }

    public static void fillBorder(Inventory inv) {
        int size = inv.getSize();
        int width = 9;
        ItemStack filler = createFillerPane();
        // Top row
        for (int i = 0; i < width; i++) inv.setItem(i, filler);
        // Bottom row
        for (int i = size - width; i < size; i++) inv.setItem(i, filler);
        // Left & right columns
        for (int i = 1; i < size / width - 1; i++) {
            inv.setItem(i * width, filler);
            inv.setItem(i * width + (width - 1), filler);
        }
    }
}
