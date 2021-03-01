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
import de.erethon.commons.misc.ProgressBar;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.building.BuildSite;
import de.erethon.factionsxl.building.Building;
import de.erethon.factionsxl.building.effects.StatusEffectTools;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.legacygui.PageGUI;
import de.erethon.factionsxl.population.HappinessLevel;
import de.erethon.factionsxl.population.PopulationLevel;
import de.erethon.factionsxl.population.SaturationLevel;
import de.erethon.factionsxl.util.ParsingUtil;
import jdk.nashorn.internal.ir.IfNode;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author Daniel Saukel
 */
public class FStorage {

    private Faction faction;
    private Map<Resource, Integer> goods = new HashMap<>();
    private PageGUI gui;

    private final Random random = new Random();

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

        updatePopulation();

        // Consume
        /*for (Region region : faction.getRegions()) {
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

        // Population
        for (Region region : faction.getRegions()) {
            for (PopulationLevel level : PopulationLevel.values()) {
                if (region.getPopulation(level) == 0) {
                    continue;
                }
                region.getPopulationHappiness().put(level, HappinessLevel.HAPPY);
            }
        }*/

        // Trade
        double importModifier = FactionsXL.getInstance().getFConfig().getImportModifier();
        double exportModifier = FactionsXL.getInstance().getFConfig().getExportModifier();
        int importLimit = StatusEffectTools.getTotalImportLimit(faction);
        int exportLimit = StatusEffectTools.getTotalExportLimit(faction);
        int currentImport = 0;
        boolean importFailed = false;
        int currentExport = 0;
        boolean exportFailed = false;
        HashMap<Resource, Integer> importActions = new HashMap<>();
        for (Entry<Resource, Integer> entry : faction.getGroceryList().entrySet()) {
            Resource resource = entry.getKey();
            int amount = entry.getValue();
            if (amount > 0) {
                currentImport = currentImport + amount;
                if (currentImport <= importLimit) {
                    importActions.put(resource, amount);
                } else {
                    importFailed = true;
                }
            } else if (amount < 0) {
                currentExport = currentExport + Math.abs(amount);
                if (currentExport <= exportLimit) {
                    faction.chargeResource(resource, -1 * amount);
                    faction.getAccount().deposit(-1 * amount * resource.getValue() * exportModifier);
                } else {
                    exportFailed = true;
                }
            }
        }
        if (exportFailed) {
            // TODO: FMessage
            faction.sendMessage("Export limit");
        }
        if (importFailed) {
            // TODO: FMessage
            faction.sendMessage("Import limit");
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

    // TODO: Might be possible to do this async, as there is no involvement with the server thread
    public void updatePopulation() {
        // Add all (finished & working!) buildings
        List<Building> factionBuildings = new ArrayList<>();
        for (BuildSite site : faction.getFactionBuildings()) {
            if (site.isFinished() && !site.isDestroyed()) {
                factionBuildings.add(site.getBuilding());
            }
        }

        // Update population for every region
        for (Region rg : faction.getRegions()) {
            Set<String> tooMany = new HashSet<>();
            Set<String> tooFew = new HashSet<>();
            rg.updateSaturatedSubcategories();
            Set<PopulationLevel> populationLevels = new HashSet<>();
            Map<PopulationLevel, Integer> uprankReady = new HashMap<>();
            Map<PopulationLevel, Integer> downrankReady = new HashMap<>();
            // Only update the levels we need
            for (PopulationLevel level : PopulationLevel.values()) {
                if (faction.getPopulation(level) > 0) {
                    populationLevels.add(level);
                }
            }
            for (PopulationLevel level : populationLevels) {
                //
                // Consumption & Saturation

                //
                MessageUtil.log("Now checking " + level.toString() + " in " + rg.getName());
                if (rg.getPopulation(level) == 0) {
                    continue;
                }
                Set<String> tooFewLevel = new HashSet<>();
                Set<String> tooManyLevel = new HashSet<>();
                for (Resource resource : Resource.values()) {
                    int saturation = rg.getSaturatedResources().get(resource);
                    int demand = rg.getDemand(resource, level);
                    int max = demand != 0 ? SaturationLevel.getByPercentage(saturation / demand * 100).getMinPercentage() : 100;
                    int daily = FactionsXL.getInstance().getFConfig().getSaturationPerDay();
                    int consume = rg.getConsumableResources().get(resource);
                    if (demand > consume && consume != 0) {
                        tooFewLevel.add(ChatColor.GOLD + resource.getName());
                    } else if (demand < consume && consume != 0) {
                        tooManyLevel.add(ChatColor.GOLD + resource.getName());
                    }
                    double change = daily;
                    if (consume != 0 && demand != 0) {
                        change = consume >= daily ? daily : -10 * (double) consume / demand;
                    } else if (consume == 0 && demand != 0) {
                        change = -1 * daily;
                    }
                    MessageUtil.log("Consuming " + consume + " of " + resource.getName() + " in " + rg + " by " + level.toString());
                    if (faction.chargeResource(resource, consume)) {
                        int newSaturation = Math.min((saturation + (int) change), max);
                        if (newSaturation < 0) {
                            newSaturation = 0;
                        }
                        rg.getSaturatedResources().put(resource, newSaturation);
                    } else {
                        rg.getSaturatedResources().put(resource, Math.max(saturation - daily, 0));
                    }
                }
                tooFew.add(ChatColor.RED + level.toString() + ": " + ParsingUtil.collectionToString(tooFewLevel));
                tooMany.add(ChatColor.GOLD + level.toString() + ": " + ParsingUtil.collectionToString(tooManyLevel));

                //
                // Be happy! (or not)
                //
                HappinessLevel current = rg.getPopulationHappiness().get(level);
                if (current == null) {
                    current = HappinessLevel.CONTENT;
                }
                // The level above this one
                HappinessLevel better = current.getAbove();
                // The level below this one
                HappinessLevel worse = current.getBelow();

                // Add all (finished and working!) buildings from this region
                List<Building> regionBuildings = new ArrayList<>();
                for (BuildSite site : rg.getBuildings()) {
                    if (site.isFinished() && !site.isDestroyed()) {
                        regionBuildings.add(site.getBuilding());
                    }
                }
                // Checks if all region buildings are present
                boolean regionBuildingsArePresent = true;
                if (!level.getRequiredRegionBuildings().isEmpty()) {
                    regionBuildingsArePresent = regionBuildings.containsAll(level.getRequiredRegionBuildings());
                }
                // Checks if all faction buildings needed are present
                boolean factionBuildingsArePresent = true;
                if (!level.getRequiredFactionBuildings().isEmpty()) {
                    factionBuildingsArePresent = factionBuildings.containsAll(level.getRequiredFactionBuildings());
                }

                double totalDiversity = 1.0;
                HashMap<ResourceSubcategory, SaturationLevel> categorySatLevels = new HashMap<>();
                for (ResourceSubcategory subcategory : ResourceSubcategory.values()) {
                    HashMap<SaturationLevel, Integer> saturation = new HashMap<>();
                    int percentage = 0;
                    for (Resource resource : subcategory.getResources()) {
                        SaturationLevel satLevel = rg.isResourceSaturated(resource, subcategory.isBasic());
                        saturation.put(satLevel, (saturation.get(satLevel) != null ? saturation.get(satLevel) : 0) + 1);
                        percentage += rg.getSaturatedResources().get(resource);
                    }
                    percentage = percentage / subcategory.getResources().length;
                    if (percentage != 0.00) {
                        totalDiversity = totalDiversity * percentage;
                    }
                    SaturationLevel catSatlevel = SaturationLevel.getByPercentage(percentage, subcategory.isBasic());
                    categorySatLevels.put(subcategory, catSatlevel);
                }

                // Basic resources
                boolean basicsFulfilled = true;
                for (ResourceSubcategory subcategory : categorySatLevels.keySet()) {
                    if (subcategory.isBasic() && categorySatLevels.get(subcategory) == SaturationLevel.NOT_SATURATED_BASIC) {
                        basicsFulfilled = false;
                    }
                }
                // Checks if resource diversity is good enough
                boolean diversityIsGood = totalDiversity > 0.75;
                // Check if all subcats are fulfilled
                int fulfilledSubs = 0;
                for (ResourceSubcategory subcategory : level.getConsumption().keySet()) {
                    if (categorySatLevels.get(subcategory) == SaturationLevel.FULLY_SATURATED || categorySatLevels.get(subcategory) == SaturationLevel.SATURATED) {
                        fulfilledSubs++;
                    }
                }
                // Check if needed subcategories are in storage
                int fulfilledCats = 0;
                for (Entry<ResourceSubcategory, Integer> needed : level.getRequiredResourcesForLevelUp().entrySet()) {
                    boolean fulfilled = false;
                    for (Resource resource : needed.getKey().getResources()) {
                        if (getGoods().get(resource) >= needed.getValue()) {
                            fulfilled = true;
                            break;
                        }
                    }
                    if (fulfilled) {
                        fulfilledCats++;
                    }
                }

                // For stuff like "roads" that does not get consumed, but needs to be in storage
                boolean fulfilledCatsStorage = (fulfilledCats >= level.getRequiredResourcesForLevelUp().keySet().size());
                // Check if all specific subcategories are fulfilled.
                boolean allSubcategoriesFulfilled = (fulfilledCats >= level.getConsumption().keySet().size());


                MessageUtil.log("Updating happiness in " + rg.getName() + " for " + level.toString());
                MessageUtil.log("Better: " + better.toString() + " Worse: " + worse.toString() + " Current: " + current.toString());
                MessageUtil.log("Region buildings: " + regionBuildingsArePresent + " (" + regionBuildings.size() + ") Faction buildings: " + factionBuildingsArePresent + " (" + factionBuildings.size() + ") Basics: " + basicsFulfilled);
                MessageUtil.log("Overall resource diversity: " + diversityIsGood + " (" + totalDiversity);
                MessageUtil.log("Fulfilled Subcategories: " + allSubcategoriesFulfilled + " (" + fulfilledCats + "). Fulfilled storage contents: " + fulfilledCatsStorage + " (" + fulfilledCats + ")");

                // Requirements to increase happiness obviously need to depend on the current happiness level.
                // People who are angry because they are hungry don't need resource diversity or roads to be happier.
                switch (current) {
                    case ANGRY:
                        // Im happy if my basic needs are met
                        if (basicsFulfilled) {
                            setHappiness(rg, level, better);
                        }
                        // Can't get worse from here :(
                        break;
                    case UNHAPPY:
                        // A few buildings in my region would be nice, right?
                        if (basicsFulfilled && regionBuildingsArePresent) {
                            setHappiness(rg, level, better);
                        }
                        // I am hungry! I am cold!
                        if (!basicsFulfilled) {
                            setHappiness(rg, level, worse);
                        }
                        break;
                    case CONTENT:
                        // Faction buildings are nice as well. And having all my needs met is certainly a nice bonus.
                        if (basicsFulfilled && regionBuildingsArePresent && factionBuildingsArePresent) {
                            setHappiness(rg, level, better);
                        }
                        // Where is my food?! Why is our regional church destroyed?
                        if (!basicsFulfilled || !regionBuildingsArePresent) {
                            setHappiness(rg, level, worse);
                        }
                        break;
                    case HAPPY:
                        // We got stone roads, we got food, we got nice buildings. I feel euphoric!
                        if (basicsFulfilled && regionBuildingsArePresent && factionBuildingsArePresent && fulfilledCatsStorage) {
                            setHappiness(rg, level, better);
                        }
                        // Where is my food? Where is my furniture?
                        if (!basicsFulfilled || !regionBuildingsArePresent || !factionBuildingsArePresent ) {
                            setHappiness(rg, level, worse);
                        }
                        break;
                    case EUPHORIC:
                        // Food diversity would be nice, right?
                        if (basicsFulfilled && diversityIsGood && allSubcategoriesFulfilled && fulfilledCatsStorage && regionBuildingsArePresent && factionBuildingsArePresent) {
                            setHappiness(rg, level, better);
                        }
                        // Oh no, I really don't want to eat the same bread everyday!
                        if (!diversityIsGood || !allSubcategoriesFulfilled || !fulfilledCatsStorage || !regionBuildingsArePresent || !factionBuildingsArePresent || !basicsFulfilled) {
                            setHappiness(rg, level, worse);
                        }
                        break;
                }


                //
                // Add to Up- or downrank
                //
                if ((rg.getPopulationHappiness().get(level) == HappinessLevel.HAPPY) &&!rg.getUprankBlocked().contains(level)) {
                    int amount = randomNumberInRange(5, 30);
                    uprankReady.put(level, randomNumberInRange(5, 30)); // Level up a few people if they are happy
                    MessageUtil.log("Upranking " + amount + " happy " + level);
                }
                if ((rg.getPopulationHappiness().get(level) == HappinessLevel.EUPHORIC) &&!rg.getUprankBlocked().contains(level)) {
                    int amount = randomNumberInRange(20, 120);
                    uprankReady.put(level, randomNumberInRange(20, 120)); // Level up more if they are euphoric
                    MessageUtil.log("Upranking " + amount + " euphoric " + level);
                }
                if (rg.getPopulationHappiness().get(level) == HappinessLevel.UNHAPPY) {
                    int amount = randomNumberInRange(10, 40);
                    downrankReady.put(level, randomNumberInRange(10, 40)); // A few people should downrank if they are unhappy
                    MessageUtil.log("Downranking " + amount + " unhappy " + level);
                }
                if (rg.getPopulationHappiness().get(level) == HappinessLevel.ANGRY) {
                    int amount = randomNumberInRange(50, 200);
                    downrankReady.put(level, randomNumberInRange(50, 200)); // More people should downrank if they are _really_ angry
                    MessageUtil.log("Removing " + amount + " angry " + level);
                }
            }
            if (!tooMany.isEmpty()) {
                faction.sendMessage(FMessage.POPULATION_WARNING_TOO_MANY_RESOURCES_GRANTED.getMessage(rg.getName()));
                faction.sendMessage(ParsingUtil.collectionToString(tooMany, ChatColor.DARK_RED));
            }
            if (!tooFew.isEmpty()) {
                faction.sendMessage(FMessage.POPULATION_WARNING_NOT_ENOUGH_RESOURCES_GRANTED.getMessage(rg.getName()));
                faction.sendMessage(ParsingUtil.collectionToString(tooFew, ChatColor.DARK_RED));
            }

            //
            // Up- or downrank the population. Do this later to prevent multiple upranks from happening to the same pop.
            // Move population up one level
            for (Entry<PopulationLevel, Integer> upLevel : uprankReady.entrySet()) {
                PopulationLevel current = upLevel.getKey();
                PopulationLevel above = current.getAbove();
                rg.getPopulation().put(current, rg.getPopulation(current) - upLevel.getValue());
                rg.getPopulation().put(above, rg.getPopulation(above) + upLevel.getValue());
            }
            // Move population down one level
            for (Entry<PopulationLevel, Integer> downLevel : downrankReady.entrySet()) {
                PopulationLevel current = downLevel.getKey();
                PopulationLevel below = current.getBelow();
                rg.getPopulation().put(current, rg.getPopulation(current) - downLevel.getValue());
                rg.getPopulation().put(below, rg.getPopulation(below) + downLevel.getValue());
            }
        }
    }

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

    private void setHappiness(Region rg, PopulationLevel level, HappinessLevel lvl) {
        rg.getPopulationHappiness().put(level, lvl);
        MessageUtil.log("The " + level + " in " + rg.getName() + " are now " + lvl);
    }

    private int randomNumberInRange(int min, int max) {
        int spread = max - min;
        return random.nextInt(spread + 1) + min;
    }


}
