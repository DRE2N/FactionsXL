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
import de.erethon.factionsxl.board.Board;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class UnclaimCommand extends FCommand {

    Board board = FactionsXL.getInstance().getBoard();

    public UnclaimCommand() {
        setCommand("unclaim");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.HELP_UNCLAIM.getMessage());
        setPermission(FPermission.CLAIM.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        Region region;
        if (args.length >= 2) {
            region = board.getByName(args[1]);
            if (region == null) {
                int id = 0;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException exception) {
                    ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_REGION.getMessage(), args[1]);
                    return;
                }
                region = board.getById(id);
            }
        } else {
            region = board.getByLocation(((Player) sender).getLocation());
        }
        if (region == null || region.isNeutral()) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_LAND_WILDERNESS.getMessage());
            return;
        }

        Faction faction = region.getOwner();
        if (!faction.isPrivileged(player)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }

        if (faction.getCapital().equals(region)) {
            ParsingUtil.sendMessage(sender, FMessage.CMD_UNCLAIM_FAIL.getMessage());
            return;
        }

        region.setOwner(null);
        region.getClaimFactions().remove(faction);
        region.getCoreFactions().remove(faction);
        ParsingUtil.sendMessage(sender, FMessage.CMD_UNCLAIM_SUCCESS.getMessage(), region);
    }

}
