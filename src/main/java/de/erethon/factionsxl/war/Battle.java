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
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.player.FPlayerCache;
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
    FPlayerCache fplayers = plugin.getFPlayerCache();
    FConfig config = plugin.getFConfig();
    WarCache warCache = plugin.getWarCache();
    WarPoints points = plugin.getWarPoints();

    public Battle(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        start = System.currentTimeMillis();
        expiration = System.currentTimeMillis() * 1000 * 60;
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
        expiration = System.currentTimeMillis() + 1000 * 60;
    }

    public boolean takesPart(Player player) {
        return player1.getName().equals(player.getName()) || player2.getName().equals(player.getName());
    }

    /**
     * @param winner The winning player
     * @param looser the loosing (dead) player
     */
    public void win(Player winner, Player looser) {
        Faction f = factions.getByMember(winner);
        Faction fl = factions.getByMember(looser);
        FPlayer winnerFP = fplayers.getByPlayer(winner);
        FPlayer looserFP = fplayers.getByPlayer(looser);
        Region r = plugin.getBoard().getByLocation(looser.getLocation());
        Faction owner = null;
        if (r != null) {
            owner = r.getOwner();
        }
        if (r != null && (r.getOccupant() != null || r.getOccupant() == f)) {
            owner = r.getOccupant();
        }

        MessageUtil.log("Battle won: " + this.toString());

        winnerFP.getData().addKill();
        looserFP.getData().addDeath();

        Set<WarParty> WP = f.getWarParties();
        Set<WarParty> lWP = fl.getWarParties();
        for (WarParty w : WP) {
            // Should only count for party where both players are involved
            if (warCache.getByParty(w).getTruce()) {
                continue;
            }
            if (!looserFP.isInWarParty(w.getEnemy())) {
                continue;
            }
            if (w.getPointsFromKills() <= config.getMaximumKillPoints() && (w.getWar().getCasusBelli().getType() != CasusBelli.Type.RAID)) {
                points.updateScore(w, WarAction.KILL);
                w.setPointsFromKills(w.getPointsFromKills() + 1);
            } else if (w.getWar().getCasusBelli().getType() == CasusBelli.Type.RAID) {
                points.updateScore(w, WarAction.KILL);
            }
            w.addKill();
            w.getWar().addPlayerParticipation(winnerFP.getPlayer(), WarPlayerAction.KILL);
            if (owner != null && w.getFactions().contains(owner)) {
                if (r.isAttacked()) {
                    addInfluence(w, r, winnerFP);;
                    MessageUtil.sendActionBarMessage(winner, FMessage.WAR_OCCUPY_REGION_DEFEND.getMessage(String.valueOf(getLimitedInfluence(config.getInfluenceFromKill() * w.getWar().getPlayerParticipation(winner.getPlayer()))), String.valueOf(r.getInfluence())));
                    break;
                }
            } else if (owner != null && w.getEnemy().getFactions().contains(owner)){
                if (r.isAttacked()) {
                    removeInfluence(w, r, winnerFP);
                    MessageUtil.sendActionBarMessage(winner, FMessage.WAR_OCCUPY_REGION_ATTACKED.getMessage(String.valueOf(getLimitedInfluence(config.getInfluenceFromKill() * w.getWar().getPlayerParticipation(looser.getPlayer()))), String.valueOf(r.getInfluence())));
                    break;
                }
            }
        }
        for (WarParty wL : lWP) {
            if (warCache.getByParty(wL).getTruce()) {
                continue;
            }
            if (!winnerFP.isInWarParty(wL.getEnemy())) {
                continue;
            }
            if (wL.getPointsFromKills() <= config.getMaximumKillPoints() && (wL.getWar().getCasusBelli().getType() != CasusBelli.Type.RAID)) {
                wL.setPointsFromKills(wL.getPointsFromKills() + 1);
            }
            wL.addDeath();
        }
    }

    public void removeInfluence(WarParty wp, Region rg, FPlayer fPlayer) {
        int influence = (int) Math.abs(Math.round(config.getInfluenceFromKill() * wp.getWar().getPlayerParticipation(fPlayer.getPlayer())));
        if (influence >= 5) {
            influence = 5;
        }
        if (rg.getInfluence() - influence < 0) {
            rg.setInfluence(0);
            return;
        }
        rg.setInfluence(rg.getInfluence() - influence);
    }

    public void addInfluence(WarParty wp, Region rg, FPlayer fPlayer) {
        int influence = (int) Math.abs(Math.round(config.getInfluenceFromKill() * wp.getWar().getPlayerParticipation(fPlayer.getPlayer())));
        if (influence >= 5) {
            influence = 5;
        }
        if (rg.getInfluence() + influence > 100) {
            rg.setInfluence(100);
            return;
        }
        rg.setInfluence(rg.getInfluence() + influence);
    }

    public double getLimitedInfluence(double influence) {
        double inf = influence;
        if (inf >= 5) {
            inf = 5;
        }
        return inf;
    }

    @Override
    public String toString() {
        return "Battle{player1=" + player1.getName() + " player2=" + player2.getName() + "}";
    }

}
