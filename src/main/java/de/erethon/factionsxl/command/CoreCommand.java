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

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Malfrador
 */

public class CoreCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FConfig config = plugin.getFConfig();

    public CoreCommand() {
        setCommand("core");
        setAliases("co");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.HELP_CORE.getMessage());
        setPermission(FPermission.CLAIM.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        Faction faction = getSenderFactionOrFromArg(sender, args, 1);
        Region region = plugin.getBoard().getByLocation(player.getLocation());
        if (!(region.getOwner() == faction)) {
            MessageUtil.sendMessage(player, FMessage.ERROR_LAND_NOT_OWNED.getMessage());
            return;
        }
        if (!faction.isPrivileged(player)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }
        if (faction.isInWar()) {
            ParsingUtil.sendMessage(player, FMessage.ERROR_AT_WAR.getMessage());
            return;
        }
        double amount = region.getClaimPrice(faction) * config.getPriceCoreMultiplier();
        if (plugin.getFConfig().isEconomyEnabled()) {
            if (faction.getAccount().getBalance() < amount) {
                ParsingUtil.sendMessage(player, FMessage.ERROR_NOT_ENOUGH_MONEY_FACTION.getMessage(), faction, String.valueOf(amount));
                return;
            } else {
                ParsingUtil.sendMessage(player, FMessage.FACTION_PAID.getMessage(), faction, String.valueOf(amount));
                faction.getAccount().withdraw(amount);
            }
        }
        ParsingUtil.sendMessage(sender, FMessage.FACTION_NEW_CORE.getMessage(), faction, region);
        region.setCoringProgress(faction, 0);
    }
}

