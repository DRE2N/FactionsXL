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

import de.erethon.commons.player.PlayerCollection;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.entity.FEntity;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.entity.RelationRequest;
import de.erethon.factionsxl.entity.Request;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.faction.Federation;
import de.erethon.factionsxl.faction.LegalEntity;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

/**
 * @author Daniel Saukel
 */
public class WarParty implements FEntity {

    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();

    private LegalEntity leader;
    private Set<LegalEntity> participants = new HashSet<>();
    private Set<Faction> invited = new HashSet<>();
    private List<Request> requests;
    private WarPartyRole role;
    public int kills;
    public int deaths;
    public int pointsFromKills;
    public int fights;
    public int points;

    public WarParty(LegalEntity entity, WarPartyRole role) {
        leader = entity;
        participants.add(entity);
        requests = new ArrayList<>();
        this.role = role;
    }

    public WarParty(Map<String, Object> serialized) {
        leader = factions.getById((int) serialized.get("leader"));
        ((List<Integer>) serialized.get("participants")).forEach(p -> participants.add(factions.getById(p)));
        ((List<Integer>) serialized.get("invited")).forEach(p -> invited.add(factions.getById(p)));
        kills = (int) serialized.get("kills");
        deaths = (int) serialized.get("deaths");
        points = (int) serialized.get("score");
        pointsFromKills = (int) serialized.get("killPoints");
        fights = (int) serialized.get("fights");
        requests = new ArrayList<>();
        role = WarPartyRole.valueOf((String) serialized.get("role"));
    }

    @Override
    public String getName() {
        return leader.getName();
    }

    @Override
    public Relation getRelation(FEntity object) {
        return leader.getRelation(object);
    }

    @Override
    public boolean isInWar(FEntity object) {
        return leader.isInWar(object);
    }

    @Override
    public PlayerCollection getRequestAuthorizedPlayers(Class<? extends Request> type) {
        return leader.getRequestAuthorizedPlayers(type);
    }

    @Override
    public List<Request> getRequests() {
        return requests;
    }


    public void initRequests() {
        requests = new ArrayList<>();
    }

    /**
     * @return
     * the party leader
     */
    public LegalEntity getLeader() {
        return leader;
    }

    /**
     * @return
     * the player who leads the party leader
     */
    public OfflinePlayer getLeaderAdmin() {
        return leader.getAdmin();
    }

    /**
     * @return
     * a Set of all federations and factions that participate
     */
    public Set<LegalEntity> getParticipants() {
        return participants;
    }

    /**
     * @return
     * a Set of all federations and factions that were invited by the leader
     */
    public Set<Faction> getInvited() {
        return invited;
    }

    public void addInvited(Faction participant) {
        invited.add(participant);
    }

    public void removeInvited(Faction participant) {
        invited.remove(participant);
    }

    /**
     * @return
     * a Set of all single factions and the factions of the federations that participate
     */
    public Set<Faction> getFactions() {
        Set<Faction> factions = new HashSet<>();
        for (LegalEntity entity : participants) {
            if (entity instanceof Faction) {
                factions.add((Faction) entity);
            } else if (entity instanceof Federation) {
                factions.addAll(((Federation) entity).getFactions());
            }
        }
        return factions;
    }

    public void addParticipant(LegalEntity participant) {
        if (participants.contains(participant)) {
            return;
        }
        participants.add(participant);
        if (participant instanceof Faction) {
            // Add vassals if any
            Faction participantFaction = (Faction) participant;
            for (Faction possibleVassal : participantFaction.getRelations().keySet()) {
                if (participantFaction.getRelation(possibleVassal).equals(Relation.VASSAL) || participantFaction.getRelation(possibleVassal).equals(Relation.LORD)) {
                    participants.add(possibleVassal);
                    addParticipant(possibleVassal); // Do it again in case of vassals of vassals/lords of lords etc.
                }
            }
        }
    }

    public void removeParticipant(LegalEntity participant) {
        if (!participants.contains(participant)) {
            return;
        }
        participants.remove(participant);
        if (participant instanceof Faction) {
            // Remove vassals if any
            Faction participantFaction = (Faction) participant;
            for (Faction possibleVassal : participantFaction.getRelations().keySet()) {
                if (participantFaction.getRelation(possibleVassal).equals(Relation.VASSAL) || participantFaction.getRelation(possibleVassal).equals(Relation.LORD)) {
                    participants.remove(possibleVassal);
                    removeParticipant(possibleVassal); // Do it again in case of vassals of vassals/lords of lords etc.
                }
            }
        }
    }

    public void joinWar(LegalEntity faction) {
        Set<Faction> factionEnemy =  this.getEnemy().getFactions();
        for (Faction f : factionEnemy) {
            new RelationRequest(Bukkit.getConsoleSender(), f, (Faction) faction, Relation.ENEMY).confirm();
        }
        addParticipant(faction);
    }

    public void leaveWar(LegalEntity faction) {
        Set<Faction> factionEnemy =  this.getEnemy().getFactions();
        // Set all relations with the participant to peace
        for (Faction f : factionEnemy) {
            new RelationRequest(Bukkit.getConsoleSender(), f, (Faction) faction, Relation.PEACE).confirm();
        }
        // Remove regions occupied by the participant
        for (Faction f : factionEnemy) {
            for (Region rg : f.getRegions()) {
                if (rg.getOccupant() != null && rg.getOccupant().equals(faction)) {
                    rg.clearOccupant();
                }
            }
        }
        removeParticipant(faction);
    }

    /**
     * @return
     * kills / deaths ratio. 0 deaths are treated like 1.
     */
    public double getKD() {
        if (deaths != 0) {
            Double kd = (double) kills / (double) deaths;
            return Math.round(kd * 100.00) / 100.0;
        }
        return kills;
    }

    public void addKill() {
        kills++;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() { deaths++; }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPointsFromKills() {
        return pointsFromKills;
    }

    public void setPointsFromKills(int points) {
        this.pointsFromKills = points;
    }

    /**
     * @return
     * the amount of collected war points
     */
    public int getPoints() {
        return points;
    }

    /**
     * @return
     * the amount of collected war points
     */
    public void addPoints(int a) {
        points = points + a;
    }

    /**
     * @return
     * the amount of collected war points
     */
    public void removePoints(int a) {
        points = points - a;
    }


    /**
     * @return
     * the WarParty's role in the war
     */
    public WarPartyRole getRole() {
        return role;
    }


    public War getWar() {
        return plugin.getWarCache().getByParty(this);
    }

    /**
     * Returns the defender if this party is the attacker; the attacker if this party is the defender and null if this party is anything else.
     *
     * @return the other party
     */
    public WarParty getEnemy() {
        return getWar().getEnemy(this);
    }

    /* Serialization */
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("leader", leader.getId());
        ArrayList<Integer> participants = new ArrayList<>();
        this.participants.forEach(p -> participants.add(p.getId()));
        serialized.put("participants", participants);
        ArrayList<Integer> invited = new ArrayList<>();
        this.invited.forEach(p -> invited.add(p.getId()));
        serialized.put("invited", invited);
        serialized.put("kills", kills);
        serialized.put("deaths", this.deaths);
        serialized.put("score", points);
        serialized.put("killPoints", pointsFromKills);
        serialized.put("fights", fights);
        serialized.put("role", role.name());
        return serialized;
    }

    @Override
    public String toString() {
        return "WarParty{leader=" + leader.toString() + "}";
    }

}
