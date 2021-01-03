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
package de.erethon.factionsxl.war.demand;

import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.war.WarParty;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * Expects static methods:<br>
 * ItemStack getGUIButton() - returns a button for the war demand menu<br>
 * void openSetupGUI(Player) - opens a GUI to setup the demand to the player
 *
 * @author Daniel Saukel
 */
public interface WarDemand extends ConfigurationSerializable {

    /**
     * Asks the demanded party to pay
     */
    public void demand();

    /**
     * if the demanded has enough Warscore
     */
    public boolean canAffordWP(WarParty wp);

    public boolean canAffordWP(Faction f);

    /**
     * Asks the demanded party to pay
     *
     * @param f, f2
     */
    public void pay(WarParty f, WarParty f2);

    public void pay(Faction f, Faction f2);

    /**
     * Warscore cost of all demands
     */
    public double getWarscoreCost();

    /**
     * @return
     * if the target faction is able to pay
     */
    public boolean canPay(WarParty wp);

    public boolean canPay(Faction f);

}
