package com.onux.onyx.abilities.leviathan;

import com.onux.onyx.abilities.Activatable;
import com.onux.onyx.abilities.Deps;
import com.onux.onyx.util.BlockRestorer;
import com.onux.onyx.util.CooldownManager.Ability;
import com.onux.onyx.util.FX;
import com.onux.onyx.util.Msg;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Ability 1 - Leviathan Tsunami. A real, moving wall of water (placed and
 * quickly reverted a few blocks behind the front, so it never permanently
 * floods anything) that carries and repeatedly ticks players caught inside.
 */
public final class LeviathanTsunami implements Activatable {

    public static final String KEY = "leviathan_tsunami";

    private final Deps deps;

    public LeviathanTsunami(Deps deps) {
        this.deps = deps;
    }

    @Override
    public void activate(Player caster) {
        if (!deps.cooldowns.check(caster, Ability.LEVIATHAN_TSUNAMI)) return;
        deps.cooldowns.start(caster, Ability.LEVIATHAN_TSUNAMI);

        double damagePerHit = deps.cfgDouble(KEY, "damage-per-hit", 1.0);
        int hitIntervalTicks = deps.cfgInt(KEY, "hit-interval-ticks", 5);
        double waveLength = deps.cfgDouble(KEY, "wave-length", 12);
        double waveSpeed = deps.cfgDouble(KEY, "wave-speed-blocks-per-tick", 1.0);
        int waveWidth = deps.cfgInt(KEY, "wave-width", 5);
        double knockback = deps.cfgDouble(KEY, "knockback", 1.1);
        boolean useRealBlocks = deps.cfgBool(KEY, "use-real-water-blocks", true);

        Vector direction = caster.getLocation().getDirection().setY(0).normalize();
        Location origin = caster.getLocation();

        FX.play(caster, Sound.ENTITY_PLAYER_SPLASH, 2.2f, 0.55f);
        FX.play(caster, Sound.BLOCK_CONDUIT_AMBIENT, 2f, 0.55f);
        caster.sendMessage(Msg.of("&3&l🌊 LEVIATHAN TSUNAMI! &7A wall of water surges forward."));

        Map<UUID, Long> lastHit = new HashMap<>();

        new BukkitRunnable() {
            double traveled = 0;
            @Override
            public void run() {
                if (traveled >= waveLength) {
                    Location end = origin.clone().add(direction.clone().multiply(waveLength));
                    FX.play(end, Sound.ENTITY_GENERIC_SPLASH, 3f, 0.5f);
                    FX.burst(end, Particle.SPLASH, 60, 0.8);
                    cancel();
                    return;
                }
                Location front = origin.clone().add(direction.clone().multiply(traveled));
                if (useRealBlocks) placeWall(front, direction, waveWidth);
                drawWaveParticles(front, direction, waveWidth);
                hitEntitiesAtWave(caster, front, direction, damagePerHit, hitIntervalTicks, knockback, lastHit);
                traveled += waveSpeed;
            }
        }.runTaskTimer(deps.plugin, 0L, 1L);
    }

    private void placeWall(Location front, Vector direction, int width) {
        Vector side = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        java.util.List<BlockRestorer.Change> changes = new java.util.ArrayList<>();
        for (int w = -width / 2; w <= width / 2; w++) {
            for (int h = 0; h <= 2; h++) {
                Location point = front.clone().add(side.clone().multiply(w)).add(0, h, 0);
                Block block = point.getBlock();
                if (block.getType() != Material.AIR) continue;
                BlockRestorer.Change change = deps.blocks.setIfAllowed(block, Material.WATER);
                if (change != null) changes.add(change);
            }
        }
        // Revert this specific slice quickly so the wall reads as moving water, not a flood.
        deps.blocks.scheduleRevert(changes, 1);
    }

    private void drawWaveParticles(Location front, Vector direction, int width) {
        Vector side = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        for (double w = -width / 2.0; w <= width / 2.0; w += 0.6) {
            for (double h = 0; h <= 2.5; h += 0.5) {
                Location point = front.clone().add(side.clone().multiply(w)).add(0, h, 0);
                FX.spawn(point, Particle.SPLASH, 2, 0.1, 0.1, 0.1, 0);
                if (h < 1.0) FX.spawn(point, Particle.BUBBLE_POP, 1, 0.1, 0.05, 0.1, 0);
                if (Math.random() < 0.15) FX.spawn(point, Particle.FALLING_WATER, 1, 0.1, 0.1, 0.1, 0);
            }
        }
        FX.spawn(front.clone().add(0, 1.2, 0), Particle.CLOUD, 3, width * 0.3, 0.6, width * 0.3, 0.01);
    }

    private void hitEntitiesAtWave(Player caster, Location front, Vector direction, double damage, int intervalTicks, double knockback, Map<UUID, Long> lastHit) {
        for (Entity nearby : front.getWorld().getNearbyEntities(front, 2.5, 2.5, 2.5)) {
            if (!(nearby instanceof LivingEntity target) || target.isDead()) continue;
            if (nearby.equals(caster)) continue;
            if (nearby instanceof Player p && deps.trust.isTrusted(caster, p)) continue;

            long now = target.getWorld().getFullTime();
            Long last = lastHit.get(nearby.getUniqueId());
            if (last != null && now - last < intervalTicks) continue;
            lastHit.put(nearby.getUniqueId(), now);

            target.damage(damage, caster);
            Vector push = direction.clone().multiply(knockback).setY(0.25);
            target.setVelocity(target.getVelocity().add(push));
        }
    }
}
