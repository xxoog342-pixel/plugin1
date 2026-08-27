package com.onux.onyx.commands;

import com.onux.onyx.abilities.Activatable;
import com.onux.onyx.util.Msg;
import com.onux.onyx.weapons.WeaponFactory;
import com.onux.onyx.weapons.WeaponType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class AbilityCommand implements CommandExecutor {

    private final WeaponFactory weapons;
    private final Activatable ability1;
    private final Activatable ability2;

    public AbilityCommand(WeaponFactory weapons, Activatable ability1, Activatable ability2) {
        this.weapons = weapons;
        this.ability1 = ability1;
        this.ability2 = ability2;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!weapons.isHoldingWeapon(player, WeaponType.WARDEN_CROSSBOW)) {
            player.sendMessage(Msg.of("&cYou must be holding the Warden Crossbow."));
            return true;
        }
        if (args.length != 1 || (!args[0].equals("1") && !args[0].equals("2"))) {
            player.sendMessage(Msg.of("&cUsage: /ability <1|2>"));
            return true;
        }
        if (!player.hasPermission("warden.use") && !player.isOp()) {
            player.sendMessage(Msg.of("&cYou do not have permission to use the Warden Crossbow."));
            return true;
        }
        (args[0].equals("1") ? ability1 : ability2).activate(player);
        return true;
    }
}
