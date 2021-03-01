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
import de.erethon.factionsxl.economy.Resource;
import de.erethon.factionsxl.economy.ResourceSubcategory;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.gui.StandardizedGUI;
import de.erethon.factionsxl.legacygui.GUIButton;
import de.erethon.factionsxl.legacygui.PageGUI;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * @author Daniel Saukel
 */
public class DemandMenu implements Listener, InventoryHolder {

    FactionsXL plugin = FactionsXL.getInstance();

    private Faction faction;
    private Region region;
    private Inventory gui;
    private SaturationMenu saturationMenu;
    private PopulationLevel poplevel;

    public DemandMenu(Faction faction, Region region, PopulationLevel level) {
        this.faction = faction;
        this.region = region;
        this.poplevel = level;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        saturationMenu = new SaturationMenu(faction, region, level);
    }

    public void update(ResourceSubcategory category) {
        gui = Bukkit.createInventory(this, 27, FMessage.POPULATION_DEMANDS_TITLE.getMessage(category.getName()));
        StandardizedGUI.addHeader(gui);

        for (Resource resource : category.getResources()) {
            gui.addItem(formButton(faction, region, resource, poplevel));
        }
    }

    // TODO: Needs updating for PopulationLevels
    public static ItemStack formButton(Faction faction, Region region, Resource resource, PopulationLevel poplevel) {
        ItemStack icon = resource.getIcon();
        ItemMeta meta = icon.getItemMeta();
        SaturationLevel level = region.isResourceSaturated(resource);
        if (level == SaturationLevel.SURPLUS) {
            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.setDisplayName(level.getColor() + resource.getName());
        ArrayList<String> lore = new ArrayList<>();
        //lore.add(ProgressBar.getBar((double) faction.getSaturatedResources().get(resource)));
        lore.add(level.getColor().toString() + region.getSaturatedResources().get(resource) + "%");
        String population = String.valueOf(region.getPopulation(poplevel));
        String units = String.valueOf(region.getDemand(resource, poplevel));
        lore.add(FMessage.POPULATION_REQUIRED.getMessage(population, units, resource.getName()));
        lore.add(FMessage.POPULATION_GRANTING1.getMessage());
        lore.add(FMessage.POPULATION_GRANTING2.getMessage(String.valueOf(region.getConsumableResources().get(resource)), resource.getName()));
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    public void open(HumanEntity player, ResourceSubcategory category) {
        update(category);
        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || gui == null || inventory.getHolder() != this) {
            return;
        }
        event.setCancelled(true);
        PageGUI.playSound(event);
        ItemStack button = event.getCurrentItem();
        if (GUIButton.BACK.equals(button)) {
            new PopulationSubMenu(faction, region, poplevel).openDemands(player);
            return;
        }
        Resource resource = Resource.getByIcon(button);
        if (resource != null && faction.isPrivileged(player)) {
            saturationMenu.open(player, resource);
        }
    }

    @Override
    public Inventory getInventory() {
        return gui;
    }
}
