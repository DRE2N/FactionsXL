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
package de.erethon.factionsxl.war.demand;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.gui.GUIButton;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.entity.RelationRequest;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.LegalEntity;
import de.erethon.factionsxl.gui.AmountSelectionGUI;
import de.erethon.factionsxl.gui.AmountSelectionGUI.Max;
import de.erethon.factionsxl.gui.AmountSelectionGUI.Min;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import de.erethon.factionsxl.war.WarParty;
import de.erethon.vignette.api.component.InventoryButton;
import de.erethon.vignette.api.component.InventoryButtonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel Saukel
 */
public class RelationDemand implements WarDemand {


    public RelationDemand() {
    }

    @Override
    public double getWarscoreCost() {
        return 75;
    }

    public static ItemStack getGUIButton() {
        return GUIButton.setDisplay(new ItemStack(Material.GREEN_WOOL), FMessage.RELATION_VASSAL.getMessage());
    }


    @Override
    public void pay(WarParty wp, WarParty wp2) {
        new RelationRequest(Bukkit.getConsoleSender(), (Faction) wp.getLeader(), (Faction) wp2.getLeader(), Relation.VASSAL).confirm();
        // TODO: Might break after government update
    }

    @Override
    public void pay(Faction f, Faction f2) {

    }

    @Override
    public boolean canPay(WarParty wp) {
        return true;
    }

    @Override
    public boolean canPay(Faction f) {
        return true;
    }

    @Override
    public void demand() {

    }

    @Override
    public boolean canAffordWP(WarParty wp) {
        return wp.getPoints() >= getWarscoreCost();
    }

    @Override
    public boolean canAffordWP(Faction f) {
        return false;
    }

    @Override
    public String toString() {
        return "&6Vassal " + "&8(&7Warscore&8: &5" + getWarscoreCost() + "&8)";
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> args = new HashMap<>();
        args.put("amount", getWarscoreCost());
        return args;
    }

}
