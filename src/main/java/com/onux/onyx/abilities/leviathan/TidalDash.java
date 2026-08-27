package com.onux.onyx.abilities.leviathan;

import com.onux.onyx.abilities.Activatable;
import com.onux.onyx.abilities.Deps;
import com.onux.onyx.util.CooldownManager.Ability;
import com.onux.onyx.util.FX;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/** Ability 2 - Tidal Dash. A short, powerful wave-carried dash in whatever direction the player is looking. */
public final class TidalDash implements Activatable {

    public static final String KEY = "tidal_dash";

    private final Deps deps;

    public TidalDash(Deps deps) {
        this.deps = deps;
    }

    @Override
    public void activate(Player caster) {
        if (!deps.cooldowns.check(caster, Ability.TIDAL_DASH)) return;
        deps.cooldowns.start(caster, Ability.TIDAL_DASH);

        int durationTicks = deps.cfgInt(KEY, "duration-ticks", 12);
        double speed = deps.cfgDouble(KEY, "speed", 1.9);
        Vector direction = caster.getEyeLocation().getDirection().normalize();

        FX.play(caster, Sound.ENTITY_DOLPHIN_SPLASH, 1.6f, 1.1f);
        FX.burst(caster.getLocation(), Particle.BUBBLE_POP, 20, 0.4);

        new BukkitRunnable() {
            int elapsed = 0;
            @Override
            public void run() {
                if (elapsed >= durationTicks || !caster.isOnline()) {
                    cancel();
                    return;
                }
                Vector velocity = direction.clone().multiply(speed);
                velocity.setY(Math.max(velocity.getY(), 0.15));
                caster.setVelocity(velocity);
                caster.setFallDistance(0);

                Location trail = caster.getLocation().add(0, 0.5, 0);
                FX.spawn(trail, Particle.SPLASH, 6, 0.3, 0.2, 0.3, 0);
                FX.spawn(trail, Particle.BUBBLE_POP, 3, 0.25, 0.2, 0.25, 0);
                FX.spawn(trail, Particle.FALLING_WATER, 2, 0.3, 0.1, 0.3, 0);
                elapsed++;
            }
        }.runTaskTimer(deps.plugin, 0L, 1L);
    }
}
