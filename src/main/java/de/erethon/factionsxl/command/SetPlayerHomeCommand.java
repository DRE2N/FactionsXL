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
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class SetPlayerHomeCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public SetPlayerHomeCommand() {
        setCommand("setPlayerHome");
        setAliases("playerSetHome", "setPlayerSpawn", "setPHome", "setPSpawn");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.HELP_SET_PLAYER_HOME.getMessage());
        setPermission(FPermission.SET_HOME.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        FPlayer fPlayer;
        if (args.length == 2 && FPermission.hasPermission(sender, FPermission.BYPASS)) {
            fPlayer = plugin.getFPlayerCache().getByName(args[1]);
        } else {
            fPlayer = plugin.getFPlayerCache().getByPlayer(player);
        }

        Region region = plugin.getBoard().getByLocation(player.getLocation());
        if (region != null && region.getOwner() != null && (!region.getOwner().getRelation(fPlayer).canBuild()
                || region.getOwner().getRelation(fPlayer) == Relation.ENEMY) ) {
            ParsingUtil.sendMessage(sender, FMessage.CMD_SET_HOME_CHAR_FAIL.getMessage());
            return;
        }

        if (!fPlayer.checkHome(player.getLocation())) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_HOME_NOT_IN_ALLIED_TERRITORY.getMessage());
            return;
        }

        fPlayer.setHome(player.getLocation());
        ParsingUtil.sendMessage(player, FMessage.CMD_SET_HOME_CHAR_SUCCESS.getMessage());
    }

}
