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
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * @author Daniel Saukel
 */
public class Battle {

    private Player player1;
    private Player player2;
    private long start;
    private long expiration;
    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();
    FConfig config = plugin.getFConfig();
    WarCache warCache = plugin.getWarCache();
    WarPoints points = plugin.getWarPoints();

    public Battle(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        start = System.currentTimeMillis();
        expiration = System.currentTimeMillis() + FConfig.MINUTE;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public long getStartTime() {
        return start;
    }

    public long getExpirationTime() {
        return expiration;
    }

    public void expandExpirationTime() {
        expiration = System.currentTimeMillis() + FConfig.MINUTE;
    }

    public boolean takesPart(Player player) {
        return player.equals(player1) || player.equals(player2);
    }

    /**
     * @param winner The winning player
     * @param looser the loosing (dead) player
     */
    public void win(Player winner, Player looser) {
        Faction f = factions.getByMember(winner);
        Faction fl = factions.getByMember(looser);
        Region r = plugin.getBoard().getByLocation(winner.getLocation());
        Faction owner = r.getOwner();
        if (r.getOccupant() != null || r.getOccupant() == f) {
            owner = r.getOccupant();
        }
        Set<WarParty> WP = f.getWarParties();
        Set<WarParty> lWP = fl.getWarParties();
        for (WarParty w : WP) {
            if (warCache.getByParty(w).getTruce()) {
                continue;
            }
            if (w.getPointsFromKills() <= config.getMaximumKillPoints() && (w.getWar().getCasusBelli().getType() != CasusBelli.Type.RAID)) {
                points.updateScore(w, WarAction.KILL);
                w.setPointsFromKills(w.getPointsFromKills() + 1);
            }
            if (w.getWar().getCasusBelli().getType() == CasusBelli.Type.RAID) {
                points.updateScore(w, WarAction.KILL);

            }
            w.addKill();
            if (w.getFactions().contains(owner)) {
                if (r.getInfluence() + config.getInfluenceFromKill() <= 100) {
                    r.setInfluence(r.getInfluence() + config.getInfluenceFromKill());
                    MessageUtil.sendActionBarMessage(winner, FMessage.WAR_OCCUPY_REGION_DEFEND.getMessage(String.valueOf(config.getInfluenceFromKill()), String.valueOf(r.getInfluence())));
                    break;
                }
            }
            else if (w.getEnemy().getFactions().contains(owner)){
                if (r.getInfluence() - config.getInfluenceFromKill() >= 0) {
                    r.setInfluence(r.getInfluence() - config.getInfluenceFromKill());
                    MessageUtil.sendActionBarMessage(winner, FMessage.WAR_OCCUPY_REGION_ATTACKED.getMessage(String.valueOf(config.getInfluenceFromKill()), String.valueOf(r.getInfluence())));
                    break;
                }
            }
        }
        for (WarParty wL : lWP) {
            if (warCache.getByParty(wL).getTruce()) {
                continue;
            }
            if (wL.getPointsFromKills() <= config.getMaximumKillPoints() && (wL.getWar().getCasusBelli().getType() != CasusBelli.Type.RAID)) {
                wL.setPointsFromKills(wL.getPointsFromKills() + 1);
            }
            if (wL.getWar().getCasusBelli().getType() == CasusBelli.Type.RAID) {
                points.updateScore(wL, WarAction.KILL);

            }
            wL.addDeath();
        }
    }

    @Override
    public String toString() {
        return "Battle{player1=" + player1.getName() + "player2=" + player2.getName() + "}";
    }

}
