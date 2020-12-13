/*
 * Copyright (C) 2017-2020 Daniel Saukel
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

package de.erethon.factionsxl.command;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.building.Building;
import de.erethon.factionsxl.building.BuildingManager;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BuildingCommand extends FCommand implements InventoryHolder, Listener {

    FactionsXL plugin = FactionsXL.getInstance();
    BuildingManager buildingManager = plugin.getBuildingManager();
    Inventory gui;

    public BuildingCommand() {
        setCommand("building");
        setAliases("build");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp("Fmessage");
        setPermission(FPermission.IDEA.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);

        gui = Bukkit.createInventory(this, 27, ChatColor.GOLD + "Select building.");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        Faction faction = getSenderFactionOrFromArg(sender, args, 1, true);
        if (faction == null) {
            return;
        }
        if (!faction.isPrivileged(sender)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }
        open(player);
    }

    public void open(Player p) {
        gui.clear();
        for (Building b : buildingManager.getBuildings()) {
            ItemStack i = new ItemStack(Material.STONE);
            ItemMeta im = i.getItemMeta();
            im.setDisplayName(ChatColor.RESET + b.getId());
            im.setLore(b.getDescription());
            i.setItemMeta(im);
            gui.addItem(i);
        }
        p.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        ItemStack building = new ItemStack(Material.CHEST);
        ItemMeta buildingMeta = building.getItemMeta();
        buildingMeta.setDisplayName(event.getCurrentItem().getItemMeta().getDisplayName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_PURPLE + "Build Site");
        buildingMeta.setLore(lore);
        building.setItemMeta(buildingMeta);
        player.getInventory().addItem(building);
    }

    @Override
    public Inventory getInventory() {
        return gui;
    }
}
