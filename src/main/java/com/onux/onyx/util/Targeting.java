package com.onux.onyx.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.Predicate;

/** Ray-trace helpers shared by every ability that needs "who/what is this player looking at". */
public final class Targeting {

    public LivingEntity getTargetedEntity(Player caster, double range) {
        RayTraceResult result = caster.getWorld().rayTraceEntities(
                caster.getEyeLocation(),
                caster.getEyeLocation().getDirection(),
                range,
                0.4,
                entity -> entity instanceof LivingEntity && !entity.equals(caster) && !entity.isDead()
        );
        if (result == null || result.getHitEntity() == null) return null;
        return (LivingEntity) result.getHitEntity();
    }

    public Player getTargetedPlayer(Player caster, double range) {
        LivingEntity entity = getTargetedEntity(caster, range);
        return entity instanceof Player ? (Player) entity : null;
    }

    public Block getTargetedBlock(Player caster, double range) {
        RayTraceResult result = caster.getWorld().rayTraceBlocks(caster.getEyeLocation(), caster.getEyeLocation().getDirection(), range);
        if (result == null || result.getHitBlock() == null) return null;
        return result.getHitBlock();
    }

    public Location resolveAimLocation(Player caster, double range) {
        LivingEntity entity = getTargetedEntity(caster, range);
        if (entity != null) return entity.getLocation();
        Block block = getTargetedBlock(caster, range);
        if (block != null) return block.getLocation().add(0.5, 1, 0.5);
        return caster.getEyeLocation().add(caster.getEyeLocation().getDirection().multiply(range));
    }

    public List<LivingEntity> nearbyEntities(Location center, double radius, Player caster, Predicate<LivingEntity> extra) {
        return center.getWorld().getNearbyLivingEntities(center, radius, radius, radius, e -> !e.equals(caster) && extra.test(e)).stream().toList();
    }

    public List<Player> nearbyPlayers(Location center, double radius, Player exclude, int max) {
        return center.getWorld().getPlayers().stream()
                .filter(p -> !p.equals(exclude))
                .filter(p -> p.getLocation().distance(center) <= radius)
                .sorted((a, b) -> Double.compare(a.getLocation().distance(center), b.getLocation().distance(center)))
                .limit(max)
                .toList();
    }

    public static Vector directionBetween(Location from, Location to) {
        return to.toVector().subtract(from.toVector()).normalize();
    }
}
