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
package de.erethon.factionsxl.board.dynmap;

import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

/**
 * This is a modified version of mikeprimms plugin Dynmap-Factions taken from FactionsUUID by drtshock.
 *
 * @author Mike Primm, Trent Hensler, Daniel Saukel
 */
public class TempMarkerSet {

    public String label;
    public int minimumZoom;
    public int priority;
    public boolean hideByDefault;

    public MarkerSet create(MarkerAPI markerApi, String id) {
        MarkerSet ret = markerApi.createMarkerSet(id, this.label, null, false); // ("null, false" at the end means "all icons allowed, not perisistent")

        if (ret == null) {
            return null;
        }

        // Minimum Zoom
        if (this.minimumZoom > 0) {
            ret.setMinZoom(this.minimumZoom);
        }

        // Priority
        ret.setLayerPriority(this.priority);

        // Hide by Default
        ret.setHideByDefault(this.hideByDefault);

        return ret;
    }

    public void update(MarkerSet markerset) {
        // Name
        if (!markerset.getMarkerSetLabel().equals(this.label)) {
            markerset.setMarkerSetLabel(this.label);
        }

        if (this.minimumZoom > 0) {
            if (markerset.getMinZoom() != this.minimumZoom) {
                markerset.setMinZoom(this.minimumZoom);
            }
        }

        if (markerset.getLayerPriority() != this.priority) {
            markerset.setLayerPriority(this.priority);
        }

        if (markerset.getHideByDefault() != this.hideByDefault) {
            markerset.setHideByDefault(this.hideByDefault);
        }
    }

}
