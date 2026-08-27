package com.onux.onyx.commands;

import com.onux.onyx.abilities.Activatable;
import com.onux.onyx.util.Msg;
import com.onux.onyx.weapons.WeaponFactory;
import com.onux.onyx.weapons.WeaponType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Backs /waterability, /frostability, /voidability, and /eyesability - all the same shape. */
public final class TwoAbilityCommand implements CommandExecutor {

    private final WeaponFactory weapons;
    private final WeaponType weaponType;
    private final String weaponDisplayName;
    private final Activatable ability1;
    private final Activatable ability2;

    public TwoAbilityCommand(WeaponFactory weapons, WeaponType weaponType, String weaponDisplayName,
                              Activatable ability1, Activatable ability2) {
        this.weapons = weapons;
        this.weaponType = weaponType;
        this.weaponDisplayName = weaponDisplayName;
        this.ability1 = ability1;
        this.ability2 = ability2;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!weapons.isHoldingWeapon(player, weaponType)) {
            player.sendMessage(Msg.of("&cYou must be holding the " + weaponDisplayName + "."));
            return true;
        }
        if (args.length != 1 || (!args[0].equals("1") && !args[0].equals("2"))) {
            player.sendMessage(Msg.of("&cUsage: /" + label + " <1|2>"));
            return true;
        }
        (args[0].equals("1") ? ability1 : ability2).activate(player);
        return true;
    }
}
