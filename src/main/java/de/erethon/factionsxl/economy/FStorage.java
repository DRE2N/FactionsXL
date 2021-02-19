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

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.legacygui.PageGUI;
import de.erethon.factionsxl.population.PopulationLevel;
import de.erethon.factionsxl.population.SaturationLevel;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Daniel Saukel
 */
public class FStorage {

    private Faction faction;
    private Map<Resource, Integer> goods = new HashMap<>();
    private PageGUI gui;

    public FStorage(Faction faction) {
        this.faction = faction;
        for (Resource resource : Resource.values()) {
            if (!goods.containsKey(resource) && resource.isPhysical()) {
                goods.put(resource, 0);
            }
        }
        update();
    }

    public FStorage(Faction faction, Map<String, Object> storageMap) {
        this.faction = faction;
        for (Entry<String, Object> entry : storageMap.entrySet()) {
            goods.put(Resource.valueOf(entry.getKey()), (int) entry.getValue());
        }
        for (Resource resource : Resource.values()) {
            if (!goods.containsKey(resource) && resource.isPhysical()) {
                goods.put(resource, 0);
            }
        }
        update();
    }

    /* Getters and setters */
    /**
     * @return
     * the goods in this storage
     */
    public Map<Resource, Integer> getGoods() {
        return goods;
    }

    /* Actions */
    public void payday() {
        // Region income
        for (Region region : faction.getRegions()) {
            MessageUtil.log(region.getName());
            for (Entry<Resource, Integer> entry : region.getResources().entrySet()) {
                MessageUtil.log(entry.toString());
                if (entry.getKey() == Resource.TAXES) {
                    faction.getAccount().deposit(entry.getValue());
                } else if (entry.getKey() == Resource.MANPOWER) {
                    int newPop = (int) Math.round(region.getTotalPopulation() + entry.getValue() * ((double) faction.getStability() / 100));
                    int maxPop = region.getType().getMaxPopulation(region.getLevel());
                    if (newPop > maxPop) {
                        newPop = maxPop;
                    }
                    if (newPop < 0) {
                        newPop = 0;
                    }
                    //region.setPopulation(newPop);
                } else {
                    MessageUtil.log("Income: " + entry.getValue() + ": " + entry.getKey());
                    goods.put(entry.getKey(), goods.get(entry.getKey()) + entry.getValue());
                }
            }
            double inf = FactionsXL.getInstance().getFConfig().getInfluencePerDay();
            if (faction.getStability() >= 30 && !faction.isInWar()) {
                // Increase influence up to 100 if core
                if (region.getCoreFactions().containsKey(faction) && region.getInfluence() + inf <= 100) {
                    region.setInfluence(region.getInfluence() + (int) inf);
                }
                // Increase influence up to 50 if not core
                else if (!(region.getCoreFactions().containsKey(faction)) && (region.getInfluence() + inf <= 50) && !faction.isInWar()) {
                    region.setInfluence(region.getInfluence() + (int) inf);
                }
            }
        }

        // Consume
        for (Region region : faction.getRegions()) {
            Set<String> tooMany = new HashSet<>();
            Set<String> tooFew = new HashSet<>();
            region.updateSaturatedSubcategories();
            for (PopulationLevel level : PopulationLevel.values()) {
                if (region.getPopulation(level) == 0) {
                    continue;
                }
                for (Resource resource : Resource.values()) {
                    int saturation = region.getSaturatedResources().get(resource);
                    int demand = region.getDemand(resource, level);
                    int max = demand != 0 ? SaturationLevel.getByPercentage(saturation / demand * 100).getMinPercentage() : 100;
                    int daily = FactionsXL.getInstance().getFConfig().getSaturationPerDay();
                    int consume = region.getConsumableResources().get(resource);
                    if (demand > consume) {
                        tooFew.add(ChatColor.GOLD + resource.getName());
                    } else if (demand < consume) {
                        tooMany.add(ChatColor.GOLD + resource.getName());
                    }
                    double change = daily;
                    if (consume != 0 && demand != 0) {
                        change = consume >= daily ? daily : -10 * (double) consume / demand;
                    } else if (consume == 0 && demand != 0) {
                        change = -1 * daily;
                    }
                    MessageUtil.log("Consuming " + consume + " of " + resource.getName() + " in " + region + " by " + level.toString());
                    if (faction.chargeResource(resource, consume)) {
                        int newSaturation = Math.min((saturation + (int) change), max);
                        if (newSaturation < 0) {
                            newSaturation = 0;
                        }
                        region.getSaturatedResources().put(resource, newSaturation);
                    } else {
                        region.getSaturatedResources().put(resource, Math.max(saturation - daily, 0));
                    }
                    if (!tooMany.isEmpty()) {
                        faction.sendMessage(FMessage.POPULATION_WARNING_TOO_MANY_RESOURCES_GRANTED.getMessage(level.toString(), region.getName()));
                        faction.sendMessage(ParsingUtil.collectionToString(tooMany, ChatColor.DARK_RED));
                    }
                    if (!tooFew.isEmpty()) {
                        faction.sendMessage(FMessage.POPULATION_WARNING_NOT_ENOUGH_RESOURCES_GRANTED.getMessage(region.getName()));
                        faction.sendMessage(ParsingUtil.collectionToString(tooFew, ChatColor.DARK_RED));
                    }
                }
            }
        }

        // Trade
        double importModifier = FactionsXL.getInstance().getFConfig().getImportModifier();
        double exportModifier = FactionsXL.getInstance().getFConfig().getExportModifier();
        HashMap<Resource, Integer> importActions = new HashMap<>();
        for (Entry<Resource, Integer> entry : faction.getGroceryList().entrySet()) {
            Resource resource = entry.getKey();
            int amount = entry.getValue();
            if (amount > 0) {
                importActions.put(resource, amount);
            } else if (amount < 0) {
                faction.chargeResource(resource, -1 * amount);
                faction.getAccount().deposit(-1 * amount * resource.getValue() * exportModifier);
            }
        }
        // Perform import actions later so that they don't fail if the money generated from exports makes them possible
        for (Entry<Resource, Integer> entry : importActions.entrySet()) {
            faction.chargeMoneyForResource(entry.getKey(), entry.getValue(), importModifier);
        }

        for (Player player : faction.getOnlineMembers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }
        faction.sendMessage(FMessage.STORAGE_PAYDAY.getMessage());
    }

    public boolean canAfford(Resource resource, int amount) {
        return getGoods().get(resource) >= amount;
    }

    public Map<String, Integer> serialize() {
        Map<String, Integer> serialized = new HashMap<>();
        for (Entry<Resource, Integer> entry : goods.entrySet()) {
            serialized.put(entry.getKey().toString(), entry.getValue());
        }
        return serialized;
    }

    /*public void updatePopulation() {
        for (Region rg : faction.getRegions()) {
            Set<PopulationLevel> populationLevels = new HashSet<>();
            // Only update the levels we need
            for (PopulationLevel level : PopulationLevel.values()) {
                if (faction.getPopulation(level) > 0) {
                    populationLevels.add(level);
                }
            }
            FStorage storage = faction.getStorage();
            for (PopulationLevel level : populationLevels) {
                for (ResourceSubcategory subcategory : level.getConsumption().keySet()) {
                    List<Resource> availableSubcategoryResources = new ArrayList<>();
                    for (Resource factionresource : storage.getGoods().keySet()) {
                        if (storage.getGoods().get(factionresource) > 1)
                    }
                    for (Resource resource : subcategory.getResources()) {
                        faction.getStorage().goods.put(resource, faction.getStorage().goods.get(resource) - )
                    }
                }
            }
        }
    }*/

    public void update() {
        gui = new PageGUI(FMessage.STORAGE_TITLE.getMessage(faction.getName()));
        for (Resource resource : Resource.values()) {
            ItemStack icon = resource.getIcon();
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + resource.getName());
            if (resource.isPhysical()) {
                meta.setLore(Arrays.asList(FMessage.STORAGE_STOCK.getMessage(String.valueOf(goods.get(resource)))));
            } else {
                List<String> lore = new ArrayList<>(Arrays.asList(FMessage.STORAGE_NON_PHYSICAL_WARNING.getMessage(resource.getName())));
                if (resource == Resource.MANPOWER) {
                    lore.add(FMessage.STORAGE_NON_PHYSICAL_MANPOWER.getMessage());
                } else if (resource == Resource.TAXES) {
                    lore.add(FMessage.STORAGE_NON_PHYSICAL_TAXES.getMessage());
                }
                meta.setLore(lore);
            }
            icon.setItemMeta(meta);
            gui.addButton(icon);
        }
    }

    public void open(HumanEntity player) {
        update();
        gui.open(player);
    }

}
