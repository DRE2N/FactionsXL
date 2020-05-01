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
import de.erethon.factionsxl.gui.StandardizedGUI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Daniel Saukel
 */
public class ResourceMenu implements Listener, InventoryHolder {

    FactionsXL plugin = FactionsXL.getInstance();

    private ItemStack exportButton;
    private ItemStack importButton;

    private Faction faction;
    private Resource resource;
    private Inventory gui;

    public ResourceMenu(Faction faction, Resource resource) {
        exportButton = StandardizedGUI.DOWN.clone();
        ItemMeta exMeta = exportButton.getItemMeta();
        exMeta.setDisplayName(FMessage.TRADE_EXPORT.getMessage());
        double exValue = resource.getValue() * plugin.getFConfig().getExportModifier();
        List<String> exLore = new ArrayList<>(Arrays.asList(ChatColor.GREEN + FMessage.TRADE_PRICE.getMessage() + ": +" + exValue));
        exMeta.setLore(exLore);
        exportButton.setItemMeta(exMeta);

        importButton = StandardizedGUI.UP.clone();
        ItemMeta imMeta = importButton.getItemMeta();
        imMeta.setDisplayName(FMessage.TRADE_IMPORT.getMessage());
        double imValue = resource.getValue() * plugin.getFConfig().getImportModifier();
        List<String> imLore = new ArrayList<>(Arrays.asList(ChatColor.DARK_RED + FMessage.TRADE_PRICE.getMessage() + ": -" + imValue));
        imMeta.setLore(imLore);
        importButton.setItemMeta(imMeta);

        this.faction = faction;
        this.resource = resource;
        setupGUI();
        update();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void setupGUI() {
        gui = Bukkit.createInventory(this, 27, FMessage.TRADE_RESOURCE_TITLE.getMessage(resource.getName(), faction.getName()));
        ItemStack banner = faction.getBannerStack();
        ItemMeta meta = banner.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + faction.getName());
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        banner.setItemMeta(meta);
        gui.setItem(0, GUIButton.BACK);
        gui.setItem(1, banner);
        gui.setItem(2, banner);
        gui.setItem(3, banner);
        gui.setItem(5, banner);
        gui.setItem(6, banner);
        gui.setItem(7, banner);
        gui.setItem(8, banner);
        gui.setItem(13, importButton);
        gui.setItem(22, exportButton);
    }

    public Inventory getGUI() {
        return gui;
    }

    public void update() {
        gui.setItem(4, TradeMenu.formButton(faction, resource));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || !PageGUI.getGUITitle(gui).equals(event.getView().getTitle())) {
            return;
        }
        event.setCancelled(true);
        PageGUI.playSound(event);
        ItemStack button = inventory.getItem(event.getSlot());
        if (button == null) {
            return;
        }
        int current = faction.getImportValue(resource);
        if (button.equals(GUIButton.BACK)) {
            faction.getTradeMenu().open(player);
            return;
        } else if (button.equals(exportButton)) {
            faction.getGroceryList().put(resource, current - 1);
        } else if (button.equals(importButton)) {
            faction.getGroceryList().put(resource, current + 1);
        }
        update();
    }

    @Override
    public Inventory getInventory() {
        return gui;
    }
}
