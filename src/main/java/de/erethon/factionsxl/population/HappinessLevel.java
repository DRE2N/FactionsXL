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

package de.erethon.factionsxl.population;

import de.erethon.factionsxl.config.FMessage;

public enum HappinessLevel {
    ANGRY(FMessage.ERROR_NAME_IN_USE),
    UNHAPPY(FMessage.ERROR_NAME_IN_USE),
    CONTENT(FMessage.ERROR_NAME_IN_USE),
    HAPPY(FMessage.ERROR_NAME_IN_USE),
    EUPHORIC(FMessage.ERROR_NAME_IN_USE);

    private FMessage name;

    HappinessLevel(FMessage name) {
        this.name = name;
    }


    public HappinessLevel getAbove() {
        int current = ordinal();
        current = current + 1;
        int length = values().length;
        if ((current + 1) > (length - 1)) {
            current = length - 1;
        }
        return values()[current];
    }

    public HappinessLevel getBelow() {
        int current = ordinal();
        current = current - 1;
        if ((current - 1) < 0) {
            current = 1;
        }
        return values()[current];
    }
}
