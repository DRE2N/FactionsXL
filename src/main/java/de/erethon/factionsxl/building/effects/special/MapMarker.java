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

package de.erethon.factionsxl.building.effects.special;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.board.dynmap.Atlas;
import de.erethon.factionsxl.board.dynmap.DynmapStyle;
import de.erethon.factionsxl.building.effects.SubEffect;
import de.erethon.factionsxl.faction.Faction;
import org.bukkit.Location;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;

public class MapMarker implements SubEffect {

    FactionsXL plugin = FactionsXL.getInstance();

    public MapMarker(String data) {
    }

    @Override
    public void load(String data) {

    }

    @Override
    public void add(Faction faction, Location loc) {
        MarkerAPI markerApi = Atlas.FACTION_MAP.markerApi;
        Atlas.FACTION_MAP.markerset.createMarker("Test", "Desc","Desc2", loc.getX(),10.00, loc.getZ(), markerApi.getMarkerIcon(DynmapStyle.DEFAULT_HOME_MARKER), true);
    }

    @Override
    public void add(Region region, Location location) {

    }

    @Override
    public String save() {
        return null;
    }
}
