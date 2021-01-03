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
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.war.WarCache;
import de.erethon.factionsxl.war.WarParty;
import de.erethon.factionsxl.war.WarRequest;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarInviteCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();
    WarCache wars = FactionsXL.getInstance().getWarCache();

    public WarInviteCommand() {
        setCommand("warInvite");
        setMinArgs(1);
        setMaxArgs(2);
        setHelp("Internal command to force a war invite");
        setPermission(FPermission.WAR.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        FPlayer fplayer = plugin.getFPlayerCache().getByPlayer((Player) sender);
        Faction leader = factions.getByMember((OfflinePlayer) sender);
        Faction warFaction = factions.getByName(args[1]);
        if (leader.getAdmin() != sender) {
            MessageUtil.sendMessage(sender, "&cDu bist nicht Fraktions-Admin");
            return;
        }
        if (!(leader.getRelation(warFaction) == Relation.ALLIANCE || leader.getRelation(warFaction) == Relation.ALLIANCE_2
                || leader.getRelation(warFaction) == Relation.VASSAL || leader.getRelation(warFaction) == Relation.LORD
                || leader.getRelation(warFaction) == Relation.PERSONAL_UNION)) {
            MessageUtil.sendMessage(sender, "&cIhr seid nicht verbündet.");
            return;
        }
        WarParty warParty = null;
        for (WarParty wp : leader.getWarParties()) {
            if (wp.getLeader().equals(leader)) {
                warParty = wp;
            }
        }
        if (warParty == null) {
            MessageUtil.sendMessage(sender, "&cDu bist nicht der Anführer deiner Kriegspartei.");
            return;
        }
        if (warParty.getEnemy().getFactions().contains(warFaction)) {
            MessageUtil.sendMessage(sender, "&cDiese Fraktion ist bereits in der feindlichen Kriegspartei.");
            return;
        }
        new WarRequest(leader, warFaction, warParty).send();
        return;
    }
}