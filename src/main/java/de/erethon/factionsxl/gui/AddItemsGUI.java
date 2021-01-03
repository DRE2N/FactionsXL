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
package de.erethon.factionsxl.gui;

import de.erethon.commons.javaplugin.DREPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Saukel
 */
public abstract class AddItemsGUI implements StandardizedGUI {

    private static Map<Inventory, AddItemsGUI> cache = new HashMap<>();

    static {
        Bukkit.getPluginManager().registerEvents(new Listener(), DREPlugin.getInstance());
    }

    private Inventory gui;
    private boolean forbidDamages;

    public AddItemsGUI(String title, int size, boolean forbidDamages) {
        gui = Bukkit.createInventory(null, size, title);
        cache.put(gui, this);
    }

    @Override
    public void open(Player player) {
        player.openInventory(gui);
    }

    public static class Listener implements org.bukkit.event.Listener {

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            AddItemsGUI gui = cache.get(event.getInventory());
            if (gui == null || !gui.forbidDamages) {
                return;
            }
            ItemStack current = event.getCurrentItem();
            if (current.getDurability() != 0) {
                event.getInventory().remove(current);
                event.getWhoClicked().getInventory().addItem(current);
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            AddItemsGUI gui = cache.get(event.getInventory());
            if (gui == null) {
                return;
            }
            gui.onClose();
        }

    }

    public ItemStack[] getStorageContents() {
        return gui.getStorageContents();
    }

    public abstract void onClose();

}
