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
package de.erethon.factionsxl.population;

import de.erethon.commons.gui.GUIButton;
import de.erethon.commons.gui.PageGUI;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.economy.Resource;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.gui.StandardizedGUI;
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

import java.awt.print.Pageable;

/**
 * @author Daniel Saukel
 */
public class SaturationMenu implements Listener, InventoryHolder {

    FactionsXL plugin = FactionsXL.getInstance();

    public static final ItemStack GRANT_BUTTON;
    public static final ItemStack DENY_BUTTON;
    public static final ItemStack DENY_BUTTON_ZERO;

    static {
        GRANT_BUTTON = StandardizedGUI.UP.clone();
        ItemMeta gMeta = GRANT_BUTTON.getItemMeta();
        gMeta.setDisplayName(FMessage.POPULATION_GRANT_RESOURCE.getMessage());
        GRANT_BUTTON.setItemMeta(gMeta);

        DENY_BUTTON = StandardizedGUI.DOWN.clone();
        ItemMeta dMeta = DENY_BUTTON.getItemMeta();
        dMeta.setDisplayName(FMessage.POPULATION_DENY_RESOURCE.getMessage());
        DENY_BUTTON.setItemMeta(dMeta);

        DENY_BUTTON_ZERO = StandardizedGUI.DOWN_ALT.clone();
        ItemMeta zMeta = DENY_BUTTON_ZERO.getItemMeta();
        zMeta.setDisplayName(ChatColor.DARK_GRAY + ChatColor.stripColor(FMessage.POPULATION_DENY_RESOURCE.getMessage()));
        DENY_BUTTON_ZERO.setItemMeta(zMeta);
    }

    private Faction faction;
    private Inventory gui;

    public SaturationMenu(Faction faction) {
        this.faction = faction;
        setupGUI();
        update(Resource.WATER);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void setupGUI() {
        gui = Bukkit.createInventory(this, 27, FMessage.POPULATION_ADJUST_CONSUME.getMessage(faction.getName()));
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
        gui.setItem(13, GRANT_BUTTON);
    }

    public void open(HumanEntity player, Resource resource) {
        update(resource);
        player.openInventory(gui);
    }

    public void update(Resource resource) {
        gui.setItem(4, DemandMenu.formButton(faction, resource));
        if (faction.getConsumableResources().get(resource) > 0) {
            gui.setItem(22, DENY_BUTTON);
        } else {
            gui.setItem(22, DENY_BUTTON_ZERO);
        }
    }

    /* Listener */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        Inventory i = event.getClickedInventory();
        if (event.getInventory().getHolder() != this) {
            return;
        }
        event.setCancelled(true);
        PageGUI.playSound(event);
        ItemStack button = i.getItem(event.getSlot());
        if (button == null) {
            return;
        }
        Resource resource = getResource(gui);
        int current = faction.getConsumableResources().get(resource);
        if (button.equals(GUIButton.BACK)) {
            faction.getPopulationMenu().openDemands(player);
            return;
        } else if (button.equals(DENY_BUTTON)) {
            faction.getConsumableResources().put(resource, current - 1);
        } else if (button.equals(GRANT_BUTTON)) {
            faction.getConsumableResources().put(resource, current + 1);
        }
        update(resource);
    }

    /* Statics */
    public static Resource getResource(Inventory gui) {
        ItemStack icon = gui.getItem(4);
        if (icon == null) {
            return null;
        } else {
            return Resource.getByIcon(icon);
        }
    }

    @Override
    public Inventory getInventory() {
        return gui;
    }
}
