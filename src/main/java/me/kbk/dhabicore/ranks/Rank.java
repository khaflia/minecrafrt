package me.kbk.dhabicore.ranks;

import org.bukkit.ChatColor;

public enum Rank {

    MEMBER(   "&7[Member]&r",    "&7", 0),
    VIP(      "&b[VIP]&r",       "&b", 1),
    MVP(      "&3[MVP]&r",       "&3", 2),
    STAFF(    "&9[Staff]&r",     "&9", 3),
    ADMIN(    "&1[Admin]&r",     "&1", 4),
    OWNER(    "&c[Owner]&r",     "&c", 5),
    DEVELOPER("&d[Dev]&r",       "&d", 6);

    private final String prefix;
    private final String color;
    private final int weight;

    Rank(String prefix, String color, int weight) {
        this.prefix = prefix;
        this.color = color;
        this.weight = weight;
    }

    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', prefix);
    }

    public String getColor() {
        return ChatColor.translateAlternateColorCodes('&', color);
    }

    public String getRawPrefix() { return prefix; }
    public int getWeight()       { return weight; }

    public boolean isAtLeast(Rank other) {
        return this.weight >= other.weight;
    }

    public boolean isStaff() {
        return this.weight >= STAFF.weight;
    }

    public static Rank fromString(String name) {
        try {
            return Rank.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEMBER;
        }
    }

    public String getDisplayName() {
        return ChatColor.translateAlternateColorCodes('&', prefix);
    }
}
