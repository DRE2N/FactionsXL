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
import de.erethon.factionsxl.board.Board;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.util.ParsingUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class CreateCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FConfig config = plugin.getFConfig();
    Economy econ = plugin.getEconomyProvider();

    public CreateCommand() {
        setCommand("create");
        setAliases("new");
        setMinArgs(1);
        setMaxArgs(1);
        setHelp(FMessage.HELP_CREATE.getMessage());
        setPermission(FPermission.CREATE.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        FPlayer fPlayer = plugin.getFPlayerCache().getByPlayer(player);
        if (fPlayer.getFaction() != null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_LEAVE_FACTION.getMessage());
            return;
        }

        Location location = player.getLocation();
        Board board = plugin.getBoard();
        if (!board.isAnnexable(location)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_LAND_NOT_FOR_SALE.getMessage());
            return;
        }

        if (plugin.getFactionCache().getByName(args[1]) != null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NAME_IN_USE.getMessage(), args[1]);
            return;
        }

        if (config.isNameForbidden(args[1])) {
            // TODO: FMessage (with next big update)
            ParsingUtil.sendMessage(sender, "&4Dieser Fraktions-Name ist nicht erlaubt.");
            return;
        }

        if (config.isEconomyEnabled()) {
            if (!econ.has(player, config.getPriceCreate())) {
                ParsingUtil.sendMessage(sender, FMessage.ERROR_NOT_ENOUGH_MONEY.getMessage(), String.valueOf(config.getPriceCreate()));
                return;
            } else {
                econ.withdrawPlayer(player, config.getPriceCreate());
            }
        }

        plugin.getFactionCache().create(player, args[1]);
        fPlayer.getData().addCreated();
        ParsingUtil.broadcastMessage(FMessage.CMD_CREATE_SUCCESS.getMessage(), sender.getName(), args[1]);
    }

}
