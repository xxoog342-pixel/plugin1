package com.onux.onyx.commands;

import com.onux.onyx.util.CooldownManager;
import com.onux.onyx.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CooldownCommand implements CommandExecutor {

    private final CooldownManager cooldowns;

    public CooldownCommand(CooldownManager cooldowns) {
        this.cooldowns = cooldowns;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        cooldowns.reset(player);
        player.sendMessage(Msg.of("&a✔ &7All of your ability cooldowns have been reset."));
        return true;
    }
}
