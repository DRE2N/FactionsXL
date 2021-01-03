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
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.entity.RelationRequest;
import de.erethon.factionsxl.event.WarTruceEndEvent;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.Bukkit;

import java.util.Date;
import java.util.Set;

import static de.erethon.factionsxl.war.CasusBelli.Type.RAID;

/**
 * @author Malfrador
 */

public class WarHandler {
    FactionsXL plugin = FactionsXL.getInstance();
    FConfig config = plugin.getFConfig();
    public void updateTruce() {
        long now = System.currentTimeMillis();
        for (War w : plugin.getWarCache().getWars()) {
            if ((w.getTruce()) && (now > (w.getStartDate().getTime() + config.getTruceTime())) ) {
                w.setTruce(false);
                Faction attacker = (Faction) w.getAttacker().getLeader();
                Faction defender = (Faction) w.getDefender().getLeader();
                ParsingUtil.broadcastMessage(FMessage.WAR_TRUCE_ENDED.getMessage(), attacker, defender);

                WarTruceEndEvent event = new WarTruceEndEvent(w.getAttacker(), w.getDefender(), w.getCasusBelli());
                Bukkit.getPluginManager().callEvent(event);

            }
        }
    }
    public void calculateWarStatus() {
        for (War war : plugin.getWarCache().getWars()) {
            //relationFixer(war); //temporary fix for wrong relations in wars.
            // Add exhaustion
            for (Faction f : war.getAttacker().getFactions()) {
                if (war.getAttacker().getPoints() < 0) {
                    f.setExhaustion(f.getExhaustion() + config.getExhaustionLoosing());
                }
                f.setExhaustion(f.getExhaustion() + config.getExhaustion());
            }
            for (Faction f : war.getDefender().getFactions()) {
                if ((war.getDefender().getPoints() < 0) || (war.getDefender().getKD() < 1)) {
                    f.setExhaustion(f.getExhaustion() + config.getExhaustionLoosing());
                    continue;
                }
                f.setExhaustion(f.getExhaustion() + config.getExhaustion());
            }

            // If the attacker wins, the war goals (depending on CB) get forced
            if (war.getAttacker().getPoints() >= 100) {
                forceWarGoal(war.getAttacker());
                return;
            }

            // If the defender wins, the war ends and cores/claims are given to the defender
            if (war.getDefender().getPoints() >= 100) {
                defenderGoals(war.getDefender());
                return;
            }

            Faction aLeader = (Faction) war.getAttacker().getLeader();
            Faction dLeader = (Faction) war.getDefender().getLeader();
            if (aLeader.getStability() <= 1) {
                defenderGoals(war.getDefender());
                return;
            }
            if (dLeader.getStability() <= 1) {
                forceWarGoal(war.getAttacker());
                return;
            }

        }

        // Regenerate exhaustion
        for (Faction f : plugin.getFactionCache().getActive()) {
            if (!f.isInWar() && f.getExhaustion() >= 0) {
                f.setExhaustion(f.getExhaustion() - config.getExhaustion());
            }
        }

    }

    public void forceWarGoal(WarParty warParty) {
        warParty.getWar().cleanup();
        MessageUtil.log("Ending war " + warParty.getWar().toString());
        WarParty enemy = warParty.getEnemy();
        Faction leader = (Faction) warParty.getLeader();
        Faction enemyLeader = (Faction) enemy.getLeader();
        Set<Region> enemyRegions = enemyLeader.getRegions();
        switch (warParty.getWar().getCasusBelli().getType()) {
            case BORDER_FRICTION: // Not implemented
            case LIBERATION:
            case RESTORATION_OF_UNION:
            case IMPERIAL_BAN:
            case CLAIM_ON_THRONE:

            case RAID: // Get "Bounty" from enemy faction.
                Faction enemyF = (Faction) warParty.getEnemy().getLeader();
                int bounty = enemyF.getMembers().size() * warParty.getKills();
                enemyF.getAccount().withdraw(bounty);
                warParty.getLeader().getAccount().deposit(bounty);
                break;

            case RECONQUEST: // Gives all core regions back to their owner. If current (loosing) owner has a core there as well, add Reconquest CB.
                for (Region r : enemyRegions) {
                    if (r.getCoreFactions().containsKey((Faction) warParty.getLeader())) {
                        if (r.getCoreFactions().containsKey(r.getOwner())) {
                            r.getOwner().getCasusBelli().add(new CasusBelli(CasusBelli.Type.RECONQUEST, warParty.getLeader(), null));
                        }
                        r.setOwner((Faction) warParty.getLeader());
                    }
                }
                break;
            case CONQUEST: // Give all claimed regions to all winners / just regions claimed by the leader to the leader. Adds Reconquest CB if core
                for (Region r : enemyRegions) {
                    if (config.isForceWarGoalsForAllWinners()) {
                        for (Faction ally : warParty.getFactions()) {
                            if (r.getClaimFactions().containsKey(ally) || r.getCoreFactions().containsKey(ally)) {
                                if (r.getCoreFactions().containsKey(r.getOwner())) {
                                    r.getOwner().getCasusBelli().add(new CasusBelli(CasusBelli.Type.RECONQUEST, warParty.getLeader(), null));
                                }
                                r.setOwner(ally);
                                if (r.getClaimFactions().containsKey((Faction) warParty.getLeader()) || r.getCoreFactions().containsKey((Faction) warParty.getLeader())) {
                                    r.setOwner((Faction) warParty.getLeader());
                                }
                                r.clearOccupant();
                                r.setAttacked(false);
                                r.setAttackStartTime(0);
                            }
                        }
                    }
                    else {
                        if (r.getClaimFactions().containsKey((Faction) warParty.getLeader()) || r.getCoreFactions().containsKey((Faction) warParty.getLeader())) {
                            if (r.getCoreFactions().containsKey(r.getOwner())) {
                                r.getOwner().getCasusBelli().add(new CasusBelli(CasusBelli.Type.RECONQUEST, warParty.getLeader(), null));
                            }   
                            r.setOwner((Faction) warParty.getLeader());
                            r.clearOccupant();
                            r.setAttacked(false);
                            r.setAttackStartTime(0);
                        }

                    }
                }
                break;

            case INDEPENDENCE: // Make faction independent
                Faction independent = (Faction) warParty.getLeader();
                Faction lord = (Faction) warParty.getWar().getCasusBelli().getTarget();
                lord.getRelations().remove(independent);
                independent.getRelations().remove(lord);
                lord.getCasusBelli().add(new CasusBelli(CasusBelli.Type.RESUBJAGATION, independent, new Date(System.currentTimeMillis() + (config.getCBLiberationExp() * FConfig.DAY))));
                break;

            case SUBJAGATION:
            case RESUBJAGATION: // Make loosing faction vassal
                Faction newLord = (Faction) warParty.getLeader();
                Faction newVassal = (Faction) warParty.getWar().getCasusBelli().getTarget();
                new RelationRequest(Bukkit.getConsoleSender(), newLord, newVassal, Relation.VASSAL).confirm();
                newVassal.setAllod(true);
                newVassal.getCasusBelli().add(new CasusBelli(CasusBelli.Type.INDEPENDENCE, newLord, new Date(System.currentTimeMillis() + (config.getCBLiberationExp() * FConfig.DAY))));
                break;
        }
        enemyLeader.setScoreLastPeace(warParty.getPoints());
        enemyLeader.setTimeLastPeace(System.currentTimeMillis());

        if (enemyRegions.isEmpty()) {
            enemyLeader.disband();
        }
        warParty.getWar().end();

    }

    public void defenderGoals(WarParty warParty) {
        warParty.getWar().cleanup();
        WarParty enemy = warParty.getEnemy(); // Attacker in this case
        if (warParty.getWar().getCasusBelli().getType() == RAID && !(config.isDefenderRaidBounty())) {
            return;
        }
        Faction enemyLeader = (Faction) enemy.getLeader();
        switch (warParty.getWar().getCasusBelli().getType()) {
            case BORDER_FRICTION:
            case CLAIM_ON_THRONE:
            case IMPERIAL_BAN:
            case RESTORATION_OF_UNION: // Not implemented
                break;
            case CONQUEST:
            case LIBERATION:
            case SUBJAGATION:
            case RESUBJAGATION:
            case RECONQUEST:
                for (Region r : enemyLeader.getRegions()) {
                    if (config.isForceWarGoalsForAllDefenders()) {
                        for (Faction ally : warParty.getFactions()) {
                            if (r.getClaimFactions().containsKey(ally) || r.getCoreFactions().containsKey(ally)) {
                                if (r.getCoreFactions().containsKey(r.getOwner())) {
                                    r.getOwner().getCasusBelli().add(new CasusBelli(CasusBelli.Type.RECONQUEST, warParty.getLeader(), null));
                                }
                                r.setOwner(ally);
                            }
                        }
                    }

                    else {
                        if (r.getClaimFactions().containsKey((Faction) warParty.getLeader()) || r.getCoreFactions().containsKey((Faction) warParty.getLeader())) {
                            if (r.getCoreFactions().containsKey(r.getOwner())) {
                                r.getOwner().getCasusBelli().add(new CasusBelli(CasusBelli.Type.RECONQUEST, warParty.getLeader(), null));
                            }
                            r.setOwner((Faction) warParty.getLeader());
                        }

                    }
                }
                return;
            case INDEPENDENCE:
                Faction newLord = (Faction) warParty.getWar().getCasusBelli().getTarget();
                Faction newVassal = (Faction) warParty.getLeader();
                new RelationRequest(Bukkit.getConsoleSender(), newLord, newVassal, Relation.VASSAL).confirm();
                newVassal.setAllod(true);
                newVassal.getCasusBelli().add(new CasusBelli(CasusBelli.Type.INDEPENDENCE, newLord, new Date(System.currentTimeMillis() + (config.getCBLiberationExp() * FConfig.DAY))));
                return;
            case RAID:
                Faction enemyF = (Faction) warParty.getEnemy().getLeader();
                int bounty = enemyF.getMembers().size() * warParty.getKills();
                enemyF.getAccount().withdraw(bounty);
                return;
        }

        enemyLeader.setTimeLastPeace(System.currentTimeMillis());
        enemyLeader.setScoreLastPeace(warParty.getPoints());

        if (enemyLeader.getRegions().isEmpty()) {
            enemyLeader.disband();
        }
        warParty.getWar().end();
    }

    // True if the peace time for faction is over. Peace time is (last score / 2) * day
    public boolean isInPeace(Faction faction) {
        long time = faction.getTimeLastPeace();
        int days = faction.getScoreLastPeace() / 2;
        if (time == 0) {
            return false;
        }
        if (days < 0) {
            days = days * (-1);
        }
        long now = System.currentTimeMillis();
        return (time + (86400000 * days)) > now;
    }

    public void relationFixer(War war) {
        Set<Faction> factionSetA =  war.getAttacker().getFactions();
        Set<Faction> factionSetD = war.getDefender().getFactions();
        // Set all relations to enemy
        for (Faction attacker : factionSetA) {
            for (Faction defender : factionSetD) {
                if (attacker != defender) { // To prevent self-declaration, for example for vassals
                    if (attacker.getRelation(defender) != Relation.ENEMY) {
                        new RelationRequest(Bukkit.getConsoleSender(), attacker, defender, Relation.ENEMY).confirm();
                        MessageUtil.log("Relation fixer found a wrong relation: " + attacker.getName() + " & " + defender.getName());
                    }
                }
            }
        }
    }
}
