package me.kbk.dhabicore.utils;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageManager {

    private static FileConfiguration messages;
    private static DhabiCore plugin;

    public static void init(DhabiCore pl) {
        plugin = pl;
        File file = new File(pl.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public static String get(String key) {
        String raw = messages.getString(key, "&cMissing message: " + key);
        String prefix = messages.getString("prefix", "&6[DhabiCore]&r ");
        raw = raw.replace("{prefix}", prefix);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
