package com.onux.onyx.commands;

import com.onux.onyx.util.Msg;
import com.onux.onyx.weapons.WeaponFactory;
import com.onux.onyx.weapons.WeaponType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Backs every plain "give me weapon X" command: /voidreaver, /frostblade, /leviathan, /eyes, /frostgui. */
public final class GiveWeaponCommand implements CommandExecutor {

    private final WeaponFactory weapons;
    private final WeaponType type;
    private final String requiredPermission; // nullable - no permission required if null

    public GiveWeaponCommand(WeaponFactory weapons, WeaponType type) {
        this(weapons, type, null);
    }

    public GiveWeaponCommand(WeaponFactory weapons, WeaponType type, String requiredPermission) {
        this.weapons = weapons;
        this.type = type;
        this.requiredPermission = requiredPermission;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (requiredPermission != null && !player.hasPermission(requiredPermission) && !player.isOp()) {
            player.sendMessage(Msg.of("&cYou do not have permission to use this command."));
            return true;
        }
        weapons.give(player, type);
        return true;
    }
}
