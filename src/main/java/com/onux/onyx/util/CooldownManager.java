package com.onux.onyx.util;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks a cooldown per player per ability and, when {@code boss-bar.enabled}
 * is true in config.yml, shows a live counting-down boss bar for the whole
 * duration - stacked if the player has more than one ability cooling down
 * at once. The bar's color and style both come from config.yml.
 */
public final class CooldownManager {

    /** Every ability in the plugin, with the config key + display name used for cooldown messages and bars. */
    public enum Ability {
        WARDEN_BEAM("warden_beam", "Warden Beam"),
        SCULK_METEOR("sculk_meteor", "Sculk Meteor"),
        FROZEN_BARRAGE("frozen_barrage", "Frozen Barrage"),
        ICE_ARMOR("ice_armor", "Ice Armor"),
        LEVIATHAN_TSUNAMI("leviathan_tsunami", "Leviathan Tsunami"),
        TIDAL_DASH("tidal_dash", "Tidal Dash"),
        VOID_SLAM("void_slam", "Void Slam"),
        BLACK_RIFT("black_rift", "Black Rift"),
        HOLLOW_PURPLE("hollow_purple", "Hollow Purple"),
        RED_BEAM("red_beam", "Red Beam");

        public final String configKey;
        public final String displayName;

        Ability(String configKey, String displayName) {
            this.configKey = configKey;
            this.displayName = displayName;
        }
    }

    private final Plugin plugin;
    private final FileConfiguration config;

    private final Map<UUID, Map<Ability, Long>> expiry = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Ability, BossBar>> bars = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Ability, BukkitTask>> barTasks = new ConcurrentHashMap<>();

    public CooldownManager(Plugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    // ---------------------------------------------------------------
    // Query
    // ---------------------------------------------------------------

    public boolean isOnCooldown(Player player, Ability ability) {
        return remainingMillis(player.getUniqueId(), ability) > 0;
    }

    public long remainingSeconds(Player player, Ability ability) {
        return (remainingMillis(player.getUniqueId(), ability) + 999) / 1000;
    }

    /** Checks the cooldown and, if still active, sends a message. Does NOT start it. */
    public boolean check(Player player, Ability ability) {
        if (isOnCooldown(player, ability)) {
            player.sendMessage(Msg.of("&7" + ability.displayName + " &7cooldown: &f" + remainingSeconds(player, ability) + "s"));
            return false;
        }
        return true;
    }

    // ---------------------------------------------------------------
    // Start / reset
    // ---------------------------------------------------------------

    /** Starts the ability's configured cooldown and, if enabled, the boss bar. */
    public void start(Player player, Ability ability) {
        int seconds = config.getInt("abilities." + ability.configKey + ".cooldown-seconds", 10);
        start(player, ability, seconds);
    }

    public void start(Player player, Ability ability, int seconds) {
        if (seconds <= 0) return;
        long total = seconds * 1000L;
        expiry.computeIfAbsent(player.getUniqueId(), u -> new EnumMap<>(Ability.class))
                .put(ability, System.currentTimeMillis() + total);

        if (config.getBoolean("boss-bar.enabled", true)) {
            startBar(player, ability, total);
        }
    }

    public void reset(Player player) {
        UUID uuid = player.getUniqueId();
        Map<Ability, Long> map = expiry.get(uuid);
        if (map != null) map.clear();
        clearBars(player);
    }

    public void forget(UUID uuid) {
        expiry.remove(uuid);
        Map<Ability, BossBar> playerBars = bars.remove(uuid);
        Map<Ability, BukkitTask> tasks = barTasks.remove(uuid);
        if (tasks != null) tasks.values().forEach(BukkitTask::cancel);
        // Bars are hidden automatically on disconnect by the client/server, no explicit hide needed here.
        if (playerBars != null) playerBars.clear();
    }

    // ---------------------------------------------------------------
    // Boss bar internals
    // ---------------------------------------------------------------

    private void startBar(Player player, Ability ability, long totalMillis) {
        UUID uuid = player.getUniqueId();
        clearBar(player, ability);

        BossBar.Color color = parseColor(config.getString("boss-bar.colors." + ability.configKey, "WHITE"));
        BossBar.Overlay overlay = parseOverlay(config.getString("boss-bar.style", "SOLID"));
        BossBar bar = BossBar.bossBar(Msg.of("&f" + ability.displayName), 1.0f, color, overlay);

        bars.computeIfAbsent(uuid, u -> new EnumMap<>(Ability.class)).put(ability, bar);
        player.showBossBar(bar);

        long startedAt = System.currentTimeMillis();
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long remaining = expiry.getOrDefault(uuid, Map.of()).getOrDefault(ability, 0L) - System.currentTimeMillis();
            if (remaining <= 0 || !player.isOnline()) {
                clearBar(player, ability);
                return;
            }
            float progress = Math.max(0f, Math.min(1f, (float) remaining / totalMillis));
            bar.progress(progress);
            long secondsLeft = (remaining + 999) / 1000;
            bar.name(Msg.of("&f" + ability.displayName + " &7- &f" + secondsLeft + "s"));
        }, 0L, 2L);

        barTasks.computeIfAbsent(uuid, u -> new EnumMap<>(Ability.class)).put(ability, task);
    }

    private void clearBar(Player player, Ability ability) {
        UUID uuid = player.getUniqueId();
        Map<Ability, BossBar> playerBars = bars.get(uuid);
        if (playerBars != null) {
            BossBar bar = playerBars.remove(ability);
            if (bar != null) player.hideBossBar(bar);
        }
        Map<Ability, BukkitTask> tasks = barTasks.get(uuid);
        if (tasks != null) {
            BukkitTask task = tasks.remove(ability);
            if (task != null) task.cancel();
        }
    }

    private void clearBars(Player player) {
        for (Ability ability : Ability.values()) {
            clearBar(player, ability);
        }
    }

    private BossBar.Color parseColor(String value) {
        try {
            return BossBar.Color.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return BossBar.Color.WHITE;
        }
    }

    private BossBar.Overlay parseOverlay(String value) {
        return switch (value.toUpperCase()) {
            case "SEGMENTED_6" -> BossBar.Overlay.NOTCHED_6;
            case "SEGMENTED_10" -> BossBar.Overlay.NOTCHED_10;
            case "SEGMENTED_12" -> BossBar.Overlay.NOTCHED_12;
            case "SEGMENTED_20" -> BossBar.Overlay.NOTCHED_20;
            default -> BossBar.Overlay.PROGRESS;
        };
    }

    private long remainingMillis(UUID uuid, Ability ability) {
        Map<Ability, Long> map = expiry.get(uuid);
        if (map == null) return 0;
        Long value = map.get(ability);
        if (value == null) return 0;
        return Math.max(0, value - System.currentTimeMillis());
    }
}
