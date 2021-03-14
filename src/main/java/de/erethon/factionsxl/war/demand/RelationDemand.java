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
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.entity.RelationRequest;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.war.WarParty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Saukel
 */
public class RelationDemand implements WarDemand, Listener, InventoryHolder {

    Inventory gui;
    Faction lord;
    Faction vassal;
    Player player;

    FactionsXL plugin = FactionsXL.getInstance();
    FConfig config = plugin.getFConfig();

    public RelationDemand(Faction l, Faction v) {
        this.lord = l;
        this.vassal = v;
    }

    public RelationDemand(Map<String, Object> args) {
        this.lord = (Faction) args.get("lord");
        this.vassal = (Faction) args.get("vassal");
    }

    public RelationDemand() {
    }

    @Override
    public double getWarscoreCost() {
        return 50;
    }

    @Override
    public void pay(WarParty wp, WarParty wp2) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                new RelationRequest(Bukkit.getConsoleSender(), (Faction) wp.getLeader(), (Faction) wp2.getLeader(), Relation.VASSAL).confirm();
                vassal.setAllod(true);
            }
        }, 5);
        // TODO: Might break after government update
    }

    @Override
    public void pay(Faction f, Faction f2) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                new RelationRequest(Bukkit.getConsoleSender(), f2, f, Relation.VASSAL).confirm();
                vassal.setAllod(true);
            }
        }, 5);
    }

    @Override
    public boolean canPay(WarParty wp) {
        return true;
    }

    @Override
    public boolean canPay(Faction f) {
        return true;
    }

    @Override
    public void demand() {

    }

    public void openSetupGUI(Player p, Faction v) {
        gui = Bukkit.createInventory(this, 54, "§eBeziehung");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        FPlayer fp =  plugin.getFPlayerCache().getByPlayer(p);
        Faction faction = fp.getFaction();
        player = p;
        if (fp.getPeaceOffer().isOffer()) {
            v = faction;
        }
        vassal = v;
        ItemStack guiItem = new ItemStack(Material.YELLOW_CONCRETE);
        ItemMeta guiMeta = guiItem.getItemMeta();
        guiMeta.setDisplayName(FMessage.RELATION_VASSAL.getMessage());
        guiItem.setItemMeta(guiMeta);
        gui.addItem(guiItem);
        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        Faction faction = plugin.getFPlayerCache().getByPlayer(player).getFaction();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getItemMeta() == null) {
            return;
        }

        if (item.getItemMeta().getDisplayName().contains(FMessage.RELATION_VASSAL.getMessage())) {
            WarDemand war = (WarDemand) new RelationDemand(faction, vassal);
            FactionsXL.getInstance().getFPlayerCache().getByPlayer(player).getPeaceOffer().getDemands().add(war);
            FactionsXL.getInstance().getWarCache().getWarDemandCreationMenu().open(player);
            FactionsXL.getInstance().getFPlayerCache().getByPlayer(player).listWarDemands();
            return;
        }
    }

    @Override
    public boolean canAffordWP(WarParty wp) {
        return wp.getPoints() >= getWarscoreCost();
    }

    @Override
    public boolean canAffordWP(Faction f) {
        return false;
    }

    @Override
    public String toString() {
        return "&6Vasall " + "&8(&7Warscore&8: &5" + getWarscoreCost() + "&8)";
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> args = new HashMap<>();
        args.put("lord", lord);
        args.put("vassal", vassal);
        return args;
    }

    @Override
    public Inventory getInventory() {
        return gui;
    }
}
