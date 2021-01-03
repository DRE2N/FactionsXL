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
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.War;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.Set;

/**
 * @author Daniel Saukel
 */
public class ClaimCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FConfig config = plugin.getFConfig();

    public ClaimCommand() {
        setCommand("claim");
        setAliases("cl");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.HELP_CLAIM.getMessage());
        setPermission(FPermission.CLAIM.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        Faction faction = getSenderFactionOrFromArg(sender, args, 1);
        if (faction == null) {
            return;
        }

        if (!faction.isPrivileged(player)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }

        Region region = plugin.getBoard().getByLocation(player.getLocation());
        Set<Region> regions = faction.getRegions();
        if (region != null && !region.isWildernessClaim()) {
            boolean nextTo = false;
            for (Region r : region.getNeighbours()) {
                if (r.getOwner() == faction) {
                    nextTo = true;
                }
            }
            if (!nextTo) {
                ParsingUtil.sendMessage(sender, FMessage.ERROR_LAND_NOT_CONNECTED.getMessage());
                return;
            }
        }
        if (region == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_LAND_NOT_FOR_SALE.getMessage());
            return;
        }
        if (region.getCoreFactions().containsKey(faction)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_REGION_IS_CORE.getMessage(), region, faction);
            return;
        }
        if (region.getClaimFactions().containsKey(faction)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_REGION_IS_ALREADY_CLAIMED.getMessage(), region, faction);
            return;
        }

        if (region.getOwner() == faction) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_LAND_NOT_FOR_SALE.getMessage(), region, faction);
            return;
        }
        War war = plugin.getWarCache().getWarTogether(region.getOwner(), faction );
        if (region.getOwner() != null && war != null) {
            if (war.getAttacker().getFactions().contains(faction)) {
                ParsingUtil.sendMessage(sender, FMessage.ERROR_IN_WAR.getMessage());
                return;
            }
        }

        if (plugin.getFConfig().isEconomyEnabled()) {
            double price = region.getClaimPrice(faction);
            if (region.getCoreFactions().containsKey(region.getOwner())) {
                price = price * 2;
            }
            if (faction.getAccount().getBalance() < price) {
                ParsingUtil.sendMessage(player, FMessage.ERROR_NOT_ENOUGH_MONEY_FACTION.getMessage(), faction, String.valueOf(price));
                return;
            } else {
                ParsingUtil.sendMessage(player, FMessage.FACTION_PAID.getMessage(), faction, String.valueOf(price));
                faction.getAccount().withdraw(price);
            }
        }

        if (region.isNeutral()) {
            region.setInfluence(50);
            region.setOwner(faction);
        }
        else {
            region.getClaimFactions().put(faction, Calendar.getInstance().getTime());
            plugin.getCBManager().addConquestOrSubjagation(faction, region.getOwner());
        }
        ParsingUtil.sendMessage(sender, FMessage.CMD_CLAIM_SUCCESS.getMessage(), region);
    }

}
