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

/**
 * @author Daniel Saukel
 */
public class OpenCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public OpenCommand() {
        setCommand("open");
        setAliases("setOpen");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.HELP_OPEN.getMessage());
        setPermission(FPermission.OPEN.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Faction faction = getSenderFactionOrFromArg(sender, args, 1);
        if (faction == null) {
            return;
        }

        if (!faction.isPrivileged(sender)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }

        faction.setOpen(!faction.isOpen());
        if (faction.isOpen()) {
            faction.sendMessage(FMessage.CMD_OPEN_OPENED.getMessage(), sender);
        } else {
            faction.sendMessage(FMessage.CMD_OPEN_CLOSED.getMessage(), sender);
        }
    }

}
