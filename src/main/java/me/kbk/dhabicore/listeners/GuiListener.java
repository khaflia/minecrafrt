package me.kbk.dhabicore.listeners;

import me.kbk.dhabicore.DhabiCore;
import me.kbk.dhabicore.gui.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class GuiListener implements Listener {

    private final DhabiCore plugin;

    public GuiListener(DhabiCore plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        if (event.getView().getTitle() == null) return;

        String rawTitle = ChatColor.stripColor(event.getView().getTitle());

        // Main Menu
        if (rawTitle.equals(strip(MainMenuGui.TITLE))) {
            event.setCancelled(true);
            handleMainMenu(player, event.getSlot());
        }
        // Home GUI
        else if (rawTitle.equals(strip(HomeGui.TITLE))) {
            event.setCancelled(true);
            handleHomeGui(player, event.getSlot(), event.isRightClick(), event.getCurrentItem());
        }
        // TPA GUI
        else if (rawTitle.equals(strip(TpaGui.TITLE))) {
            event.setCancelled(true);
            handleTpaGui(player, event.getSlot(), event.getCurrentItem());
        }
        // Not our GUI — let it through
    }

    private void handleMainMenu(Player player, int slot) {
        switch (slot) {
            case 19 -> player.openInventory(HomeGui.build(plugin, player));
            case 21 -> player.openInventory(TpaGui.build(plugin, player));
            case 23 -> {
                plugin.getVeinMinerManager().toggle(player);
                boolean on = plugin.getVeinMinerManager().isToggled(player);
                player.sendMessage(plugin.msg("veinminer.toggled")
                    .replace("{status}", on ? "&aEnabled" : "&cDisabled"));
                player.openInventory(MainMenuGui.build(plugin, player));
            }
            case 25 -> {
                plugin.getTreeFellerManager().toggle(player);
                boolean on = plugin.getTreeFellerManager().isToggled(player);
                player.sendMessage(plugin.msg("treefeller.toggled")
                    .replace("{status}", on ? "&aEnabled" : "&cDisabled"));
                player.openInventory(MainMenuGui.build(plugin, player));
            }
            // slot 31 = zone tool info, nothing to click through
        }
    }

    private void handleHomeGui(Player player, int slot, boolean rightClick, ItemStack item) {
        if (item == null || item.getItemMeta() == null) return;
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        Map<String, Location> homes = plugin.getHomeManager().getHomes(player);
        String homeName = null;
        for (String name : homes.keySet()) {
            if (name.equalsIgnoreCase(displayName)) {
                homeName = name;
                break;
            }
        }
        if (homeName == null) return;

        if (rightClick) {
            plugin.getHomeManager().deleteHome(player, homeName);
            player.sendMessage(plugin.msg("homes.deleted").replace("{name}", homeName));
            player.openInventory(HomeGui.build(plugin, player));
        } else {
            Location dest = plugin.getHomeManager().getHome(player, homeName);
            if (dest == null) return;
            player.closeInventory();
            int delay = plugin.getConfig().getInt("homes.teleport-delay-seconds", 3);
            final String finalName = homeName;
            Location startLoc = player.getLocation();
            player.sendMessage(plugin.msg("homes.teleporting")
                .replace("{name}", finalName)
                .replace("{seconds}", String.valueOf(delay)));
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                if (plugin.getConfig().getBoolean("homes.cancel-on-move", true)
                        && player.getLocation().distanceSquared(startLoc) > 0.5) {
                    player.sendMessage(plugin.msg("homes.teleport-cancelled"));
                    return;
                }
                player.teleport(dest);
                player.sendMessage(plugin.msg("homes.teleported").replace("{name}", finalName));
            }, delay * 20L);
        }
    }

    private void handleTpaGui(Player player, int slot, ItemStack item) {
        if (item == null || item.getItemMeta() == null) return;
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (displayName.startsWith("Accept ")) {
            String targetName = displayName.substring(7).trim();
            for (Player req : plugin.getTpaManager().getAllRequestersFor(player)) {
                if (req.getName().equals(targetName)) {
                    player.closeInventory();
                    plugin.getTpaManager().acceptRequest(player);
                    return;
                }
            }
        } else if (displayName.startsWith("Deny ")) {
            String targetName = displayName.substring(5).trim();
            for (Player req : plugin.getTpaManager().getAllRequestersFor(player)) {
                if (req.getName().equals(targetName)) {
                    player.closeInventory();
                    plugin.getTpaManager().denyRequest(player);
                    return;
                }
            }
        }
    }

    private String strip(String s) {
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', s));
    }
}
