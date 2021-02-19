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
package de.erethon.factionsxl.economy;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.gui.StandardizedGUI;
import de.erethon.factionsxl.legacygui.PageGUI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Saukel
 */
public class TradeMenu implements Listener {

    FactionsXL plugin = FactionsXL.getInstance();

    private Faction faction;
    private PageGUI gui;
    private Map<Resource, ResourceMenu> resourceMenus = new HashMap<>();

    public TradeMenu(Faction faction) {
        this.faction = faction;
        gui = new PageGUI(FMessage.TRADE_TITLE.getMessage(faction.getName()));
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void update() {
        if (!plugin.getFConfig().isEconomyEnabled()) {
            gui = new PageGUI(FMessage.ERROR_ECON_DISABLED.getMessage());
            for (int i = 0; i <= 44; i++) {
                gui.getPages().get(0).setItem(i, StandardizedGUI.DISABLED);
            }
        } else {
            gui.clear();
            for (Resource resource : Resource.values()) {
                gui.addButton(formButton(faction, resource));
                if (!resourceMenus.containsKey(resource)) {
                    resourceMenus.put(resource, new ResourceMenu(faction, resource));
                } else {
                    resourceMenus.get(resource).update();
                }
            }
        }
    }

    public static ItemStack formButton(Faction faction, Resource resource) {
        FactionsXL plugin = FactionsXL.getInstance();
        FConfig config = plugin.getFConfig();
        Economy econ = plugin.getEconomyProvider();
        ItemStack icon = resource.getIcon();
        ItemMeta meta = icon.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();

        int consume = faction.getTotalConsumableResources().get(resource);
        int income = faction.getImportValue(resource);
        if (consume != 0) {
            lore.add(FMessage.TRADE_CONSUME.getMessage("-" + consume));
        }

        boolean export = income < 0;
        if (income != 0) {
            ChatColor color = (export ? ChatColor.DARK_RED : ChatColor.GREEN);
            ChatColor color2 = (export ? ChatColor.GREEN : ChatColor.DARK_RED);
            String money = econ.format(-1 * income * resource.getValue() * (export ? config.getExportModifier() : config.getImportModifier()));
            lore.add(color + (export ? FMessage.TRADE_EXPORT : FMessage.TRADE_IMPORT).getMessage() + ": " + (export ? new String() : "+") + income + color2 + " (" + money + ")");
        }

        for (Region region : faction.getRegions()) {
            Map<Resource, Integer> resources = region.getType().getResources(region.getLevel());
            if (resources.containsKey(resource)) {
                int amount = resources.get(resource);
                income += amount;
                lore.add(ChatColor.GREEN + region.getName() + ": +" + amount);
            }
        }
        meta.setLore(lore);

        int balance = income - consume;
        ChatColor color = ChatColor.YELLOW;
        if (balance > 0) {
            color = ChatColor.GREEN;
        } else if (balance < 0) {
            color = ChatColor.DARK_RED;
        }
        meta.setDisplayName(color + resource.getName() + " (" + (balance > 0 ? "+" : new String()) + balance + ")");

        icon.setItemMeta(meta);
        return icon;
    }

    public void open(HumanEntity player) {
        update();
        gui.open(player);
    }

    public void openResourceMenu(HumanEntity player, Resource resource) {
        player.openInventory(resourceMenus.get(resource).getGUI());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || !gui.getTitle().equals(event.getView().getTitle()) || event.getSlot() >= 45) {
            return;
        }
        ItemStack button = inventory.getItem(event.getSlot());
        if (button == null || button.equals(StandardizedGUI.DISABLED)) {
            return;
        }
        if (!faction.isPrivileged(event.getWhoClicked())) {
            return;
        }
        Resource resource = Resource.getByIcon(button);
        openResourceMenu(player, resource);
    }

}
