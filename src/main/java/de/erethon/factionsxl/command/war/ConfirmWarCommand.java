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
package de.erethon.factionsxl.command.war;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.command.FCommand;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.scoreboard.FScoreboard;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.WarCache;
import de.erethon.factionsxl.war.WarRequest;
import org.bukkit.command.CommandSender;

/**
 * @author Daniel Saukel
 */

public class ConfirmWarCommand extends FCommand {

    WarCache wars = FactionsXL.getInstance().getWarCache();

    public ConfirmWarCommand() {
        setCommand("confirmWar");
        setMinArgs(1);
        setMaxArgs(2);
        setHelp(FMessage.HELP_CONFIRM_WAR.getMessage());
        setPermission(FPermission.WAR.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        long date;
        try {
            date = Long.parseLong(args[1]);
        } catch (NumberFormatException exception) {
            displayHelp(sender);
            return;
        }
        for (War war : wars.getUnconfirmedWars()) {
            if (war.getAttacker().getLeaderAdmin().equals(sender) && war.getStartDate().getTime() == date) {
                if (args.length == 3 && args[2].equalsIgnoreCase("-cancel")) {
                    wars.getUnconfirmedWars().remove(war);
                    MessageUtil.sendMessage(sender, FMessage.WAR_DECLARATION_CANCELLED.getMessage());
                } else {
                    war.confirm();
                    MessageUtil.broadcastMessage(" ");
                    ParsingUtil.broadcastMessage(FMessage.WAR_DECLARATION_BROADCAST.getMessage(), getFSender(sender), getSenderFaction(sender), war.getDefender().getLeader());
                    MessageUtil.broadcastMessage(" ");
                    for (Faction f : war.getAttacker().getInvited()) {
                        new WarRequest((Faction) war.getAttacker().getLeader(), f, war.getAttacker()).send();
                    }
                    FScoreboard.updateAllProviders();
                }
            }
        }
    }

}
