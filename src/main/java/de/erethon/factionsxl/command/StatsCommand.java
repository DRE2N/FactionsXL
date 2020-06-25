/*
 *
 *  * Copyright (C) 2017-2020 Daniel Saukel, Malfrador
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.erethon.factionsxl.command;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.WarParty;
import org.bukkit.command.CommandSender;

public class StatsCommand extends FCommand {

    public StatsCommand() {
        setCommand("stats");
        setAliases("statistics");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp("/f stats [<Spieler>]");
        setPermission(FPermission.POWER.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        FPlayer player = getFSenderOrFromArg(sender, args, 1);
        MessageUtil.sendCenteredMessage(sender, "&6Spieler-Statistik von " + player.getName());
        MessageUtil.sendMessage(sender, " ");
        MessageUtil.sendMessage(sender, "&6Fraktionen gegründet&8: &6" + player.getData().getFactionsCreated());
        MessageUtil.sendMessage(sender, "&6Geld in Fraktionen eingezahlt&8: &6" + player.getData().getMoneyDeposited());
        MessageUtil.sendMessage(sender, " ");
        MessageUtil.sendMessage(sender, "&6Kills im Krieg&8:&6 " + player.getData().getKills());
        MessageUtil.sendMessage(sender, "&6Tode im Krieg&8:&6 " + player.getData().getDeaths());
        if (player.getFaction() != null && player.getFaction().isInWar()) {
            Faction faction = player.getFaction();
            MessageUtil.sendMessage(sender, "&6Kriegsbeteiligung:");
            for (WarParty wp : faction.getWarParties()) {
                War war = wp.getWar();
                MessageUtil.sendMessage(sender, "&a" + wp.getName() + " &6vs. &c" + wp.getEnemy().getName() + "&8:&6 " + war.getPlayerParticipation(player.getPlayer()));
            }
        }
    }
}
