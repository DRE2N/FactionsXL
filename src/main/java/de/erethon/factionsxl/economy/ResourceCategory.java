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
package de.erethon.factionsxl.economy;

import static de.erethon.factionsxl.economy.ResourceSubcategory.*;

/**
 * @author Daniel Saukel
 */
public enum ResourceCategory {

    BUILDING(WOOD, ROADS, STONE),
    CLOTHES(FUR, LEATHER),
    FOOD(STAPLES, MEAT, SPICES, WATER),
    HEATING(ResourceSubcategory.HEATING),
    JEWELRY(ResourceSubcategory.JEWELRY),
    MILITARY(CANNONING, RIDERS, SMITHERY),
    SCIENCE(ALCHEMY, LITERATURE);

    private ResourceSubcategory[] subcategories;

    ResourceCategory(ResourceSubcategory... subcategories) {
        this.subcategories = subcategories;
    }

    public ResourceSubcategory[] getSubcategories() {
        return subcategories;
    }

}
