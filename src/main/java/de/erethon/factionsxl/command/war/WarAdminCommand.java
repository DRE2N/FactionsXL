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

package de.erethon.factionsxl.command.war;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.command.FCommand;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.WarCache;
import org.bukkit.command.CommandSender;

public class WarAdminCommand extends FCommand {
    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();
    WarCache wars = plugin.getWarCache();

    public WarAdminCommand() {
        setCommand("waradmin");
        setAliases("editwar", "waredit");
        setMinArgs(0);
        setMaxArgs(6);
        setHelp("Internal command to edit wars");
        setPermission(FPermission.WORLD.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Faction leaderOne = factions.getByName(args[1]);
        Faction leaderTwo = factions.getByName(args[2]);
        War war = wars.getByPartyLeaders(leaderOne, leaderTwo);
        if (args[3].equalsIgnoreCase("participants")){
            if (args[4].equalsIgnoreCase("list")) {
                MessageUtil.sendMessage(sender, "Attacker:");
                for (Faction f : war.getAttacker().getFactions()) {
                    MessageUtil.sendMessage(sender, f.getName());
                }
                MessageUtil.sendMessage(sender, "Defender:");
                for (Faction f : war.getDefender().getFactions()) {
                    MessageUtil.sendMessage(sender, f.getName());
                }
                return;
            } else if (args[4].equalsIgnoreCase("add")) {
                Faction add = factions.getByName(args[6]);
                if (add == null) {
                    MessageUtil.sendMessage(sender, "Invalid faction");
                    return;
                }
                if (args[5].equalsIgnoreCase("attacker")) {
                    war.getAttacker().addParticipant(add);
                } else if (args[5].equalsIgnoreCase("defender")) {
                    war.getAttacker().addParticipant(add);
                }
            } else if (args[4].equalsIgnoreCase("remove")) {
                Faction rem = factions.getByName(args[6]);
                if (rem == null) {
                    MessageUtil.sendMessage(sender, "Invalid faction");
                    return;
                }
                if (args[5].equalsIgnoreCase("attacker")) {
                    war.getAttacker().removeParticipant(rem);
                } else if (args[5].equalsIgnoreCase("defender")) {
                    war.getAttacker().removeParticipant(rem);
                }
            }
        }
    }
}
