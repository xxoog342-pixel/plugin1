package com.onux.onyx.commands;

import com.onux.onyx.util.Msg;
import com.onux.onyx.weapons.WeaponFactory;
import com.onux.onyx.weapons.WeaponType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class WardenCommand implements CommandExecutor {

    private final WeaponFactory weapons;

    public WardenCommand(WeaponFactory weapons) {
        this.weapons = weapons;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0 || !args[0].equalsIgnoreCase("go")) {
            player.sendMessage(Msg.of("&7Usage: &f/warden go"));
            return true;
        }
        if (!player.hasPermission("warden.use") && !player.isOp()) {
            player.sendMessage(Msg.of("&cYou do not have permission to use the Warden Crossbow."));
            return true;
        }
        weapons.give(player, WeaponType.WARDEN_CROSSBOW);
        return true;
    }
}
