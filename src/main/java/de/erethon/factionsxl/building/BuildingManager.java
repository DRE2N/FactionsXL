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

package de.erethon.factionsxl.building;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import org.bukkit.block.Block;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BuildingManager {

    FactionsXL plugin = FactionsXL.getInstance();

    private List<Building> buildings = new CopyOnWriteArrayList<>();

    public BuildingManager() {
        load();
    }

    public Building getByID(String id) {
        for (Building building : buildings) {
            if (building.getId().equals(id)) {
                return building;
            }
        }
        return null;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public void load() {
        File dir = FactionsXL.BUILDINGS;
        if (dir.listFiles() == null) {
            MessageUtil.log("No buildings found. Please create some.");
            return;
        }
        for (File file : dir.listFiles()) {
            buildings.add(new Building(file));
        }
        MessageUtil.log("Loaded " + buildings.size() + " Buildings.");
    }

    public BuildSite getBuildSite(Block check, Region region) {
        for (BuildSite buildSite : region.getBuildings()) {
            if (buildSite.getInteractive().equals(check)) {
                return buildSite;
            }
        }
        return null;
    }
}
