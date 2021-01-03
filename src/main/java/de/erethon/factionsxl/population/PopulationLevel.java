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

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.economy.ResourceSubcategory;
import de.erethon.factionsxl.faction.Faction;

import java.util.Map;

public enum PopulationLevel {

    BEGGAR(),
    PEASANT(),
    CITIZEN(),
    PATRICIAN(),
    NOBLEMEN();

    PopulationLevel() {
    }

    public Map<ResourceSubcategory, Integer> getRequiredResources() {
        FactionsXL plugin = FactionsXL.getInstance();
        Map<PopulationLevel, Map<ResourceSubcategory, Integer>> required = plugin.getFConfig().getPopulationLevelResources();
        return required.get(this);
    }

    public boolean canLevelUp(Region rg, PopulationLevel level) {
        FactionsXL plugin = FactionsXL.getInstance();
        Faction faction = rg.getOwner();
        faction.updateSaturatedSubcategories();
        boolean happy = true;
        Map<ResourceSubcategory, Integer> saturated = faction.getSaturatedSubcategories();
        for (ResourceSubcategory subcategory : getRequiredResources().keySet()) {
            //faction.getDemand();
            }

        return true;
    }
}
