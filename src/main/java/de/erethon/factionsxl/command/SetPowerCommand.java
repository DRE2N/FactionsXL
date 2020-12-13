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

import de.erethon.commons.misc.NumberUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/**
 * @author Daniel Saukel
 */
public class SetPowerCommand extends FCommand {

    public SetPowerCommand() {
        setCommand("setPower");
        setMinArgs(2);
        setMaxArgs(2);
        setHelp(FMessage.HELP_SET_POWER.getMessage());
        setPermission(FPermission.POWER_SET.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
        if (!player.hasPlayedBefore()) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_PLAYER.getMessage(), args[1]);
            return;
        }

        int power = NumberUtil.parseInt(args[2], 0);
        FactionsXL.getInstance().getFData().power.put(player.getUniqueId(), (double) power);
        ParsingUtil.sendMessage(sender, FMessage.CMD_SET_POWER_SENDER.getMessage(), player.getName(), String.valueOf(power));
        if (player.isOnline()) {
            ParsingUtil.sendMessage(player.getPlayer(), FMessage.CMD_SET_POWER_TARGET.getMessage(), sender, String.valueOf(power));
        }
    }

}
