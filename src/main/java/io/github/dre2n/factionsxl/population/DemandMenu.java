/*
 * Copyright (C) 2017 Daniel Saukel
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
package io.github.dre2n.factionsxl.population;

import io.github.dre2n.factionsxl.FactionsXL;
import io.github.dre2n.factionsxl.config.FMessage;
import io.github.dre2n.factionsxl.economy.Resource;
import io.github.dre2n.factionsxl.economy.ResourceSubcategory;
import io.github.dre2n.factionsxl.faction.Faction;
import io.github.dre2n.factionsxl.util.PageGUI;
import io.github.dre2n.factionsxl.util.ProgressBar;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Daniel Saukel
 */
public class DemandMenu implements Listener {

    FactionsXL plugin = FactionsXL.getInstance();

    private Faction faction;
    private Inventory gui;
    private SaturationMenu saturationMenu;

    public DemandMenu(Faction faction) {
        this.faction = faction;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        saturationMenu = new SaturationMenu(faction);
    }

    public void update(ResourceSubcategory category) {
        gui = Bukkit.createInventory(null, 27, FMessage.POPULATION_DEMANDS_TITLE.getMessage(category.getName()));
        PageGUI.addHeader(gui);

        for (Resource resource : category.getResources()) {
            gui.addItem(formButton(faction, resource));
        }
    }

    public static ItemStack formButton(Faction faction, Resource resource) {
        ItemStack icon = resource.getIcon();
        ItemMeta meta = icon.getItemMeta();
        SaturationLevel level = faction.isResourceSaturated(resource);
        if (level == SaturationLevel.SURPLUS) {
            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.setDisplayName(level.getColor() + resource.getName());
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ProgressBar.getBar((double) faction.getSaturatedResources().get(resource)));
        lore.add(level.getColor().toString() + faction.getSaturatedResources().get(resource) + "%");
        lore.add(FMessage.POPULATION_REQUIRED.getMessage(String.valueOf(faction.getPopulation()), String.valueOf(0), resource.getName()));//TODO: REQUIRED RESOURCE AMOUNT
        lore.add(FMessage.POPULATION_GRANTING1.getMessage());
        lore.add(FMessage.POPULATION_GRANTING2.getMessage(String.valueOf(faction.getConsumableResources().get(resource)), resource.getName()));
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
        if (inventory == null || gui == null || !gui.getTitle().equals(inventory.getTitle())) {
            return;
        }
        event.setCancelled(true);
        PageGUI.playSound(event);
        ItemStack button = event.getCurrentItem();
        if (PageGUI.BACK.equals(button)) {
            faction.getPopulationMenu().openDemands(player);
            return;
        }
        Resource resource = Resource.getByIcon(button);
        if (resource != null && faction.isPrivileged(player)) {
            saturationMenu.open(player, resource);
        }
    }

}
