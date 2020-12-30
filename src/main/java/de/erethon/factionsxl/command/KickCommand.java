/*
 * Copyright (C) 2017-2020 Daniel Saukel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.erethon.factionsxl.command;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.api.event.FPlayerFactionLeaveEvent;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class KickCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public KickCommand() {
        setCommand("kick");
        setMinArgs(1);
        setMaxArgs(1);
        setHelp(FMessage.HELP_KICK.getMessage());
        setPermission(FPermission.KICK.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
        if (!player.hasPlayedBefore()) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_PLAYER.getMessage(), args[1]);
            return;
        } else if (!plugin.getFPlayerCache().getByPlayer(player).hasFaction()) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_PLAYER_NOT_IN_ANY_FACTION.getMessage(), args[1]);
            return;
        }
        Faction faction = plugin.getFactionCache().getByMember(player);
        if (!faction.isPrivileged(sender)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        } else if (faction.getMods().contains(player) && sender instanceof Player && !faction.isAdmin(sender)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }
        faction.kick(sender, player);

        FPlayerFactionLeaveEvent event = new FPlayerFactionLeaveEvent(plugin.getFPlayerCache().getByPlayer(player), faction);
        Bukkit.getPluginManager().callEvent(event);

    }

}
