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

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.building.Building;
import de.erethon.factionsxl.economy.Resource;
import de.erethon.factionsxl.economy.ResourceSubcategory;
import de.erethon.factionsxl.faction.Faction;
import org.bukkit.ChatColor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum PopulationLevel {

    BEGGAR(),
    PEASANT(),
    CITIZEN(),
    PATRICIAN(),
    NOBLEMEN();

    PopulationLevel() {
    }

    public Map<ResourceSubcategory, Integer> getConsumption() {
        FactionsXL plugin = FactionsXL.getInstance();
        Map<PopulationLevel, Map<ResourceSubcategory, Integer>> required = plugin.getFConfig().getResourceConsumption();
        return required.get(this);
    }

    public Map<ResourceSubcategory, Integer> getRequiredResourcesForLevelUp() {
        FactionsXL plugin = FactionsXL.getInstance();
        Map<PopulationLevel, Map<ResourceSubcategory, Integer>> required = plugin.getFConfig().getPopulationLevelResourcesRequired();
        return required.get(this);
    }

    public Set<Building> getRequiredRegionBuildings() {
        FactionsXL plugin = FactionsXL.getInstance();
        Map<String, Boolean> buildingMap = plugin.getFConfig().getPopulationLevelBuildings().get(this);
        Set<Building> buildings = new HashSet<>();
        for (String id : buildingMap.keySet()) {
            if (!buildingMap.get(id)) {
                Building building = plugin.getBuildingManager().getByID(id);
                if (building == null) {
                    MessageUtil.log(ChatColor.RED + "There was an error loading required buildings for " + this.toString() + ". Building " + id + " does not exist. Check your config.yml!");
                    continue;
                }
                buildings.add(building);
            }
        }
        return buildings;
    }

    public Set<Building> getRequiredFactionBuildings() {
        FactionsXL plugin = FactionsXL.getInstance();
        Map<String, Boolean> buildingMap = plugin.getFConfig().getPopulationLevelBuildings().get(this);
        Set<Building> buildings = new HashSet<>();
        for (String id : buildingMap.keySet()) {
            if (buildingMap.get(id)) {
                Building building = plugin.getBuildingManager().getByID(id);
                if (building == null) {
                    MessageUtil.log(ChatColor.RED + "There was an error loading required buildings for " + this.toString() + ". Building " + id + " does not exist. Check your config.yml!");
                    continue;
                }
                buildings.add(building);
            }
        }
        return buildings;
    }

    public boolean hasRequiredRegionBuildings(Region rg) {
        Set<Building> buildingsRequired = getRequiredRegionBuildings();
        Set<Building> buildingsBuilt = new HashSet<>();
        rg.getBuildings().forEach(buildSite -> buildingsBuilt.add(buildSite.getBuilding()));
        return buildingsBuilt.containsAll(buildingsRequired);
    }

    public boolean hasRequiredFactionBuildings(Faction f) {
        Set<Building> buildingsRequired = getRequiredFactionBuildings();
        Set<Building> buildingsBuilt = new HashSet<>();
        f.getFactionBuildings().forEach(buildSite -> buildingsBuilt.add(buildSite.getBuilding()));
        return buildingsBuilt.containsAll(buildingsRequired);
    }

    public boolean canLevelUp(Region rg, PopulationLevel level) {
        FactionsXL plugin = FactionsXL.getInstance();
        Faction faction = rg.getOwner();
        rg.updateSaturatedSubcategories();
        boolean happy = true;
        Map<ResourceSubcategory, Integer> saturated = rg.getSaturatedSubcategories();
        for (ResourceSubcategory subcategory : getRequiredResourcesForLevelUp().keySet()) {
            //faction.getDemand();
            }

        return true;
    }
}
