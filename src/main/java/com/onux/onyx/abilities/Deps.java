package com.onux.onyx.abilities;

import com.onux.onyx.util.BlockRestorer;
import com.onux.onyx.util.CooldownManager;
import com.onux.onyx.util.Targeting;
import com.onux.onyx.util.TrustManager;
import com.onux.onyx.weapons.WeaponFactory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

/** Bundles everything an ability needs so ability classes take one constructor argument instead of seven. */
public final class Deps {

    public final Plugin plugin;
    public final WeaponFactory weapons;
    public final CooldownManager cooldowns;
    public final Targeting targeting;
    public final TrustManager trust;
    public final BlockRestorer blocks;
    public final FileConfiguration config;

    public Deps(Plugin plugin, WeaponFactory weapons, CooldownManager cooldowns, Targeting targeting,
                TrustManager trust, BlockRestorer blocks, FileConfiguration config) {
        this.plugin = plugin;
        this.weapons = weapons;
        this.cooldowns = cooldowns;
        this.targeting = targeting;
        this.trust = trust;
        this.blocks = blocks;
        this.config = config;
    }

    public double cfgDouble(String abilityKey, String field, double def) {
        return config.getDouble("abilities." + abilityKey + "." + field, def);
    }

    public int cfgInt(String abilityKey, String field, int def) {
        return config.getInt("abilities." + abilityKey + "." + field, def);
    }

    public boolean cfgBool(String abilityKey, String field, boolean def) {
        return config.getBoolean("abilities." + abilityKey + "." + field, def);
    }
}
