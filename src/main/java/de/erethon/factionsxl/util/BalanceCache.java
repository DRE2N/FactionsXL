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

package de.erethon.factionsxl.util;

import de.erethon.factionsxl.FactionsXL;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class BalanceCache extends BukkitRunnable {

    private Map<Player, Double> balanceCache = new HashMap<>();

    FactionsXL plugin = FactionsXL.getInstance();
    Economy econ = plugin.getEconomyProvider();

    public BalanceCache() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            balanceCache.put(p, econ.getBalance(p));
        }
    }

    @Override
    public void run() {
        balanceCache.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            balanceCache.put(p, econ.getBalance(p));
        }
    }

    public double getCachedBalance(Player p) {
        if (balanceCache.get(p) == null) {  // If player is not in cache, add to cache
            balanceCache.put(p, econ.getBalance(p));
        }
        return balanceCache.get(p);
    }
}
