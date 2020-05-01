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
package de.erethon.factionsxl.gui;

import static de.erethon.commons.gui.GUIButton.*;
import de.erethon.commons.javaplugin.DREPlugin;
import de.erethon.factionsxl.config.FMessage;
import static de.erethon.factionsxl.gui.AmountSelectionGUI.Max.*;
import static de.erethon.factionsxl.gui.AmountSelectionGUI.Min.*;
import static de.erethon.factionsxl.gui.StandardizedGUI.CONTINUE;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel Saukel
 *
 * @param <N> the type of Number to calculate may be either Byte, Short, Integer, Long, Float, Double or BigDecimal
 */
public abstract class AmountSelectionGUI<N extends Number> implements Listener, StandardizedGUI {

    public enum Min {
        MIN_0_01,
        MIN_1
    }

    public enum Max {
        MAX_10(3, 5),
        MAX_100(2, 4),
        MAX_1000(2, 4),
        MAX_10000(1, 3),
        MAX_100000(1, 3),
        MAX_1000000(0, 2);

        private int min001;
        private int min1;

        Max(int min001, int min1) {
            this.min001 = min001;
            this.min1 = min1;
        }
    }

    private static Map<Inventory, AmountSelectionGUI> cache = new HashMap<>();

    static {
        Bukkit.getPluginManager().registerEvents(new Listener(), DREPlugin.getInstance());
    }

    private ItemStack[] contents;
    private String titleStart = ChatColor.GREEN.toString() + ChatColor.BOLD.toString()
            + FMessage.TRADE_OFFER_AMOUNT.getMessage() + ": " + ChatColor.DARK_AQUA.toString();
    private String plus = ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "+";
    private String minus = ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() + "-";

    private N amount;

    public AmountSelectionGUI(Min min, Max max, N amount) {
        this.amount = amount;
        if (amount instanceof Byte || amount instanceof Short || amount instanceof Integer || amount instanceof Long || amount instanceof BigInteger) {
            min = Min.MIN_1;
        }

        ItemStack amountInc1000000 = setDisplay(UP_ALT, plus + 1000000);
        ItemStack amountInc100000 = setDisplay(UP_ALT, plus + 100000);
        ItemStack amountInc10000 = setDisplay(UP_ALT, plus + 10000);
        ItemStack amountInc1000 = setDisplay(UP_ALT, plus + 1000);
        ItemStack amountInc100 = setDisplay(UP_ALT, plus + 100);
        ItemStack amountInc10 = setDisplay(UP_ALT, plus + 10);
        ItemStack amountInc1 = setDisplay(UP_ALT, plus + 1);
        ItemStack amountInc01 = setDisplay(UP_ALT, plus + 0.1);
        ItemStack amountInc001 = setDisplay(UP_ALT, plus + 0.01);
        ItemStack amountDec1000000 = setDisplay(DOWN_ALT, minus + 1000000);
        ItemStack amountDec100000 = setDisplay(DOWN_ALT, minus + 100000);
        ItemStack amountDec10000 = setDisplay(DOWN_ALT, minus + 10000);
        ItemStack amountDec1000 = setDisplay(DOWN_ALT, minus + 1000);
        ItemStack amountDec100 = setDisplay(DOWN_ALT, minus + 100);
        ItemStack amountDec10 = setDisplay(DOWN_ALT, minus + 10);
        ItemStack amountDec1 = setDisplay(DOWN_ALT, minus + 1);
        ItemStack amountDec01 = setDisplay(DOWN_ALT, minus + 0.1);
        ItemStack amountDec001 = setDisplay(DOWN_ALT, minus + 0.01);
        List<ItemStack> inc = new ArrayList<>();
        List<ItemStack> dec = new ArrayList<>();
        switch (max) {
            case MAX_1000000:
                inc.add(amountInc1000000);
                dec.add(amountDec1000000);
            case MAX_100000:
                inc.add(amountInc100000);
                dec.add(amountDec100000);
            case MAX_10000:
                inc.add(amountInc10000);
                dec.add(amountDec10000);
            case MAX_1000:
                inc.add(amountInc1000);
                dec.add(amountDec1000);
            case MAX_100:
                inc.add(amountInc100);
                dec.add(amountDec100);
            case MAX_10:
                inc.add(amountInc10);
                dec.add(amountDec10);
                inc.add(amountInc1);
                dec.add(amountDec1);
        }
        if (min == MIN_0_01) {
            inc.add(amountInc01);
            dec.add(amountDec01);
            inc.add(amountInc001);
            dec.add(amountDec001);
        }
        contents = new ItemStack[]{
            BACK, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, CONTINUE,
            PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER,
            PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER
        };
        int i = 9 + (min == MIN_0_01 ? max.min001 : max.min1);
        for (ItemStack button : inc) {
            contents[i] = button;
            i++;
        }
        i = 18 + (min == MIN_0_01 ? max.min001 : max.min1);
        for (ItemStack button : dec) {
            contents[i] = button;
            i++;
        }
    }

    @Override
    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, titleStart + amount);
        gui.setContents(contents);
        player.openInventory(gui);
        cache.put(gui, this);
    }

    public static class Listener implements org.bukkit.event.Listener {

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            AmountSelectionGUI gui = cache.get(event.getClickedInventory());
            if (gui == null || !(event.getWhoClicked() instanceof Player)) {
                return;
            }
            Player player = (Player) event.getWhoClicked();

            ItemStack button = event.getCurrentItem();
            if (button == null) {
                return;
            }
            if (BACK.equals(button)) {
                gui.onClickBack(player);
            } else if (CONTINUE.equals(button)) {
                gui.onClickContinue(player, gui.amount);
            } else {
                String name = button.getItemMeta().getDisplayName();
                if (name.startsWith(gui.plus)) {
                    gui.amount = gui.asN(gui.bdAmount().add(new BigDecimal(name.replace(gui.plus, ""))));
                } else if (name.startsWith(gui.minus)) {
                    gui.amount = gui.asN(gui.bdAmount().subtract(new BigDecimal(name.replace(gui.minus, ""))));
                } else {
                    return;
                }
                gui.open(player);
            }
        }

    }

    private BigDecimal bdAmount() {
        if (amount instanceof BigDecimal) {
            return (BigDecimal) amount;
        } else {
            return new BigDecimal(amount.doubleValue());
        }
    }

    private N asN(BigDecimal d) {
        if (amount instanceof BigDecimal) {
            return (N) d;
        } else if (amount instanceof Double) {
            return (N) new Double(d.doubleValue());
        } else if (amount instanceof Float) {
            return (N) new Float(d.floatValue());
        } else if (amount instanceof Long) {
            return (N) new Long(d.longValue());
        } else if (amount instanceof Integer) {
            return (N) new Integer(d.intValue());
        } else if (amount instanceof Short) {
            return (N) new Short(d.shortValue());
        } else if (amount instanceof Byte) {
            return (N) new Byte(d.byteValue());
        } else {
            return null;
        }
    }

    public abstract void onClickBack(Player player);

    public abstract void onClickContinue(Player player, N amount);

}
