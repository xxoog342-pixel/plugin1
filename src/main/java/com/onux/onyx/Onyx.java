package com.onux.onyx;

import com.onux.onyx.abilities.Activatable;
import com.onux.onyx.abilities.Deps;
import com.onux.onyx.abilities.eyes.HollowPurple;
import com.onux.onyx.abilities.eyes.RedBeam;
import com.onux.onyx.abilities.frost.FrozenBarrage;
import com.onux.onyx.abilities.frost.IceArmor;
import com.onux.onyx.abilities.leviathan.LeviathanTsunami;
import com.onux.onyx.abilities.leviathan.TidalDash;
import com.onux.onyx.abilities.void_.BlackRift;
import com.onux.onyx.abilities.void_.VoidSlam;
import com.onux.onyx.abilities.warden.SculkMeteor;
import com.onux.onyx.abilities.warden.WardenBeam;
import com.onux.onyx.abilities.warden.WardensGaze;
import com.onux.onyx.commands.AbilityCommand;
import com.onux.onyx.commands.CooldownCommand;
import com.onux.onyx.commands.GiveWeaponCommand;
import com.onux.onyx.commands.OnuxMenuCommand;
import com.onux.onyx.commands.TrustCommand;
import com.onux.onyx.commands.TwoAbilityCommand;
import com.onux.onyx.commands.WardenCommand;
import com.onux.onyx.gui.ArsenalMenu;
import com.onux.onyx.listeners.MenuListener;
import com.onux.onyx.listeners.PlayerCleanupListener;
import com.onux.onyx.listeners.SwapHandListener;
import com.onux.onyx.listeners.WardenChargeListener;
import com.onux.onyx.util.BlockRestorer;
import com.onux.onyx.util.CooldownManager;
import com.onux.onyx.util.Targeting;
import com.onux.onyx.util.TrustManager;
import com.onux.onyx.weapons.WeaponFactory;
import com.onux.onyx.weapons.WeaponType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;

public final class Onyx extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        WeaponFactory weapons = new WeaponFactory(this);
        Targeting targeting = new Targeting();
        TrustManager trust = new TrustManager();
        CooldownManager cooldowns = new CooldownManager(this, getConfig());
        BlockRestorer blocks = new BlockRestorer(
                this,
                getConfig().getStringList("world-safety.protected-blocks"),
                getConfig().getInt("world-safety.revert-terrain-after-seconds", 25)
        );

        Deps deps = new Deps(this, weapons, cooldowns, targeting, trust, blocks, getConfig());

        // ---------------------------------------------------------------
        // Abilities
        // ---------------------------------------------------------------
        WardenBeam wardenBeam = new WardenBeam(deps);
        SculkMeteor sculkMeteor = new SculkMeteor(deps);
        new WardensGaze(deps); // self-registers as a listener

        FrozenBarrage frozenBarrage = new FrozenBarrage(deps);
        IceArmor iceArmor = new IceArmor(deps); // self-registers as a listener

        LeviathanTsunami leviathanTsunami = new LeviathanTsunami(deps);
        TidalDash tidalDash = new TidalDash(deps);

        VoidSlam voidSlam = new VoidSlam(deps);
        BlackRift blackRift = new BlackRift(deps);

        HollowPurple hollowPurple = new HollowPurple(deps);
        RedBeam redBeam = new RedBeam(deps);

        // ---------------------------------------------------------------
        // F / Shift+F registry for SwapHandListener
        // (Warden Crossbow's primary ability, Warden Beam, is intentionally
        // absent here - it's triggered by WardenChargeListener via
        // Shift+right-click instead, so plain-F stays free for Sculk Meteor.)
        // ---------------------------------------------------------------
        Map<WeaponType, Activatable> primary = new EnumMap<>(WeaponType.class);
        Map<WeaponType, Activatable> secondary = new EnumMap<>(WeaponType.class);

        primary.put(WeaponType.WARDEN_CROSSBOW, sculkMeteor);

        primary.put(WeaponType.FROST_BLADE, frozenBarrage);
        secondary.put(WeaponType.FROST_BLADE, iceArmor);

        primary.put(WeaponType.LEVIATHANS_FANG, leviathanTsunami);
        secondary.put(WeaponType.LEVIATHANS_FANG, tidalDash);

        primary.put(WeaponType.VOID_BLADE, voidSlam);
        secondary.put(WeaponType.VOID_BLADE, blackRift);

        primary.put(WeaponType.EYES, hollowPurple);
        secondary.put(WeaponType.EYES, redBeam);

        // ---------------------------------------------------------------
        // Listeners
        // ---------------------------------------------------------------
        ArsenalMenu menu = new ArsenalMenu(weapons);

        getServer().getPluginManager().registerEvents(new SwapHandListener(weapons, primary, secondary), this);
        getServer().getPluginManager().registerEvents(new WardenChargeListener(weapons, wardenBeam), this);
        getServer().getPluginManager().registerEvents(new MenuListener(menu, weapons), this);
        getServer().getPluginManager().registerEvents(new PlayerCleanupListener(cooldowns, trust), this);

        // ---------------------------------------------------------------
        // Commands
        // ---------------------------------------------------------------
        getCommand("ability").setExecutor(new AbilityCommand(weapons, wardenBeam, sculkMeteor));
        getCommand("waterability").setExecutor(new TwoAbilityCommand(weapons, WeaponType.LEVIATHANS_FANG, "Leviathan's Fang", leviathanTsunami, tidalDash));
        getCommand("frostability").setExecutor(new TwoAbilityCommand(weapons, WeaponType.FROST_BLADE, "Frost Blade", frozenBarrage, iceArmor));
        getCommand("voidability").setExecutor(new TwoAbilityCommand(weapons, WeaponType.VOID_BLADE, "Void Blade", voidSlam, blackRift));
        getCommand("eyesability").setExecutor(new TwoAbilityCommand(weapons, WeaponType.EYES, "The Eyes", hollowPurple, redBeam));

        getCommand("warden").setExecutor(new WardenCommand(weapons));
        getCommand("voidreaver").setExecutor(new GiveWeaponCommand(weapons, WeaponType.VOID_BLADE));
        getCommand("frostblade").setExecutor(new GiveWeaponCommand(weapons, WeaponType.FROST_BLADE));
        getCommand("leviathan").setExecutor(new GiveWeaponCommand(weapons, WeaponType.LEVIATHANS_FANG));
        getCommand("eyes").setExecutor(new GiveWeaponCommand(weapons, WeaponType.EYES));
        // Legacy command name kept from the previous build - it has always given the Warden Crossbow.
        getCommand("frostgui").setExecutor(new GiveWeaponCommand(weapons, WeaponType.WARDEN_CROSSBOW));

        getCommand("onuxmenu").setExecutor(new OnuxMenuCommand(menu));
        getCommand("trust").setExecutor(new TrustCommand(trust));
        getCommand("cooldown").setExecutor(new CooldownCommand(cooldowns));

        getLogger().info("Onyx enabled - Warden Crossbow, Frost Blade, Leviathan's Fang, Void Blade, The Eyes.");
    }
}
