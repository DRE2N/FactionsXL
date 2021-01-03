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
package de.erethon.factionsxl.idea;

import de.erethon.factionsxl.economy.Resource;
import de.erethon.factionsxl.faction.Faction;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * @author Daniel Saukel
 */
public interface Idea {

    /**
     * @return
     * the inventory GUI icon
     */
    public ItemStack getIcon();

    /**
     * @return
     * the name of the idea
     */
    public String getName();

    /**
     * @return
     * a short description of what the idea does
     */
    public String[] getDescription();

    /**
     * @return
     * the price to purchase the idea
     */
    public Map<Resource, Integer> getPrice();

    /**
     * @param faction
     * the faction that purchases the idea
     * @return
     * if the faction can afford the idea
     */
    public boolean purchase(Faction faction);

    /**
     * @return
     * the IdeaGroup
     */
    public IdeaGroup getGroup();

}
