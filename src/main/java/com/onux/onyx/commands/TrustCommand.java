package com.onux.onyx.commands;

import com.onux.onyx.util.Msg;
import com.onux.onyx.util.TrustManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class TrustCommand implements CommandExecutor {

    private final TrustManager trust;

    public TrustCommand(TrustManager trust) {
        this.trust = trust;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Msg.of("&7Usage: &f/trust <add|remove|list> [player]"));
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("list")) {
            var trusted = trust.list(player);
            if (trusted.isEmpty()) {
                player.sendMessage(Msg.of("&7You have not trusted anyone."));
                return true;
            }
            StringBuilder names = new StringBuilder();
            for (UUID uuid : trusted) {
                Player p = Bukkit.getPlayer(uuid);
                names.append(p != null ? p.getName() : uuid).append(", ");
            }
            player.sendMessage(Msg.of("&7Trusted: &f" + names.substring(0, Math.max(0, names.length() - 2))));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Msg.of("&cUsage: /trust " + sub + " <player>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(Msg.of("&cPlayer not found or not online."));
            return true;
        }

        switch (sub) {
            case "add" -> {
                trust.trust(player, target);
                player.sendMessage(Msg.of("&a✔ &7You now trust &f" + target.getName() + "&7."));
            }
            case "remove" -> {
                trust.untrust(player, target);
                player.sendMessage(Msg.of("&c✘ &7You no longer trust &f" + target.getName() + "&7."));
            }
            default -> player.sendMessage(Msg.of("&cUsage: /trust <add|remove|list> [player]"));
        }
        return true;
    }
}
