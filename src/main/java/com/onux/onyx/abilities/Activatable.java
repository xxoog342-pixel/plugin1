package com.onux.onyx.abilities;

import org.bukkit.entity.Player;

/**
 * One command/­input-triggerable ability action. Implementations own their
 * own cooldown gating via the shared CooldownManager.
 * <p>
 * Named "Activatable" rather than "Ability" to avoid clashing with
 * {@link com.onux.onyx.util.CooldownManager.Ability}, the enum that
 * identifies *which* ability something is for cooldown/boss-bar purposes.
 */
public interface Activatable {

    /** Runs the ability for this player. */
    void activate(Player player);
}
