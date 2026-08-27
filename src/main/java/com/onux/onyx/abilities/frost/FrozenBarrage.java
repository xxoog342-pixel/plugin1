package com.onux.onyx.abilities.frost;

import com.onux.onyx.abilities.Activatable;
import com.onux.onyx.abilities.Deps;
import com.onux.onyx.util.CooldownManager.Ability;
import com.onux.onyx.util.FX;
import com.onux.onyx.util.Msg;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Ability 1 - Frozen Barrage. Briefly levitates the caster, plays a short
 * charge-up, then launches up to {@code projectile-count} homing ice
 * projectiles at nearby players.
 */
public final class FrozenBarrage implements Activatable {

    public static final String KEY = "frozen_barrage";

    private final Deps deps;

    public FrozenBarrage(Deps deps) {
        this.deps = deps;
    }

    @Override
    public void activate(Player caster) {
        if (!deps.cooldowns.check(caster, Ability.FROZEN_BARRAGE)) return;
        deps.cooldowns.start(caster, Ability.FROZEN_BARRAGE);

        int chargeTicks = deps.cfgInt(KEY, "charge-time-ticks", 20);
        int count = deps.cfgInt(KEY, "projectile-count", 4);
        double searchRadius = deps.cfgInt(KEY, "target-search-radius", 15);

        // Rise slightly into the air.
        caster.setVelocity(new Vector(0, 0.5, 0));
        FX.play(caster, Sound.BLOCK_POWDER_SNOW_HIT, 1.2f, 0.8f);

        new BukkitRunnable() {
            int elapsed = 0;
            @Override
            public void run() {
                if (elapsed >= chargeTicks) {
                    cancel();
                    launch(caster, count, searchRadius);
                    return;
                }
                Location loc = caster.getLocation().add(0, 1, 0);
                FX.ring(loc, 0.8, 10, elapsed * 20, Particle.SNOWFLAKE, 1);
                FX.spawn(loc, Particle.CLOUD, 2, 0.4, 0.4, 0.4, 0.01);
                if (elapsed % 4 == 0) {
                    FX.play(caster, Sound.BLOCK_SNOW_STEP, 0.8f, 1.4f);
                }
                elapsed++;
            }
        }.runTaskTimer(deps.plugin, 0L, 1L);
    }

    private void launch(Player caster, int count, double searchRadius) {
        FX.play(caster, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.4f, 1.6f);
        List<Player> targets = deps.targeting.nearbyPlayers(caster.getLocation(), searchRadius, caster, count);
        if (targets.isEmpty()) {
            caster.sendMessage(Msg.of("&b❄ &7No nearby players to target."));
            return;
        }

        double damage = deps.cfgDouble(KEY, "projectile-damage", 4.0);
        double speed = deps.cfgDouble(KEY, "projectile-speed", 1.6);

        for (Player target : targets) {
            fireProjectile(caster, target, damage, speed);
        }
    }

    private void fireProjectile(Player caster, Player target, double damage, double speed) {
        Location start = caster.getEyeLocation();
        Location destination = target.getLocation().add(0, 1, 0);
        double distance = start.distance(destination);
        int ticks = Math.max(4, (int) (distance / speed));

        new BukkitRunnable() {
            int elapsed = 0;
            @Override
            public void run() {
                if (!target.isOnline() || elapsed >= ticks) {
                    cancel();
                    if (target.isOnline() && elapsed >= ticks) {
                        impact(caster, target, damage);
                    }
                    return;
                }
                double t = (double) elapsed / ticks;
                Location current = FX.lerp(start, target.getLocation().add(0, 1, 0), t);
                FX.spawn(current, Particle.SNOWFLAKE, 3, 0.05, 0.05, 0.05, 0);
                FX.spawn(current, Particle.CLOUD, 1, 0.05, 0.05, 0.05, 0.01);
                if (elapsed % 3 == 0) FX.spawn(current, Particle.ITEM_SNOWBALL, 1, 0.05, 0.05, 0.05, 0);
                elapsed++;
            }
        }.runTaskTimer(deps.plugin, 0L, 1L);
    }

    private void impact(Player caster, Player target, double damage) {
        Location loc = target.getLocation().add(0, 1, 0);
        FX.play(loc, Sound.BLOCK_GLASS_BREAK, 1.3f, 1.2f);
        FX.burst(loc, Particle.SNOWFLAKE, 15, 0.3);
        FX.burst(loc, Particle.ITEM_SNOWBALL, 8, 0.25);

        if (deps.trust.isTrusted(caster, target)) return;
        target.damage(damage, caster);
    }
}
