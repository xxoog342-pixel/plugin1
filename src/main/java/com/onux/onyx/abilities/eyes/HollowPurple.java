package com.onux.onyx.abilities.eyes;

import com.onux.onyx.abilities.Activatable;
import com.onux.onyx.abilities.Deps;
import com.onux.onyx.util.CooldownManager.Ability;
import com.onux.onyx.util.FX;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Ability 1 - Hollow Purple. Rises the caster into the air, forms a red
 * sphere and a blue sphere that spiral toward each other, merges them into
 * one large purple sphere, then fires it as a single devastating
 * projectile toward wherever the caster is looking at the moment the
 * merge completes.
 */
public final class HollowPurple implements Activatable {

    public static final String KEY = "hollow_purple";

    private static final Color RED = Color.fromRGB(220, 30, 30);
    private static final Color BLUE = Color.fromRGB(40, 90, 230);
    private static final Color PURPLE = Color.fromRGB(160, 30, 220);

    private final Deps deps;

    public HollowPurple(Deps deps) {
        this.deps = deps;
    }

    @Override
    public void activate(Player caster) {
        if (!deps.cooldowns.check(caster, Ability.HOLLOW_PURPLE)) return;
        deps.cooldowns.start(caster, Ability.HOLLOW_PURPLE);

        double riseHeight = deps.cfgDouble(KEY, "rise-height", 7);
        int chargeTicks = deps.cfgInt(KEY, "charge-time-ticks", 60);

        double targetY = caster.getLocation().getY() + riseHeight;
        FX.play(caster, Sound.BLOCK_BEACON_POWER_SELECT, 1.5f, 0.7f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!caster.isOnline()) {
                    cancel();
                    return;
                }
                if (caster.getLocation().getY() >= targetY) {
                    cancel();
                    charge(caster, chargeTicks);
                    return;
                }
                Vector v = caster.getVelocity();
                caster.setVelocity(new Vector(v.getX(), 0.4, v.getZ()));
                caster.setFallDistance(0);
            }
        }.runTaskTimer(deps.plugin, 0L, 1L);
    }

    private void charge(Player caster, int chargeTicks) {
        FX.play(caster, Sound.BLOCK_BEACON_ACTIVATE, 1.6f, 0.6f);

        new BukkitRunnable() {
            int elapsed = 0;
            @Override
            public void run() {
                if (!caster.isOnline()) {
                    cancel();
                    return;
                }
                Vector v = caster.getVelocity();
                caster.setVelocity(new Vector(v.getX() * 0.5, 0.02, v.getZ() * 0.5));

                double progress = (double) elapsed / chargeTicks;
                Location origin = caster.getLocation().add(0, 1.2, 0);
                double separation = 2.2 * (1 - progress);

                Vector side = sideVector(caster);
                Location redLoc = origin.clone().add(side.clone().multiply(separation));
                Location blueLoc = origin.clone().add(side.clone().multiply(-separation));

                FX.dust(redLoc, RED, 1.4f, 4);
                FX.spawn(redLoc, Particle.SOUL_FIRE_FLAME, 2, 0.15, 0.15, 0.15, 0.01);
                FX.dust(blueLoc, BLUE, 1.4f, 4);
                FX.spawn(blueLoc, Particle.SOUL_FIRE_FLAME, 2, 0.15, 0.15, 0.15, 0.01);

                if (elapsed % 8 == 0) {
                    FX.play(origin, Sound.BLOCK_CONDUIT_AMBIENT, 0.6f, 1f + (float) progress);
                }

                if (elapsed >= chargeTicks) {
                    cancel();
                    fire(caster, origin);
                    return;
                }
                elapsed++;
            }
        }.runTaskTimer(deps.plugin, 0L, 1L);
    }

    private Vector sideVector(Player caster) {
        Vector dir = caster.getEyeLocation().getDirection().setY(0).normalize();
        return new Vector(-dir.getZ(), 0, dir.getX()).normalize();
    }

    private void fire(Player caster, Location origin) {
        FX.play(origin, Sound.ENTITY_GENERIC_EXPLODE, 1.8f, 0.5f);
        FX.play(origin, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.6f, 0.5f);
        FX.dust(origin, PURPLE, 2.5f, 20);
        FX.burst(origin, Particle.WITCH, 25, 0.4);

        double damage = deps.cfgDouble(KEY, "damage", 10.0);
        double range = deps.cfgDouble(KEY, "range", 40);
        double speed = deps.cfgDouble(KEY, "projectile-speed", 2.2);
        double explosionRadius = deps.cfgDouble(KEY, "explosion-radius", 3.5);

        Vector direction = caster.getEyeLocation().getDirection();
        Location start = origin.clone();

        new BukkitRunnable() {
            double traveled = 0;
            @Override
            public void run() {
                if (traveled >= range) {
                    cancel();
                    return;
                }
                Location current = start.clone().add(direction.clone().multiply(traveled));
                FX.dust(current, PURPLE, 2.0f, 6);
                FX.spawn(current, Particle.WITCH, 4, 0.2, 0.2, 0.2, 0.01);
                FX.spawn(current, Particle.END_ROD, 2, 0.1, 0.1, 0.1, 0.01);

                for (Entity nearby : current.getWorld().getNearbyEntities(current, 1.3, 1.3, 1.3)) {
                    if (!(nearby instanceof LivingEntity target) || target.isDead() || nearby.equals(caster)) continue;
                    if (nearby instanceof Player p && deps.trust.isTrusted(caster, p)) continue;
                    cancel();
                    impact(caster, current, damage, explosionRadius);
                    return;
                }
                if (!current.getBlock().getType().isAir() && current.getBlock().getType().isSolid()) {
                    cancel();
                    impact(caster, current, damage, explosionRadius);
                    return;
                }

                traveled += speed;
            }
        }.runTaskTimer(deps.plugin, 0L, 1L);
    }

    private void impact(Player caster, Location impact, double damage, double radius) {
        FX.play(impact, Sound.ENTITY_GENERIC_EXPLODE, 2.5f, 0.4f);
        FX.dust(impact, PURPLE, 3.0f, 40);
        FX.burst(impact, Particle.WITCH, 40, 0.6);
        FX.shockwave(impact, radius, 3, 30, Particle.WITCH);

        for (Entity nearby : impact.getWorld().getNearbyEntities(impact, radius, radius, radius)) {
            if (!(nearby instanceof LivingEntity target) || target.isDead() || nearby.equals(caster)) continue;
            if (nearby instanceof Player p && deps.trust.isTrusted(caster, p)) continue;
            target.damage(damage, caster);
        }
    }
}
