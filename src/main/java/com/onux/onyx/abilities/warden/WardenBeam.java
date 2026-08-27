package com.onux.onyx.abilities.warden;

import com.onux.onyx.abilities.Activatable;
import com.onux.onyx.abilities.Deps;
import com.onux.onyx.util.CooldownManager.Ability;
import com.onux.onyx.util.FX;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ability 1 - Warden Beam.
 * Hold Shift + right-click while holding the Warden Crossbow to charge;
 * releasing Shift fires a Warden sonic beam. The actual start/stop of the
 * charge is driven by {@code WardenChargeListener}; this class owns the
 * charge state machine and the beam itself. Also implements
 * {@link Activatable} so {@code /ability 1} can trigger a full-power beam
 * without needing to physically hold shift.
 */
public final class WardenBeam implements Activatable {

    public static final String KEY = "warden_beam";

    private final Deps deps;
    private final Map<UUID, BukkitTask> chargeTasks = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> chargeTicks = new ConcurrentHashMap<>();

    public WardenBeam(Deps deps) {
        this.deps = deps;
    }

    public boolean isCharging(Player player) {
        return chargeTasks.containsKey(player.getUniqueId());
    }

    /** Command-triggered path: simulates a full charge-and-release without needing to physically hold shift. */
    @Override
    public void activate(Player player) {
        if (isCharging(player)) return;
        if (!deps.cooldowns.check(player, Ability.WARDEN_BEAM)) return;
        int maxTicks = deps.cfgInt(KEY, "charge-time-ticks", 30);
        beginCharge(player);
        deps.plugin.getServer().getScheduler().runTaskLater(deps.plugin, () -> {
            if (isCharging(player)) release(player);
        }, maxTicks + 1L);
    }

    public void beginCharge(Player player) {
        UUID uuid = player.getUniqueId();
        if (chargeTasks.containsKey(uuid)) return;

        if (!deps.cooldowns.check(player, Ability.WARDEN_BEAM)) return;

        int maxTicks = deps.cfgInt(KEY, "charge-time-ticks", 30);
        chargeTicks.put(uuid, 0);

        FX.play(player, Sound.ENTITY_WARDEN_HEARTBEAT, 1.2f, 0.6f);

        BukkitTask task = deps.plugin.getServer().getScheduler().runTaskTimer(deps.plugin, () -> {
            int ticks = chargeTicks.merge(uuid, 1, Integer::sum);
            Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(0.6));
            FX.spawn(loc, Particle.SCULK_SOUL, 1, 0.08, 0.08, 0.08, 0);
            FX.spawn(loc, Particle.ELECTRIC_SPARK, 1, 0.1, 0.1, 0.1, 0.01);
            if (ticks % 5 == 0) {
                FX.play(player, Sound.BLOCK_SCULK_SENSOR_CLICKING_STOP, 0.8f, 1.0f + (ticks / (float) maxTicks));
            }
            if (ticks >= maxTicks) {
                FX.burst(loc, Particle.SCULK_CHARGE_POP, 6, 0.15);
            }
        }, 0L, 1L);

        chargeTasks.put(uuid, task);
    }

    public void release(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitTask task = chargeTasks.remove(uuid);
        int ticks = chargeTicks.getOrDefault(uuid, 0);
        chargeTicks.remove(uuid);
        if (task == null) return;
        task.cancel();

        int minTicks = deps.cfgInt(KEY, "min-charge-ticks", 6);
        if (ticks < minTicks) {
            FX.play(player, Sound.BLOCK_SCULK_SENSOR_CLICKING_STOP, 1f, 0.5f);
            return;
        }
        if (!deps.cooldowns.check(player, Ability.WARDEN_BEAM)) return;

        fire(player);
    }

    private void fire(Player caster) {
        deps.cooldowns.start(caster, Ability.WARDEN_BEAM);

        double damage = deps.cfgDouble(KEY, "damage", 6.0);
        double range = deps.cfgDouble(KEY, "range", 25);
        double knockback = deps.cfgDouble(KEY, "knockback", 1.6);

        Location start = caster.getEyeLocation();
        Vector direction = start.getDirection();
        Location end = start.clone().add(direction.clone().multiply(range));

        FX.play(caster, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.6f, 1.0f);
        FX.play(caster, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.2f, 1.3f);

        // Beam core + swirling sculk/sonic particles along the line.
        FX.line(start, end, 40, 0, Particle.END_ROD, 1);
        FX.line(start, end, 40, 0.15, Particle.SCULK_SOUL, 1);
        FX.line(start, end, 20, 0.25, Particle.SONIC_BOOM, 0);

        RayTraceResult trace = caster.getWorld().rayTraceEntities(start, direction, range, 0.7,
                e -> e instanceof LivingEntity && !e.equals(caster));

        double actualRange = trace != null && trace.getHitEntity() != null
                ? start.distance(trace.getHitEntity().getLocation())
                : range;
        Location impact = start.clone().add(direction.clone().multiply(actualRange));
        FX.burst(impact, Particle.SCULK_CHARGE_POP, 25, 0.35);
        FX.play(impact, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1.8f, 0.6f);

        if (trace != null && trace.getHitEntity() instanceof LivingEntity target && !target.isDead()) {
            if (!(target instanceof Player p && deps.trust.isTrusted(caster, p))) {
                target.damage(damage, caster);
                Vector push = direction.clone().multiply(knockback).setY(0.3);
                target.setVelocity(target.getVelocity().add(push));
            }
        }
    }

    public void cancelChargeSilently(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitTask task = chargeTasks.remove(uuid);
        chargeTicks.remove(uuid);
        if (task != null) task.cancel();
    }
}
