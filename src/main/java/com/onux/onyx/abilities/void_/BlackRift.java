package com.onux.onyx.abilities.void_;

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

import java.util.concurrent.ThreadLocalRandom;

/**
 * Ability 2 - Black Rift. Locks onto the targeted player, surrounds them
 * with a storm of rapid black-energy slashes from randomized directions
 * (left/right/above/below/behind/diagonal), then - after a brief pause -
 * lands one massive finishing slash.
 */
public final class BlackRift implements Activatable {

    public static final String KEY = "black_rift";

    /** Unit offsets the rapid slashes are drawn from, relative to the target. */
    private static final Vector[] DIRECTIONS = {
            new Vector(1, 0, 0), new Vector(-1, 0, 0),
            new Vector(0, 1, 0), new Vector(0, -1, 0),
            new Vector(0, 0, 1), new Vector(0, 0, -1),
            new Vector(1, 0, 1).normalize(), new Vector(-1, 0, -1).normalize(),
            new Vector(1, 0, -1).normalize(), new Vector(-1, 0, 1).normalize()
    };

    private final Deps deps;

    public BlackRift(Deps deps) {
        this.deps = deps;
    }

    @Override
    public void activate(Player caster) {
        double range = deps.cfgInt(KEY, "target-range", 20);
        Player target = deps.targeting.getTargetedPlayer(caster, range);
        if (target == null) {
            caster.sendMessage(Msg.of("&5✦ &lNO TARGET! &7Look directly at a player."));
            return;
        }
        if (!deps.cooldowns.check(caster, Ability.BLACK_RIFT)) return;
        deps.cooldowns.start(caster, Ability.BLACK_RIFT);

        int slashCount = deps.cfgInt(KEY, "slash-count", 7);
        double slashDamage = deps.cfgDouble(KEY, "slash-damage", 1.0);
        int slashInterval = deps.cfgInt(KEY, "slash-interval-ticks", 3);
        double finalDamage = deps.cfgDouble(KEY, "final-strike-damage", 8.0);
        int finalDelay = deps.cfgInt(KEY, "final-strike-delay-ticks", 12);

        FX.play(caster, Sound.ENTITY_ENDERMAN_TELEPORT, 1.6f, 0.5f);
        FX.burst(target.getLocation().add(0, 1, 0), Particle.REVERSE_PORTAL, 30, 0.4);
        caster.sendMessage(Msg.of("&5&l✦ BLACK RIFT! &7" + target.getName() + " is trapped."));

        runSlashes(caster, target, slashCount, slashDamage, slashInterval, () ->
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        finalStrike(caster, target, finalDamage);
                    }
                }.runTaskLater(deps.plugin, finalDelay));
    }

    private void runSlashes(Player caster, Player target, int count, double damage, int interval, Runnable onComplete) {
        new BukkitRunnable() {
            int done = 0;
            @Override
            public void run() {
                if (done >= count || !target.isOnline()) {
                    cancel();
                    if (target.isOnline()) onComplete.run();
                    return;
                }
                slash(caster, target, damage);
                done++;
            }
        }.runTaskTimer(deps.plugin, 0L, interval);
    }

    private void slash(Player caster, Player target, double damage) {
        Vector dir = DIRECTIONS[ThreadLocalRandom.current().nextInt(DIRECTIONS.length)];
        Location center = target.getLocation().add(0, 1, 0);
        Location origin = center.clone().add(dir.clone().multiply(2.2));

        FX.play(origin, Sound.ENTITY_ENDER_DRAGON_HURT, 0.7f, 1.8f);
        FX.slash(center, dir.clone().multiply(-1), 1.6, 0.5, Particle.REVERSE_PORTAL, 10);
        FX.spawn(origin, Particle.SOUL_FIRE_FLAME, 4, 0.1, 0.1, 0.1, 0.01);
        FX.spawn(center, Particle.SMOKE, 3, 0.2, 0.2, 0.2, 0.01);

        if (deps.trust.isTrusted(caster, target)) return;
        target.damage(damage, caster);
        target.setVelocity(target.getVelocity().add(dir.clone().multiply(-1).multiply(0.15)));
    }

    private void finalStrike(Player caster, Player target, double damage) {
        Location center = target.getLocation().add(0, 1, 0);

        FX.play(center, Sound.ENTITY_WITHER_SHOOT, 2f, 0.4f);
        FX.play(center, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.5f);
        FX.slash(center, caster.getEyeLocation().getDirection(), 3.5, 1.2, Particle.REVERSE_PORTAL, 30);
        FX.burst(center, Particle.REVERSE_PORTAL, 60, 0.6);
        FX.shockwave(center, 3.5, 3, 30, Particle.PORTAL);

        if (!deps.trust.isTrusted(caster, target)) {
            target.damage(damage, caster);
            Vector push = target.getLocation().toVector().subtract(caster.getLocation().toVector());
            if (push.lengthSquared() > 0) {
                target.setVelocity(target.getVelocity().add(push.normalize().multiply(1.3).setY(0.35)));
            }
        }
    }
}
