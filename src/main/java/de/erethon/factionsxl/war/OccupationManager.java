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

package de.erethon.factionsxl.war;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.player.FPlayerCache;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
                        if (rg.isAttacked() && isInRegion(war.getDefender(), rg, false) && rg.getInfluence() > 0) {
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
                        if (rg.isAttacked() && isInRegion(war.getAttacker(), rg, false) && rg.getInfluence() > 0) {
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
        if (rg.getCoreFactions().containsKey(rg.getOwner()) && isInRegion(defender, rg, true) && rg.getInfluence() <= 50) {
            return;
        }
        if (isInRegion(defender, rg, true) && rg.getInfluence() <= 25) {
            return;
        }
        if (rg.getOccupant() != null && attacker.getFactions().contains(rg.getOccupant())) {
            return;
        }
        if (isInRegion(attacker, rg, false)) {
            rg.setInfluence(rg.getInfluence() - 1);
        }
    }

    // Checks if a player of WarParty wp is in Region rg.
    public boolean isInRegion(WarParty wp, Region rg, boolean offlineProtection) {
        long now = System.currentTimeMillis();
        int online = 0;
        for (Faction f : wp.getFactions()) {
            for (FPlayer fp : f.getFPlayers()) {
                // Only count players that are online or that were online up to 10 minutes ago
                if (!fp.isOnline() && fp.getData().getTimeLastLogout() != 0 && (now > (fp.getData().getTimeLastLogout() + 600000))) {
                    continue;
                }
                online++;
            }
        }
        if (online == 0 && offlineProtection) {
            // Offline factions are always defended
            return true;
        }

        for (FPlayer fp : fplayers.getFPlayers()) {
            if (fp.getFaction() == null || !fp.getFaction().isInWar()) {
                continue;
            }
            // Invisible players do not count
            PotionEffect potionEffect = fp.getPlayer().getPotionEffect(PotionEffectType.INVISIBILITY);
            if (potionEffect != null) {
                continue;
            }
            if (fp.getLastRegion() == null) {
                continue;
            }
            if (fp.getFaction().getWarParties().contains(wp) && fp.getLastRegion().equals(rg)) {
                return true;
            }
        }
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
            MessageUtil.log("Preparation for " + rg.getName() + " is over. Starting attack.");
            return true;
        }
        return false;
    }

    public boolean canStartOccupation(WarParty attacker, WarParty defender) {
        War war = attacker.getWar();
        double attackerParticipation = 0.00;
        double defenderParticipation = 0.00;
        long now = System.currentTimeMillis();
        for (Faction f : attacker.getFactions()) {
            for (FPlayer fp : f.getFPlayers()) {
                // Only count players that are online or that were online up to 10 minutes ago
                if (!fp.isOnline() && fp.getData().getTimeLastLogout() != 0 && (now > (fp.getData().getTimeLastLogout() + 600000))) {
                    continue;
                }
                attackerParticipation = attackerParticipation + war.getPlayerParticipation(fp.getPlayer());

            }
        }
        for (Faction f : defender.getFactions()) {
            for (FPlayer fp : f.getFPlayers()) {
                // Only count players that are online or that were online up to 10 minutes ago
                if (!fp.isOnline() && fp.getData().getTimeLastLogout() != 0 && (now > (fp.getData().getTimeLastLogout() + 600000))) {
                    continue;
                }
                defenderParticipation = defenderParticipation + war.getPlayerParticipation(fp.getPlayer());
            }
        }
        if (defenderParticipation <= 1.00) {
            MessageUtil.log("Defender has no participation. Cancelling... ");
            return false;
        }
        // If the attacker is weaker they should still be able to attack
        if (attackerParticipation < defenderParticipation) {
            MessageUtil.log("Attacker is weaker than defender. Can start attack. ");
            return true;
        }
        MessageUtil.log("Participation: Defender: " + attackerParticipation + " / Attacker: " + defenderParticipation);
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
