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
import de.erethon.commons.gui.PageGUI;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.gui.StandardizedGUI;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.war.WarParty;
import de.erethon.factionsxl.war.peaceoffer.PeaceOffer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.erethon.vignette.api.InventoryGUI;
import de.erethon.vignette.api.SingleInventoryGUI;
import de.erethon.vignette.api.layout.CenteredInventoryLayout;
import de.erethon.vignette.api.layout.FlowInventoryLayout;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Daniel Saukel
 * TODO: Should get updated to Vignette
 */
public class WarDemandCreationGUI implements Listener, StandardizedGUI, InventoryHolder {

    private FactionsXL plugin;

    private Inventory gui;

    private ItemStack listDemands = GUIButton.setDisplay(new ItemStack(Material.BOOK), FMessage.WAR_DEMAND_CREATION_MENU_LIST.getMessage());
    private ItemStack send = GUIButton.setDisplay(StandardizedGUI.MAILBOX, FMessage.WAR_DEMAND_CREATION_MENU_SEND.getMessage());
    private Faction enemy;


    public WarDemandCreationGUI(FactionsXL plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void open(Player player) {
        player.openInventory(gui);
    }

    public void open(Player player, Faction e, boolean isOffer) {
        this.enemy = e;
        if (isOffer)
            gui = Bukkit.createInventory(this, 18, FMessage.WAR_DEMAND_CREATION_MENU_TITLE_OFFER.getMessage());
        else {
            gui = Bukkit.createInventory(this, 18, FMessage.WAR_DEMAND_CREATION_MENU_TITLE_DEMAND.getMessage());
        }
        StandardizedGUI.addHeader(gui);
        gui.addItem(listDemands);
        PeaceOffer.getDemandTypes().forEach(t -> {
            try {
                gui.addItem((ItemStack) t.getDeclaredMethod("getGUIButton").invoke(null));
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
                FactionsXL.debug("[Warning] The demand type " + t.getName() + " lacks a GUI button.");
                gui.addItem(GUIButton.setDisplay(new ItemStack(Material.ANVIL), t.getSimpleName()));
            }
        });
        ItemStack raidItem = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta raidMeta = raidItem.getItemMeta();
        raidMeta.setDisplayName(FMessage.WAR_DEMAND_CREATION_MENU_REGION_BUTTON.getMessage());
        raidItem.setItemMeta(raidMeta);
        gui.addItem(raidItem);
        gui.setItem(17, send);
        ItemStack delItem = new ItemStack(Material.BARRIER);
        ItemMeta delMeta = delItem.getItemMeta();
        delMeta.setDisplayName(FMessage.WAR_DEMAND_CREATION_MENU_CLEAR_BUTTON.getMessage());
        delItem.setItemMeta(delMeta);
        gui.setItem(15, delItem);
        player.openInventory(gui);
    }


    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Inventory inventory = event.getClickedInventory();
        if (event.getInventory().getHolder() != this) {
            return;
        }
        event.setCancelled(true);
        PageGUI.playSound(event);
        ItemStack button = event.getCurrentItem();
        if (button == null) {
            return;
        }
        FPlayer player = plugin.getFPlayerCache().getByPlayer((Player) event.getWhoClicked());
        if (GUIButton.BACK.equals(button)) {
            Set<Faction> ownFactions = plugin.getFactionCache().getByLeader(player.getPlayer());
            Set<WarParty> parties = new HashSet<>();
            ownFactions.forEach(f -> f.getWarParties().forEach(p -> parties.add(p.getEnemy())));
            new WarDemandWarPartyGUI(plugin, ownFactions, parties.toArray(new WarParty[]{})).open(player.getPlayer());
        } else if (listDemands.equals(button)) {
            player.listWarDemands();
        } else if (send.equals(button)) {
            player.getPeaceOffer().send();
        } else if (button.getItemMeta().getDisplayName().contains(FMessage.WAR_DEMAND_CREATION_MENU_CLEAR_BUTTON.getMessage())) {
            player.getPeaceOffer().getDemands().clear();
            MessageUtil.sendMessage(event.getWhoClicked(), FMessage.WAR_DEMAND_CREATION_MENU_CLEARED.getMessage());
        } else if (button.getItemMeta().getDisplayName().contains(FMessage.WAR_DEMAND_CREATION_MENU_REGION_BUTTON.getMessage())) {
            if (player.getPeaceOffer().isOffer()) {
                new RegionDemand().openSetupGUI((Player) event.getWhoClicked(), plugin.getFactionCache().getByFPlayer(player));
            }
            new RegionDemand().openSetupGUI((Player) event.getWhoClicked(), enemy);
        } else if (button.getItemMeta().getDisplayName().contains(FMessage.RELATION_VASSAL.getMessage())) {
            new RelationDemand();
        } else {
            PeaceOffer.openSetupGUI(PeaceOffer.getDemandType(button), player.getPlayer());
        }

    }

    @Override
    public Inventory getInventory() {
        return gui;
    }
}
