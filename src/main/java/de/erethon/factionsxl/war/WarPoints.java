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

package de.erethon.factionsxl.war;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

// TODO: Needs to be added to the config file

public class WarPoints {

    FactionsXL plugin = FactionsXL.getInstance();

    public void updateScore(WarParty warParty, WarAction action) {
        double regions = 0;
        double occupied = 0;
        for (Faction f : warParty.getEnemy().getFactions()) {
            for (Region rg : f.getRegions()) {
                regions++;
                if (rg.getOccupant() != null && warParty.getFactions().contains(rg.getOccupant())) {
                    occupied++;
                }
            }
        }
        double mod = occupied / regions;
        MessageUtil.log("Occupied: " + occupied + " Total: " + regions + " Mod: " + String.valueOf(mod));
        switch (warParty.getWar().getCasusBelli().getType()) {
            case RAID:
                switch (action) {
                    case OCCUPY:
                    case OCCUPY_CORE:
                    case OCCUPY_WAR_TARGET:
                    case OCCUPY_CLAIM:
                    case OCCUPY_CAPITAL:
                    case REOCCUPY_OWN_CORE:
                        break;
                    case KILL:
                        setScore(warParty, 1, 0);
                        break;
                }
                break;
            case CONQUEST:
                switch (action) {
                    case OCCUPY:
                        setScore(warParty, 5, mod);
                    case OCCUPY_CLAIM:
                        setScore(warParty, 10, mod);
                        break;
                    case OCCUPY_CORE:
                        setScore(warParty, 12, mod);
                        break;
                    case OCCUPY_WAR_TARGET:
                    case REOCCUPY_OWN_CORE:
                        setScore(warParty, 15, mod);
                        break;
                    case OCCUPY_CAPITAL:
                        setScore(warParty, 25, mod);
                        break;
                    case KILL:
                        setScore(warParty, 1, 0);
                        break;
                }
                break;
            case RECONQUEST:
                switch (action) {
                    case OCCUPY:
                        setScore(warParty, 5, mod);
                        break;
                    case OCCUPY_CLAIM:
                        setScore(warParty, 10, mod);
                        break;
                    case OCCUPY_CORE:
                        setScore(warParty, 12, mod);
                        break;
                    case OCCUPY_CAPITAL:
                        setScore(warParty, 20, mod);
                        break;
                    case OCCUPY_WAR_TARGET:
                        setScore(warParty, 15, mod);
                        break;
                    case REOCCUPY_OWN_CORE:
                        setScore(warParty, 22, mod);
                        break;
                    case KILL:
                        setScore(warParty, 1, 0);
                        break;
                }
                break;
            case INDEPENDENCE:
                switch (action) {
                    case OCCUPY:
                        break;
                    case OCCUPY_CLAIM:
                    case OCCUPY_CORE:
                        setScore(warParty, 7, mod);
                        break;
                    case OCCUPY_CAPITAL:
                        setScore(warParty, 30, mod);
                        break;
                    case REOCCUPY_OWN_CORE:
                    case OCCUPY_WAR_TARGET:
                        setScore(warParty, 20, mod);
                        break;
                    case KILL:
                        setScore(warParty, 1, 0);
                        break;
                }
            case RESUBJAGATION:
                switch (action) {
                    case OCCUPY:
                        break;
                    case OCCUPY_CLAIM:
                        setScore(warParty, 10, mod);
                        break;
                    case OCCUPY_CORE:
                        setScore(warParty, 12, mod);
                        break;
                    case OCCUPY_WAR_TARGET:
                        setScore(warParty, 20, mod);
                        break;
                    case OCCUPY_CAPITAL:
                        setScore(warParty, 25, mod);
                        break;
                    case REOCCUPY_OWN_CORE:
                        setScore(warParty, 18, mod);
                        break;
                    case KILL:
                        setScore(warParty, 1, 0);
                        break;
                }
                break;
            case BORDER_FRICTION:
            case CLAIM_ON_THRONE:
            case IMPERIAL_BAN:
            case LIBERATION:
            case RESTORATION_OF_UNION:
                break;
        }
    }
    public void setScore(WarParty wp, int score, double modifier) {
        double finalScore = 0.00;
        finalScore = score * (1 + modifier);
        score =  (int)Math.round(finalScore);
        wp.addPoints(score);
        wp.getEnemy().removePoints(score);

        for (Faction f : wp.getFactions()) {
            f.sendMessage(FMessage.WAR_SCORE_CHANGED.getMessage(String.valueOf(wp.getPoints()), String.valueOf(wp.getEnemy().getPoints())));
        }
        for (Faction f : wp.getEnemy().getFactions()) {
            f.sendMessage(FMessage.WAR_SCORE_CHANGED.getMessage(String.valueOf(wp.getEnemy().getPoints()), String.valueOf(wp.getPoints())));
        }
        if (wp.getPoints() >= 100 || wp.getPoints() <= -100){
            Bukkit.getScheduler().runTaskLater(plugin, new BukkitRunnable() {
                @Override
                public void run() {
                    MessageUtil.log("100 Points reached. Ending the war...");
                    plugin.getWarHandler().calculateWarStatus();
                }
            }, 40);
        }
    }


}
