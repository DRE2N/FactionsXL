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

package de.erethon.factionsxl.population;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.economy.ResourceSubcategory;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.legacygui.PageGUI;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PopulationMenu implements Listener, InventoryHolder {

    FactionsXL plugin = FactionsXL.getInstance();

    private final Faction faction;
    private final Region region;
    private Inventory inv;

    public PopulationMenu(Faction faction, Region region) {
        this.faction = faction;
        this.region = region;
        setupGUI();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void setupGUI() {
        inv = Bukkit.createInventory(this, 9, FMessage.POPULATION_TITLE.getMessage(faction.getName()));
        for (PopulationLevel level : PopulationLevel.values()) {
            if (!region.getPopulationHappiness().containsKey(level)) {
                continue;
            }
            ItemStack icon = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(level.name());
            List<String> lore = new ArrayList<>();
            lore.add("Pop: " + region.getPopulation().get(level));
            lore.add("Happy: " + region.getPopulationHappiness().get(level).name());
            List<String> catsNeeded = new ArrayList<>();
            for (ResourceSubcategory subcategory : plugin.getFConfig().getResourceConsumption().get(level).keySet()) {
                catsNeeded.add(subcategory.getName());
            }
            lore.add("Cats needed: " + ParsingUtil.collectionToString(catsNeeded));
            meta.setLore(lore);
            icon.setItemMeta(meta);
            inv.addItem(icon);
        }
    }

    public void open(Player player) {
        player.openInventory(inv);
    }

    public Inventory getInventory() {
        return inv;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        HumanEntity player = event.getWhoClicked();
        if (inventory == null || inventory.getHolder() != this) {
            return;
        }
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) {
            return;
        }
        PageGUI.playSound(event);
        String buttonName = event.getCurrentItem().getItemMeta().getDisplayName();
        PopulationLevel level = PopulationLevel.valueOf(buttonName);
        new PopulationSubMenu(faction, region, level).openMain(player);


    }
}
