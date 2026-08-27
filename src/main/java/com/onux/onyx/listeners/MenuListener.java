package com.onux.onyx.listeners;

import com.onux.onyx.gui.ArsenalMenu;
import com.onux.onyx.weapons.WeaponFactory;
import com.onux.onyx.weapons.WeaponType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class MenuListener implements Listener {

    private final ArsenalMenu menu;
    private final WeaponFactory weapons;

    public MenuListener(ArsenalMenu menu, WeaponFactory weapons) {
        this.menu = menu;
        this.weapons = weapons;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ArsenalMenu.Holder)) return;
        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        WeaponType type = menu.typeAt(slot);
        if (type == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        weapons.give(player, type);
        player.closeInventory();
    }
}
