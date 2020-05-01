/*
 *
 *  * Copyright (C) 2017-2020 Daniel Saukel, Malfrador
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.erethon.factionsxl.board.dynmap;

import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

/**
 * This is a modified version of mikeprimms plugin Dynmap-Factions taken from FactionsUUID by drtshock.
 *
 * @author Mike Primm, Trent Hensler, Daniel Saukel
 */
public class TempMarker {

    // -------------------------------------------- //
    // FIELDS
    // -------------------------------------------- //
    public String label;
    public String world;
    public double x;
    public double y;
    public double z;
    public String iconName;
    public String description;

    // -------------------------------------------- //
    // CREATE
    // -------------------------------------------- //
    public Marker create(MarkerAPI markerApi, MarkerSet markerset, String markerId) {
        Marker ret = markerset.createMarker(markerId, this.label, this.world, this.x, this.y, this.z, getMarkerIcon(markerApi, this.iconName), false // not persistent
        );

        if (ret == null) {
            return null;
        }

        ret.setDescription(this.description);

        return ret;
    }

    // -------------------------------------------- //
    // UPDATE
    // -------------------------------------------- //
    public void update(MarkerAPI markerApi, Marker marker) {
        if (!this.world.equals(marker.getWorld()) || this.x != marker.getX() || this.y != marker.getY() || this.z != marker.getZ()) {
            marker.setLocation(this.world, this.x, this.y, this.z);
        }

        if (!marker.getLabel().equals(this.label)) {
            marker.setLabel(this.label);
        }

        MarkerIcon icon = getMarkerIcon(markerApi, this.iconName);
        if (marker.getMarkerIcon() == null || marker.getMarkerIcon().equals(icon)) {
            marker.setMarkerIcon(icon);
        }

        if (!marker.getDescription().equals(this.description)) {
            marker.setDescription(this.description);
        }
    }

    // -------------------------------------------- //
    // UTIL
    // -------------------------------------------- //
    public static MarkerIcon getMarkerIcon(MarkerAPI markerApi, String name) {
        MarkerIcon ret = markerApi.getMarkerIcon(name);
        if (ret == null) {
            ret = markerApi.getMarkerIcon(DynmapStyle.DEFAULT_HOME_MARKER);
        }
        return ret;
    }

}
