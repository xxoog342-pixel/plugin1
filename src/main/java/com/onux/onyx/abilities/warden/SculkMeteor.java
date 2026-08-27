package com.onux.onyx.abilities.warden;

import com.onux.onyx.abilities.Activatable;
import com.onux.onyx.abilities.Deps;
import com.onux.onyx.util.CooldownManager.Ability;
import com.onux.onyx.util.FX;
import com.onux.onyx.util.Msg;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Ability 2 - Sculk Meteor. Look directly at a player and activate: a
 * ring forms around them, then a heavy Sculk meteor falls from the sky
 * and crashes down where they were standing.
 */
public final class SculkMeteor implements Activatable {

    public static final String KEY = "sculk_meteor";

    private final Deps deps;

    public SculkMeteor(Deps deps) {
        this.deps = deps;
    }

    @Override
    public void activate(Player caster) {
        double lookRange = 40;
        Player target = deps.targeting.getTargetedPlayer(caster, lookRange);
        if (target == null) {
            caster.sendMessage(Msg.of("&c☠ &lNO TARGET! &7Look directly at a player."));
            return;
        }
        if (!deps.cooldowns.check(caster, Ability.SCULK_METEOR)) return;
        deps.cooldowns.start(caster, Ability.SCULK_METEOR);

        double damage = deps.cfgDouble(KEY, "damage", 8.0);
        int fallTicks = deps.cfgInt(KEY, "fall-time-ticks", 55);
        double fallStartHeight = deps.cfgDouble(KEY, "fall-start-height", 18);
        double impactRadius = deps.cfgDouble(KEY, "impact-radius", 3.0);
        double ringRadius = deps.cfgDouble(KEY, "ring-radius", 3.5);

        Location impactPoint = target.getLocation().clone();
        FX.ring(impactPoint, ringRadius, 32, Particle.SCULK_CHARGE_POP, 1);
        FX.play(impactPoint, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1.8f, 0.7f);
        caster.sendMessage(Msg.of("&5☠ &lSCULK METEOR! &7A meteor is falling on &f" + target.getName() + "&7."));
        target.sendMessage(Msg.of("&c☠ &lINCOMING! &7A Sculk meteor is falling on you!"));

        Location start = impactPoint.clone().add(0, fallStartHeight, 0);

        new BukkitRunnable() {
            int elapsed = 0;
            @Override
            public void run() {
                if (elapsed >= fallTicks) {
                    cancel();
                    impact(caster, impactPoint, damage, impactRadius);
                    return;
                }
                double t = (double) elapsed / fallTicks;
                Location current = FX.lerp(start, impactPoint, t);
                FX.blockCrack(current, Material.SCULK.createBlockData(), 4);
                FX.spawn(current, Particle.SMOKE, 2, 0.2, 0.2, 0.2, 0.01);
                FX.spawn(current, Particle.ASH, 2, 0.25, 0.25, 0.25, 0);
                FX.spawn(current, Particle.SCULK_SOUL, 1, 0.1, 0.1, 0.1, 0);
                elapsed++;
            }
        }.runTaskTimer(deps.plugin, 0L, 1L);
    }

    private void impact(Player caster, Location impact, double damage, double radius) {
        FX.play(impact, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.5f, 0.5f);
        FX.play(impact, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 2f, 0.5f);
        FX.play(impact, Sound.BLOCK_SCULK_CATALYST_BLOOM, 1.8f, 0.6f);
        FX.burst(impact, Particle.SCULK_CHARGE_POP, 40, 0.4);
        FX.shockwave(impact, radius + 1, 3, 30, Particle.SCULK_CHARGE_POP);

        var changes = deps.blocks.spreadFlat(impact, radius, Material.SCULK);
        deps.blocks.scheduleRevert(changes);

        for (Entity nearby : impact.getWorld().getNearbyEntities(impact, radius, radius, radius)) {
            if (!(nearby instanceof LivingEntity le) || le.isDead()) continue;
            if (nearby.equals(caster)) continue;
            if (nearby instanceof Player p && deps.trust.isTrusted(caster, p)) continue;
            le.damage(damage, caster);
        }
    }
}
