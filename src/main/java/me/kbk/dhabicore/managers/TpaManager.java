package me.kbk.dhabicore.managers;

import me.kbk.dhabicore.DhabiCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TpaManager {

    private final DhabiCore plugin;

    // requester UUID -> target UUID
    private final Map<UUID, UUID> pendingRequests = new HashMap<>();
    // requester UUID -> expiry task
    private final Map<UUID, BukkitTask> expiryTasks = new HashMap<>();
    // requester UUID -> last request time (millis)
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public TpaManager(DhabiCore plugin) {
        this.plugin = plugin;
    }

    public boolean sendRequest(Player requester, Player target) {
        UUID reqId = requester.getUniqueId();

        // Cooldown check
        int cooldownSecs = plugin.getConfig().getInt("tpa.cooldown-seconds", 30);
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(reqId)) {
            long elapsed = (now - cooldowns.get(reqId)) / 1000;
            if (elapsed < cooldownSecs) {
                return false; // still on cooldown
            }
        }

        // Cancel previous request if any
        cancelRequest(requester);

        pendingRequests.put(reqId, target.getUniqueId());
        cooldowns.put(reqId, now);

        int expirySecs = plugin.getConfig().getInt("tpa.expire-seconds", 60);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingRequests.containsKey(reqId)) {
                pendingRequests.remove(reqId);
                expiryTasks.remove(reqId);
                Player r = Bukkit.getPlayer(reqId);
                Player t = Bukkit.getPlayer(target.getUniqueId());
                if (r != null) r.sendMessage(plugin.msg("tpa.expired-requester").replace("{player}", target.getName()));
                if (t != null) t.sendMessage(plugin.msg("tpa.expired-target").replace("{player}", requester.getName()));
            }
        }, expirySecs * 20L);

        expiryTasks.put(reqId, task);
        return true;
    }

    public long getCooldownRemaining(Player requester) {
        int cooldownSecs = plugin.getConfig().getInt("tpa.cooldown-seconds", 30);
        long now = System.currentTimeMillis();
        if (!cooldowns.containsKey(requester.getUniqueId())) return 0;
        long elapsed = (now - cooldowns.get(requester.getUniqueId())) / 1000;
        return Math.max(0, cooldownSecs - elapsed);
    }

    public boolean hasPendingRequest(Player target) {
        return pendingRequests.values().contains(target.getUniqueId());
    }

    // Returns the player who sent a request to 'target', or null
    public Player getRequesterFor(Player target) {
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(target.getUniqueId())) {
                return Bukkit.getPlayer(entry.getKey());
            }
        }
        return null;
    }

    // Returns all players whose requests are pending to 'target'
    public List<Player> getAllRequestersFor(Player target) {
        List<Player> list = new ArrayList<>();
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(target.getUniqueId())) {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null) list.add(p);
            }
        }
        return list;
    }

    public void acceptRequest(Player target) {
        Player requester = getRequesterFor(target);
        if (requester == null) return;
        cancelRequest(requester);

        int delay = plugin.getConfig().getInt("tpa.teleport-delay-seconds", 3);
        requester.sendMessage(plugin.msg("tpa.teleport-countdown").replace("{seconds}", String.valueOf(delay)));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (requester.isOnline() && target.isOnline()) {
                requester.teleport(target.getLocation());
                requester.sendMessage(plugin.msg("tpa.teleported").replace("{player}", target.getName()));
                target.sendMessage(plugin.msg("tpa.accepted-target").replace("{player}", requester.getName()));
            }
        }, delay * 20L);
    }

    public void denyRequest(Player target) {
        Player requester = getRequesterFor(target);
        if (requester == null) return;
        cancelRequest(requester);
        if (requester.isOnline()) {
            requester.sendMessage(plugin.msg("tpa.denied-requester").replace("{player}", target.getName()));
        }
        target.sendMessage(plugin.msg("tpa.denied-target").replace("{player}", requester.getName()));
    }

    public void cancelRequest(Player requester) {
        UUID reqId = requester.getUniqueId();
        pendingRequests.remove(reqId);
        BukkitTask task = expiryTasks.remove(reqId);
        if (task != null) task.cancel();
    }

    public void cancelAllFor(Player player) {
        cancelRequest(player);
        // Also cancel requests targeting this player
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(player.getUniqueId())) {
                toRemove.add(entry.getKey());
            }
        }
        for (UUID uuid : toRemove) {
            pendingRequests.remove(uuid);
            BukkitTask t = expiryTasks.remove(uuid);
            if (t != null) t.cancel();
        }
    }

    public Map<UUID, UUID> getPendingRequests() { return Collections.unmodifiableMap(pendingRequests); }
}
