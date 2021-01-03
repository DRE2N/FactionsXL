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

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.LegalEntity;
import de.erethon.factionsxl.war.demand.WarDemandCreationGUI;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Daniel Saukel
 */
public class WarCache {

    private Set<War> wars = new HashSet<>();
    private Set<War> unconfirmedWars = new HashSet<>();

    private WarDemandCreationGUI warDemandCreationMenu;

    public WarCache(FactionsXL plugin, File dir) {
        assert dir.isDirectory();
        warDemandCreationMenu = new WarDemandCreationGUI(plugin);
        for (File file : dir.listFiles()) {
            wars.add(new War(file));
        }
    }

    /* Getters and setters */
    /**
     * @return all confirmed wars
     */
    public Set<War> getWars() {
        return wars;
    }

    /**
     * @return all wars that are not confirmed yet
     */
    public Set<War> getUnconfirmedWars() {
        return unconfirmedWars;
    }

    /**
     * @param date the start date
     * @return the war with the matching date ID
     */
    public War getByDate(Date date) {
        for (War war : wars) {
            if (war.getStartDate().equals(date)) {
                return war;
            }
        }
        return null;
    }

    /**
     * @param date the start date
     * @return the war with the matching date ID
     */
    public War getByDate(long date) {
        for (War war : wars) {
            if (war.getStartDate().getTime() == date) {
                return war;
            }
        }
        return null;
    }

    /**
     * @param faction the faction to check
     * @return all wars in that the faction takes part
     */
    public Set<War> getByFaction(Faction faction) {
        Set<War> fWars = new HashSet<>();
        for (War war : wars) {
            if (war.getAttacker().getFactions().contains(faction) || war.getDefender().getFactions().contains(faction)) {
                fWars.add(war);
            }
        }
        return fWars;
    }

    /**
     * @param f1 the faction to check
     * @param f2 the other faction
     * @return the war the two factions are in. // TODO: Only works with the current system (one war only)
     */
    public War getWarTogether(Faction f1, Faction f2) {
        for (War war : wars) {
            if (war.getAttacker().getFactions().contains(f1) && war.getDefender().getFactions().contains(f2)) {
                return war;
            }
        }
        return null;
    }

    /**
     * @param party the WarParty to check
     * @return all wars in that the faction takes part
     */
    public War getByParty(WarParty party) {
        for (War war : wars) {
            if (war.getAttacker().equals(party) || war.getDefender().equals(party)) {
                return war;
            }
        }
        return null;
    }

    /**
     * @param attacker the attacker to check
     * @param defender the defender to check
     * @return all wars in that the faction takes part
     */
    public War getByPartyLeaders(LegalEntity attacker, LegalEntity defender) {
        for (War war : wars) {
            if (war.getAttacker().getLeader().equals(attacker) && war.getDefender().getLeader().equals(defender)) {
                return war;
            }
        }
        return null;
    }

    /**
     * @return the WarDemandCreationGUI
     */
    public WarDemandCreationGUI getWarDemandCreationMenu() {
        return warDemandCreationMenu;
    }

    @Deprecated
    public War getUnsafe(Faction faction) {
        return getByFaction(faction).toArray(new War[]{})[0];
    }

    /* Persistence */
    /**
     * Saves all wars
     */
    public void saveAll() {
        for (War war : wars) {
            war.save();
        }
    }

}
