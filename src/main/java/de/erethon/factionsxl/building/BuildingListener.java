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

package de.erethon.factionsxl.building;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.player.FPlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BuildingListener implements Listener {

    FactionsXL plugin = FactionsXL.getInstance();
    FPlayerCache playerCache = plugin.getFPlayerCache();
    BuildingManager buildingManager;

    public BuildingListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        buildingManager = plugin.getBuildingManager();
    }

    @EventHandler
    public void interactEvent(PlayerInteractEvent event) {
        if (event.getItem() == null) {
            return;
        }
        if (!event.getItem().getType().equals(Material.CHEST)) {
            return;
        }
        Player player = event.getPlayer();
        FPlayer fPlayer = playerCache.getByPlayer(player);
        Faction faction = fPlayer.getFaction();
        String name = event.getItem().getItemMeta().getDisplayName();
        Building building = buildingManager.getByID(name);
        if (building == null) {
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = player.getTargetBlock(25);
            if (block == null) {
                return;
            }
            building.displayFrame(player, block.getLocation(), building.checkRequirements(player, faction, block.getLocation()));
            event.setCancelled(true);
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = player.getTargetBlock(25);
            if (block == null) {
                return;
            }
            Block buildingCenter = block.getRelative(0, 1, 0);
            if (building.checkRequirements(player, faction, buildingCenter.getLocation())) {
                Region rg = fPlayer.getLastRegion();
                building.build(player, faction, rg, buildingCenter.getLocation());
            } else {
                event.setCancelled(true);
            }
        }
    }





}
