package me.kbk.dhabicore.gui;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class TpaGui {

    public static final String TITLE = "&9&lTPA Requests";

    public static Inventory build(DhabiCore plugin, Player player) {
        List<Player> requesters = plugin.getTpaManager().getAllRequestersFor(player);

        int size = 27;
        Inventory inv = GuiManager.createGui(TITLE, size);
        GuiManager.fillBorder(inv);

        if (requesters.isEmpty()) {
            inv.setItem(13, GuiManager.createItem(Material.BARRIER,
                "&cNo Pending Requests",
                "&7Nobody has sent you a TPA request."));
            return inv;
        }

        int slot = 10;
        for (Player requester : requesters) {
            if (slot > 16) break; // max 7 visible at once

            // Each request: player head + accept/deny
            inv.setItem(slot, GuiManager.createPlayerHead(requester.getName(),
                "&a" + requester.getName(),
                "&7Click to &aaccept &7or see below",
                "&ePing: &f" + requester.getPing() + "ms"));

            inv.setItem(slot + 9, GuiManager.createItem(Material.LIME_WOOL,
                "&aAccept &f" + requester.getName(),
                "&7Click to accept teleport"));

            inv.setItem(slot + 10, GuiManager.createItem(Material.RED_WOOL,
                "&cDeny &f" + requester.getName(),
                "&7Click to deny teleport"));

            slot += 2;
        }

        return inv;
    }
}
