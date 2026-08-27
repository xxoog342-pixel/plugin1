package com.onux.onyx.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Handles every "turn the ground into X" or "break blocks around here"
 * effect safely: it never touches a configured protected-block list
 * (containers, spawners, command blocks, bedrock, etc.), and every change
 * is captured as a {@link Change} so a batch of blocks can be reverted
 * together after a delay instead of a plugin permanently craterring a
 * survival world.
 */
public final class BlockRestorer {

    private final Plugin plugin;
    private final Set<Material> protectedBlocks;
    private final int revertAfterSeconds;

    public BlockRestorer(Plugin plugin, List<String> protectedNames, int revertAfterSeconds) {
        this.plugin = plugin;
        EnumSet<Material> set = EnumSet.noneOf(Material.class);
        for (String name : protectedNames) {
            Material m = Material.matchMaterial(name);
            if (m != null) set.add(m);
        }
        this.protectedBlocks = set;
        this.revertAfterSeconds = Math.max(0, revertAfterSeconds);
    }

    public boolean isProtected(Block block) {
        Material type = block.getType();
        if (type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR) return true;
        return protectedBlocks.contains(type);
    }

    /** A single captured block change, ready to be reverted with {@link Change#revert()}. */
    public record Change(BlockState before) {
        public void revert() {
            before.update(true, false);
        }
    }

    /** Sets one block, unless protected. Returns the Change (for batching) or null if skipped. */
    public Change setIfAllowed(Block block, Material material) {
        if (isProtected(block)) return null;
        Change change = new Change(block.getState());
        block.setType(material, false);
        return change;
    }

    /** Sets every non-protected block within a flat circular radius (top surface only) to `material`. */
    public List<Change> spreadFlat(Location center, double radius, Material material) {
        List<Change> changes = new ArrayList<>();
        Block centerBlock = center.getBlock();
        int r = (int) Math.ceil(radius);
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                if (x * x + z * z > radius * radius) continue;
                Block target = centerBlock.getRelative(x, 0, z);
                if (target.getType() == Material.AIR && target.getRelative(0, -1, 0).getType() != Material.AIR) continue;
                Change change = setIfAllowed(target, material);
                if (change != null) changes.add(change);
            }
        }
        return changes;
    }

    /** Clears every non-protected block within a sphere to AIR - used for crater/destruction effects. */
    public List<Change> clearSphere(Location center, double radius) {
        List<Change> changes = new ArrayList<>();
        Block centerBlock = center.getBlock();
        int r = (int) Math.ceil(radius);
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if (x * x + y * y + z * z > radius * radius) continue;
                    Change change = setIfAllowed(centerBlock.getRelative(x, y, z), Material.AIR);
                    if (change != null) changes.add(change);
                }
            }
        }
        return changes;
    }

    /** Schedules an entire batch of changes to revert together after {@code afterSeconds} (falls back to config default if &lt; 0). */
    public void scheduleRevert(List<Change> changes, int afterSeconds) {
        int seconds = afterSeconds >= 0 ? afterSeconds : revertAfterSeconds;
        if (seconds <= 0 || changes.isEmpty()) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Change change : changes) change.revert();
            }
        }.runTaskLater(plugin, seconds * 20L);
    }

    public void scheduleRevert(List<Change> changes) {
        scheduleRevert(changes, -1);
    }
}
