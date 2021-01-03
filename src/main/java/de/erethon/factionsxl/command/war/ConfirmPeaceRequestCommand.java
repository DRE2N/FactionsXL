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
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.scoreboard.FScoreboard;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.WarCache;
import de.erethon.factionsxl.war.WarParty;
import de.erethon.factionsxl.war.peaceoffer.FinalPeaceOffer;
import de.erethon.factionsxl.war.peaceoffer.SeparatePeaceOffer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;

/**
 * @author Malfrador
 */

public class ConfirmPeaceRequestCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    WarCache wars = FactionsXL.getInstance().getWarCache();

    public ConfirmPeaceRequestCommand() {
        setCommand("confirmPeace");
        setMinArgs(1);
        setMaxArgs(5);
        setHelp(FMessage.CMD_PEACE_CONFIRM_HELP.getMessage());
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
        War war = plugin.getWarCache().getByDate(Long.parseLong(args[1]));
        if (!(war.getAttacker().getFactions().contains(faction)) && !(war.getDefender().getFactions().contains(faction))) {
            MessageUtil.sendMessage(sender, FMessage.CMD_PEACE_CONFIRM_NOT_IN_WAR.getMessage());
            return;
        }
        if (args[2].contains("Single")) {
            Faction objectFaction = plugin.getFactionCache().getByName(args[3]);

            Faction subjectFaction = plugin.getFactionCache().getByName(args[4]);
            if (objectFaction == null) {
                ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_FACTION.getMessage(), args[3]);
                return;
            }

            if (subjectFaction == objectFaction) {
                ParsingUtil.sendMessage(sender, FMessage.ERROR_OWN_FACTION.getMessage());
                return;
            }
            Collection<SeparatePeaceOffer> requests = objectFaction.getRequests(SeparatePeaceOffer.class);
            MessageUtil.log(requests.toString());
            MessageUtil.log(objectFaction.getRequests(SeparatePeaceOffer.class).toString());

            SeparatePeaceOffer matching = null;
            for (SeparatePeaceOffer request : requests) {
                if (request.getSubject().equals(subjectFaction) && request.getObject().equals(objectFaction)) {
                    matching = request;
                    break;
                }
            }
            if (matching != null && faction.getStability() < matching.getCost() ) {
                matching.confirm();
                ParsingUtil.broadcastMessage(FMessage.CMD_PEACE_CONFIRM_SUCCESS.getMessage(), objectFaction, subjectFaction);
                return;
            }
            if (args[2].equals("-denySingle") && matching != null) {
                ParsingUtil.broadcastMessage(FMessage.CMD_PEACE_CONFIRM_REJECTED_FACTION.getMessage(), objectFaction);
                matching.purge();
                return;

            }
            if (matching != null) {
                matching.confirm();
                ParsingUtil.broadcastMessage(FMessage.CMD_PEACE_CONFIRM_SUCCESS.getMessage(), objectFaction, subjectFaction);
            }
            else {
                MessageUtil.sendMessage(player, "&cError. No Peace request found.");
            }


        } else if (args[2].contains("Final")) {
            Set<WarParty> warParties = faction.getWarParties();
            WarParty wp = null;
            for (WarParty w : warParties) {
                if (w.getLeader() == faction) {
                    wp = w;
                    break;
                }
            }
            if (wp == null) {
                MessageUtil.sendMessage(sender, FMessage.CMD_PEACE_CONFIRM_NOT_LEADER.getMessage());
                return;
            }
            Collection<FinalPeaceOffer> requests = null;
            try {
                MessageUtil.log("Getting final peace offers for " + wp.getName() + " (Leader: " + wp.getLeader().getName() + ")");
                requests = wp.getRequests(FinalPeaceOffer.class);
            } catch (NullPointerException e) {
                MessageUtil.log("Error: NPE while looking for FinalPeaceOffers  for " + wp.getName() + " found!");
                e.printStackTrace();
                MessageUtil.sendMessage(player, FMessage.CMD_PEACE_CONFIRM_EMPTY.getMessage());
            }
            if (requests == null) {
                return;
            }
            FinalPeaceOffer peace = null;
            for (FinalPeaceOffer p : requests) {
                if (p.getWar() == war) {
                    peace = p;
                    break;
                }
            }
            if (peace == null) {
                MessageUtil.log("Error: No FinalPeaceOffers  for " + wp.getName() + " found!");
                return;
            }
            if (wp.getEnemy().getPoints() >= 50) { // Auto confirm if enemy has over 50 score
                if (!peace.canPay()) {
                    MessageUtil.sendMessage(player, FMessage.CMD_PEACE_CONFIRM_CANTAFFORD.getMessage());
                    return;
                }
                peace.confirm();
                FScoreboard.updateAllProviders();
                return;
            }
            if (args[2].equals("-denyFinal")) {
                ParsingUtil.broadcastMessage(FMessage.CMD_PEACE_CONFIRM_REJECTED_WARPARTY.getMessage(), wp.getName());
                peace.purge();
                if (peace.canPay() && !peace.isOffer()) {
                    for (Faction f : wp.getFactions()) {
                        f.setExhaustion(f.getExhaustion() + 2);
                    }
                }
                return;
            }
            if (!peace.canPay()) {
                MessageUtil.sendMessage(player, FMessage.CMD_PEACE_CONFIRM_CANTAFFORD.getMessage());
                return;
            }
            peace.confirm();
            FScoreboard.updateAllProviders();


        }
        else {
            MessageUtil.log("Invalid argument. This should never happen.");
        }
    }
}