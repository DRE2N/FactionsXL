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
package de.erethon.factionsxl.board;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.config.ConfigUtil;
import de.erethon.commons.misc.EnumUtil;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.economy.Resource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Daniel Saukel
 */
public enum RegionType {

    BARREN(2000, FMessage.REGION_BARREN),
    CITY(500000, FMessage.REGION_CITY),
    DESERT(2000, FMessage.REGION_DESERT),
    FARMLAND(20000, FMessage.REGION_FARMLAND),
    FOREST(10000, FMessage.REGION_FOREST),
    MAGIC(5000, FMessage.REGION_MAGIC),
    MOUNTAINOUS(4000, FMessage.REGION_MOUNTAINOUS),
    SEA(20000, FMessage.REGION_SEA),
    WARZONE(0, FMessage.REGION_WAR_ZONE);

    private int maxPopulation;
    private static List<Map<Resource, Integer>> resources = new ArrayList<>();
    private FMessage name;

    RegionType(int maxPopulation, FMessage name) {
        this.maxPopulation = maxPopulation;
        this.name = name;
    }

    /**
     * @return
     * the name of the type
     */
    public String getName() {
        return name.getMessage();
    }

    /**
     * @return
     * the maximum population
     */
    public int getMaxPopulation() {
        return maxPopulation;
    }

    /**
     * (maximum population / maximum level) * level
     *
     * @return
     * the maximum population
     */
    public int getMaxPopulation(int level) {
        return (maxPopulation / (resources.isEmpty() ? 1 : resources.size())) * level;
    }

    /**
     * @param level
     * the region level
     * @return
     * the resources
     */
    public Map<Resource, Integer> getResources(int level) {
        if (level > 0 && resources.size() > level) {
            MessageUtil.log("Got resources");
            return resources.get(level - 1);
        } else {
            MessageUtil.log("Empty resources");
            return new HashMap<>();
        }
    }

    /**
     * @param level
     * the level of the region
     * @return
     * a GUI with the income values
     */
    public Inventory getGUI(int level) {
        Inventory gui = Bukkit.createInventory(null, InventoryType.CHEST, ChatColor.GREEN + name.getMessage() + " - " + level);
        for (Map.Entry<Resource, Integer> resource : resources.get(level - 1).entrySet()) {
            ItemStack icon = resource.getKey().getIcon();
            icon.setAmount(resource.getValue());
            gui.addItem(icon);
        }
        return gui;
    }

    /**
     * Loads the resource prices
     */
    public static void loadResources(Map<String, Object> rs) {
        for (Entry<String, Object> entry : rs.entrySet()) {
            MessageUtil.log(entry.toString());
            if (!EnumUtil.isValidEnum(RegionType.class, entry.getKey()) || !(entry.getValue() instanceof ConfigurationSection)) {
                continue;
            }
            Map<?, ?> levelMap = (Map<?, ?>) ConfigUtil.getMap((ConfigurationSection) entry.getValue(), new String());
            for (Entry<?, ?> lEntry : levelMap.entrySet()) {
                MessageUtil.log(lEntry.toString());
                if (!(lEntry.getValue() instanceof ConfigurationSection)) {
                    continue;
                }
                Map<Resource, Integer> map = new HashMap<>();
                Map<?, ?> resourceMap = (Map<?, ?>) ConfigUtil.getMap((ConfigurationSection) lEntry.getValue(), new String());
                for (Entry<?, ?> rEntry : resourceMap.entrySet()) {
                    MessageUtil.log(rEntry.toString());
                    if (!(rEntry.getKey() instanceof String) || !EnumUtil.isValidEnum(Resource.class, (String) rEntry.getKey()) || !(rEntry.getValue() instanceof Integer)) {
                        continue;
                    }
                    map.put(Resource.valueOf((String) rEntry.getKey()), (Integer) rEntry.getValue());
                }
                valueOf(entry.getKey()).resources.add(map);
                MessageUtil.log("Added: " + map.toString());
                resources.add(map);
            }
        }
    }

}
