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

package de.erethon.factionsxl.building;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Board;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.economy.FStorage;
import de.erethon.factionsxl.economy.Resource;
import de.erethon.factionsxl.economy.StatusEffect;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.player.FPlayerCache;
import de.erethon.factionsxl.population.PopulationLevel;
import de.erethon.factionsxl.util.LazyChunk;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Can be constructed in a region to give bonuses to the region or the faction.
 *
 * @author Malfrador
 */

public class Building {

    FactionsXL plugin = FactionsXL.getInstance();

    public static final String YAML = ".yml";

    private File file;
    private FileConfiguration config;

    private String id;
    private String name;
    private List<String> description;
    private boolean isCoreRequired;
    private boolean isCapitalRequired;
    private boolean isFactionBuilding;
    private int size;
    private Map<Resource, Integer> unlockCost = new HashMap<>();
    private Map<Material, Integer> requiredBlocks = new HashMap<>();
    private Map<PopulationLevel, Integer> requiredPopulation = new HashMap<>();
    private List<String> requiredBuildings = new ArrayList<>(); // String with ids because the other buildings might not be loaded yet.
    private Set<StatusEffect> effects = new HashSet<>();


    public Building(File file) {
        this.file = file;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException exception) {
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        id = file.getName().replace(YAML, "");
        load();
    }


    public void build(Player p, Faction faction, Region rg, Location center) {
        pay(faction);
        new BuildSite(this, rg, getCorner1(center), getCorner2(center), center);
        MessageUtil.sendMessage(p, FMessage.BUILDING_SITE_CREATED.getMessage());
    }

    public boolean checkRequirements(Player p, Faction faction, Location loc) {
        FPlayerCache playerCache = plugin.getFPlayerCache();
        Board board = plugin.getBoard();
        FPlayer fPlayer = playerCache.getByPlayer(p);
        if (faction == null) {
            MessageUtil.sendMessage(p, FMessage.ERROR_JOIN_FACTION.getMessage());
            return false;
        }
        if (!faction.isPrivileged(fPlayer)) {
            ParsingUtil.sendMessage(p, FMessage.ERROR_NO_PERMISSION.getMessage());
            return false;
        }
        Region rg = fPlayer.getLastRegion();
        if (rg == null) {
            MessageUtil.sendMessage(p, FMessage.ERROR_LAND_WILDERNESS.getMessage());
            return false;
        }
        if (rg.getOwner() != faction) {
            MessageUtil.sendMessage(p, FMessage.ERROR_LAND_NOT_OWNED.getMessage());
            return false;
        }
        boolean isBorder = false;
        LazyChunk chunk = new LazyChunk(p.getChunk());
        for (Chunk c : chunk.getFastChunksAround(p.getWorld())) {
            if (board.getByChunk(c, rg) != rg) {
                isBorder = true;
            }
        }
        if (isBorder) {
            MessageUtil.sendMessage(p, FMessage.ERROR_BUILDING_TOO_CLOSE_BORDER.getMessage());
            return false;
        }
        if (isCoreRequired() && !rg.getCoreFactions().containsKey(faction)) {
            MessageUtil.sendMessage(p, FMessage.ERROR_LAND_NO_CORE.getMessage());
            return false;
        }
        if (isCapitalRequired() && faction.getCapital() != rg) {
            MessageUtil.sendMessage(p, FMessage.ERROR_LAND_NO_CAPITAL.getMessage());
            return false;
        }
        if (isFactionBuilding() && !hasRequiredBuilding(faction)) {
            MessageUtil.sendMessage(p, FMessage.ERROR_BUILDING_REQUIRED_FACTION.getMessage());
            return false;
        }
        if (!isFactionBuilding() && !hasRequiredBuilding(rg)) {
            MessageUtil.sendMessage(p, FMessage.ERROR_BUILDING_REQUIRED_REGION.getMessage());
            return false;
        }
        if (!isFactionBuilding() && !hasRequiredPopulation(rg)) {
            MessageUtil.sendMessage(p, FMessage.ERROR_BUILDING_POPULATION_TOO_LOW.getMessage());
            return false;
        }
        if (faction.isInWar()) {
            MessageUtil.sendMessage(p, FMessage.ERROR_IN_WAR.getMessage());
            return false;
        }
        if (!canPay(faction)) {
            MessageUtil.sendMessage(p, FMessage.ERROR_NOT_ENOUGH_RESOURCES.getMessage());
            return false;
        }
        return true;
    }

    public boolean canPay(Faction f) {
        FStorage storage = f.getStorage();
        boolean canPay = true;
        for (Map.Entry<Resource, Integer> resource : getUnlockCost().entrySet()) {
            if (!storage.canAfford(resource.getKey(), resource.getValue())) {
                canPay = false;
            }
        }
        return canPay;
    }

    public void pay(Faction faction) {
        FStorage storage = faction.getStorage();
        for (Map.Entry<Resource, Integer> resource : getUnlockCost().entrySet()) {
            int current = storage.getGoods().get(resource.getKey());
            storage.getGoods().put(resource.getKey(), current - resource.getValue());
        }
    }

    public boolean hasRequiredBuilding(Faction f) {
        BuildingManager buildingManager = plugin.getBuildingManager();
        Set<Building> buildings = new HashSet<>();
        if (getRequiredBuildings() == null || getRequiredBuildings().isEmpty()) {
            return true;
        }
        if (f.getFactionBuildings() == null || f.getFactionBuildings().isEmpty()) {
            return false;
        }
        for (BuildSite bs : f.getFactionBuildings()) {
            if (!bs.isFinished()) {
                continue;
            }
            buildings.add(bs.getBuilding());
        }
        Set<Building> required = new HashSet<>();
        for (String s : requiredBuildings) {
            required.add(buildingManager.getByID(s));
        }
        return buildings.containsAll(required);
    }

    public boolean hasRequiredBuilding(Region rg) {
        BuildingManager buildingManager = plugin.getBuildingManager();
        Set<Building> buildings = new HashSet<>();
        if (getRequiredBuildings() == null || getRequiredBuildings().isEmpty()) {
            return true;
        }
        if (rg.getBuildings() == null || rg.getBuildings().isEmpty()) {
            return false;
        }
        for (BuildSite bs : rg.getBuildings()) {
            if (!bs.isFinished()) {
                continue;
            }
            buildings.add(bs.getBuilding());
        }
        Set<Building> required = new HashSet<>();
        for (String s : requiredBuildings) {
            required.add(buildingManager.getByID(s));
        }
        return buildings.containsAll(required);
    }

    public boolean hasRequiredPopulation(Region rg) {
        Set<PopulationLevel> pop = new HashSet<>();
        if (getRequiredPopulation() == null || getRequiredPopulation().isEmpty()) {
            return true;
        }
        if (rg.getPopulation() == null || rg.getPopulation().isEmpty()) {
            return false;
        }
        boolean requirements = true;
        for (PopulationLevel level : getRequiredPopulation().keySet()) {
            if (rg.getPopulation().get(level) < getRequiredPopulation().get(level)) {
                requirements = false;
            }
        }
        return requirements;
    }

    /**
     * Displays a particle frame with the maximum building size
     * @param player the player who will see the particles
     * @param center the center location of the building
     * @param allowed true/false = green/red
     */
    public void displayFrame(Player player, Location center, boolean allowed) {
        BukkitRunnable particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                List<Location> result = new ArrayList<>();
                World world = center.getWorld();
                int radius = getSize();
                int cx = center.getBlockX() + radius;
                int cy = center.getBlockY() + (radius * 2);
                int cz = center.getBlockZ() + radius;
                int cx2 = center.getBlockX() - radius;
                int cy2 = center.getBlockY() - (radius / 2); // don't go underground  too much.
                int cz2 = center.getBlockZ() - radius;
                Location corner1 = new Location(world, cx, cy, cz);
                Location corner2 = new Location(world, cx2, cy2, cz2);
                double minX = Math.min(corner1.getX(), corner2.getX());
                double minY = Math.min(corner1.getY(), corner2.getY());
                double minZ = Math.min(corner1.getZ(), corner2.getZ());
                double maxX = Math.max(corner1.getX(), corner2.getX());
                double maxY = Math.max(corner1.getY(), corner2.getY());
                double maxZ = Math.max(corner1.getZ(), corner2.getZ());

                for (double x = minX; x <= maxX; x+=1) {
                    for (double y = minY; y <= maxY; y+=1) {
                        for (double z = minZ; z <= maxZ; z+=1) {
                            int components = 0;
                            if (x == minX || x == maxX) components++;
                            if (y == minY || y == maxY) components++;
                            if (z == minZ || z == maxZ) components++;
                            if (components >= 2) {
                                result.add(new Location(world, x, y, z));
                            }
                        }
                    }
                }
                for (Location loc : result) {
                    if (allowed) {
                        player.spawnParticle(Particle.REDSTONE, loc, 5, new Particle.DustOptions(Color.LIME, 3));
                    } else {
                        player.spawnParticle(Particle.REDSTONE, loc, 5, new Particle.DustOptions(Color.RED, 3));
                    }
                }
            }
        };
        particleTask.runTaskTimer(plugin, 0, 20);
        BukkitRunnable cancel = new BukkitRunnable() {
            @Override
            public void run() {
                particleTask.cancel();
            }
        };
        cancel.runTaskLater(plugin, 200);
    }

    public Location getCorner1(Location center) {
        World world = center.getWorld();
        int radius = getSize();
        int x = center.getBlockX() + radius;
        int y = center.getBlockY() + (radius * 2);
        int z = center.getBlockZ() + radius;
        return new Location(world, x, y, z);
    }

    public Location getCorner2(Location center) {
        World world = center.getWorld();
        int radius = getSize();
        int x = center.getBlockX() - radius;
        int y = center.getBlockY() - (radius / 2);
        int z = center.getBlockZ() - radius;
        return new Location(world, x, y, z);
    }

    public void setId(String identifier) {
        this.id = identifier;
    }

    public boolean isCoreRequired() {
        return isCoreRequired;
    }

    public void setCoreRequired(boolean coreRequired) {
        isCoreRequired = coreRequired;
    }

    public boolean isCapitalRequired() {
        return isCapitalRequired;
    }

    public boolean isFactionBuilding() {
        return isFactionBuilding;
    }

    public void setCapitalRequired(boolean capitalRequired) {
        isCapitalRequired = capitalRequired;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Map<Resource, Integer> getUnlockCost() {
        return unlockCost;
    }

    public void setUnlockCost(Map<Resource, Integer> unlockCost) {
        this.unlockCost = unlockCost;
    }

    public Map<Material, Integer> getRequiredBlocks() {
        return requiredBlocks;
    }

    public Map<PopulationLevel, Integer> getRequiredPopulation() {
        return requiredPopulation;
    }

    public void setRequiredBlocks(Map<Material, Integer> requiredBlocks) {
        this.requiredBlocks = requiredBlocks;
    }

    public List<String> getRequiredBuildings() {
        return requiredBuildings;
    }


    public String getId() {
        return id;
    }

    public List<String> getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Set<StatusEffect> getEffects() {
        return effects;
    }

    public StatusEffect loadEffect(ConfigurationSection section) {
        StatusEffect effect = null;
        MessageUtil.log("Loading effect... " + section.getKeys(false).toString());
        boolean isRegional = section.getBoolean("regionEffect", true);
        long expiration = section.getLong("expiration", 0);
        effect = new StatusEffect(isRegional, expiration);
        effect.setMemberModifier(section.getDouble("member", 0.0));
        effect.setPrestige(section.getInt("prestige", 0));
        effect.setRegionModifier(section.getDouble("regions", 0.0));
        effect.setManpowerModifier(section.getDouble("manpower", 0.0));
        effect.setDisplayName(section.getString("displayName"));
        if (section.contains("production")) {
            Set<String> cfgList = section.getConfigurationSection("production").getKeys(false);
            for (String s : cfgList) {
                Resource resource = Resource.getByName(s);
                double mod = section.getDouble("production." + s);
                effect.getProductionModifier().put(resource, mod);
            }
        }
        if (section.contains("consumption")) {
            Set<String> cfgList = section.getConfigurationSection("consumption").getKeys(false);
            for (String s : cfgList) {
                Resource resource = Resource.getByName(s);
                double mod = section.getDouble("consumption." + s);
                effect.getConsumptionModifier().put(resource, mod);
            }
        }
        if (section.contains("productionBuff")) {
            Set<String> cfgList = section.getConfigurationSection("productionBuff").getKeys(false);
            for (String s : cfgList) {
                Resource resource = Resource.getByName(s);
                int mod = section.getInt("productionBuff." + s);
                effect.getProductionBuff().put(resource, mod);
            }
        }
        return effect;
    }

    public void load() {
        ConfigurationSection config = this.config;
        name = config.getString("name");
        MessageUtil.log("Loading building " + name + "...");
        isCoreRequired = config.getBoolean("coreRequired");
        isCapitalRequired = config.getBoolean("capitalRequired");
        isFactionBuilding = config.getBoolean("isFactionBuilding");
        size = config.getInt("size");
        description = (List<String>) config.getList("description");
        requiredBuildings = (List<String>) config.getList("requiredBuildings");
        if (config.contains("requiredBlocks")) {
            Set<String> cfgList = config.getConfigurationSection("requiredBlocks").getKeys(false);
            for (String s : cfgList) {
                Material material = Material.getMaterial(s);
                int amount = config.getInt("requiredBlocks." + s);
                requiredBlocks.put(material, amount);
            }
        } else {
            MessageUtil.log("Building " + name + "has a invalid config.");
        }
        if (config.contains("unlockCost")) {
            Set<String> cfgList = config.getConfigurationSection("unlockCost").getKeys(false);
            for (String s : cfgList) {
                Resource resource = Resource.getByName(s);
                int mod = config.getInt("unlockCost." + s);
                unlockCost.put(resource, mod);
            }
        }
        if (config.contains("requiredPopulation")) {
            Set<String> cfgList = config.getConfigurationSection("requiredPopulation").getKeys(false);
            for (String s : cfgList) {
                PopulationLevel level = PopulationLevel.valueOf(s);
                int mod = config.getInt("requiredPopulation." + s);
                requiredPopulation.put(level, mod);
            }
        }
        if (config.contains("effects")) {
            for (String key : config.getConfigurationSection("effects").getKeys(false)) {
                try {
                    effects.add(loadEffect(config.getConfigurationSection("effects." + key)));
                } catch (NullPointerException ex) {
                    MessageUtil.log("There was an error loading effect " + key + " (Building: " + name + ")");
                    MessageUtil.log(ex.toString());
                }
            }
        }
        MessageUtil.log("Loaded building with size " + size);
        MessageUtil.log("Blocks: " + requiredBlocks.toString());
        MessageUtil.log("Effects: " + effects.toString());

    }

    public void save() {

    }

}
