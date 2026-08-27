package com.onux.onyx.abilities.eyes;

import com.onux.onyx.abilities.Activatable;
import com.onux.onyx.abilities.Deps;
import com.onux.onyx.util.CooldownManager.Ability;
import com.onux.onyx.util.FX;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Ability 2 - Red Beam. An instant, highly visible beam that damages the
 * first player it hits and shatters blocks directly along its path.
 */
public final class RedBeam implements Activatable {

    public static final String KEY = "red_beam";
    private static final Color RED = Color.fromRGB(230, 20, 20);

    private final Deps deps;

    public RedBeam(Deps deps) {
        this.deps = deps;
    }

    @Override
    public void activate(Player caster) {
        if (!deps.cooldowns.check(caster, Ability.RED_BEAM)) return;
        deps.cooldowns.start(caster, Ability.RED_BEAM);

        double damage = deps.cfgDouble(KEY, "damage", 8.0);
        double range = deps.cfgDouble(KEY, "range", 20);
        int breakRadius = deps.cfgInt(KEY, "block-break-radius", 1);

        Location start = caster.getEyeLocation();
        Vector direction = start.getDirection();

        RayTraceResult entityTrace = caster.getWorld().rayTraceEntities(start, direction, range, 0.6,
                e -> e instanceof LivingEntity && !e.equals(caster));
        RayTraceResult blockTrace = caster.getWorld().rayTraceBlocks(start, direction, range);

        double entityDist = entityTrace != null && entityTrace.getHitEntity() != null ? start.distance(entityTrace.getHitEntity().getLocation()) : Double.MAX_VALUE;
        double blockDist = blockTrace != null && blockTrace.getHitBlock() != null ? start.distance(blockTrace.getHitBlock().getLocation()) : Double.MAX_VALUE;
        double actualRange = Math.min(range, Math.min(entityDist, blockDist));
        if (actualRange == Double.MAX_VALUE) actualRange = range;

        Location end = start.clone().add(direction.clone().multiply(actualRange));

        FX.play(caster, Sound.ENTITY_GHAST_SHOOT, 1.8f, 0.6f);
        FX.play(caster, Sound.ENTITY_BLAZE_SHOOT, 1.4f, 0.7f);

        FX.line(start, end, 45, 0, Particle.DUST, 1);
        FX.line(start, end, 45, 0, Particle.FLAME, 1);
        FX.line(start, end, 25, 0.2, Particle.END_ROD, 1);
        drawRedCore(start, end);

        breakBlocksAlongPath(start, end, breakRadius);

        FX.burst(end, Particle.DUST, 30, 0.4);
        FX.play(end, Sound.ENTITY_GENERIC_EXPLODE, 1.6f, 0.7f);

        if (entityTrace != null && entityTrace.getHitEntity() instanceof LivingEntity target && entityDist <= actualRange + 0.01) {
            if (!(target instanceof Player p && deps.trust.isTrusted(caster, p))) {
                target.damage(damage, caster);
            }
        }
    }

    private void drawRedCore(Location start, Location end) {
        int segments = 45;
        for (int i = 0; i <= segments; i++) {
            double t = (double) i / segments;
            Location point = FX.lerp(start, end, t);
            FX.dust(point, RED, 1.6f, 3);
        }
    }

    private void breakBlocksAlongPath(Location start, Location end, int radius) {
        int segments = (int) Math.ceil(start.distance(end));
        for (int i = 0; i <= segments; i++) {
            double t = segments == 0 ? 0 : (double) i / segments;
            Location point = FX.lerp(start, end, t);
            var changes = deps.blocks.clearSphere(point, radius);
            deps.blocks.scheduleRevert(changes);
        }
    }
}
