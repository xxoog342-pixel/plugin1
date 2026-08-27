package com.onux.onyx.abilities.void_;

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
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ability 1 - Void Slam. First activation launches the caster upward inside
 * four rotating void rings; a second activation while airborne slams them
 * down toward whatever they're looking at. Auto-cancels (no damage, no
 * cooldown refund) if the second press never comes.
 */
public final class VoidSlam implements Activatable {

    public static final String KEY = "void_slam";

    private final Deps deps;
    private final Set<UUID> airborne = ConcurrentHashMap.newKeySet();
    private final Map<UUID, BukkitTask> ringTasks = new ConcurrentHashMap<>();

    public VoidSlam(Deps deps) {
        this.deps = deps;
    }

    @Override
    public void activate(Player caster) {
        if (airborne.contains(caster.getUniqueId())) {
            slam(caster);
        } else {
            rise(caster);
        }
    }

    private void rise(Player caster) {
        if (!deps.cooldowns.check(caster, Ability.VOID_SLAM)) return;
        deps.cooldowns.start(caster, Ability.VOID_SLAM);

        UUID uuid = caster.getUniqueId();
        airborne.add(uuid);

        double riseHeight = deps.cfgDouble(KEY, "rise-height", 9);
        int riseTicks = deps.cfgInt(KEY, "rise-time-ticks", 40);
        int timeoutTicks = deps.cfgInt(KEY, "airborne-timeout-seconds", 8) * 20;

        double targetY = caster.getLocation().getY() + riseHeight;
        FX.play(caster, Sound.ENTITY_ENDERMAN_TELEPORT, 1.4f, 0.6f);
        caster.sendMessage(Msg.of("&5&l✦ VOID SLAM &7- press again while looking at your target to slam."));

        BukkitTask task = new BukkitRunnable() {
            int elapsed = 0;
            @Override
            public void run() {
                if (!airborne.contains(uuid) || !caster.isOnline()) {
                    cancel();
                    return;
                }
                if (elapsed >= timeoutTicks) {
                    airborne.remove(uuid);
                    cancel();
                    return;
                }
                if (elapsed < riseTicks) {
                    Vector v = caster.getVelocity();
                    caster.setVelocity(new Vector(v.getX(), 0.45, v.getZ()));
                    caster.setFallDistance(0);
                } else {
                    // Hover in place, waiting for the slam trigger.
                    Vector v = caster.getVelocity();
                    caster.setVelocity(new Vector(v.getX() * 0.6, 0.02, v.getZ() * 0.6));
                }

                Location base = caster.getLocation();
                for (int ring = 0; ring < 4; ring++) {
                    double spin = elapsed * 10 + ring * 90;
                    double yOffset = Math.min(elapsed * 0.15, riseHeight) - (ring * 0.6);
                    FX.verticalRing(base, 1.3 + ring * 0.15, 10, spin, Math.max(0, yOffset), Particle.REVERSE_PORTAL, 1);
                }
                FX.spawn(base, Particle.PORTAL, 3, 0.3, 0.1, 0.3, 0.02);
                elapsed++;
            }
        }.runTaskTimer(deps.plugin, 0L, 1L);

        ringTasks.put(uuid, task);
    }

    private void slam(Player caster) {
        UUID uuid = caster.getUniqueId();
        airborne.remove(uuid);
        BukkitTask task = ringTasks.remove(uuid);
        if (task != null) task.cancel();

        double range = deps.cfgDouble(KEY, "target-range", 20);
        Location targetLoc = deps.targeting.resolveAimLocation(caster, range);

        FX.play(caster, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 1.3f);
        FX.play(caster, Sound.ENTITY_WITHER_SHOOT, 1.2f, 1.4f);

        Location start = caster.getLocation();
        double distance = start.distance(targetLoc);
        int fallTicks = Math.max(6, (int) (distance / 2.2));

        new BukkitRunnable() {
            int elapsed = 0;
            @Override
            public void run() {
                if (!caster.isOnline() || elapsed >= fallTicks) {
                    cancel();
                    if (caster.isOnline()) {
                        caster.teleport(safeLanding(targetLoc));
                        impact(caster, targetLoc);
                    }
                    return;
                }
                double t = (double) elapsed / fallTicks;
                Location current = FX.lerp(start, targetLoc, t);
                caster.teleport(current);
                caster.setFallDistance(0);
                FX.spawn(current, Particle.REVERSE_PORTAL, 4, 0.15, 0.15, 0.15, 0.02);
                FX.spawn(current, Particle.SMOKE, 2, 0.15, 0.15, 0.15, 0.01);
                elapsed++;
            }
        }.runTaskTimer(deps.plugin, 0L, 1L);
    }

    private Location safeLanding(Location target) {
        Location loc = target.clone();
        for (int i = 0; i < 30; i++) {
            if (loc.getBlock().getRelative(0, -1, 0).getType().isSolid()) break;
            loc.subtract(0, 1, 0);
        }
        return loc;
    }

    private void impact(Player caster, Location impact) {
        double damage = deps.cfgDouble(KEY, "slam-damage", 8.0);
        double radius = deps.cfgInt(KEY, "destruction-radius", 3);
        double knockback = deps.cfgDouble(KEY, "knockback", 1.4);

        FX.play(impact, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.6f);
        FX.play(impact, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5f, 0.4f);
        FX.burst(impact, Particle.REVERSE_PORTAL, 50, 0.5);
        FX.burst(impact, Particle.SMOKE, 30, 0.4);
        FX.shockwave(impact, radius + 1.5, 3, 30, Particle.PORTAL);

        var changes = deps.blocks.clearSphere(impact, Math.min(radius, 3.5));
        deps.blocks.scheduleRevert(changes);

        for (Entity nearby : impact.getWorld().getNearbyEntities(impact, radius + 1, radius + 1, radius + 1)) {
            if (!(nearby instanceof LivingEntity target) || target.isDead()) continue;
            if (nearby.equals(caster)) continue;
            if (nearby instanceof Player p && deps.trust.isTrusted(caster, p)) continue;

            target.damage(damage, caster);
            Vector push = target.getLocation().toVector().subtract(impact.toVector());
            if (push.lengthSquared() > 0) {
                push.normalize().multiply(knockback).setY(0.4);
                target.setVelocity(target.getVelocity().add(push));
            }
        }
    }
}
