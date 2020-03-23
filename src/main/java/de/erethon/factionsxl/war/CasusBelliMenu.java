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

package de.erethon.factionsxl.war;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.gui.PageGUI;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;

public class CasusBelliMenu implements Listener, InventoryHolder {
    FactionsXL plugin = FactionsXL.getInstance();
    Inventory gui;
    Faction object;

    public CasusBelliMenu() {
        gui = Bukkit.createInventory(this, 27, ChatColor.GOLD + "Casus Belli");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player, Faction f) {
        FPlayer fplayer = plugin.getFPlayerCache().getByPlayer(player);
        Faction faction = fplayer.getFaction();
        object = f;
        // Raid is always available
        ItemStack raidItem = new ItemStack(Material.STONE_SWORD);
        ItemMeta raidMeta = raidItem.getItemMeta();
        raidMeta.setDisplayName(FMessage.WAR_CB_RAID.getMessage());
        raidItem.setItemMeta(raidMeta);
        gui.addItem(raidItem);
        // Other CBs
        for (CasusBelli cb : faction.getCasusBelli()) {
            ItemStack guiItem = new ItemStack(Material.BEDROCK);
            ItemMeta guiMeta = guiItem.getItemMeta();
            // TODO: Descriptions for the CBs. Already in FMessage.
            switch (cb.getType()) {
                case CLAIM_ON_THRONE:
                    break;
                case CONQUEST:
                    guiMeta.setDisplayName(FMessage.WAR_CB_CONQUEST.getMessage());
                    guiItem.setType(Material.DIAMOND_SWORD);
                    guiItem.setItemMeta(guiMeta);
                    gui.addItem(guiItem);
                case IMPERIAL_BAN:
                    break;
                case INDEPENDENCE:
                    break;
                case LIBERATION:
                    guiMeta.setDisplayName(FMessage.WAR_CB_LIBERATION.getMessage());
                    guiItem.setType(Material.LEAD);
                    guiItem.setItemMeta(guiMeta);
                    gui.addItem(guiItem);
                case BORDER_FRICTION:
                    guiMeta.setDisplayName(FMessage.WAR_CB_BORDER.getMessage());
                    guiItem.setType(Material.STICKY_PISTON);
                    guiItem.setItemMeta(guiMeta);
                    gui.addItem(guiItem);
                case RESTORATION_OF_UNION:
                    break;
                case RECONQUEST:
                    guiMeta.setDisplayName(FMessage.WAR_CB_RECONQUEST.getMessage());
                    guiItem.setType(Material.GOLDEN_SWORD);
                    guiItem.setItemMeta(guiMeta);
                    gui.addItem(guiItem);
                case RESUBJAGATION:
                    break;
            }
        }
        player.openInventory(gui);
    }
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Faction faction = plugin.getFPlayerCache().getByPlayer(player).getFaction();
        event.setCancelled(true);
        PageGUI.playSound(event);
        ItemStack item = event.getCurrentItem();
        String itemName = item.getItemMeta().getDisplayName();

        // Object
        if (object == null) {
            ParsingUtil.sendMessage(player, FMessage.ERROR_NO_SUCH_FACTION.getMessage(), object);
            return;
        }
        // TO DO!
        if (object.isInWar()) {
            player.sendMessage("&cKriege gegen Fraktionen, die schon im Krieg sind, sind bis auf Weiteres deaktiviert!");
            return;
        }

        // Subject:
        WarParty subject = null;
        Set<Faction> factions = plugin.getFactionCache().getByLeader(player);
        if (factions.isEmpty()) {
            ParsingUtil.sendMessage(player, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }
        for (Faction f : factions) {
            if (f.getMembers().contains(player)) {
                subject = new WarParty(f, WarPartyRole.ATTACKER);
                break;
            }
        }
        if (subject == null) {
            ParsingUtil.sendMessage(player, FMessage.ERROR_JOIN_FACTION.getMessage());
            return;
        }
        for (Faction f : factions) {
            subject.addParticipant(f);
        }

        // Type:
        CasusBelli casus = null;
        if (itemName.equals(FMessage.WAR_CB_RAID.getMessage())) {
            casus = new CasusBelli(CasusBelli.Type.RAID, object, null);
        }
        if (itemName.equals(FMessage.WAR_CB_CONQUEST.getMessage())) {
            for (CasusBelli cb : faction.getCasusBelli()) {
                if (cb.getType() == CasusBelli.Type.CONQUEST && cb.getTarget() == object) {
                    casus = cb;
                    break;
                }
            }
        }
        if (itemName.equals(FMessage.WAR_CB_LIBERATION.getMessage())) {
            for (CasusBelli cb : faction.getCasusBelli()) {
                if (cb.getType() == CasusBelli.Type.LIBERATION && cb.getTarget() == object) {
                    casus = cb;
                    break;
                }
            }
        }
        if (itemName.equals(FMessage.WAR_CB_RESUBJAGATION.getMessage())) {
            for (CasusBelli cb : faction.getCasusBelli()) {
                if (cb.getType() == CasusBelli.Type.RESUBJAGATION && cb.getTarget() == object) {
                    casus = cb;
                    break;
                }
            }
        }
        if (itemName.equals(FMessage.WAR_CB_RECONQUEST.getMessage())) {
            for (CasusBelli cb : faction.getCasusBelli()) {
                if (cb.getType() == CasusBelli.Type.RECONQUEST && cb.getTarget() == object) {
                    casus = cb;
                    break;
                }
            }
        }
        player.closeInventory();
        if (casus == null) {
            MessageUtil.sendMessage(player, "&cInvalid CB. Please contact an Admin if you think this is an error.");
            return;
        }
        new CallToArmsMenu(subject, object, casus).open(player);
    }

    @Override
    public Inventory getInventory() {
        return gui;
    }
}