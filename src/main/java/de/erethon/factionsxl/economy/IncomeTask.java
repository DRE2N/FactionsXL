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

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.util.CoringHandler;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Daniel Saukel
 */
public class IncomeTask extends BukkitRunnable {

    FactionsXL plugin = FactionsXL.getInstance();
    CoringHandler core =  plugin.getCoring();

    @Override
    public void run() {
        core.calculateCoringProgress();
        core.calculateClaimTime();
        for (Faction faction : plugin.getFactionCache().getActive()) {
            faction.payday();
        }
        plugin.getFData().lastNewDay = System.currentTimeMillis();
        plugin.getFData().save();
    }

}
