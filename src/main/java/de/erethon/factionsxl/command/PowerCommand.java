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
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.player.FPlayerCache;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.command.CommandSender;

/**
 * @author Daniel Saukel
 */
public class PowerCommand extends FCommand {

    FPlayerCache fPlayers = FactionsXL.getInstance().getFPlayerCache();

    public PowerCommand() {
        setCommand("power");
        setAliases("pow");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.HELP_POWER.getMessage());
        setPermission(FPermission.POWER.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        FPlayer fPlayer = getFSenderOrFromArg(sender, args, 1);
        if (fPlayer != null) {
            ParsingUtil.sendMessage(sender, FMessage.CMD_POWER.getMessage(), fPlayer, fPlayer.getPower());
        }
    }

}
