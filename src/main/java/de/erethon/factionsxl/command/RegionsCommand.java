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
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.FPermission;
import org.bukkit.command.CommandSender;

/**
 * @author Daniel Saukel
 */
public class RegionsCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();
    Board board = plugin.getBoard();

    public RegionsCommand() {
        setCommand("regions");
        setMinArgs(0);
        setMaxArgs(0);
        setHelp(FMessage.HELP_REGIONS.getMessage());
        setPermission(FPermission.REGIONS.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        for (Region region : plugin.getBoard().getRegions()) {
            sender.sendMessage(region.getId() + " / " + region.getName());
        }
    }

}
