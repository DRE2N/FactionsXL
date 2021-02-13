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

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.player.FPlayer;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a building in the world.
 *
 * @author Malfrador
 */

public class BuildSite implements ConfigurationSerializable {

    FactionsXL plugin = FactionsXL.getInstance();
    BuildingManager buildingManager = plugin.getBuildingManager();
    FConfig fConfig = plugin.getFConfig();

    private final Building building;
    private final Region region;
    private final Location corner;
    private final Location otherCorner;
    private final Location interactive;
    private String problemMessage = null;
    private final FTouchHandler fTouchHandler;
    private Map<Material, Integer> placedBlocks = new HashMap<>();
    private boolean finished;
    private boolean hasTicket = false;
    private boolean isBusy = false;

    Hologram progressHolo;

    public BuildSite(Building b, Region rg, Location loc1, Location loc2, Location center) {
        building = b;
        region = rg;
        finished = false;
        corner = loc1;
        otherCorner = loc2;
        interactive = center;
        MessageUtil.log("Created new building site in " + region.getName() + ". Building type: " + b.getName());
        rg.getBuildings().add(this);
        fTouchHandler = new FTouchHandler();
        setupHolo();
    }

    public BuildSite (Map<String, Object> args) {
        building = buildingManager.getByID((String) args.get("building"));
        region = plugin.getBoard().getById((int) args.get("region"));
        corner = Location.deserialize((Map<String, Object>) args.get("location.corner"));
        otherCorner = Location.deserialize((Map<String, Object>) args.get("location.otherCorner"));
        MessageUtil.log((String) args.get("location.interactable"));
        interactive = Location.deserialize((Map<String, Object>)args.get("location.interactable"));
        finished = (boolean) args.get("finished");
        hasTicket = (boolean) args.get("hasTicket");
        problemMessage = (String) args.get("problemMessage");
        region.getBuildings().add(this);
        fTouchHandler = new FTouchHandler();
        if (hasTicket) {
            buildingManager.getBuildingTickets().add(getSite());
        }
        setupHolo();
    }
    
    public BuildSite (ConfigurationSection config) {
        building = buildingManager.getByID(config.getString("building"));
        region = plugin.getBoard().getById((int) config.get("region"));
        corner =  Location.deserialize(config.getConfigurationSection("location.corner").getValues(false));
        otherCorner = Location.deserialize(config.getConfigurationSection("location.otherCorner").getValues(false));
        interactive = Location.deserialize(config.getConfigurationSection("location.interactable").getValues(false));
        MessageUtil.log(corner.toString());
        finished = config.getBoolean("finished");
        hasTicket = config.getBoolean("hasTicket");
        problemMessage = config.getString("problemMessage");
        region.getBuildings().add(this);
        fTouchHandler = new FTouchHandler();
        scheduleProgressUpdate();
        BukkitRunnable delayedSetup = new BukkitRunnable() {
            @Override
            public void run() {
                setupHolo();
            }
        };
        delayedSetup.runTaskLater(plugin, 60);
        if (hasTicket) {
            buildingManager.getBuildingTickets().add(getSite());
        }
    }

    public void setupHolo() {
        if (!fConfig.areHologramsEnabled()) {
            return;
        }
        if (progressHolo != null) {
            progressHolo.delete();
        }
        MessageUtil.log("Setting up holo");
        MessageUtil.log(interactive.toString());
        MessageUtil.log(interactive.getBlock().toString());
        progressHolo = HologramsAPI.createHologram(plugin, interactive.getBlock().getRelative(0, 2, 0).getLocation());
        progressHolo.appendTextLine(ChatColor.GOLD + building.getName()).setTouchHandler(fTouchHandler);
        progressHolo.appendTextLine(" ").setTouchHandler(fTouchHandler);
        String bar = "----------";
        progressHolo.appendTextLine(bar).setTouchHandler(fTouchHandler); // Placeholder
        Location newLoc = progressHolo.getLocation();
        for (Material material : building.getRequiredBlocks().keySet()) {
            String output = plugin.getFTranslation().getTranslatedName(material);
            progressHolo.appendTextLine(ChatColor.GRAY + output + ChatColor.DARK_GRAY + ": " + (getProgressString(material))).setTouchHandler(fTouchHandler);
            newLoc.add(0, 0.2,0); // Holo needs to be moved to not get stuck in the floor
        }
        progressHolo.teleport(newLoc);
        progressHolo.appendTextLine(FMessage.BUILDING_SITE_HINT.getMessage()).setTouchHandler(fTouchHandler);
    }

    public void updateHolo() {
        if (!fConfig.areHologramsEnabled()) {
            return;
        }
        if (progressHolo == null) {
            setupHolo();
        }
        MessageUtil.log("Update holo");
        progressHolo.clearLines();
        progressHolo.appendTextLine(ChatColor.GOLD + building.getName()).setTouchHandler(fTouchHandler);
        progressHolo.appendTextLine(" ").setTouchHandler(fTouchHandler);
        if (!isFinished() || !buildingManager.getBuildingTickets().contains(getSite())) {
            for (Material material : building.getRequiredBlocks().keySet()) {
                String output = plugin.getFTranslation().getTranslatedName(material);
                progressHolo.appendTextLine(ChatColor.GRAY + output + ChatColor.DARK_GRAY + ": " + (getProgressString(material))).setTouchHandler(fTouchHandler);
            }
        }
        if (buildingManager.getBuildingTickets().contains(getSite())) {
            progressHolo.appendTextLine(FMessage.BUILDING_SITE_WAITING.getMessage());
            if (problemMessage != null) {
                progressHolo.appendTextLine(ChatColor.RED + problemMessage);
            }
        }
        progressHolo.appendTextLine(FMessage.BUILDING_SITE_HINT.getMessage()).setTouchHandler(fTouchHandler);
    }

    public void finishBuilding() {
        region.getEffects().addAll(getBuilding().getEffects());
        if (getBuilding().isFactionBuilding()) {
            region.getOwner().getEffects().addAll(getBuilding().getEffects());
        }
        finished = true;
        problemMessage = null;
        hasTicket = false;
        getRegion().getOwner().sendMessage("&aEin(e) &6" + getBuilding().getName() + " &ain " + getRegion().getName() + " &awurde akzeptiert und die Effekte sind nun aktiv.");
    }

    public void removeEffects() {
        getRegion().getEffects().removeIf(effect -> effect.getOrigin() == getSite());
        getRegion().getOwner().getEffects().removeIf(effect -> effect.getOrigin() == getSite());
    }

    public void scheduleProgressUpdate() {
        CompletableFuture<Chunk> chunk = getCorner().getWorld().getChunkAtAsync(getCorner());
        isBusy = true;
        BukkitRunnable waitForChunk = new BukkitRunnable() {
            @Override
            public void run() {
                if (chunk.isDone()) {
                    checkProgress();
                    cancel();
                }
            }
        };
        waitForChunk.runTaskTimer(plugin, 10,10);
    }

    public boolean isDestroyed() {
        boolean damaged = false;
        for (Material material : building.getRequiredBlocks().keySet()) {
            if (getPlacedBlocks().get(material) < building.getRequiredBlocks().get(material)) {
                damaged = true;
            }
        }
        return damaged;
    }

    public void checkProgress() {
        isBusy = true;
        BukkitRunnable complete = new BukkitRunnable() {
            @Override
            public void run() {
                updateHolo();
                isBusy = false;
                boolean fini = true;
                for (Material material : building.getRequiredBlocks().keySet()) {
                    if (getPlacedBlocks().get(material) < building.getRequiredBlocks().get(material)) {
                        fini = false;
                    }
                }
                if (finished && !fini) {
                    finished = false;
                    getRegion().getOwner().sendMessage("&aEin(e) &6" + getBuilding().getName() + " &ain " + getRegion().getName() + " &awurde zerstört!");
                    removeEffects();
                    return;
                }
                if (fini && !isFinished() && !hasTicket) {
                    buildingManager.getBuildingTickets().add(getSite());
                    hasTicket = true;
                    getRegion().getOwner().sendMessage("&aEin(e) &6" + getBuilding().getName() + " &ain " + getRegion().getName() + " &awurde fertiggestellt");
                    getRegion().getOwner().sendMessage("&7&oEin Ticket wurde automatisch erstellt und das Gebäude wird zeitnah überprüft.");
                    MessageUtil.log("A new BuildSite ticket for " + getBuilding().getName() + " in " + getRegion().getName() + " was created.");
                }
            }
        };
        BukkitRunnable runAsync = new BukkitRunnable() {
            @Override
            public void run() {
                Set<Block> blocks;
                Map<Material, Integer> placed = new HashMap<>();
                blocks = getBlocks(corner.getWorld());
                MessageUtil.log(building.getRequiredBlocks().toString());
                for (Block block : blocks) {
                    Material type = block.getType();
                    if (building.getRequiredBlocks().containsKey(type)) {
                        int amount = 0;
                        if (placed.containsKey(type)) {
                            amount = placed.get(type);
                        }
                        placed.put(type, amount + 1);
                    }
                }
                MessageUtil.log("-------------------");
                MessageUtil.log(placed.toString());
                placedBlocks = placed;
                complete.runTask(plugin);
            }
        };
        runAsync.runTaskAsynchronously(plugin);
    }

    public String getProgressString(Material block) {
        int total = 0;
        if (building.getRequiredBlocks().get(block) != null) {
            total = building.getRequiredBlocks().get(block);
        }
        int placed = 0;
        if (getPlacedBlocks().get(block) != null) {
            placed = getPlacedBlocks().get(block);
        }
        if (placed >= total) {
            return ChatColor.translateAlternateColorCodes('&', "&a&m" + placed + "&a&m/" + total + "&a ✔");
        }
        return ChatColor.translateAlternateColorCodes('&', "&a" + placed + "&8/&7" + total);
    }

    public boolean isInBuildSite(Location location) {
        double xp = location.getX();
        double yp = location.getY();
        double zp = location.getZ();

        double x1 = corner.getX();
        double y1 = corner.getY();
        double z1 = corner.getZ();
        double x2 = otherCorner.getX();
        double y2 = otherCorner.getY();
        double z2 = otherCorner.getZ();
        return new IntRange(x1, x2).containsDouble(xp) && new IntRange(y1, y2).containsDouble(yp) && new IntRange(z1, z2).containsDouble(zp);
    }

    public boolean isInBuildSite(Player player) {
        FPlayer fPlayer = plugin.getFPlayerCache().getByPlayer(player);
        Region rg = fPlayer.getLastRegion();
        if (rg == null) {
            return false;
        }
        Location location = player.getLocation();
        return isInBuildSite(location);
    }



    public Set<Block> getBlocks(World world) {
        Set<Block> blockList = new HashSet<>();
        Set<Location> result = new HashSet<>();
        double minX = Math.min(corner.getX(), otherCorner.getX());
        double minY = Math.min(corner.getY(), otherCorner.getY());
        double minZ = Math.min(corner.getZ(), otherCorner.getZ());
        double maxX = Math.max(corner.getX(), otherCorner.getX());
        double maxY = Math.max(corner.getY(), otherCorner.getY());
        double maxZ = Math.max(corner.getZ(), otherCorner.getZ());
        for (double x = minX; x <= maxX; x+=1) {
            for (double y = minY; y <= maxY; y+=1) {
                for (double z = minZ; z <= maxZ; z+=1) {
                    result.add(new Location(world, x, y, z));
                }
            }
        }
        for (Location location : result) {
            blockList.add(world.getBlockAt(location));
        }

        return blockList;
    }

    public BuildSite getSite(){
        return this;
    }

    public Building getBuilding() {
        return building;
    }

    public Region getRegion() {
        return region;
    }

    public Location getCorner() {
        return corner;
    }

    public Location getOtherCorner() {
        return otherCorner;
    }

    public Location getInteractive() {
        return interactive;
    }

    public Map<Material, Integer> getPlacedBlocks() {
        return placedBlocks;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setProblemMessage(String msg) {
        problemMessage = msg;
    }


    /**
     * true if there is already a async operation running
     * on this buildsite
     */
    public boolean isBusy() {
        return isBusy;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> args = new HashMap<>();
        args.put("building", building.getId());
        args.put("region", region.getId());
        args.put("location.corner", corner.serialize());
        args.put("location.otherCorner", otherCorner.serialize());
        args.put("location.interactable", interactive.serialize());
        args.put("finished", finished);
        args.put("hasTicket", hasTicket);
        args.put("problemMessage", problemMessage);
        return args;
    }
}
