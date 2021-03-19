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
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.util.FDebugLevel;
import de.erethon.factionsxl.util.LazyChunk;
import de.erethon.factionsxl.war.CasusBelli;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Stores all regions and claim ownerships.
 *
 * @author Daniel Saukel
 */
public class Board {

    private List<Region> regions = new CopyOnWriteArrayList<>();
    private final HashMap<Region, Long> cache = new HashMap<>();
    private final HashMap<Chunk, AbstractMap.SimpleEntry<Region, Long>> chunkCache = new HashMap<>();
    private int cacheHitsChunk = 0;
    private int cacheMissChunk = 0;
    private int cacheHitsRegion = 0;
    private int cacheMissRegion = 0;

    public Board(File dir) {
        for (File file : dir.listFiles()) {
            regions.add(new Region(file));
        }
        File oldBoard = new File(FactionsXL.getInstance().getDataFolder(), "board.yml");
        if (oldBoard.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(oldBoard);
            if (config.contains("regions")) {
                for (Entry<String, Object> region : ConfigUtil.getMap(config, "regions").entrySet()) {
                    int id = Integer.parseInt(region.getKey());
                    regions.add(new Region(id, (ConfigurationSection) region.getValue()));
                }
            }
        }
        new CleanerTask().runTaskTimer(FactionsXL.getInstance(), FConfig.HOUR, FConfig.HOUR);
        new CacheCleanTask().runTaskTimer(FactionsXL.getInstance(), FConfig.MINUTE * 5, FConfig.MINUTE * 5);
        new DelayedUpdateTask().runTaskLater(FactionsXL.getInstance(), 60);
    }

    /* Getters and setters */
    /**
     * @param id
     * the ID to check
     * @return
     * the region that contains the chunk
     */
    public Region getById(int id) {
        for (Region region : regions) {
            if (region.getId() == id) {
                return region;
            }
        }
        return null;
    }

    /**
     * @param name
     * the name to check
     * @return
     * the region that contains the chunk
     */
    public Region getByName(String name) {
        for (Region rg : regions) {
            if (rg.getName() == null) {
                continue;
            }
            if (rg.getName().equals(name)) {
                return rg;
            }
            if (rg.getName(true).equals(name)) {
                return rg;
            }
        }
        return null;
    }

    /**
     * @param location
     * the location to check
     * @return
     * the region that contains the chunk
     */
    public Region getByLocation(Location location) {
        return getByChunk(location.getChunk());
    }

    /**
     * @param chunk
     * the chunk to check
     * @return
     * the region that contains the chunk
     */
    public Region getByChunk(Chunk chunk) {
        // Check chunk cache first
        if (chunkCache.containsKey(chunk)) {
            cacheHitsChunk++;
            return chunkCache.get(chunk).getKey();
        }
        cacheMissChunk++;
        // Check region cache
        for (Region cachedRegion : cache.keySet()) {
            if (cachedRegion.getWorld().equals(chunk.getWorld())) {
                for (LazyChunk rChunk : cachedRegion.getChunks()) {
                    if (rChunk.getX() == chunk.getX() && rChunk.getZ() == chunk.getZ()) {
                        cacheHitsRegion++;
                        return cachedRegion;
                    }
                }
            }
        }
        cacheMissRegion++;
        // Check entire board
        for (Region region : regions) {
            if (region.getWorld().equals(chunk.getWorld())) {
                for (LazyChunk rChunk : region.getChunks()) {
                    if (rChunk.getX() == chunk.getX() && rChunk.getZ() == chunk.getZ()) {
                        cache.put(region, System.currentTimeMillis()); // Add region to cache
                        AbstractMap.SimpleEntry<Region, Long> entry = new AbstractMap.SimpleEntry<Region, Long>(region, System.currentTimeMillis());
                        chunkCache.put(rChunk.asBukkitChunk(region.getWorld()), entry);
                        return region;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Tries adjacent regions first, then falls back to getByChunk(Chunk).
     * Should be a lot faster in most cases.
     * @param chunk
     * the chunk to check
     * @param region
     * A known region
     * @return
     * the region that contains the chunk
     */
    public Region getByChunk(Chunk chunk, Region region) {
        if (chunkCache.containsKey(chunk)) {
            cacheHitsChunk++;
            return chunkCache.get(chunk).getKey();
        }
        cacheMissChunk++;
        // Check region cache
        for (Region cachedRegion : cache.keySet()) {
            if (cachedRegion.getWorld().equals(chunk.getWorld())) {
                for (LazyChunk rChunk : cachedRegion.getChunks()) {
                    if (rChunk.getX() == chunk.getX() && rChunk.getZ() == chunk.getZ()) {
                        cacheHitsRegion++;
                        return cachedRegion;
                    }
                }
            }
        }
        cacheMissRegion++;
        // Check chunks of the region
        if (region != null) {
            for (LazyChunk ownChunk : region.getChunks()) {
                if (ownChunk.getX() == chunk.getX() && ownChunk.getZ() == chunk.getZ()) {
                    return region;
                }
            }
        }
        return getByChunk(chunk);
    }

    /**
     * @return
     * a list of all regions
     */
    public List<Region> getRegions() {
        return regions;
    }

    /**
     * @param one the first faction
     * @param two the second faction
     * @return true if both factions share a border, false if otherwise
     */
    public boolean doShareBorder(Faction one, Faction two) {
        for (Region rg : one.getRegions()) {
            if (!Collections.disjoint(rg.getNeighbours(), two.getRegions())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param one the first faction
     * @param two the second faction
     * @return a list of all Regions owned by faction One, that border Regions owned by faction Two.
     */
    public Set<Region> getBorderRegions(Faction one, Faction two) {
        Set<Region> regions = new HashSet<>();
        for (Region rg : one.getRegions()) {
            for (Region neighbour : rg.getNeighbours()) {
                if (two.getRegions().contains(neighbour)) {
                    regions.add(rg);
                }
            }
        }
        return regions;
    }

    /**
     * @param location
     * the location to check
     * @return
     * true if the region is neutral
     * false if the region is owned by a faction
     * false if the chunk is not covered by a region
     */
    public boolean isNeutral(Location location) {
        Region region = getByLocation(location);
        return region == null ? false : region.isNeutral();
    }

    /**
     * @param chunk
     * the chunk to check
     * @return
     * true if the region is neutral
     * false if the region is owned by a faction
     * false if the chunk is not covered by a region
     */
    public boolean isNeutral(Chunk chunk) {
        Region region = getByChunk(chunk);
        return region == null ? false : region.isNeutral();
    }

    /**
     * @param location
     * the location to check
     * @return
     * true if the region is annexable
     * false if the chunk is not covered by a region
     */
    public boolean isAnnexable(Location location) {
        Region region = getByLocation(location);
        return region == null ? false : region.isWildernessClaim();
    }

    /**
     * @param chunk
     * the chunk to check
     * @return
     * true if the region is annexable
     * false if the chunk is not covered by a region
     */
    public boolean isAnnexable(Chunk chunk) {
        Region region = getByChunk(chunk);
        return region == null ? false : region.isWildernessClaim();
    }

    /**
     * @param location
     * the location to check
     * @return
     * true if the chunk is not covered by a region
     */
    public boolean isWilderness(Location location) {
        return getByLocation(location) == null;
    }

    /**
     * @param chunk
     * the chunk to check
     * @return
     * true if the chunk is not covered by a region
     */
    public boolean isWilderness(Chunk chunk) {
        return getByChunk(chunk) == null;
    }

    /**
     * @return
     * a new, unused region ID.
     */
    public int generateId() {
        return getHighestId() + 1;
    }

    /**
     * @return
     * the highest id used by any region
     */
    public int getHighestId() {
        int highestId = 0;
        for (Region region : regions) {
            if (region.getId() > highestId) {
                highestId = region.getId();
            }
        }
        return highestId;
    }

    /**
     * @param region
     * region to delete
     */
    public void deleteRegion(Region region) {
        regions.remove(region);
        region.delete();
    }

    /* Persistence */
    /**
     * Saves all factions
     */
    public void saveAll() {
        for (Region region : regions) {
            region.save();
        }
    }

    /**
     * Loads the persistent data of all regions
     */
    public void loadAll() {
        for (Region region : regions) {
            region.load();
        }
        FactionsXL.debug(FDebugLevel.DATA, "Loaded board with " + regions.size() + " regions.");
    }

    public class CacheCleanTask extends BukkitRunnable {
        @Override
        public void run() {
            FactionsXL.debug(FDebugLevel.BOARD, "RegionCache hits/5 min.: " + cacheHitsRegion + " | Cache Miss/5 min.: " + cacheMissRegion);
            FactionsXL.debug(FDebugLevel.BOARD, "ChunkCache hits/5 min.: " + cacheHitsChunk + " | Cache Miss/5 min.: " + cacheMissChunk);
            cacheHitsRegion = 0;
            cacheMissRegion = 0;
            cacheHitsChunk = 0;
            cacheMissChunk = 0;
            Set<Region> toRemove = new HashSet<>();
            long time = System.currentTimeMillis();
            for (Entry<Region, Long> entry : cache.entrySet()) {
                if (entry.getValue() + 300000 < time) {
                    toRemove.add(entry.getKey());
                }
            }
            Set<Chunk> toRemoveChunks = new HashSet<>();
            for (Entry<Chunk, AbstractMap.SimpleEntry<Region, Long>> entry : chunkCache.entrySet()) {
                if (entry.getValue().getValue() + 300000 < time) {
                    toRemoveChunks.add(entry.getKey());
                }
            }
            cache.keySet().removeAll(toRemove);
            chunkCache.keySet().removeAll(toRemoveChunks);
        }
    }

    public class DelayedUpdateTask extends BukkitRunnable {
        @Override
        public void run() {
            // Add BorderFriction CBs if necessary
            MessageUtil.log("Updating region-related Casus Belli...");
            FactionsXL plugin = FactionsXL.getInstance();
            for (Faction faction : plugin.getFactionCache().getActive()) {
                List<Faction> cbCandidates = new ArrayList<>();
                for (Region rg : faction.getRegions()) {
                    for (Region neighbour : rg.getNeighbours()) {
                        if (neighbour.getOwner() != null && !cbCandidates.contains(neighbour.getOwner())) {
                            cbCandidates.add(neighbour.getOwner());
                        }
                    }
                }
                for (Faction faction2 : cbCandidates) {
                    if (faction.getRelation(faction2) == null || faction.getRelation(faction2) == Relation.PEACE) {
                        if (plugin.getCBManager().hasCBAlready(faction, CasusBelli.Type.BORDER_FRICTION, faction2)) {
                            continue;
                        }
                        CasusBelli toAdd = new CasusBelli(CasusBelli.Type.BORDER_FRICTION, faction2, null);
                        faction.getCasusBelli().add(toAdd);
                        FactionsXL.debug(FDebugLevel.BOARD, "Added missing BorderFriction CB to " + faction + " (" + toAdd.toString() + ")");
                    }
                }
            }
            MessageUtil.log("Finished updating CBs!");
        }
    }

    @Deprecated
    public class CleanerTask extends BukkitRunnable {
        @Override
        public void run() {
            for (Region region : regions) {
                if (region.getOwner() != null && !region.getOwner().isActive()) {
                    FactionsXL.debug(FDebugLevel.BOARD, "Cleaned " + region + ". It was owned by " + region.getOwner());
                    region.setOwner(null);
                }
            }
        }

    }

}
