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

package de.erethon.factionsxl.war;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.player.FPlayerCache;
import de.erethon.factionsxl.util.FDebugLevel;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class OccupationManager {

    FactionsXL plugin = FactionsXL.getInstance();
    FConfig config = plugin.getFConfig();
    FPlayerCache fplayers = plugin.getFPlayerCache();

    public void updateOccupationStatus() {
        long now = System.currentTimeMillis();

        for (War war : plugin.getWarCache().getWars()) {
            for (Faction f : war.getAttacker().getFactions()) {
                for (Region rg : f.getRegions()) {
                    if (rg.getAttackStartTime() != 0) {
                        if (!rg.isAttacked() && isPreparationOver(rg)) {
                            rg.setAttacked(true);
                            MessageUtil.log("Region " + rg.getName() + " is now attacked.");
                            f.sendMessage("&aEure Region &6" + rg.getName() + "&a verliert nun minütlich Einfluss. Verteidigt sie!");
                        }
                        if (rg.isAttacked()  && rg.getInfluence() > 0) {
                            FactionsXL.debug(FDebugLevel.WAR, "Checking influence for " + rg.getName());
                            reduceInfluence(war.getDefender(), war.getAttacker(), rg);
                        }
                        if (rg.isAttacked() && (rg.getAttackStartTime() + 7200000) < now) {
                            rg.setAttacked(false);
                            MessageUtil.log("Region " + rg.getName() + " is now safe.");
                            rg.setAttackStartTime(0);
                            rg.setLastDefendedTime(now);
                            MessageUtil.broadcastMessage("&aDie Region &6" + rg.getName() + "&a wurde erfolgreich verteidigt!");
                            MessageUtil.broadcastMessage("&aSie ist nun für 48 Stunden vor Angriffen geschützt.");
                        }
                    }
                }
            }
            for (Faction f : war.getDefender().getFactions()) {
                for (Region rg : f.getRegions()) {
                    if (rg.getAttackStartTime() != 0) {
                        if (!rg.isAttacked() && isPreparationOver(rg)) {
                            rg.setAttacked(true);
                            MessageUtil.log("Region " + rg.getName() + " is now attacked.");
                            f.sendMessage("&aEure Region &6" + rg.getName() + "&a verliert nun minütlich Einfluss. Verteidigt sie!");
                        }
                        if (rg.isAttacked() && rg.getInfluence() > 0) {
                            FactionsXL.debug(FDebugLevel.WAR, "Checking influence for " + rg.getName());
                            reduceInfluence(war.getAttacker(), war.getDefender(), rg);
                        }
                        if (rg.isAttacked() && (rg.getAttackStartTime() + 7200000) <  now) {
                            rg.setAttacked(false);
                            MessageUtil.log("Region " + rg.getName() + " is now safe.");
                            rg.setAttackStartTime(0);
                            rg.setLastDefendedTime(now);
                            MessageUtil.broadcastMessage("&aDie Region &6" + rg.getName() + "&a wurde erfolgreich verteidigt!");
                            MessageUtil.broadcastMessage("&aSie ist nun für 48 Stunden vor Angriffen geschützt.");
                        }
                    }
                }
            }
        }
    }

    public void reduceInfluence(WarParty attacker, WarParty defender, Region rg) {
        FactionsXL.debug(FDebugLevel.WAR, "Checking influence in " + rg.getName() + " for attacker " + attacker.getName() + " and defender " + defender.getName());
        if (rg.getCoreFactions().containsKey(rg.getOwner()) && isInRegion(defender, rg, true) && rg.getInfluence() <= 50) {
            FactionsXL.debug(FDebugLevel.WAR, "Region: " + rg.getName() + ": Influence not reduced: defender in region (Offline prot.) & influence < 50 (Core)");
            return;
        }
        if (isInRegion(defender, rg, true) && rg.getInfluence() <= 25) {
            FactionsXL.debug(FDebugLevel.WAR, "Region: " + rg.getName() + ": Influence not reduced: defender in region (Offline prot.) & influence < 25 (non Core)");
            return;
        }
        if (rg.getOccupant() != null && attacker.getFactions().contains(rg.getOccupant())) {
            FactionsXL.debug(FDebugLevel.WAR, "Region: " + rg.getName() + ": Attacker contains occupant");
            return;
        }
        if (isInRegion(attacker, rg, false) && !isInRegion(defender, rg, false)) {
            rg.setInfluence(rg.getInfluence() - 5);
            FactionsXL.debug(FDebugLevel.WAR, "Region: " + rg.getName() + ": Attacker in region & defender not ( no Offline prot.) Influence: " + rg.getInfluence());
            return;
        }
        if (isInRegion(attacker, rg, false)) {
            rg.setInfluence(rg.getInfluence() - 1);
            FactionsXL.debug(FDebugLevel.WAR, "Region: " + rg.getName() + ": Attacker in region & defender ( no Offline prot.) Influence: " + rg.getInfluence());

        }
    }

    // Checks if a player of WarParty wp is in Region rg.
    public boolean isInRegion(WarParty wp, Region rg, boolean offlineProtection) {
        FactionsXL.debug(FDebugLevel.WAR, "WarParty: " + wp.getName());
        long now = System.currentTimeMillis();
        int online = getActivePlayers(wp);
        if (online == 0 && offlineProtection) {
            // Offline factions are always defended
            FactionsXL.debug(FDebugLevel.WAR, "Region: " + rg.getName() + ": isInRegion: true - Offline protection");
            return true;
        }

        for (FPlayer fp : fplayers.getFPlayers()) {
            if (fp.getFaction() == null || !fp.getFaction().isInWar()) {
                continue;
            }
            // Invisible players do not count
            PotionEffect potionEffect = fp.getPlayer().getPotionEffect(PotionEffectType.INVISIBILITY);
            if (potionEffect != null) {
                FactionsXL.debug(FDebugLevel.WAR, fp.getName() + " is invisible - skipped");
                continue;
            }
            if (fp.getLastRegion() == null) {
                FactionsXL.debug(FDebugLevel.WAR, fp.getName() + " has no lastRegion - skipped");
                continue;
            }
            if (fp.getFaction().getWarParties().contains(wp) && fp.getLastRegion().equals(rg)) {
                FactionsXL.debug(FDebugLevel.WAR, "Region: " + rg.getName() + ": isInRegion: true - WarParty: " + wp.getName() + " Player: " + fp.getName());
                return true;
            }
        }
        FactionsXL.debug(FDebugLevel.WAR, "Region: " + rg.getName() + ": No players for " + wp.getName() + " in region.");
        return false;
        }


    public void showTimers() {
        for (FPlayer fp : fplayers.getFPlayers()) {
            if (fp.getFaction() == null || !fp.getFaction().isInWar()) {
                continue;
            }
            if (fp.getLastRegion() == null) {
                continue;
            }
            Region rg = fp.getLastRegion();
            if (rg.getAttackStartTime() != 0) {
                if (rg.isAttacked()) {
                    MessageUtil.sendActionBarMessage(fp.getPlayer(), "&cAngriff: &6" + getTimeLeft(rg) + "&6 Minuten &8| &7Einfluss: &a" + rg.getInfluence() + "%");
                } else {
                    MessageUtil.sendActionBarMessage(fp.getPlayer(), "&7Vorbereitungszeit: &6" + getTimeLeft(rg) + "&6 Minuten");
                }
            }
        }
    }

    public double getParticipation(WarParty wp) {
        War war = wp.getWar();
        long now = System.currentTimeMillis();
        double participation = 0.00;
        for (Faction f : wp.getFactions()) {
            for (FPlayer fp : f.getFPlayers()) {
                // Only count players that are online or that were online up to 10 minutes ago
                if (!fp.isOnline() && fp.getData().getTimeLastLogout() != 0 && (now > (fp.getData().getTimeLastLogout() + 600000))) {
                    continue;
                }
                participation = participation + war.getPlayerParticipation(fp.getPlayer());

            }
        }
        return participation;
    }

    public int getActivePlayers(WarParty wp) {
        War war = wp.getWar();
        long now = System.currentTimeMillis();
        int active = 0;
        for (Faction f : wp.getFactions()) {
            for (FPlayer fp : f.getFPlayers()) {
                // Only count players that are online or that were online up to 10 minutes ago
                if (!fp.isOnline() && fp.getData().getTimeLastLogout() != 0 && (now > (fp.getData().getTimeLastLogout() + 600000))) {
                    continue;
                }
                active++;

            }
        }
        return active;
    }

    public int getTimeLeft(Region rg) {
        long now = System.currentTimeMillis();
        long minutes;
        long difference = now - rg.getAttackStartTime();
        minutes = TimeUnit.MILLISECONDS.toMinutes(difference);
        if (rg.isAttacked()) {
            return 120 - Math.round(minutes);
        } else {
            return 20 - Math.round(minutes);
        }
    }


    public boolean isPreparationOver(Region rg) {
        long now = System.currentTimeMillis();
        if (rg.getAttackStartTime() == 0) {
            return false;
        }
        if ((rg.getAttackStartTime() + 1200000) < now) {
            FactionsXL.debug(FDebugLevel.WAR, "Preparation for " + rg.getName() + " is over. Starting attack.");
            return true;
        }
        return false;
    }

    public boolean canStartOccupation(WarParty attacker, WarParty defender) {
        War war = attacker.getWar();
        double attackerParticipation = getParticipation(attacker);
        int attackerPlayers = getActivePlayers(attacker);
        double defenderParticipation = getParticipation(defender);
        int defenderPlayers = getActivePlayers(defender);
        long now = System.currentTimeMillis();
        // If the attacker is weaker they should still be able to attack
        if (attackerParticipation < defenderParticipation) {
            FactionsXL.debug(FDebugLevel.WAR,"Attacker is weaker than defender. Can start attack. ");
            return true;
        }
        if (defenderParticipation <= 1.00 && defenderPlayers >= 1 && attacker.getPoints() <= 10) {
            FactionsXL.debug(FDebugLevel.WAR,"Defender has no participation, but is online and attacker has less than 10 points. Can start attack");
            return true;
        }
        if (defenderParticipation <= 1.00) {
            FactionsXL.debug(FDebugLevel.WAR,"Defender has no participation (" + defenderParticipation + "). Cancelling... ");
            return false;
        }
        FactionsXL.debug(FDebugLevel.WAR,"Participation: Defender: " + attackerParticipation + " / Attacker: " + defenderParticipation);
        return Math.abs(attackerParticipation - defenderParticipation) < 10;
    }

    public boolean isAlreadyAttacked (Faction f) {
        for (Region rg : f.getRegions()) {
            if (rg.getAttackStartTime() != 0) {
                return true;
            }
        }
        return false;
    }
}
