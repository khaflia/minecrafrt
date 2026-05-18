package me.kbk.dhabicore.listeners;

import me.kbk.dhabicore.DhabiCore;
import me.kbk.dhabicore.managers.ZoneManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ZoneListener implements Listener {

    private final DhabiCore plugin;

    public ZoneListener(DhabiCore plugin) { this.plugin = plugin; }

    // ── Block Break ──────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (canBypass(player)) return;
        if (plugin.getZoneManager().isProtected(event.getBlock().getLocation())) {
            event.setCancelled(true);
            notify(player);
        }
    }

    // ── Block Place ──────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (canBypass(player)) return;
        if (plugin.getZoneManager().isProtected(event.getBlock().getLocation())) {
            event.setCancelled(true);
            notify(player);
        }
    }

    // ── Interact (chests, buttons, doors, etc.) ──────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Stick selection tool — works even inside zones
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.STICK && item.hasItemMeta()
            && item.getItemMeta() != null
            && ("§bProtection Wand".equals(item.getItemMeta().getDisplayName())
                || "§3Protection Wand".equals(item.getItemMeta().getDisplayName())
                || item.getItemMeta().getDisplayName().contains("Protection Wand"))
            && player.hasPermission("dhabicore.zone")) {
            Block clicked = event.getClickedBlock();
            if (clicked == null) return;
            if (event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
                plugin.getZoneManager().setPos1(player, clicked.getLocation());
                String coords = "(" + clicked.getX() + ", " + clicked.getY() + ", " + clicked.getZ() + ")";
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&b\u25a0 &fPosition 1 &7set to &b" + coords));
                checkBothSet(player);
            } else if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                plugin.getZoneManager().setPos2(player, clicked.getLocation());
                String coords = "(" + clicked.getX() + ", " + clicked.getY() + ", " + clicked.getZ() + ")";
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&b\u25a0 &fPosition 2 &7set to &b" + coords));
                checkBothSet(player);
            }
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;
        if (canBypass(player)) return;

        if (plugin.getZoneManager().isProtected(clicked.getLocation())) {
            // Block interactions with containers, doors, buttons inside zone
            if (isInteractable(clicked.getType())) {
                event.setCancelled(true);
                notify(player);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (plugin.getZoneManager().isProtected(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (plugin.getZoneManager().isProtected(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (plugin.getZoneManager().isProtected(event.getBlock().getLocation())
            || plugin.getZoneManager().isProtected(event.getSource().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        if (canBypass(player)) return;
        if (plugin.getZoneManager().isProtected(event.getBlock().getLocation())) {
            event.setCancelled(true);
            notify(player);
        }
    }

    // ── Explosions (TNT, creeper, etc.) ─────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event) {
        event.blockList().removeIf(b -> plugin.getZoneManager().isProtected(b.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplosion(BlockExplodeEvent event) {
        event.blockList().removeIf(b -> plugin.getZoneManager().isProtected(b.getLocation()));
    }

    // ── Piston push/pull into zone ───────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block b : event.getBlocks()) {
            if (plugin.getZoneManager().isProtected(b.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block b : event.getBlocks()) {
            if (plugin.getZoneManager().isProtected(b.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────

    /** Only ops or explicit bypass permission can edit protected zones */
    private boolean canBypass(Player player) {
        return player.isOp() || player.hasPermission("dhabicore.zone.bypass");
    }

    private void notify(Player player) {
        ZoneManager.ProtectedZone zone = plugin.getZoneManager().getZoneAt(player.getLocation());
        String name = zone != null ? zone.getName() : "this area";
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&b\u25a0 &cThis area is protected &8(&f" + name + "&8)&c."));
    }

    private void checkBothSet(Player player) {
        if (plugin.getZoneManager().hasPos1(player) && plugin.getZoneManager().hasPos2(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&7Both positions set \u2014 run &b/zone create <name> &7to protect."));
        }
    }

    private boolean isInteractable(Material m) {
        String name = m.name();
        return name.contains("CHEST") || name.contains("BARREL") || name.contains("SHULKER")
            || name.contains("FURNACE") || name.contains("HOPPER") || name.contains("DISPENSER")
            || name.contains("DROPPER") || name.contains("DOOR") || name.contains("GATE")
            || name.contains("TRAPDOOR") || name.contains("BUTTON") || name.contains("LEVER")
            || name.contains("ANVIL") || name.contains("ENCHANTING") || name.contains("BEACON")
            || name.contains("CRAFTING") || name.contains("LOOM") || name.contains("GRINDSTONE")
            || name.contains("STONECUTTER") || name.contains("SMITHING") || name.contains("CAMPFIRE")
            || name.contains("SIGN")
            || m == Material.JUKEBOX || m == Material.NOTE_BLOCK || m == Material.BELL;
    }
}
