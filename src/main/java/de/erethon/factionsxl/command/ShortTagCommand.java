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

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class ShortTagCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public ShortTagCommand() {
        setCommand("shortTag");
        setAliases("setShortTag", "shortName", "setshortName");
        setMinArgs(1);
        setMaxArgs(2);
        setHelp(FMessage.HELP_SHORT_TAG.getMessage());
        setPermission(FPermission.TAG.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        i = 1;
        Faction faction = null;
        if (args.length == 3) {
            faction = getSenderFactionOrFromArg(sender, args, 1, true);
            if (faction == null) {
                return;
            }
        } else {
            faction = getSenderFaction(sender);
            if (faction == null) {
                ParsingUtil.sendMessage(sender, FMessage.ERROR_SPECIFY_FACTION.getMessage());
                return;
            }
        }

        if (sender instanceof Player && !faction.isAdmin(sender)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }

        String shortTag = args[i].replace("&", new String()).toUpperCase();
        if (shortTag.length() > 11) {
            shortTag = shortTag.substring(0, 10);
        }
        ParsingUtil.broadcastMessage(FMessage.CMD_TAG_SUCCESS.getMessage(), faction, shortTag);
        faction.setShortName(shortTag);
    }

}
