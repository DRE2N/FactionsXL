/*
 *
 *  * Copyright (C) 2017-2020 Daniel Saukel, Malfrador
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.erethon.factionsxl.protection;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Board;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.bukkit.Material.*;

/**
 * @author Daniel Saukel
 */
public class LandProtectionListener implements Listener {

    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();
    Board board = plugin.getBoard();
    FConfig config = plugin.getFConfig();
    boolean wildernessProtected = config.isWildernessProtected();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        forbidIfInProtectedTerritory(event.getPlayer(), event.getBlock(), event, true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        forbidIfInProtectedTerritory(event.getPlayer(), event.getBlock(), event, false);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        forbidIfInProtectedTerritory(event.getPlayer(), event.getBlock(), event, true);
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        forbidIfInProtectedTerritory(event.getPlayer(), event.getBlockClicked(), event, true);
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        forbidIfInProtectedTerritory(event.getPlayer(), event.getBlockClicked(), event, false);
    }

    private static final Set<Material> WAR_BREAKABLE = new HashSet<>(Arrays.asList(
            BEETROOTS,
            CAKE,
            CARROTS,
            CHORUS_PLANT,
            COARSE_DIRT,
            COBWEB,
            DEAD_BUSH,
            DANDELION,
            POPPY,
            BLUE_ORCHID,
            ALLIUM,
            AZURE_BLUET,
            RED_TULIP,
            ORANGE_TULIP,
            WHITE_TULIP,
            PINK_TULIP,
            OXEYE_DAISY,
            SUNFLOWER,
            LILAC,
            ROSE_BUSH,
            PEONY,
            DIRT,
            END_ROD,
            FLOWER_POT,
            FROSTED_ICE,
            GLASS,
            GLASS_PANE,
            GRASS,
            FERN,
            LARGE_FERN,
            GRASS_PATH,
            GRASS_BLOCK,
            GRAVEL,
            HAY_BLOCK,
            ICE,
            JACK_O_LANTERN,
            LADDER,
            OAK_LEAVES,
            BIRCH_LEAVES,
            SPRUCE_LEAVES,
            JUNGLE_LEAVES,
            ACACIA_LEAVES,
            DARK_OAK_LEAVES,
            LILY_PAD,
            MELON,
            MELON_STEM,
            NETHER_WART_BLOCK,
            PACKED_ICE,
            PODZOL,
            POTATOES,
            PUMPKIN,
            PUMPKIN_STEM,
            SAND,
            SOUL_SAND,
            SUGAR_CANE,
            TALL_GRASS,
            TNT,
            TORCH,
            VINE,
            WHEAT,
            WHITE_BANNER,
            ORANGE_BANNER,
            MAGENTA_BANNER,
            LIGHT_BLUE_BANNER,
            YELLOW_BANNER,
            LIME_BANNER,
            PINK_BANNER,
            GRAY_BANNER,
            LIGHT_GRAY_BANNER,
            CYAN_BANNER,
            PURPLE_BANNER,
            BLUE_BANNER,
            BROWN_BANNER,
            GREEN_BANNER,
            RED_BANNER,
            BLACK_BANNER,
            WHITE_WALL_BANNER,
            ORANGE_WALL_BANNER,
            MAGENTA_WALL_BANNER,
            LIGHT_BLUE_WALL_BANNER,
            YELLOW_WALL_BANNER,
            LIME_WALL_BANNER,
            PINK_WALL_BANNER,
            GRAY_WALL_BANNER,
            LIGHT_GRAY_WALL_BANNER,
            CYAN_WALL_BANNER,
            PURPLE_WALL_BANNER,
            BLUE_WALL_BANNER,
            BROWN_WALL_BANNER,
            GREEN_WALL_BANNER,
            RED_WALL_BANNER,
            BLACK_WALL_BANNER,
            WHITE_CARPET,
            ORANGE_CARPET,
            MAGENTA_CARPET,
            LIGHT_BLUE_CARPET,
            YELLOW_CARPET,
            LIME_CARPET,
            PINK_CARPET,
            GRAY_CARPET,
            LIGHT_GRAY_CARPET,
            CYAN_CARPET,
            PURPLE_CARPET,
            BLUE_CARPET,
            BROWN_CARPET,
            GREEN_CARPET,
            RED_CARPET,
            BLACK_CARPET,
            WHITE_STAINED_GLASS_PANE,
            ORANGE_STAINED_GLASS_PANE,
            MAGENTA_STAINED_GLASS_PANE,
            LIGHT_BLUE_STAINED_GLASS_PANE,
            YELLOW_STAINED_GLASS_PANE,
            LIME_STAINED_GLASS_PANE,
            PINK_STAINED_GLASS_PANE,
            GRAY_STAINED_GLASS_PANE,
            LIGHT_GRAY_STAINED_GLASS_PANE,
            CYAN_STAINED_GLASS_PANE,
            PURPLE_STAINED_GLASS_PANE,
            BLUE_STAINED_GLASS_PANE,
            BROWN_STAINED_GLASS_PANE,
            GREEN_STAINED_GLASS_PANE,
            RED_STAINED_GLASS_PANE,
            BLACK_STAINED_GLASS_PANE
    ));

    private static final Set<Material> WAR_PLACABLE = new HashSet<>(Arrays.asList(
            FIRE,
            LADDER,
            TNT,
            TORCH,
            VINE,
            LILY_PAD,
            WHITE_BANNER,
            ORANGE_BANNER,
            MAGENTA_BANNER,
            LIGHT_BLUE_BANNER,
            YELLOW_BANNER,
            LIME_BANNER,
            PINK_BANNER,
            GRAY_BANNER,
            LIGHT_GRAY_BANNER,
            CYAN_BANNER,
            PURPLE_BANNER,
            BLUE_BANNER,
            BROWN_BANNER,
            GREEN_BANNER,
            RED_BANNER,
            BLACK_BANNER,
            WHITE_WALL_BANNER,
            ORANGE_WALL_BANNER,
            MAGENTA_WALL_BANNER,
            LIGHT_BLUE_WALL_BANNER,
            YELLOW_WALL_BANNER,
            LIME_WALL_BANNER,
            PINK_WALL_BANNER,
            GRAY_WALL_BANNER,
            LIGHT_GRAY_WALL_BANNER,
            CYAN_WALL_BANNER,
            PURPLE_WALL_BANNER,
            BLUE_WALL_BANNER,
            BROWN_WALL_BANNER,
            GREEN_WALL_BANNER,
            RED_WALL_BANNER,
            BLACK_WALL_BANNER,
            COBWEB
    ));

    private static final Set<Material> NO_INTERACT = new HashSet<>(Arrays.asList(
            ANVIL,
            BEACON,
            BLACK_SHULKER_BOX,
            BLUE_SHULKER_BOX,
            BREWING_STAND,
            BROWN_SHULKER_BOX,
            CAKE,
            CAULDRON,
            CHEST,
            COMPARATOR,
            CYAN_SHULKER_BOX,
            DISPENSER,
            DRAGON_EGG,
            DROPPER,
            ENCHANTING_TABLE,
            ENDER_CHEST,
            END_PORTAL_FRAME,
            FIRE,
            FURNACE,
            GRASS,
            GRAY_SHULKER_BOX,
            GREEN_SHULKER_BOX,
            HEAVY_WEIGHTED_PRESSURE_PLATE,
            HOPPER,
            LIGHT_BLUE_SHULKER_BOX,
            LIGHT_GRAY_SHULKER_BOX,
            LIGHT_WEIGHTED_PRESSURE_PLATE,
            LIME_SHULKER_BOX,
            MAGENTA_SHULKER_BOX,
            NOTE_BLOCK,
            ORANGE_SHULKER_BOX,
            PINK_SHULKER_BOX,
            PURPLE_SHULKER_BOX,
            RED_SHULKER_BOX,
            REPEATER,
            WHITE_SHULKER_BOX,
            YELLOW_SHULKER_BOX
    ));

    private static final Set<Material> INTERACTABLE = new HashSet<>(Arrays.asList(
            LEVER,
            ACACIA_DOOR,
            BIRCH_DOOR,
            DARK_OAK_DOOR,
            JUNGLE_DOOR,
            SPRUCE_DOOR,
            OAK_DOOR,
            STONE_BUTTON,
            ACACIA_BUTTON,
            BIRCH_BUTTON,
            DARK_OAK_BUTTON,
            JUNGLE_BUTTON,
            SPRUCE_BUTTON,
            OAK_BUTTON
    ));

    public void onPlayerInteract(PlayerInteractEvent event) {
        if (config.isExcludedWorld(event.getPlayer().getWorld())) {
            return;
        }
        if (event.hasBlock() && NO_INTERACT.contains(event.getClickedBlock().getType())) {
            Player breaker = event.getPlayer();
            if (breaker == null) {
                return;
            }
            if (FPermission.hasPermission(breaker, FPermission.BUILD)) {
                return;
            }

            Region region = board.getByChunk(event.getClickedBlock().getChunk());
            if (region == null || region.isNeutral()) {
                return;
            }

            Faction bFaction = factions.getByMember(breaker);
            Faction owner = region.getOwner();
            if (region.getOccupant() != null) {
                Faction occupant = region.getOccupant();
                if (occupant == bFaction) {
                    return;
                }
            }
            Relation rel = owner.getRelation(bFaction);
            if (rel == Relation.ENEMY) {
                Material type = event.getClickedBlock().getType();
                if (!WAR_BREAKABLE.contains(type) && !NO_INTERACT.contains(type) && !INTERACTABLE.contains(type)) {
                    event.setCancelled(true);
                }
            } else if (!rel.canBuild()) {
                event.setCancelled(true);
                ParsingUtil.sendActionBarMessage(breaker, FMessage.PROTECTION_CANNOT_INTERACT_FACTION.getMessage(), event.getClickedBlock().getType().toString(), region.getOwner());
            }
        }
    }

    private void forbidIfInProtectedTerritory(Player breaker, Block destroyed, Cancellable event, boolean destroy) {
        if (config.isExcludedWorld(destroyed.getWorld())) {
            return;
        }
        if (breaker == null) {
            return;
        }
        if (FPermission.hasPermission(breaker, FPermission.BUILD)) {
            return;
        }

        Region region = board.getByChunk(destroyed.getChunk());
        if (region == null || region.isNeutral()) {
            if (wildernessProtected) {
                event.setCancelled(true);
                ParsingUtil.sendActionBarMessage(breaker, (destroy ? FMessage.PROTECTION_CANNOT_DESTROY_WILDERNESS : FMessage.PROTECTION_CANNOT_BUILD_WILDERNESS).getMessage());
            }
            return;
        }

        Faction bFaction = factions.getByMember(breaker);
        Faction owner = region.getOwner();
        if (region.getOccupant() != null) {
            Faction occupant = region.getOccupant();
            if (occupant == bFaction) {
                return;
            }
        }
        Relation rel = owner.getRelation(bFaction);
        if (rel == Relation.ENEMY) {
            Material type = destroyed.getType();
            if (event instanceof BlockBreakEvent) {
                if (!WAR_BREAKABLE.contains(type) && !NO_INTERACT.contains(type) && !INTERACTABLE.contains(type)) {
                    event.setCancelled(true);
                }
            } else if (event instanceof BlockPlaceEvent) {
                if (!WAR_PLACABLE.contains(type) && !NO_INTERACT.contains(type) && !INTERACTABLE.contains(type)) {
                    event.setCancelled(true);
                }
            }
        } else if (!rel.canBuild()) {
            event.setCancelled(true);
            ParsingUtil.sendActionBarMessage(breaker, (destroy ? FMessage.PROTECTION_CANNOT_DESTROY_FACTION : FMessage.PROTECTION_CANNOT_BUILD_FACTION).getMessage(), region.getOwner());
        }
    }

}
