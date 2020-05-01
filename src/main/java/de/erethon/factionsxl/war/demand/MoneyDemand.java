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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel Saukel
 */
public class MoneyDemand implements WarDemand {

    private BigDecimal amount;

    public MoneyDemand(BigDecimal amount) {
        this.amount = amount;
    }

    public MoneyDemand(Map<String, Object> args) {
        amount = BigDecimal.valueOf((Double) args.get("amount"));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public double getWarscoreCost() {
        return amount.doubleValue() / 1000;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public static ItemStack getGUIButton() {
        return GUIButton.setDisplay(new ItemStack(Material.GOLD_INGOT), FMessage.WAR_DEMAND_CREATION_MENU_MONEY.getMessage());
    }

    public static void openSetupGUI(Player player) {
        new AmountSelectionGUI<BigDecimal>(Min.MIN_0_01, Max.MAX_1000000, new BigDecimal(0)) {
            @Override
            public void onClickBack(Player player) {
                FactionsXL.getInstance().getWarCache().getWarDemandCreationMenu().open(player);
            }

            @Override
            public void onClickContinue(Player player, BigDecimal amount) {
                WarDemand war = (WarDemand) new MoneyDemand(amount);
                FactionsXL.getInstance().getFPlayerCache().getByPlayer(player).getPeaceOffer().getDemands().add(war);
                MessageUtil.sendMessage(player, FMessage.WAR_DEMAND_MONEY_ADDED.getMessage());
                FactionsXL.getInstance().getFPlayerCache().getByPlayer(player).listWarDemands();
                FactionsXL.getInstance().getWarCache().getWarDemandCreationMenu().open(player);
            }
        }.open(player);
    }

    @Override
    public void demand() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void pay(WarParty wp, WarParty wp2) {
        wp2.getLeader().getAccount().withdraw(amount.doubleValue());
        float size = wp.getFactions().size();
        for (Faction f : wp.getFactions()) {
            f.getAccount().deposit(amount.doubleValue() / size);
        }
    }

    @Override
    public void pay(Faction f, Faction f2) {
        f2.getAccount().withdraw(amount.doubleValue());
        f.getAccount().deposit(amount.doubleValue());
    }

    @Override
    public boolean canPay(WarParty wp) {
        if (amount.doubleValue() < 0) {
            return false;
        }
        return wp.getLeader().getAccount().getBalance() >= amount.doubleValue();
    }

    @Override
    public boolean canPay(Faction f) {
        if (amount.doubleValue() < 0) {
            return false;
        }
        return f.getAccount().getBalance() >= amount.doubleValue();
    }

    @Override
    public boolean canAffordWP(WarParty wp) {
        return wp.getPoints() >= getWarscoreCost();
    }

    @Override
    public boolean canAffordWP(Faction f) {
        return  true;
    }


    @Override
    public String toString() {
        return FMessage.WAR_DEMAND_MONEY_CHAT.getMessage(String.valueOf(amount), String.valueOf(getWarscoreCost()));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> args = new HashMap<>();
        args.put("amount", amount);
        return args;
    }

}
