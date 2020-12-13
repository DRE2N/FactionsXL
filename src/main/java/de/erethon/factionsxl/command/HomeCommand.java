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
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.CooldownTeleportationTask;
import de.erethon.factionsxl.util.ParsingUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class HomeCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FConfig config = plugin.getFConfig();
    Economy econ = plugin.getEconomyProvider();

    public HomeCommand() {
        setCommand("home");
        setAliases("spawn");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.HELP_HOME.getMessage());
        setPermission(FPermission.HOME.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        Faction faction;
        if (args.length == 2 && FPermission.hasPermission(sender, FPermission.HOME_OTHERS)) {
            faction = plugin.getFactionCache().getByName(args[1]);
        } else {
            faction = plugin.getFactionCache().getByMember(player);
        }

        if (faction == null) {
            if (args.length == 1) {
                ParsingUtil.sendMessage(sender, FMessage.ERROR_JOIN_FACTION.getMessage());
            } else if (args.length == 2) {
                ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_FACTION.getMessage(), args[1]);
            }
            return;
        }

        if (FPermission.hasPermission(sender, FPermission.BYPASS)) {
            player.teleport(faction.getHome());
            return;
        }

        new CooldownTeleportationTask(player, faction.getHome(), true).runTaskTimer(plugin, 0L, 20L);
    }

}
