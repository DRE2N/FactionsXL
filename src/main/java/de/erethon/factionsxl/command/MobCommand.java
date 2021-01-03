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
import de.erethon.factionsxl.faction.FMob;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class MobCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public MobCommand() {
        setCommand("mob");
        setMinArgs(1);
        setMaxArgs(1);
        setHelp(FMessage.HELP_MOB.getMessage());
        setPermission(FPermission.MOB.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        Region region = plugin.getBoard().getByLocation(player.getLocation());
        if (region == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_LAND_WILDERNESS.getMessage());
            return;
        }
        Faction faction = region.getOwner();
        if (faction == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_LAND_WILDERNESS.getMessage());
            return;
        }
        if (!faction.isPrivileged(player)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }

        String type = FMob.fromString(args[1]);
        if (FMessage.MOB_TRADER.getMessage().equals(type)) {
            FMob.spawnTrader(player.getLocation());
        } else if (FMessage.MOB_VILLAGER.getMessage().equals(type)) {
            FMob.spawnVillager(player.getLocation());
        } else {
            displayHelp(player);
        }
    }

}
