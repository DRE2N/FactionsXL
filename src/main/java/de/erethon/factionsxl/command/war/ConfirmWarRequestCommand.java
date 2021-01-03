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

package de.erethon.factionsxl.command.war;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.command.FCommand;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.WarCache;
import de.erethon.factionsxl.war.WarRequest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * @author Malfrador
 */

public class ConfirmWarRequestCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();
    WarCache wars = FactionsXL.getInstance().getWarCache();

    public ConfirmWarRequestCommand() {
        setCommand("confirmWarRequest");
        setMinArgs(1);
        setMaxArgs(2);
        setHelp("Internal command to confirm a war invite");
        setPermission(FPermission.WAR.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        FPlayer fPlayer = getFSender(sender);
        Faction faction = getSenderFaction(sender);
        if (!(fPlayer.hasFaction())) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_PLAYER_NOT_IN_ANY_FACTION.getMessage());
            return;
        }
        if (!(faction.isAdmin(sender))) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }
        Faction f = factions.getByName(args[1]);
        Collection<WarRequest> requests = null;
        try {
            requests = faction.getRequests(WarRequest.class);
        }
        catch (NullPointerException e) {
            MessageUtil.sendMessage(player, "&cNo war requests or war request is empty.");
        }
        if (requests == null) {
            return;
        }
        WarRequest warReq = null;
        for (WarRequest r : requests) {
            if (r.getSubject() == f) {
                warReq = r;
                break;
            }
        }
        if (args.length == 3 && args[2].equalsIgnoreCase("-cancel")) {
            warReq.purge();
            MessageUtil.sendMessage(sender, FMessage.WAR_DECLARATION_CANCELLED.getMessage());
        } else {
            warReq.confirm();
        }
    }
}