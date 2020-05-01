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
package de.erethon.factionsxl.war.peaceoffer;

import de.erethon.commons.gui.GUIButton;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.entity.Request;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.demand.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel Saukel
 */
public abstract class PeaceOffer extends Request {

    private static Map<Class<? extends WarDemand>, Method[]> types = new HashMap<>();
    private boolean isOffer;

    static {
        registerDemandType(ItemDemand.class);
        registerDemandType(MoneyDemand.class);
        registerDemandType(RelationDemand.class);
        registerDemandType(RegionDemand.class);
    }

    /**
     * A Set of all types that can be expanded
     *
     * @return all types
     */
    public static Set<Class<? extends WarDemand>> getDemandTypes() {
        return types.keySet();
    }

    /**
     * Registers a DemandType
     *
     * @param type the type to register
     */
    public static void registerDemandType(Class<? extends WarDemand> type) {
        try {
            types.put(type, new Method[]{type.getDeclaredMethod("getGUIButton"), type.getDeclaredMethod("openSetupGUI", Player.class)});
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException exception) {
        }
    }

    /**
     * Returns the demand type that matchs the GUI button
     *
     * @param button the GUI button
     * @return the type of the demand
     */
    public static Class<? extends WarDemand> getDemandType(ItemStack button) {
        for (Class<? extends WarDemand> clazz : getDemandTypes()) {
            if (getDemandButton(clazz).equals(button)) {
                return clazz;
            }
        }
        return null;
    }

    /**
     * Returns the demand button of the type
     *
     * @param type the type
     * @return the demand button of the type
     */
    public static ItemStack getDemandButton(Class<? extends WarDemand> type) {
        try {
            return (ItemStack) types.get(type)[0].invoke(null);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
            FactionsXL.debug("[Warning] The demand type " + type.getName() + " lacks a GUI button.");
            return GUIButton.setDisplay(new ItemStack(Material.ANVIL), type.getSimpleName());
        }
    }

    /**
     * Opens a setup GUI to the player
     *
     * @param type   the type
     * @param player the player
     */
    public static void openSetupGUI(Class<? extends WarDemand> type, Player player) {
        try {
            types.get(type)[1].invoke(null, player);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
            FactionsXL.debug("[Warning] The demand type " + type.getName() + " lacks a setup GUI.");
        }
    }

    protected War war;
    protected List<WarDemand> demands = new ArrayList<>(); // UnsupportedOperationException: null

    /**
     * The war that the two parties fight
     *
     * @return the war
     */
    public War getWar() {
        return war;
    }
    public boolean isOffer() {
        return isOffer;
    }


    public boolean canPay() {return false;}

    /**
     * @return the demands
     */
    public List<WarDemand> getDemands() {
        return demands;
    }

}
