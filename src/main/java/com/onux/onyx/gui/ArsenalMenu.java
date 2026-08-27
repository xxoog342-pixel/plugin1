package com.onux.onyx.gui;

import com.onux.onyx.util.Msg;
import com.onux.onyx.weapons.WeaponFactory;
import com.onux.onyx.weapons.WeaponType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/** The /Onuxmenu GUI - one slot per weapon, all five now included (Frost Blade was previously missing). */
public final class ArsenalMenu {

    private static final int SIZE = 27;
    private static final int[] WEAPON_SLOTS = { 10, 12, 13, 14, 16 };

    private final WeaponFactory weapons;
    private final Map<Integer, WeaponType> slotMap = new HashMap<>();

    public ArsenalMenu(WeaponFactory weapons) {
        this.weapons = weapons;
        WeaponType[] types = WeaponType.values();
        for (int i = 0; i < types.length && i < WEAPON_SLOTS.length; i++) {
            slotMap.put(WEAPON_SLOTS[i], types[i]);
        }
    }

    public Inventory build() {
        Holder holder = new Holder();
        Inventory inv = Bukkit.createInventory(holder, SIZE, Msg.of("&8☠ &5&lONYX ARSENAL"));
        holder.setInventory(inv);

        ItemStack filler = filler();
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, filler);
        }
        for (Map.Entry<Integer, WeaponType> entry : slotMap.entrySet()) {
            inv.setItem(entry.getKey(), weapons.create(entry.getValue()));
        }
        return inv;
    }

    public WeaponType typeAt(int slot) {
        return slotMap.get(slot);
    }

    private ItemStack filler() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Msg.of(" "));
        item.setItemMeta(meta);
        return item;
    }

    /** Marker holder so {@code MenuListener} can identify our GUI without matching on title text. */
    public static final class Holder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }
}
