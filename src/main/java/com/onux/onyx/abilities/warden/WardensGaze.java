package com.onux.onyx.abilities.warden;

import com.onux.onyx.abilities.Deps;
import com.onux.onyx.util.FX;
import com.onux.onyx.weapons.WeaponType;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Passive - Warden's Gaze. Only applies to *normal* crossbow shots (the
 * player was not sneaking, so {@code WardenChargeListener} let the vanilla
 * shoot-bow flow through untouched). A tagged arrow has a flat chance to
 * blind whoever it hits.
 */
public final class WardensGaze implements Listener {

    public static final String KEY = "wardens_gaze";

    private final Deps deps;
    private final NamespacedKey tagKey;

    public WardensGaze(Deps deps) {
        this.deps = deps;
        this.tagKey = new NamespacedKey(deps.plugin, "wardens_gaze_shot");
        deps.plugin.getServer().getPluginManager().registerEvents(this, deps.plugin);
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getBow() == null || !deps.weapons.isWeapon(event.getBow(), WeaponType.WARDEN_CROSSBOW)) return;
        if (!(event.getProjectile() instanceof Projectile projectile)) return;

        projectile.getPersistentDataContainer().set(tagKey, PersistentDataType.BYTE, (byte) 1);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Projectile projectile)) return;
        Byte tag = projectile.getPersistentDataContainer().get(tagKey, PersistentDataType.BYTE);
        if (tag == null) return;
        if (!(event.getHitEntity() instanceof Player target)) return;

        int chance = deps.cfgInt(KEY, "chance-percent", 20);
        if (ThreadLocalRandom.current().nextInt(100) >= chance) return;

        int seconds = deps.cfgInt(KEY, "blindness-duration-seconds", 2);
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, seconds * 20, 0));
        FX.spawn(target.getEyeLocation(), Particle.SCULK_SOUL, 10, 0.3, 0.3, 0.3, 0.02);
        FX.spawn(target.getEyeLocation(), Particle.SQUID_INK, 6, 0.2, 0.2, 0.2, 0.01);
    }
}
