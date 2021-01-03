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

import de.erethon.commons.misc.SimpleDateUtil;
import de.erethon.factionsxl.FactionsXL;
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
public class SetCapitalCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public SetCapitalCommand() {
        setCommand("setCapital");
        setMinArgs(0);
        setMaxArgs(0);
        setHelp(FMessage.HELP_SET_CAPITAL.getMessage());
        setPermission(FPermission.SET_CAPITAL.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        Faction faction = plugin.getFactionCache().getByMember(player);
        if (faction == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_JOIN_FACTION.getMessage());
            return;
        }
        if (!faction.isAdmin(player)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }

        Region region = plugin.getBoard().getByLocation(player.getLocation());
        if (region == null || region.isNeutral()) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_LAND_WILDERNESS.getMessage());
            return;
        }
        if (!region.getOwner().equals(faction)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_LAND_NOT_FOR_SALE.getMessage());
            return;
        }
        if (!region.getCoreFactions().containsKey(faction)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_LAND_NO_CORE.getMessage());
            return;
        }
        if (faction.isInWar()) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_AT_WAR.getMessage());
            return;
        }
        long moveAllowedTime = faction.getTimeLastCapitalMove() + plugin.getFConfig().getMoveCapitalCooldown();
        FactionsXL.debug("setCapitalCommand: timeLastCapitalMove=" + SimpleDateUtil.ddMMyyyyhhmm(faction.getTimeLastCapitalMove())
                + "; moveAllowedTime=" + SimpleDateUtil.ddMMyyyyhhmm(moveAllowedTime) + "; system=" + SimpleDateUtil.ddMMyyyyhhmm(System.currentTimeMillis()));
        if (moveAllowedTime > System.currentTimeMillis()) {
            ParsingUtil.sendMessage(player, FMessage.ERROR_CAPITAL_MOVE_COOLDOWN.getMessage(), SimpleDateUtil.ddMMyyyyhhmm(moveAllowedTime));
            return;
        }

        faction.setCapital(region);
        ParsingUtil.broadcastMessage(FMessage.CMD_SET_CAPITAL_SUCCESS.getMessage(), faction, region);
    }

}
