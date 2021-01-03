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
package de.erethon.factionsxl.economy;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.Federation;
import net.milkbowl.vault.economy.Economy;

/**
 * @author Daniel Saukel
 */
public class FAccount {

    Economy econ = FactionsXL.getInstance().getEconomyProvider();

    private String name;

    public FAccount(Faction faction) {
        name = "faction-" + faction.getId();
    }

    public FAccount(Federation federation) {
        name = "federation-" + federation.getId();
    }

    /* Getters and setters */
    /**
     * @return
     * the Vault account name
     */
    public String getName() {
        return name;
    }

    /* Actions */
    /**
     * @param amount
     * the amount of money to deposit
     */
    public void deposit(double amount) {
        if (!econ.hasAccount(name)) {
            econ.createPlayerAccount(name);
        }
        econ.depositPlayer(name, amount);
    }

    /**
     * @param amount
     * the amount of money to withdraw
     */
    public void withdraw(double amount) {
        if (!econ.hasAccount(name)) {
            econ.createPlayerAccount(name);
        }
        econ.withdrawPlayer(name, amount);
    }

    /**
     * @return
     * the amount of money that this account stores
     */
    public double getBalance() {
        if (!econ.hasAccount(name)) {
            econ.createPlayerAccount(name);
        }
        return econ.getBalance(name);
    }

    /**
     * @param amount
     * the amount of money to set
     */
    public void setBalance(double amount) {
        withdraw(getBalance());
        deposit(amount);
    }

    /**
     * @return
     * a formatted balance String
     */
    public String getFormatted() {
        return econ.format(getBalance());
    }

}
