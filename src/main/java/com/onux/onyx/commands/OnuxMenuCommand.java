package com.onux.onyx.commands;

import com.onux.onyx.gui.ArsenalMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class OnuxMenuCommand implements CommandExecutor {

    private final ArsenalMenu menu;

    public OnuxMenuCommand(ArsenalMenu menu) {
        this.menu = menu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        player.openInventory(menu.build());
        return true;
    }
}
