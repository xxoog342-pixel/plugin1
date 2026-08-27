package com.onux.onyx.weapons;

/**
 * Every custom weapon Onyx knows about. The {@link #id} is written into
 * each item's PersistentDataContainer and is the only thing used to
 * identify a weapon - never display name or lore, so renaming an item
 * (ItemEdit, anvils, resource packs) never breaks its abilities.
 */
public enum WeaponType {

    WARDEN_CROSSBOW("warden_crossbow", 73111),
    FROST_BLADE("frost_blade", 73114),
    LEVIATHANS_FANG("leviathans_fang", 73112),
    VOID_BLADE("void_blade", 73113),
    EYES("eyes", 73115);

    private final String id;
    private final int customModelData;

    WeaponType(String id, int customModelData) {
        this.id = id;
        this.customModelData = customModelData;
    }

    public String id() {
        return id;
    }

    public int customModelData() {
        return customModelData;
    }
}
