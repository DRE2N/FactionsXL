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
package de.erethon.factionsxl.board.dynmap;

/**
 * @author Daniel Saukel
 */
public class Atlas {

    public static final FactionMap FACTION_MAP = new FactionMap();
    public static final RegionMap REGION_MAP = new RegionMap();

    public static void init() {
        FACTION_MAP.init();
        REGION_MAP.init();
    }

}
