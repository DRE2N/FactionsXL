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
import de.erethon.factionsxl.config.FMessage;
import org.bukkit.ChatColor;

import static org.bukkit.ChatColor.*;

/**
 * @author Daniel Saukel
 */
public enum SaturationLevel {

    SURPLUS(FMessage.SATURATION_LEVEL_SURPLUS, BLUE, 100, 10),
    FULLY_SATURATED(FMessage.SATURATION_LEVEL_FULLY_SATURATED, DARK_AQUA, 90, 7),
    SATURATED(FMessage.SATURATION_LEVEL_SATURATED, DARK_GREEN, 80, 5),
    MOSTLY_SATURATED(FMessage.SATURATION_LEVEL_MOSTLY_SATURATED, GREEN, 60, 2),
    PARTIALLY_SATURATED(FMessage.SATURATION_LEVEL_PARTIALLY_SATURATED, YELLOW, 40, 0),
    POOR(FMessage.SATURATION_LEVEL_POOR, GOLD, 20, -2),
    NOT_SATURATED(FMessage.SATURATION_LEVEL_NOT_SATURATED, RED, 0, -5),
    NOT_SATURATED_BASIC(FMessage.SATURATION_LEVEL_NOT_SATURATED, DARK_RED, 0, -50);

    private FMessage name;
    private ChatColor color;
    private int minPercentage;
    private int stabilityBonus;

    SaturationLevel(FMessage name, ChatColor color, int minPercentage, int stabilityBonus) {
        this.name = name;
        this.color = color;
        this.minPercentage = minPercentage;
        this.stabilityBonus = stabilityBonus;
    }

    public String getName() {
        return name.getMessage();
    }

    public ChatColor getColor() {
        return color;
    }

    public int getMinPercentage() {
        return minPercentage;
    }

    public int getStabilityBonus() {
        return stabilityBonus;
    }

    /* Statics */
    /**
     * For non-basic resource subcategories.
     *
     * @param percentage
     * the saturation percentage of a resource
     */
    public static SaturationLevel getByPercentage(int percentage) {
        return getByPercentage(percentage, false);
    }

    /**
     * @param percentage
     * the saturation percentage of a resource
     * @param basic
     * if the resource subcategory is basic. Basic resource subcategories use NOT_SATURATED_BASIC instead of NOT_SATURATED
     */
    public static SaturationLevel getByPercentage(int percentage, boolean basic) {
        for (SaturationLevel level : values()) {
            if (level == NOT_SATURATED) {
                return basic ? NOT_SATURATED_BASIC : NOT_SATURATED;
            } else if (percentage >= level.getMinPercentage()) {
                return level;
            }
        }
        return NOT_SATURATED;
    }

    /**
     * @param population
     * the population of a faction
     * @return
     * the amount of required resource units for the population
     */
    public static int getRequiredResourceUnits(int population) {
        return (int) ((((double) population) / 1000) * FactionsXL.getInstance().getFConfig().getRequiredResourceUnitsPer1000Persons());
    }

}
