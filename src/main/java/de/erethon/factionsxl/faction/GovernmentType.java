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
package de.erethon.factionsxl.faction;

import de.erethon.factionsxl.config.FMessage;

/**
 * An enumeration of all government types.
 *
 * @author Daniel Saukel
 */
public enum GovernmentType {

    MONARCHY(false, true, FMessage.GOVERNMENT_TYPE_MONARCHY),
    REPUBLIC(true, false, FMessage.GOVERNMENT_TYPE_REPUBLIC),
    THEOCRACY(true, false, FMessage.GOVERNMENT_TYPE_THEOCRACY);

    private boolean indivisible;
    private boolean personalUnions;
    private FMessage name;

    GovernmentType(boolean indivisible, boolean personalUnions, FMessage name) {
        this.indivisible = indivisible;
        this.personalUnions = personalUnions;
        this.name = name;
    }

    /**
     * @return
     * the name of the type
     */
    public String getName() {
        return name.getMessage();
    }

    /**
     * @return
     * if the land is indivisible
     */
    public boolean isIndivisible() {
        return indivisible;
    }

    /**
     * @return
     * if the government type allows personal unions
     */
    public boolean allowsPersonalUnions() {
        return personalUnions;
    }

    public static GovernmentType fromString(String string) {
        string = string.toUpperCase();
        switch (string) {
            case "A":
            case "ARISTOCRACY":
            case "M":
            case "MONARCHY":
            case "K":
            case "KINGDOM":
                return MONARCHY;
            case "D":
            case "DEMOCRACY":
            case "R":
            case "REPUBLIC":
                return REPUBLIC;
            case "B":
            case "BISHOPRIC":
            case "T":
            case "THEOCRACY":
                return THEOCRACY;
            default:
                return null;
        }
    }

}
