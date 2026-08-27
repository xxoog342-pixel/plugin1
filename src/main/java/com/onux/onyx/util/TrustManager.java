package com.onux.onyx.util;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/** Backs {@code /trust add|remove|list <player>} - a trusted player is exempt from the owner's area-effect damage. */
public final class TrustManager {

    private final Map2 trusted = new Map2();

    public void trust(Player owner, Player target) {
        trusted.get(owner.getUniqueId()).add(target.getUniqueId());
    }

    public void untrust(Player owner, Player target) {
        trusted.get(owner.getUniqueId()).remove(target.getUniqueId());
    }

    public boolean isTrusted(Player owner, Player target) {
        if (owner.equals(target)) return true;
        return trusted.get(owner.getUniqueId()).contains(target.getUniqueId());
    }

    public Set<UUID> list(Player owner) {
        return trusted.get(owner.getUniqueId());
    }

    public void forget(UUID uuid) {
        trusted.map.remove(uuid);
    }

    private static final class Map2 {
        private final ConcurrentHashMap<UUID, Set<UUID>> map = new ConcurrentHashMap<>();

        Set<UUID> get(UUID owner) {
            return map.computeIfAbsent(owner, u -> new CopyOnWriteArraySet<>());
        }
    }
}
