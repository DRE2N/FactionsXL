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
package de.erethon.factionsxl.idea;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.gui.GUIButton;
import de.erethon.commons.gui.PageGUI;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.economy.Resource;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.gui.StandardizedGUI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Daniel Saukel
 */
public class IdeaMenu implements Listener {

    FactionsXL plugin = FactionsXL.getInstance();

    private Faction faction;
    private Inventory groups;
    private Map<IdeaGroup, Inventory> ideas = new HashMap<>();

    public IdeaMenu(Faction faction) {
        this.faction = faction;
        updateGroups();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void updateGroups() {
        int size = (int) (9 * Math.ceil(((double) IdeaGroup.values().length / 9)));
        String title = ChatColor.translateAlternateColorCodes('&', FMessage.IDEA_MENU_GROUPS_TITLE.getMessage());
        groups = Bukkit.createInventory(null, size, title);
        for (IdeaGroup group : IdeaGroup.values()) {
            boolean has = faction.getIdeaGroups().contains(group);
            boolean freeSlots = plugin.getFConfig().getMaxIdeaGroups() > faction.getIdeaGroups().size();
            ItemStack button = has || freeSlots ? group.getIcon() : StandardizedGUI.DISABLED.clone();
            ItemMeta meta = button.getItemMeta();
            meta.setDisplayName((has ? ChatColor.GREEN : ChatColor.DARK_RED) + group.getName());
            List<String> lore = new ArrayList<>();
            if (freeSlots || has) {
                lore.add(FMessage.MISC_SHIFT_CLICK_SELECT.getMessage());
            } else {
                lore.add(FMessage.ERROR_MAX_IDEA_GROUPS_REACHED.getMessage());
            }
            meta.setLore(lore);
            button.setItemMeta(meta);
            groups.addItem(button);
        }
    }

    private void updateIdeas(IdeaGroup group) {
        Class handler = group.getHandler();
        int size = (int) (9 * Math.ceil(((double) (handler.getEnumConstants().length + 1) / 9)));
        ideas.put(group, Bukkit.createInventory(null, size, FMessage.IDEA_MENU_IDEAS_TITLE.getMessage(group.getName())));
        ideas.get(group).setItem(0, GUIButton.BACK);
        for (Object ideaObj : handler.getEnumConstants()) {
            Idea idea = (Idea) ideaObj;
            boolean has = faction.getIdeas().contains(idea);
            ItemStack button = idea.getIcon();
            ItemMeta meta = button.getItemMeta();
            meta.setDisplayName((has ? ChatColor.GREEN : ChatColor.DARK_RED) + idea.getName());
            List<String> lore = new ArrayList<>(Arrays.asList(idea.getDescription()));
            if (!has) {
                for (Entry<Resource, Integer> entry : idea.getPrice().entrySet()) {
                    lore.add(ChatColor.DARK_RED + "-" + entry.getKey().getName() + ": " + entry.getValue());
                }
                if (faction.getIdeaGroups().contains(group)) {
                    lore.add(FMessage.MISC_SHIFT_CLICK_PURCHASE.getMessage());
                } else {
                    lore.add(FMessage.ERROR_SELECT_IDEA_GROUP.getMessage());
                }
            }
            meta.setLore(lore);
            button.setItemMeta(meta);
            ideas.get(group).addItem(button);
        }
    }

    public void openGroups(HumanEntity player) {
        updateGroups();
        player.openInventory(groups);
    }

    public void openIdeas(HumanEntity player, ItemStack button) {
        if (button == null) {
            return;
        }
        IdeaGroup group = IdeaGroup.getByIcon(button);
        if (group == null) {
            return;
        }
        openIdeas(player, group);
    }

    public void openIdeas(HumanEntity player, IdeaGroup group) {
        updateIdeas(group);
        player.openInventory(ideas.get(group));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        if (event.getView().getTitle().equals(PageGUI.getGUITitle(groups))) {
            onGroupsClick(event);
        }
        for (Entry<IdeaGroup, Inventory> gui : ideas.entrySet()) {
            if (PageGUI.getGUITitle(gui.getValue()).equals(event.getView().getTitle())) {
                onIdeasClick(event);
                break;
            }
        }
    }

    private void onGroupsClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        ItemStack button = event.getCurrentItem();
        event.setCancelled(true);
        PageGUI.playSound(event);
        IdeaGroup group = IdeaGroup.getByIcon(button);
        if (event.getClick() == ClickType.SHIFT_LEFT) {
            boolean selected = group.select(faction);
            MessageUtil.sendMessage(player, (selected ? FMessage.IDEA_MENU_GROUPS_SELECTED : FMessage.IDEA_MENU_GROUPS_DESELECTED).getMessage(group.getName()));
            openGroups(player);
        } else {
            openIdeas(player, group);
        }
    }

    private void onIdeasClick(InventoryClickEvent event) {
        event.setCancelled(true);
        PageGUI.playSound(event);
        ItemStack button = event.getCurrentItem();
        if (button == null) {
            return;
        }

        HumanEntity player = event.getWhoClicked();
        if (button.equals(GUIButton.BACK)) {
            openGroups(player);
            return;
        } else if (!faction.isPrivileged(player)) {
            return;
        }

        Idea idea = IdeaGroup.getIdeaByIcon(button);
        if (faction.getIdeaGroups().contains(idea.getGroup()) && !faction.getIdeas().contains(idea) && event.getClick() == ClickType.SHIFT_LEFT) {
            boolean purchased = idea.purchase(faction);
            MessageUtil.sendMessage(player, (purchased ? FMessage.MISC_PURCHASE_SUCCESS : FMessage.MISC_PURCHASE_FAIL).getMessage(idea.getName()));
        }
        openIdeas(player, idea.getGroup());
    }

}
