package com.onux.onyx.listeners;

import com.onux.onyx.util.CooldownManager;
import com.onux.onyx.util.TrustManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerCleanupListener implements Listener {

    private final CooldownManager cooldowns;
    private final TrustManager trust;

    public PlayerCleanupListener(CooldownManager cooldowns, TrustManager trust) {
        this.cooldowns = cooldowns;
        this.trust = trust;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.forget(event.getPlayer().getUniqueId());
        trust.forget(event.getPlayer().getUniqueId());
    }
}
