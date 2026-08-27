package com.onux.onyx.listeners;

import com.onux.onyx.abilities.Activatable;
import com.onux.onyx.weapons.WeaponFactory;
import com.onux.onyx.weapons.WeaponType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.Map;

/**
 * Repurposes the vanilla "swap item to offhand" key (F by default) as the
 * ability trigger for every weapon: plain F = primary ability, Shift+F =
 * secondary ability. The Warden Crossbow only registers a primary handler
 * here (Sculk Meteor) since its own primary ability, Warden Beam, is
 * triggered separately by {@link WardenChargeListener} via Shift+right-click.
 */
public final class SwapHandListener implements Listener {

    private final WeaponFactory weapons;
    private final Map<WeaponType, Activatable> primary;
    private final Map<WeaponType, Activatable> secondary;

    public SwapHandListener(WeaponFactory weapons, Map<WeaponType, Activatable> primary, Map<WeaponType, Activatable> secondary) {
        this.weapons = weapons;
        this.primary = primary;
        this.secondary = secondary;
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        WeaponType type = weapons.identifyHeld(player);
        if (type == null) return;

        event.setCancelled(true);

        Activatable ability = (player.isSneaking() ? secondary : primary).get(type);
        if (ability != null) {
            ability.activate(player);
        }
    }
}
