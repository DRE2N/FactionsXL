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
package de.erethon.factionsxl.protection;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Board;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.player.FPlayerCache;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.WarCache;
import de.erethon.factionsxl.war.WarPlayerAction;
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
    FPlayerCache fPlayers = plugin.getFPlayerCache();
    Board board = plugin.getBoard();
    WarCache wars = plugin.getWarCache();
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
            ACACIA_LEAVES,
            ALLIUM,
            AZURE_BLUET,
            BAMBOO,
            BEETROOTS,
            BIRCH_LEAVES,
            BLACK_BANNER,
            BLACK_CARPET,
            BLACK_STAINED_GLASS_PANE,
            BLACK_WALL_BANNER,
            BLUE_BANNER,
            BLUE_CARPET,
            BLUE_ORCHID,
            BLUE_STAINED_GLASS_PANE,
            BLUE_WALL_BANNER,
            BRAIN_CORAL,
            BRAIN_CORAL_FAN,
            BROWN_BANNER,
            BROWN_CARPET,
            BROWN_STAINED_GLASS_PANE,
            BROWN_WALL_BANNER,
            BUBBLE_CORAL,
            BUBBLE_CORAL_FAN,
            CAKE,
            CARROTS,
            CHORUS_PLANT,
            COARSE_DIRT,
            COBWEB,
            CORNFLOWER,
            CRIMSON_FUNGUS,
            CRIMSON_NYLIUM,
            CRIMSON_ROOTS,
            CYAN_BANNER,
            CYAN_CARPET,
            CYAN_STAINED_GLASS_PANE,
            CYAN_WALL_BANNER,
            DANDELION,
            DARK_OAK_LEAVES,
            DEAD_BRAIN_CORAL,
            DEAD_BRAIN_CORAL_FAN,
            DEAD_BUBBLE_CORAL,
            DEAD_BUBBLE_CORAL_FAN,
            DEAD_BUSH,
            DEAD_FIRE_CORAL,
            DEAD_FIRE_CORAL_FAN,
            DEAD_HORN_CORAL,
            DEAD_HORN_CORAL_FAN,
            DEAD_TUBE_CORAL,
            DEAD_TUBE_CORAL_FAN,
            DIRT,
            END_ROD,
            FERN,
            FIRE_CORAL,
            FIRE_CORAL_FAN,
            FLOWER_POT,
            FROSTED_ICE,
            GLASS,
            GLASS_PANE,
            GRASS,
            GRASS_BLOCK,
            GRASS_PATH,
            GRAVEL,
            GRAY_BANNER,
            GRAY_CARPET,
            GRAY_STAINED_GLASS_PANE,
            GRAY_WALL_BANNER,
            GREEN_BANNER,
            GREEN_CARPET,
            GREEN_STAINED_GLASS_PANE,
            GREEN_WALL_BANNER,
            HAY_BLOCK,
            HORN_CORAL,
            HORN_CORAL_FAN,
            ICE,
            JACK_O_LANTERN,
            JUNGLE_LEAVES,
            KELP_PLANT,
            LADDER,
            LARGE_FERN,
            LIGHT_BLUE_BANNER,
            LIGHT_BLUE_CARPET,
            LIGHT_BLUE_STAINED_GLASS_PANE,
            LIGHT_BLUE_WALL_BANNER,
            LIGHT_GRAY_BANNER,
            LIGHT_GRAY_CARPET,
            LIGHT_GRAY_STAINED_GLASS_PANE,
            LIGHT_GRAY_WALL_BANNER,
            LILAC,
            LILY_OF_THE_VALLEY,
            LILY_PAD,
            LIME_BANNER,
            LIME_CARPET,
            LIME_STAINED_GLASS_PANE,
            LIME_WALL_BANNER,
            MAGENTA_BANNER,
            MAGENTA_CARPET,
            MAGENTA_STAINED_GLASS_PANE,
            MAGENTA_WALL_BANNER,
            MELON,
            MELON_STEM,
            NETHER_SPROUTS,
            NETHER_WART_BLOCK,
            OAK_LEAVES,
            ORANGE_BANNER,
            ORANGE_CARPET,
            ORANGE_STAINED_GLASS_PANE,
            ORANGE_TULIP,
            ORANGE_WALL_BANNER,
            OXEYE_DAISY,
            PEONY,
            PINK_BANNER,
            PINK_CARPET,
            PINK_STAINED_GLASS_PANE,
            PINK_TULIP,
            PINK_WALL_BANNER,
            PODZOL,
            POPPY,
            POTATOES,
            PUMPKIN,
            PUMPKIN_STEM,
            PURPLE_BANNER,
            PURPLE_CARPET,
            PURPLE_STAINED_GLASS_PANE,
            PURPLE_WALL_BANNER,
            RED_BANNER,
            RED_CARPET,
            RED_STAINED_GLASS_PANE,
            RED_TULIP,
            RED_WALL_BANNER,
            ROSE_BUSH,
            SAND,
            SEAGRASS,
            SOUL_SAND,
            SOUL_TORCH,
            SPRUCE_LEAVES,
            SUGAR_CANE,
            SUNFLOWER,
            STRING,
            SWEET_BERRY_BUSH,
            TALL_GRASS,
            TALL_SEAGRASS,
            TNT,
            TORCH,
            TUBE_CORAL,
            TUBE_CORAL_FAN,
            TWISTING_VINES,
            VINE,
            WARPED_FUNGUS,
            WARPED_NYLIUM,
            WARPED_ROOTS,
            WARPED_WART_BLOCK,
            WEEPING_VINES,
            WHEAT,
            WHITE_BANNER,
            WHITE_CARPET,
            WHITE_STAINED_GLASS_PANE,
            WHITE_TULIP,
            WHITE_WALL_BANNER,
            WITHER_ROSE,
            YELLOW_BANNER,
            YELLOW_CARPET,
            YELLOW_STAINED_GLASS_PANE,
            YELLOW_WALL_BANNER

    ));

    private static final Set<Material> WAR_PLACABLE = new HashSet<>(Arrays.asList(
            BLACK_BANNER,
            BLACK_WALL_BANNER,
            BLUE_BANNER,
            BLUE_WALL_BANNER,
            BROWN_BANNER,
            BROWN_WALL_BANNER,
            COBWEB,
            CYAN_BANNER,
            CYAN_WALL_BANNER,
            FIRE,
            GRAY_BANNER,
            GRAY_WALL_BANNER,
            GREEN_BANNER,
            GREEN_WALL_BANNER,
            LADDER,
            LIGHT_BLUE_BANNER,
            LIGHT_BLUE_WALL_BANNER,
            LIGHT_GRAY_BANNER,
            LIGHT_GRAY_WALL_BANNER,
            LILY_PAD,
            LIME_BANNER,
            LIME_WALL_BANNER,
            MAGENTA_BANNER,
            MAGENTA_WALL_BANNER,
            ORANGE_BANNER,
            ORANGE_WALL_BANNER,
            PINK_BANNER,
            PINK_WALL_BANNER,
            PURPLE_BANNER,
            PURPLE_WALL_BANNER,
            RED_BANNER,
            RED_WALL_BANNER,
            TNT,
            TORCH,
            VINE,
            WHITE_BANNER,
            WHITE_WALL_BANNER,
            YELLOW_BANNER,
            YELLOW_WALL_BANNER
    ));

    private static final Set<Material> WAR_IMPORTANT = new HashSet<>(Arrays.asList(
            ACACIA_BUTTON,
            ACACIA_DOOR,
            ANVIL,
            BEACON,
            BIRCH_BUTTON,
            BIRCH_DOOR,
            BLACK_SHULKER_BOX,
            BLUE_SHULKER_BOX,
            BREWING_STAND,
            BROWN_SHULKER_BOX,
            CAKE,
            CAULDRON,
            CHEST,
            CHIPPED_ANVIL,
            COMPARATOR,
            CYAN_SHULKER_BOX,
            DAMAGED_ANVIL,
            DARK_OAK_BUTTON,
            DARK_OAK_DOOR,
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
            JUNGLE_BUTTON,
            JUNGLE_DOOR,
            LEVER,
            LIGHT_BLUE_SHULKER_BOX,
            LIGHT_GRAY_SHULKER_BOX,
            LIGHT_WEIGHTED_PRESSURE_PLATE,
            LIME_SHULKER_BOX,
            MAGENTA_SHULKER_BOX,
            NOTE_BLOCK,
            OAK_BUTTON,
            OAK_DOOR,
            ORANGE_SHULKER_BOX,
            PINK_SHULKER_BOX,
            PURPLE_SHULKER_BOX,
            RED_SHULKER_BOX,
            REPEATER,
            SPRUCE_BUTTON,
            SPRUCE_DOOR,
            STONE_BUTTON,
            WHITE_SHULKER_BOX,
            YELLOW_SHULKER_BOX

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
            CHIPPED_ANVIL,
            COMPARATOR,
            CYAN_SHULKER_BOX,
            DAMAGED_ANVIL,
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
            ACACIA_BUTTON,
            ACACIA_DOOR,
            BIRCH_BUTTON,
            BIRCH_DOOR,
            DARK_OAK_BUTTON,
            DARK_OAK_DOOR,
            JUNGLE_BUTTON,
            JUNGLE_DOOR,
            LEVER,
            OAK_BUTTON,
            OAK_DOOR,
            SPRUCE_BUTTON,
            SPRUCE_DOOR,
            STONE_BUTTON
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
        FPlayer fPlayer = fPlayers.getByPlayer(breaker);
        if (region.getOccupant() != null) {
            Faction occupant = region.getOccupant();
            if (occupant == bFaction) {
                return;
            }
        }
        Relation rel = owner.getRelation(bFaction);
        if (rel == Relation.ENEMY) {
            Material type = destroyed.getType();
            War war = null;
            for (War w : wars.getByFaction(owner)) {
                for (Faction f : w.getAttacker().getFactions()) {
                    if (f.equals(bFaction)) {
                        war = w;
                        break;
                    }
                }
                for (Faction f : w.getDefender().getFactions()) {
                    if (f.equals(bFaction)) {
                        war = w;
                        break;
                    }
                }
            }
            if (!region.isAttacked() && region.getOccupant() == null) {
                event.setCancelled(true);
                MessageUtil.sendMessage(breaker, "&cDiese Region wird aktuell nicht angegriffen. Greife sie erst mit /f occupy an.");
                return;
            }
            if (event instanceof BlockBreakEvent) {
                if (!WAR_BREAKABLE.contains(type) && !NO_INTERACT.contains(type) && !INTERACTABLE.contains(type)) {
                    event.setCancelled(true);
                    return;
                }
                if (WAR_IMPORTANT.contains(type) && war != null) {
                    war.addPlayerParticipation(breaker, WarPlayerAction.DESTROYED_IMPORTANT_BLOCK);
                } else if (war != null){
                    war.addPlayerParticipation(breaker, WarPlayerAction.GRIEF);
                }
            } else if (event instanceof BlockPlaceEvent) {
                if (!WAR_PLACABLE.contains(type) && !NO_INTERACT.contains(type) && !INTERACTABLE.contains(type)) {
                    event.setCancelled(true);
                }
                if (type.equals(TNT) && war != null) {
                    war.addPlayerParticipation(breaker, WarPlayerAction.PLACED_TNT);
                }
                if (type.equals(CHEST) && breaker.getInventory().getItemInMainHand().getItemMeta() != null &&
                        breaker.getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("Blaupause")
                        && war != null) {
                    war.addPlayerParticipation(breaker, WarPlayerAction.PLACED_SIEGE);
                }
            } else if (event instanceof PlayerBucketEmptyEvent) {
                event.setCancelled(true);
            }
        } else if (!rel.canBuild()) {
            event.setCancelled(true);
            ParsingUtil.sendActionBarMessage(breaker, (destroy ? FMessage.PROTECTION_CANNOT_DESTROY_FACTION : FMessage.PROTECTION_CANNOT_BUILD_FACTION).getMessage(), region.getOwner());
        }
    }

}
