package com.onux.onyx.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Every particle/sound call in the plugin goes through here. Ability code
 * reads as "FX.ring(...)" / "FX.play(...)" instead of raw World calls
 * scattered across eleven ability classes, and if a Particle/Sound enum
 * constant is ever renamed upstream there's exactly one file to fix.
 */
public final class FX {

    private FX() {}

    // ---------------------------------------------------------------
    // Sound
    // ---------------------------------------------------------------

    public static void play(Location loc, Sound sound, float volume, float pitch) {
        World world = loc.getWorld();
        if (world == null) return;
        world.playSound(loc, sound, volume, pitch);
    }

    public static void play(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    // ---------------------------------------------------------------
    // Basic particle spawns
    // ---------------------------------------------------------------

    public static void spawn(Location loc, Particle particle, int count) {
        spawn(loc, particle, count, 0, 0, 0, 0);
    }

    public static void spawn(Location loc, Particle particle, int count, double ox, double oy, double oz, double extra) {
        World world = loc.getWorld();
        if (world == null) return;
        world.spawnParticle(particle, loc, count, ox, oy, oz, extra);
    }

    public static <T> void spawnData(Location loc, Particle particle, int count, double ox, double oy, double oz, double extra, T data) {
        World world = loc.getWorld();
        if (world == null) return;
        world.spawnParticle(particle, loc, count, ox, oy, oz, extra, data);
    }

    public static void dust(Location loc, Color color, float size, int count) {
        spawnData(loc, Particle.DUST, count, 0.15, 0.15, 0.15, 0, new Particle.DustOptions(color, size));
    }

    public static void blockCrack(Location loc, BlockData data, int count) {
        spawnData(loc, Particle.BLOCK, count, 0.25, 0.25, 0.25, 0, data);
    }

    public static void burst(Location center, Particle particle, int count, double spread) {
        spawn(center, particle, count, spread, spread, spread, 0.02);
    }

    // ---------------------------------------------------------------
    // Shapes
    // ---------------------------------------------------------------

    public static void ring(Location center, double radius, int points, Particle particle, int perPoint) {
        ring(center, radius, points, 0, particle, perPoint);
    }

    public static void ring(Location center, double radius, int points, double spinDegrees, Particle particle, int perPoint) {
        World world = center.getWorld();
        if (world == null) return;
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians((360.0 / points) * i + spinDegrees);
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            world.spawnParticle(particle, new Location(world, x, center.getY(), z), perPoint, 0, 0, 0, 0);
        }
    }

    /** A ring that also has thickness/height - used for Void Slam's four rising rings. */
    public static void verticalRing(Location center, double radius, int points, double spinDegrees, double yOffset, Particle particle, int perPoint) {
        World world = center.getWorld();
        if (world == null) return;
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians((360.0 / points) * i + spinDegrees);
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            world.spawnParticle(particle, new Location(world, x, center.getY() + yOffset, z), perPoint, 0, 0, 0, 0);
        }
    }

    public static void shockwave(Location center, double maxRadius, int rings, int pointsPerRing, Particle particle) {
        for (int r = 1; r <= rings; r++) {
            double radius = maxRadius * ((double) r / rings);
            ring(center.clone().add(0, 0.05 * r, 0), radius, pointsPerRing, particle, 1);
        }
    }

    public static Location lerp(Location a, Location b, double t) {
        return a.clone().add(b.clone().subtract(a).toVector().multiply(t));
    }

    /** A straight or lightly-jittered line of particles from start to end - beams, slashes, projectile trails. */
    public static void line(Location start, Location end, int segments, double jitter, Particle particle, int perSegment) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i <= segments; i++) {
            double t = (double) i / segments;
            Location point = lerp(start, end, t);
            if (jitter > 0 && i != 0 && i != segments) {
                point.add((random.nextDouble() - 0.5) * jitter, (random.nextDouble() - 0.5) * jitter, (random.nextDouble() - 0.5) * jitter);
            }
            spawn(point, particle, perSegment, 0.02, 0.02, 0.02, 0);
        }
    }

    /** A short jagged slash "swipe" across a fixed plane facing `direction`, used by Black Rift. */
    public static void slash(Location center, org.bukkit.util.Vector direction, double length, double arc, Particle particle, int density) {
        org.bukkit.util.Vector side = new org.bukkit.util.Vector(-direction.getZ(), 0, direction.getX()).normalize();
        for (int i = 0; i <= density; i++) {
            double t = (double) i / density - 0.5;
            Location point = center.clone()
                    .add(direction.clone().multiply(t * length))
                    .add(side.clone().multiply(Math.sin(t * Math.PI) * arc));
            spawn(point, particle, 2, 0.05, 0.05, 0.05, 0);
        }
    }
}
