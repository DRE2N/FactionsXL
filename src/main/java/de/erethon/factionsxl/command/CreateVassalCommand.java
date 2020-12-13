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
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.event.FPlayerFactionLeaveEvent;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.scoreboard.FTeamWrapper;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.CasusBelli;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;

/**
 * @author Daniel Saukel
 */
public class CreateVassalCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    Board board = plugin.getBoard();
    FConfig config = plugin.getFConfig();

    public CreateVassalCommand() {
        setCommand("createVassal");
        setAliases("newVassal");
        setMinArgs(2);
        setMaxArgs(2);
        setHelp(FMessage.HELP_CREATE_VASSAL.getMessage());
        setPermission(FPermission.CREATE_VASSAL.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        FPlayer fPlayer = plugin.getFPlayerCache().getByPlayer(player);
        Faction mother = fPlayer.getFaction();
        if (mother == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_JOIN_FACTION.getMessage());
            return;
        } else if (!mother.isAdmin(player)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }
        if (mother.isInWar()) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_IN_WAR.getMessage());
            return;
        }

        Location location = player.getLocation();
        Region region = board.getByLocation(location);
        if (region.getOwner() == null || !region.getOwner().equals(mother)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_CANNOT_PASS_LAND.getMessage());
            return;
        } else if (region.equals(mother.getCapital()) || mother.getRegions().size() <= 1) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_CANNOT_PASS_CAPITAL.getMessage());
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
            if (mother.getAccount().getBalance() < config.getPriceCreateVassal()) {
                ParsingUtil.sendMessage(sender, FMessage.ERROR_NOT_ENOUGH_MONEY_FACTION.getMessage(), mother, String.valueOf(config.getPriceCreateVassal()));
                return;
            } else {
                mother.getAccount().withdraw(config.getPriceCreateVassal());
            }
        }

        OfflinePlayer leader = Bukkit.getOfflinePlayer(args[2]);
        if (!leader.hasPlayedBefore()) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_PLAYER.getMessage(), args[2]);
            return;
        }
        if (mother.isAdmin(leader.getUniqueId())) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_VASSAL_IS_MOTHER_ADMIN.getMessage(), fPlayer);
            return;
        }

        mother.getMods().remove(leader);
        mother.getMembers().remove(leader);
        if (leader.isOnline()) {
            mother.getOnlineMods().remove(leader.getPlayer());
            mother.getOnlineMembers().remove(leader.getPlayer());
        }
        FTeamWrapper.updatePrefixes(mother);

        FPlayerFactionLeaveEvent event = new FPlayerFactionLeaveEvent(plugin.getFPlayerCache().getByPlayer(leader), mother);
        Bukkit.getPluginManager().callEvent(event);

        Faction vassal = plugin.getFactionCache().create(leader, location, args[1]);
        mother.getRelations().put(vassal, Relation.VASSAL);
        vassal.getRelations().put(mother, Relation.LORD);
        vassal.getCasusBelli().add(new CasusBelli(CasusBelli.Type.INDEPENDENCE, mother, new Date(System.currentTimeMillis() + (config.getCBLiberationExp() * FConfig.DAY))));
        vassal.setAllod(false);
        ParsingUtil.broadcastMessage(FMessage.CMD_CREATE_SUCCESS.getMessage(), sender.getName(), args[1]);
        ParsingUtil.broadcastMessage(FMessage.RELATION_VASSALIZED.getMessage(), mother, vassal);
    }

}
