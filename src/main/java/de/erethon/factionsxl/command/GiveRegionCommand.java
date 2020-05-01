/*
 *
 *  * Copyright (C) 2017-2020 Daniel Saukel, Malfrador
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.erethon.factionsxl.command;

import de.erethon.commons.misc.NumberUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Board;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class GiveRegionCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();
    Board board = plugin.getBoard();

    public GiveRegionCommand() {
        setCommand("giveRegion");
        setAliases("giveLand", "giveProvince", "gr", "gl", "gp");
        setMinArgs(1);
        setMaxArgs(1);
        setHelp(FMessage.HELP_GIVE_REGION.getMessage());
        setPermission(FPermission.CREATE_VASSAL.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Region region = board.getByLocation(((Player) sender).getLocation());
        Faction faction = factions.getByName(args[1]);
        if (faction == null) {
            faction = factions.getById(NumberUtil.parseInt(args[1], -1));
        }
        if (faction == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_FACTION.getMessage(), args[1]);
            return;
        }
        if (region.getOwner() == null || !region.getOwner().isAdmin(sender)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }
        region.setOwner(faction);
        if (region.getCoreFactions().containsKey(region.getOwner())) {
            region.getCoreFactions().put(faction, region.getCoreFactions().get(region.getOwner()));
            region.getCoreFactions().remove(region.getOwner());
        }
        ParsingUtil.broadcastMessage(FMessage.CMD_GIVE_REGION_SUCCESS.getMessage(), region.getOwner(), region, faction);
    }

}
