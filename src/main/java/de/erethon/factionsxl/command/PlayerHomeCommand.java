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
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.util.CooldownTeleportationTask;
import de.erethon.factionsxl.util.ParsingUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class PlayerHomeCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FConfig config = plugin.getFConfig();
    Economy econ = plugin.getEconomyProvider();

    public PlayerHomeCommand() {
        setCommand("playerHome");
        setAliases("playerSpawn", "pHome", "pSpawn");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.HELP_PLAYER_HOME.getMessage());
        setPermission(FPermission.HOME.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        FPlayer fPlayer;
        if (args.length == 2 && FPermission.hasPermission(sender, FPermission.HOME_OTHERS)) {
            fPlayer = plugin.getFPlayerCache().getByName(args[1]);
        } else {
            fPlayer = plugin.getFPlayerCache().getByPlayer(player);
        }

        if (fPlayer == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_PLAYER.getMessage(), args[1]);
        }

        if (fPlayer.getHome() == null) {
            plugin.getCommandCache().setPlayerHome.displayHelp(sender);
            return;
        }
        if (!fPlayer.checkHome()) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_HOME_NOT_IN_ALLIED_TERRITORY.getMessage());
            return;
        }

        if (FPermission.hasPermission(sender, FPermission.BYPASS)) {
            player.teleport(fPlayer.getHome());
            return;
        }

        new CooldownTeleportationTask(player, fPlayer.getHome(), true).runTaskTimer(plugin, 0L, 20L);
    }

}
