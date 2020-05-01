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

import de.erethon.commons.gui.GUIButton;
import de.erethon.commons.gui.PageGUI;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel Saukel
 */
public class EconomyMenu implements Listener, InventoryHolder {

    FactionsXL plugin = FactionsXL.getInstance();

    public static final ItemStack INCOME_MANAGEMENT = GUIButton.setDisplay(new ItemStack(Material.IRON_INGOT), FMessage.TRADE_INCOME_MANAGEMENT.getMessage());
    public static final ItemStack TRADE_OFFER = GUIButton.setDisplay(GUIButton.PLAYER_HEAD, FMessage.TRADE_OFFER_TITLE.getMessage());
    public static final ItemStack STORAGE = GUIButton.setDisplay(new ItemStack(Material.CHEST), FMessage.FACTION_STORAGE.getMessage());

    private Faction faction;
    private Inventory gui;

    public EconomyMenu(Faction faction) {
        this.faction = faction;
        setupGUI();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void setupGUI() {
        gui = Bukkit.createInventory(this, 9, FMessage.TRADE_ECONOMY.getMessage(faction.getName()));
        gui.setItem(2, INCOME_MANAGEMENT);
        gui.setItem(4, TRADE_OFFER);
        gui.setItem(6, STORAGE);
    }

    public void open(HumanEntity player) {
        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        Inventory i = event.getClickedInventory();
        if (event.getInventory().getHolder() != this) {
            return;
        }
        event.setCancelled(true);
        PageGUI.playSound(event);
        ItemStack button = event.getCurrentItem();
        if (INCOME_MANAGEMENT.equals(button)) {
            faction.getTradeMenu().open(player);
        } else if (TRADE_OFFER.equals(button)) {
            plugin.getCommandCache().tradeOffer.onExecute(new String[]{"tradeOffer", faction.getName()}, player);
        } else if (STORAGE.equals(button)) {
            plugin.getCommandCache().storage.onExecute(new String[]{"storage", faction.getName()}, player);
        }
    }

    @Override
    public Inventory getInventory() {
        return gui;
    }
}
