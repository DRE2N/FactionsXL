/*
 * Copyright (c) 2017-2018 Daniel Saukel
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
package io.github.dre2n.factionsxl.war.demand;

import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel Saukel
 */
public interface WarDemand {

    /**
     * @return
     * a button for the war demand menu
     */
    public ItemStack getGUIButton();

    /**
     * Asks the demanded party to pay
     */
    public void demand();

    /**
     * Asks the demanded party to pay
     *
     * @return
     * if the target faction is able to pay
     */
    public boolean pay();

    /**
     * @return
     * if the target faction is able to pay
     */
    public boolean canPay();

}
