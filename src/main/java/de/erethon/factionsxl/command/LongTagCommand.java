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

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.command.CommandSender;

/**
 * @author Daniel Saukel
 */
public class LongTagCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public LongTagCommand() {
        setCommand("longTag");
        setAliases("setLongTag", "longName", "setLongName", "ltag");
        setMinArgs(-1);
        setMaxArgs(-1);
        setHelp(FMessage.HELP_LONG_TAG.getMessage());
        setPermission(FPermission.TAG.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        i = 1;
        if (args.length < 2) {
            displayHelp(sender);
            return;
        }

        Faction faction = getSenderFactionOrFromArg(sender, args, 1, true);
        if (faction == null) {
            return;
        }

        if (!faction.isPrivileged(sender)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }

        String longName = new String();
        for (String arg : args) {
            if (args[0] != arg && args[i - 1] != arg) {
                if (!longName.isEmpty()) {
                    longName += " ";
                }
                longName += arg;
            }
        }
        if (longName.isEmpty()) {
            longName = faction.getName();
        }

        longName = longName.replace("&", new String());
        faction.setLongName(longName);
        ParsingUtil.broadcastMessage(FMessage.CMD_TAG_SUCCESS.getMessage(), faction, longName);
    }

}
