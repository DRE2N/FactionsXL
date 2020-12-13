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

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.LegalEntity;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A reason to start a war.
 *
 * @author Daniel Saukel
 */
public class CasusBelli {

    public enum Type {
        // When two factions share a border
        BORDER_FRICTION,
        // When the leader of the attacker has a claim on the throne of the defender
        CLAIM_ON_THRONE,
        // When a factions has claims on foreign land
        CONQUEST,
        // When a member of a federation is disloyal
        IMPERIAL_BAN,
        // Vassals may declare war on their lord to gain independence
        INDEPENDENCE,
        // When an ally of the faction got vassalized
        LIBERATION,
        // Permanent CB with no possible demands 
        RAID,
        // When a PU broke
        RESTORATION_OF_UNION,
        // When a faction has cores on foreign land
        RECONQUEST,
        // When a faction has more than 50% claims on foreign land
        SUBJAGATION,
        // When a vassal declared its independence
        RESUBJAGATION
    }

    private Type type;
    private LegalEntity target;
    private Faction liberate;
    private Date expiration;

    public CasusBelli(Type type, LegalEntity target, Date expiration) {
        this.type = type;
        this.target = target;
        this.expiration = expiration;
    }

    public CasusBelli(LegalEntity target, Faction liberate, Date expiration) {
        this(Type.LIBERATION, target, expiration);
        this.liberate = liberate;
    }

    public CasusBelli(ConfigurationSection config) {
        type = Type.valueOf(config.getString("type"));
        target = FactionsXL.getInstance().getFactionCache().getById(config.getInt("target"));
        if (type == Type.LIBERATION) {
            liberate = FactionsXL.getInstance().getFactionCache().getById(config.getInt("liberate"));
        }
        expiration = new Date(config.getLong("expiration"));
    }

    /**
     * @return
     * the CB type
     */
    public Type getType() {
        return type;
    }

    /**
     * @return
     * the enemy
     */
    public LegalEntity getTarget() {
        return target;
    }

    /**
     * @return
     * the faction to liberate if the CB is LIBERATION;
     * null if CB is anything else
     */
    public Faction getFactionToLiberate() {
        return liberate;
    }

    /**
     * @return
     * the date when the CB expires
     */
    public Date getExpirationDate() {
        return expiration;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("target", target.getId());
        serialized.put("type", type.toString());
        if (liberate != null) {
            serialized.put("liberate", liberate.getId());
        }
        if (expiration != null) {
            serialized.put("expiration", expiration.getTime());
        }
        return serialized;
    }

    @Override
    public String toString() {
        return "CasusBelli{type=" + type.name() + "target=" + target.toString() + "}";
    }

}
