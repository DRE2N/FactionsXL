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

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.war.WarParty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnemySelectGUI implements InventoryHolder, Listener {

    FactionsXL plugin;
    Inventory gui;
    Set<WarParty> enemies = new HashSet<>();

    public EnemySelectGUI(FactionsXL plugin) {
        this.plugin = plugin;
        gui = Bukkit.createInventory(this, 54,"Kriegspartei auswählen:");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        FPlayer fPlayer = plugin.getFPlayerCache().getByPlayer(player);
        Faction ownFaction = fPlayer.getFaction();
        ownFaction.getWarParties().forEach(wp -> enemies.add(wp.getEnemy()));
        for (WarParty warParty : enemies) {
            ItemStack item = new ItemStack(Material.RED_BANNER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(warParty.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§a" + warParty.getEnemy().getName() + " §7vs. " + warParty.getName());
            lore.add("§7Kriegspunkte: §a" + warParty.getEnemy().getPoints() + "§8/§c" + warParty.getPoints());
            lore.add("§r");
            lore.add("§7Anklicken, um eine Liste der Fraktionen in");
            lore.add("§7dieser Kriegspartei zu sehen & einen Frieden");
            lore.add("§7zu schließen.");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.addItem(item);
        }
        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) {
            return;
        }
        FPlayer fPlayer = plugin.getFPlayerCache().getByPlayer((OfflinePlayer) event.getWhoClicked());
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) {
            return;
        }
        String name = event.getCurrentItem().getItemMeta().getDisplayName();
        WarParty selected = null;
        for (WarParty warParty : enemies) {
            if (warParty.getName().equals(name)) {
                selected = warParty;
            }
        }
        if (selected != null) {
            event.getWhoClicked().closeInventory();
            new WarDemandWarPartyGUI(selected).open(fPlayer.getPlayer());
        }

    }

    @Override
    public Inventory getInventory() {
        return gui;
    }
}
