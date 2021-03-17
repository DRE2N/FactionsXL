/*
 * Copyright (C) 2017-2021 Daniel Saukel
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

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.event.FPlayerFactionJoinEvent;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.scoreboard.FTeamWrapper;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class JoinCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public JoinCommand() {
        setCommand("join");
        setMinArgs(1);
        setMaxArgs(2);
        setHelp(FMessage.HELP_JOIN.getMessage());
        setPermission(FPermission.JOIN.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Faction faction = plugin.getFactionCache().getByName(args[1]);
        if (faction == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_FACTION.getMessage(), args[1]);
            return;
        }

        Player player = (Player) sender;
        if (plugin.getFactionCache().getByMember(player) != null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_LEAVE_FACTION.getMessage());
            return;
        }

        FPlayer fPlayer = plugin.getFPlayerCache().getByPlayer(player);
        long now = System.currentTimeMillis(); // 3600000

        if (player.hasPermission("fxl.bypass") && args.length == 2) {
            faction.getMembers().add(player);

            FPlayerFactionJoinEvent event = new FPlayerFactionJoinEvent(fPlayer, faction);
            Bukkit.getPluginManager().callEvent(event);

            fPlayer.getData().setLastJoinedFaction(now);
            faction.sendMessage(FMessage.FACTION_JOIN_ACCEPT.getMessage(), player);
            FTeamWrapper.updatePrefixes(faction);
            faction.getInvitedPlayers().remove(player);
            return;
        }
        if ( (fPlayer.getData().getLastJoinedFaction()) != 0 && (fPlayer.getData().getLastJoinedFaction() + 3600000) > now  ){
            MessageUtil.sendMessage(player, "&cDu musst eine Stunde warten, bevor du wieder einer Fraktion beitreten kannst.");
            return;
        }

        if (faction.getInvitedPlayers().contains(player) || faction.isPrivileged(player) || faction.isOpen()) {
            if (args.length == 2) {

                FPlayerFactionJoinEvent event = new FPlayerFactionJoinEvent(fPlayer, faction);
                Bukkit.getPluginManager().callEvent(event);

                faction.getMembers().add(player);
                fPlayer.getData().setLastJoinedFaction(now);
                faction.sendMessage(FMessage.FACTION_JOIN_ACCEPT.getMessage(), player);
                FTeamWrapper.updatePrefixes(faction);
            } else {
                faction.sendMessage(FMessage.FACTION_JOIN_DENY.getMessage(), player);
                ParsingUtil.sendMessage(player, FMessage.FACTION_JOIN_DENY.getMessage(), player);
            }
            faction.getInvitedPlayers().remove(player);

        } else {
            ParsingUtil.sendMessage(player, FMessage.ERROR_NO_PERMISSION.getMessage());
        }
    }

}
