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
package de.erethon.factionsxl.war.demand;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.gui.WarPartyGUI;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.player.FPlayerCache;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.WarCache;
import de.erethon.factionsxl.war.WarParty;
import de.erethon.factionsxl.war.peaceoffer.FinalPeaceOffer;
import de.erethon.factionsxl.war.peaceoffer.SeparatePeaceOffer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Daniel Saukel
 */
public class WarDemandWarPartyGUI implements InventoryHolder, Listener {

    private final FPlayerCache fPlayers;
    FactionsXL plugin = FactionsXL.getInstance();
    Inventory gui;
    WarParty enemy;
    private final WarCache wars;

    public WarDemandWarPartyGUI(WarParty warParty) {
        enemy = warParty;
        gui = Bukkit.createInventory(this, 54, "Fraktion auswählen:");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        fPlayers = plugin.getFPlayerCache();
        wars = plugin.getWarCache();
    }

    public void open(Player player) {
        for (Faction faction : enemy.getFactions()) {
            ItemStack banner = faction.getBannerStack();
            ItemMeta meta = banner.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("§6§nRechtsklick§r§7: §r§cForderung §7erstellen");
            lore.add("§7Ihr erhaltet etwas von " + faction.getName());
            lore.add("§7§oFoderungen mit über 50 Kriegspunkten können nicht abgelehnt werden.");
            lore.add("§7§oForderungen mit weniger Kriegspunkten sorgen bei Ablehnung für Kriegsermüdung.");
            lore.add("§6§nLinksklick§r§7: §r§aAngebot §7erstellen");
            lore.add("§7Ihr gebt etwas an " + faction.getName() + "§7 ab.");
            lore.add("§7§oEin Angebot ist freiwillig, hat dafür aber keine Konsequenzen.");
            if (enemy.getLeader() == faction) {
                lore.add("§r");
                lore.add("§9§oDiese Fraktion ist Anführer ihrer");
                lore.add("§9§oKriegspartei. Ein Frieden §9§lbeendet §r§9den Krieg.");
            } else {
                lore.add("§r");
                lore.add("§9§oDies ist ein Separatfrieden. Ihr scheidet");
                lore.add("§9§onur aus dem Krieg aus.");
            }
            meta.setLore(lore);
            meta.setDisplayName(faction.getName());
            banner.setItemMeta(meta);
            gui.addItem(banner);
        }
        player.openInventory(gui);
    }

    @EventHandler
    public void onButtonClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        boolean right = event.isRightClick();
        boolean left = event.isLeftClick();
        String name = event.getCurrentItem().getItemMeta().getDisplayName();
        Faction faction = plugin.getFactionCache().getByName(name);
        Player whoClicked = (Player) event.getWhoClicked();
        WarParty offerTarget = null;
        if (enemy.getLeader() == faction) {
            offerTarget = enemy;
        }
        if (offerTarget != null) {
            War war = offerTarget.getWar();
            WarParty demanding = war.getEnemy(offerTarget);
            if (right) {
                fPlayers.getByPlayer(whoClicked).setPeaceOffer(new FinalPeaceOffer(enemy.getWar(), demanding, offerTarget));
                MessageUtil.sendMessage(whoClicked, FMessage.WAR_DEMAND_CREATION_MENU_MAKE_DEMANDS.getMessage());
                wars.getWarDemandCreationMenu().open(whoClicked, faction, false);
            }
            if (left) {
                fPlayers.getByPlayer(whoClicked).setPeaceOffer(new FinalPeaceOffer(enemy.getWar(), true, demanding, offerTarget));
                MessageUtil.sendMessage(whoClicked, FMessage.WAR_DEMAND_CREATION_MENU_MAKE_OFFER.getMessage());
                wars.getWarDemandCreationMenu().open(whoClicked, faction, true);
            }
        } else {
            if (right) {
                fPlayers.getByPlayer(whoClicked).setPeaceOffer(new SeparatePeaceOffer(enemy.getWar(), fPlayers.getByPlayer(whoClicked).getFaction() , faction, false));
                MessageUtil.sendMessage(whoClicked, FMessage.WAR_DEMAND_CREATION_MENU_MAKE_DEMANDS.getMessage());
                wars.getWarDemandCreationMenu().open(whoClicked, faction, false);
            }
            if (left) {
                fPlayers.getByPlayer(whoClicked).setPeaceOffer(new SeparatePeaceOffer(enemy.getWar(), fPlayers.getByPlayer(whoClicked).getFaction(), faction, true));
                MessageUtil.sendMessage(whoClicked, FMessage.WAR_DEMAND_CREATION_MENU_MAKE_OFFER.getMessage());
                wars.getWarDemandCreationMenu().open(whoClicked, faction, true);
            }
        }
        HandlerList.unregisterAll(this);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

}
