package com.onux.onyx.abilities.frost;

import com.onux.onyx.abilities.Activatable;
import com.onux.onyx.abilities.Deps;
import com.onux.onyx.util.CooldownManager.Ability;
import com.onux.onyx.util.FX;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ability 2 - Ice Armor. Full damage immunity + Glowing for a fixed
 * duration; blocked hits from other players play an icy crack and burst.
 */
public final class IceArmor implements Activatable, Listener {

    public static final String KEY = "ice_armor";

    private final Deps deps;
    private final Set<UUID> active = ConcurrentHashMap.newKeySet();

    public IceArmor(Deps deps) {
        this.deps = deps;
        deps.plugin.getServer().getPluginManager().registerEvents(this, deps.plugin);
    }

    public boolean isActive(Player player) {
        return active.contains(player.getUniqueId());
    }

    @Override
    public void activate(Player caster) {
        if (!deps.cooldowns.check(caster, Ability.ICE_ARMOR)) return;
        deps.cooldowns.start(caster, Ability.ICE_ARMOR);

        int durationTicks = deps.cfgInt(KEY, "duration-seconds", 4) * 20;
        UUID uuid = caster.getUniqueId();
        active.add(uuid);

        caster.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, durationTicks + 5, 0));
        FX.play(caster, Sound.BLOCK_GLASS_PLACE, 1.5f, 1.1f);
        FX.burst(caster.getLocation().add(0, 1, 0), Particle.SNOWFLAKE, 25, 0.4);

        new BukkitRunnable() {
            int elapsed = 0;
            @Override
            public void run() {
                if (elapsed >= durationTicks || !caster.isOnline()) {
                    active.remove(uuid);
                    if (caster.isOnline()) {
                        FX.play(caster, Sound.BLOCK_GLASS_BREAK, 1.3f, 0.8f);
                        FX.burst(caster.getLocation().add(0, 1, 0), Particle.ITEM_SNOWBALL, 15, 0.35);
                    }
                    cancel();
                    return;
                }
                FX.ring(caster.getLocation().add(0, 1, 0), 0.7, 12, elapsed * 15, Particle.SNOWFLAKE, 1);
                elapsed += 2;
            }
        }.runTaskTimer(deps.plugin, 0L, 2L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!active.contains(player.getUniqueId())) return;

        event.setCancelled(true);

        if (event instanceof EntityDamageByEntityEvent byEntity) {
            FX.play(player, Sound.BLOCK_GLASS_BREAK, 1.2f, 1.6f);
            FX.burst(player.getLocation().add(0, 1, 0), Particle.SNOWFLAKE, 10, 0.3);
        }
    }
}
