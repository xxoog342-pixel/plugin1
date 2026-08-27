package com.onux.onyx.weapons;

import com.onux.onyx.util.FX;
import com.onux.onyx.util.Msg;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class WeaponFactory {

    private final Plugin plugin;
    private final NamespacedKey weaponIdKey;

    public WeaponFactory(Plugin plugin) {
        this.plugin = plugin;
        this.weaponIdKey = new NamespacedKey(plugin, "weapon_id");
    }

    public NamespacedKey weaponIdKey() {
        return weaponIdKey;
    }

    // ---------------------------------------------------------------
    // Identification
    // ---------------------------------------------------------------

    public WeaponType identify(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String id = meta.getPersistentDataContainer().get(weaponIdKey, PersistentDataType.STRING);
        if (id == null) return null;
        for (WeaponType type : WeaponType.values()) {
            if (type.id().equals(id)) return type;
        }
        return null;
    }

    public boolean isWeapon(ItemStack item, WeaponType type) {
        return identify(item) == type;
    }

    public boolean isHoldingWeapon(Player player, WeaponType type) {
        return isWeapon(player.getInventory().getItemInMainHand(), type)
                || isWeapon(player.getInventory().getItemInOffHand(), type);
    }

    public WeaponType identifyHeld(Player player) {
        WeaponType main = identify(player.getInventory().getItemInMainHand());
        if (main != null) return main;
        return identify(player.getInventory().getItemInOffHand());
    }

    // ---------------------------------------------------------------
    // Creation
    // ---------------------------------------------------------------

    public ItemStack create(WeaponType type) {
        return switch (type) {
            case WARDEN_CROSSBOW -> createWardenCrossbow();
            case FROST_BLADE -> createFrostBlade();
            case LEVIATHANS_FANG -> createLeviathansFang();
            case VOID_BLADE -> createVoidBlade();
            case EYES -> createEyes();
        };
    }

    public ItemStack createWardenCrossbow() {
        ItemStack item = new ItemStack(Material.CROSSBOW);
        ItemMeta meta = item.getItemMeta();
        tag(meta, WeaponType.WARDEN_CROSSBOW);
        meta.displayName(Msg.of("&8☠ &3Warden's Resonance"));
        meta.lore(Msg.lore(
                "&8━━━━━━━━━━━━━━━━━━━━━━━━",
                "&7A forbidden weapon forged from",
                "&7the resonance of the Deep Dark.",
                "",
                "&3⚡ &bWARDEN BEAM",
                "&7Hold &fSHIFT + Right-Click &7to charge,",
                "&7release to fire a Warden sonic beam.",
                "&7Damage: &f3 hearts",
                "&7Cooldown: &f60 seconds",
                "",
                "&5☠ &dSCULK METEOR",
                "&7Look at a player, press &fF&7, to call",
                "&7a Sculk meteor down on them.",
                "&7Damage: &f4 hearts",
                "",
                "&8✦ &7Passive: &fWarden's Gaze",
                "&8  Normal shots may blind on hit.",
                "&8━━━━━━━━━━━━━━━━━━━━━━━━"
        ));
        meta.addEnchant(Enchantment.QUICK_CHARGE, 3, true);
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createFrostBlade() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        tag(meta, WeaponType.FROST_BLADE);
        meta.displayName(Msg.of("&b&lFrost Blade"));
        meta.lore(Msg.lore(
                "&8━━━━━━━━━━━━━━━━━━━━━━━━",
                "&7Forged in an eternal winter,",
                "&7its edge never melts.",
                "",
                "&b❄ &lFROZEN BARRAGE",
                "&7Press &fF &7to rise, charge, and",
                "&7launch 4 ice projectiles.",
                "&7Damage: &f2 hearts each",
                "&7Cooldown: &f70 seconds",
                "",
                "&f❄ &lICE ARMOR",
                "&7Press &fSHIFT + F &7to become briefly",
                "&7invincible and impossible to hit.",
                "&7Duration: &f4 seconds",
                "&8━━━━━━━━━━━━━━━━━━━━━━━━"
        ));
        meta.addEnchant(Enchantment.SHARPNESS, 3, true);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createLeviathansFang() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        tag(meta, WeaponType.LEVIATHANS_FANG);
        meta.displayName(Msg.of("&b&lLeviathan's Fang"));
        meta.lore(Msg.lore(
                "&7",
                "&3&l🌊 LEVIATHAN TSUNAMI",
                "&7Press &fF &7to summon a real",
                "&7wall of water in front of you.",
                "&7Damage: &f0.5 hearts / hit while inside",
                "",
                "&b&l⚡ TIDAL DASH",
                "&7Press &fSHIFT + F &7to surge forward",
                "&7in the direction you're looking.",
                "&7",
                "&8Global cooldown applies per ability"
        ));
        meta.addEnchant(Enchantment.SHARPNESS, 3, true);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createVoidBlade() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        tag(meta, WeaponType.VOID_BLADE);
        meta.displayName(Msg.of("&5&lVOID BLADE"));
        meta.lore(Msg.lore(
                "&7",
                "&5&l✦ VOID SLAM",
                "&7Press &fF &7to rise on void rings,",
                "&7press again to slam your target.",
                "&7Slam damage: &c~4 hearts",
                "&7Cooldown: &f70 seconds",
                "",
                "&d&l✦ BLACK RIFT",
                "&7Press &fSHIFT + F &7on a locked target",
                "&7to unleash a storm of void slashes",
                "&7and one massive finishing strike.",
                "&7Cooldown: &f60 seconds",
                "&7",
                "&8Weapon id is PDC-based - safe to rename"
        ));
        meta.addEnchant(Enchantment.SHARPNESS, 4, true);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createEyes() {
        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = item.getItemMeta();
        tag(meta, WeaponType.EYES);
        meta.displayName(Msg.of("&d&l👁 THE EYES"));
        meta.lore(Msg.lore(
                "&7",
                "&5&l✦ HOLLOW PURPLE",
                "&7Press &fF &7to rise, merge red and",
                "&7blue energy, and fire a massive",
                "&7purple annihilation blast.",
                "&7Cooldown: &f90 seconds",
                "",
                "&c&l✦ RED BEAM",
                "&7Press &fSHIFT + F &7to fire a piercing",
                "&7red energy beam that shatters blocks.",
                "&7Damage: &f4 hearts &7| Range: &f20 blocks",
                "&7Cooldown: &f40 seconds",
                "&7",
                "&8A different kind of power entirely."
        ));
        meta.addEnchant(Enchantment.SHARPNESS, 5, true);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        item.setItemMeta(meta);
        return item;
    }

    private void tag(ItemMeta meta, WeaponType type) {
        meta.setCustomModelData(type.customModelData());
        meta.getPersistentDataContainer().set(weaponIdKey, PersistentDataType.STRING, type.id());
    }

    // ---------------------------------------------------------------
    // Giving
    // ---------------------------------------------------------------

    public void give(Player player, WeaponType type) {
        ItemStack item = create(type);
        player.getInventory().addItem(item);
        switch (type) {
            case WARDEN_CROSSBOW -> {
                player.sendMessage(Msg.of("&b❄ &lWARDEN CROSSBOW &7received!"));
                FX.play(player, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.5f, 1.2f);
            }
            case FROST_BLADE -> {
                player.sendMessage(Msg.of("&b❄ &lFROST BLADE &7received!"));
                FX.play(player, Sound.BLOCK_GLASS_BREAK, 1.2f, 1.4f);
            }
            case LEVIATHANS_FANG -> {
                player.sendMessage(Msg.of("&b&l🌊 LEVIATHAN'S FANG &7received!"));
                FX.play(player, Sound.BLOCK_CONDUIT_ACTIVATE, 1.5f, 1.2f);
            }
            case VOID_BLADE -> {
                player.sendMessage(Msg.of("&5&l✦ VOID BLADE &7received!"));
                FX.play(player, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.5f, 0.7f);
            }
            case EYES -> {
                player.sendMessage(Msg.of("&d&l👁 THE EYES &7received!"));
                FX.play(player, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 0.9f);
            }
        }
    }
}
