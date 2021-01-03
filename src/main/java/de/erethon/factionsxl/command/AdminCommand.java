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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/**
 * @author Daniel Saukel
 */
public class AdminCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public AdminCommand() {
        setCommand("admin");
        setAliases("leader");
        setMinArgs(1);
        setMaxArgs(2);
        setHelp(FMessage.HELP_ADMIN.getMessage());
        setPermission(FPermission.ADMIN.getNode());
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

        if (!faction.isAdmin(sender) && !FPermission.hasPermission(sender, FPermission.BYPASS)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage(), args[i]);
            return;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[i]);
        if (!player.hasPlayedBefore()) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_PLAYER.getMessage(), args[i]);
            return;
        }
        if (!faction.getMembers().contains(player)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_PLAYER_NOT_IN_FACTION.getMessage(), player.getName(), faction.getName());
            return;
        }

        faction.setAdmin(player);
        ParsingUtil.broadcastMessage(FMessage.CMD_ADMIN_SUCCESS.getMessage(), sender, player, faction);
    }

}
