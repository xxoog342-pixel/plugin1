package com.onux.onyx.listeners;

import com.onux.onyx.abilities.warden.WardenBeam;
import com.onux.onyx.weapons.WeaponFactory;
import com.onux.onyx.weapons.WeaponType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Warden Beam's input: hold Shift + right-click while holding the Warden
 * Crossbow to start charging (this cancels the interaction so no real
 * ammo gets loaded), release Shift to fire. Right-clicking *without*
 * sneaking is left completely alone, so the crossbow's normal vanilla
 * shot (and {@code WardensGaze}'s passive) still works.
 */
public final class WardenChargeListener implements Listener {

    private final WeaponFactory weapons;
    private final WardenBeam wardenBeam;

    public WardenChargeListener(WeaponFactory weapons, WardenBeam wardenBeam) {
        this.weapons = weapons;
        this.wardenBeam = wardenBeam;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        if (!weapons.isWeapon(event.getItem(), WeaponType.WARDEN_CROSSBOW)) return;
        if (!player.isSneaking()) return;

        event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);

        if (!wardenBeam.isCharging(player)) {
            wardenBeam.beginCharge(player);
        }
    }

    @EventHandler
    public void onSneakToggle(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) return; // only care about releasing shift
        Player player = event.getPlayer();
        if (wardenBeam.isCharging(player)) {
            wardenBeam.release(player);
        }
    }

    @EventHandler
    public void onItemSwitch(PlayerItemHeldEvent event) {
        wardenBeam.cancelChargeSilently(event.getPlayer());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        wardenBeam.cancelChargeSilently(event.getPlayer());
    }
}
