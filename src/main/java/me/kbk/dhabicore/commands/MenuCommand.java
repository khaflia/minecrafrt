package me.kbk.dhabicore.commands;

import me.kbk.dhabicore.DhabiCore;
import me.kbk.dhabicore.gui.MainMenuGui;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class MenuCommand implements CommandExecutor {
    private final DhabiCore plugin;
    public MenuCommand(DhabiCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
        player.openInventory(MainMenuGui.build(plugin, player));
        return true;
    }
}
