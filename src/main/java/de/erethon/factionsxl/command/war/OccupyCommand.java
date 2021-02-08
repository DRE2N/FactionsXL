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
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.command.FCommand;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.event.WarRegionAttackEvent;
import de.erethon.factionsxl.event.WarRegionOccupiedEvent;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.*;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.Set;

import static de.erethon.factionsxl.war.CasusBelli.Type.*;

/**
 * @author Malfrador
 */
public class OccupyCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FConfig config = plugin.getFConfig();
    WarPoints points = plugin.getWarPoints();
    WarHandler handler = plugin.getWarHandler();

    public OccupyCommand() {
        setCommand("occupy");
        setAliases("a", "annex", "o", "attack");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.WAR_OCCUPY_HELP.getMessage());
        setPermission(FPermission.CLAIM.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        Faction faction = getSenderFactionOrFromArg(sender, args, 1);
        Region region = plugin.getBoard().getByLocation(player.getLocation());
        if (region == null) {
            MessageUtil.sendMessage(player, FMessage.ERROR_LAND_WILDERNESS.getMessage());
            return;
        }
        if (region.getOwner() == null) {
            MessageUtil.sendMessage(player, FMessage.ERROR_LAND_WILDERNESS.getMessage());
            return;
        }

        Faction annexFrom = region.getOwner();
        if (region.getOccupant() != null) {
            annexFrom = region.getOccupant();
        }
        if (!faction.isPrivileged(player)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }
        if ( !(faction.isInWar() && annexFrom.isInWar()) ) {
            MessageUtil.sendMessage(player, FMessage.WAR_OCCUPY_NOT_AT_WAR.getMessage());
            return;
        }
        if (region.getOccupant() != null) {
            for (WarParty warParty : faction.getWarParties()) {
                if (warParty.getFactions().contains(region.getOccupant())) {
                    ParsingUtil.sendMessage(sender, FMessage.WAR_OCCUPY_ALREADY_OCCUPIED.getMessage());
                    return;
                }
            }
        }


        if (annexFrom == faction) {
            ParsingUtil.sendMessage(sender, FMessage.WAR_OCCUPY_ALREADY_OCCUPIED.getMessage());
            return;
        }

        Set<War> warSet = plugin.getWarCache().getByFaction(faction);
        War war = null;
        for (War w : warSet) {
            if ((w.getAttacker().getFactions().contains(annexFrom)) || (w.getDefender().getFactions().contains(annexFrom))) {
                war = w;
            }
        }
        if (war == null) {
            ParsingUtil.sendMessage(sender, FMessage.WAR_OCCUPY_NOT_AT_WAR.getMessage());
            return;
        }

        if (war.getCasusBelli().getType() == RAID) {
            ParsingUtil.sendMessage(sender, FMessage.WAR_OCCUPY_RAID.getMessage());
            return;
        }

        WarParty factionWP = null;
        for (WarParty wp : faction.getWarParties()) {
            if (wp.getWar() == war) {
                factionWP = wp;
            }
        }
        if (factionWP == null || (factionWP.getFactions().contains(region.getOwner()) && region.getOccupant() == null)) {
            MessageUtil.sendMessage(player, FMessage.WAR_OCCUPY_NOT_AT_WAR.getMessage());
            return;
        }

        double price;
        if (!(war.getTruce())) {
            Faction enemyLeader = (Faction) factionWP.getEnemy().getLeader();
            long now = System.currentTimeMillis();
            if (region.getAttackStartTime() == 0) {
                if (region.isAttackable(factionWP)) {
                if (enemyLeader.getCapital().equals(region) && getOccupiedRegionsOfLeader(factionWP.getEnemy()) < (enemyLeader.getRegions().size() * 0.75)) {
                    MessageUtil.sendMessage(sender, "&cDu kannst die feindliche Hauptstadt erst angreifen, wenn du 75% der Regionen des Feindes besetzt hast.");
                    return;
                }
                if (region.isAttacked()) {
                    MessageUtil.sendMessage(sender, "&cDiese Region wird bereits angegriffen.");
                    return;
                }
                if (region.getLastDefendedTime() != 0 && (now < (region.getLastDefendedTime() + 172800000))) { // 48 Stunden
                    MessageUtil.sendMessage(sender, "&cDiese Region ist noch geschützt.");
                    return;
                }
                if (plugin.getOccupationManager().isAlreadyAttacked(annexFrom)) {
                    MessageUtil.sendMessage(sender, "&cEine Region dieser Fraktion wird bereits angegriffen.");
                    return;
                }
                if (!plugin.getOccupationManager().canStartOccupation(factionWP, factionWP.getEnemy())) {
                    MessageUtil.sendMessage(sender, "&cDu kannst aktuell keinen Angriff starten. Der Beteiligungs-Unterschied ist zu groß.");
                    MessageUtil.sendMessage(sender, "&7&oEventuell ist die Besitzer-Kriegspartei nicht aktiv oder eure eigenen Kriegsbeteiligung ist zu hoch.");
                    return;
                }
                region.setAttackStartTime(System.currentTimeMillis());
                MessageUtil.sendMessage(sender, "&aAngriff gestartet! Der Feind erhält 20 Minuten Vorbereitungszeit. Der Angriff dauert insgesamt 120 Minuten.");
                for (Faction f : factionWP.getEnemy().getFactions()) {
                    f.sendMessage("&aEure Region &6" + region.getName() + " &awird angegriffen!");
                }

                WarRegionAttackEvent event = new WarRegionAttackEvent(factionWP, factionWP.getEnemy(), region);
                Bukkit.getPluginManager().callEvent(event);

                } else {
                    MessageUtil.sendMessage(sender, "&cDu kannst Regionen nur angreifen wenn sie an eigene oder besetzte Regionen angrenzen.");
                }
                return;
            }

            if (region.getInfluence() <= config.getInfluenceNeeded() || ((region.getCoreFactions().containsKey(faction)) && (config.getInfluenceNeeded() * 2 >= region.getInfluence()))) {
                price = region.getClaimPrice(faction) * (region.getInfluence() + 1); // Multiply base price by influence. You can annex earlier, but its more expensive
                // Price for region with cores of owner is price * 2
                if (region.getCoreFactions().containsKey(annexFrom)) {
                    price = price * 2;
                }
                // Price for regions with own claims is price/4
                if (region.getClaimFactions().containsKey(faction)) {
                    price = price / 4;
                }
                if (region.getInfluence() == 0) {
                    price = 0;
                }
                if (faction.getAccount().getBalance() < price) {
                    ParsingUtil.sendMessage(player, FMessage.ERROR_NOT_ENOUGH_MONEY_FACTION.getMessage(), faction, String.valueOf(price));
                    return;
                }
                ParsingUtil.sendMessage(player, FMessage.FACTION_PAID.getMessage(), faction, String.valueOf(price));
                faction.getAccount().withdraw(price);
                for (WarParty wp : faction.getWarParties()) {
                    if (wp.getFactions().contains(faction)) {
                        if (region.getCoreFactions().containsKey(region.getOwner())) {
                            points.updateScore(wp, WarAction.OCCUPY_CORE);
                        } else if (region.getClaimFactions().containsKey(faction)) {
                            points.updateScore(wp, WarAction.OCCUPY_CLAIM);
                        } else if (wp.getWar().getCasusBelli().getTarget() == region.getOwner() && wp.getRole() == WarPartyRole.ATTACKER) {
                            points.updateScore(wp, WarAction.OCCUPY_WAR_TARGET);
                        } else if (annexFrom.getCapital() == region) {
                            points.updateScore(wp, WarAction.OCCUPY_CAPITAL);
                        } else if (region.getCoreFactions().containsKey(faction)) {
                            points.updateScore(wp, WarAction.REOCCUPY_OWN_CORE);
                        } else {
                            points.updateScore(wp, WarAction.OCCUPY);
                        }
                    }
                }
                if (getOccupiedRegionsOfLeader(factionWP.getEnemy()) >= enemyLeader.getRegions().size()) {
                    handler.forceWarGoal(factionWP);
                }
                region.setOccupant(faction);
                region.setAttacked(false);
                region.setAttackStartTime(0);
                if (region.getOwner() == region.getOccupant()) {
                    region.clearOccupant();
                    region.setOwner(faction);
                }

                region.getClaimFactions().putIfAbsent(annexFrom, Calendar.getInstance().getTime());
                region.setInfluence((int) (config.getInfluenceNeeded() + 10));

                faction.sendMessage(FMessage.WAR_OCCUPY_SUCCESS.getMessage(), region);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 10, 1);

                WarRegionOccupiedEvent event = new WarRegionOccupiedEvent(factionWP, factionWP.getEnemy(), region, faction);
                Bukkit.getPluginManager().callEvent(event);

                plugin.getWarHandler().calculateWarStatus();

            } else {
                MessageUtil.sendMessage(player, FMessage.WAR_OCCUPY_INFLUENCE_TOO_HIGH.getMessage());
            }
        }
        else {
            MessageUtil.sendMessage(player, FMessage.WAR_OCCUPY_TRUCE.getMessage());
        }
    }

    public int getOccupiedRegionsOfLeader(WarParty wp) {
        int occupiedRegions = 0;
        Faction leader = (Faction) wp.getLeader();
        for (Region r : leader.getRegions()) {
            if (r.getOccupant() != null) {
                occupiedRegions++;
            }
        }
        return occupiedRegions;
    }

}