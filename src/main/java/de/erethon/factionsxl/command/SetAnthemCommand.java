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
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class SetAnthemCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public SetAnthemCommand() {
        setCommand("setAnthem");
        setMinArgs(1);
        setMaxArgs(2);
        setHelp(FMessage.HELP_SET_ANTHEM.getMessage());
        setPermission(FPermission.SET_ANTHEM.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        i = 1;
        Faction faction = getSenderFactionOrFromArg(sender, args, 1, true);
        if (faction == null) {
            return;
        }

        if (sender instanceof Player && !faction.isAdmin(sender)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }

        faction.setAnthem(args[i]);
        faction.sendMessage(FMessage.CMD_SET_ANTHEM_SUCCESS.getMessage(), sender, faction, args[i]);
    }

}
