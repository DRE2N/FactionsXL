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

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum FSetTag {

    WALL(MaterialSetTag.STONE_BRICKS.getValues(), MaterialTags.COBBLESTONES.getValues(), MaterialSetTag.LOGS.getValues(), MaterialTags.SANDSTONES.getValues(), MaterialTags.RED_SANDSTONES.getValues(), MaterialSetTag.PLANKS.getValues(),
            new HashSet<>(Arrays.asList(Material.BRICKS, Material.STONE, Material.ANDESITE, Material.POLISHED_ANDESITE, Material.DIORITE, Material.POLISHED_DIORITE, Material.GRANITE, Material.POLISHED_GRANITE))),
    WINDOW(MaterialTags.GLASS.getValues(), MaterialTags.GLASS_PANES.getValues()),
    DOORS(MaterialSetTag.DOORS.getValues()),
    CROPS(MaterialSetTag.CROPS.getValues()),
    FENCES(MaterialSetTag.FENCES.getValues(),MaterialSetTag.FENCE_GATES.getValues(), MaterialSetTag.WALLS.getValues()),
    FLOWERS(MaterialSetTag.FLOWERS.getValues()),
    RURAL_WINDOW(MaterialSetTag.WOODEN_FENCES.getValues()),
    ROOF(MaterialSetTag.STAIRS.getValues(), MaterialSetTag.SLABS.getValues()),
    FURNITURE(MaterialSetTag.STAIRS.getValues(), MaterialTags.LANTERNS.getValues(), MaterialTags.TRAPDOORS.getValues(), MaterialTags.TORCHES.getValues(), MaterialSetTag.SIGNS.getValues(), MaterialSetTag.FLOWER_POTS.getValues(), MaterialSetTag.BANNERS.getValues(),
            MaterialSetTag.CARPETS.getValues(), MaterialSetTag.BEDS.getValues(), new HashSet<>(Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.CRAFTING_TABLE, Material.FURNACE, Material.BLAST_FURNACE, Material.BARREL, Material.SMOKER,
            Material.DISPENSER, Material.DROPPER, Material.ENCHANTING_TABLE, Material.BREWING_STAND, Material.BEACON, Material.ANVIL, Material.GRINDSTONE, Material.CARTOGRAPHY_TABLE, Material.LOOM, Material.SMITHING_TABLE, Material.LADDER, Material.STONECUTTER,
            Material.BELL, Material.NOTE_BLOCK, Material.REDSTONE_LAMP, Material.LECTERN, Material.JUKEBOX, Material.END_ROD, Material.FLETCHING_TABLE))),
    WOOD_FARM_STUFF(MaterialSetTag.SAPLINGS.getValues(), MaterialSetTag.LEAVES.getValues());

    Set<Material> materialSetTags = new HashSet<>();

    @SafeVarargs // Shouldn't be able to cause heap pollution
    FSetTag(Set<Material>... tags) {
        for (Set<Material> set : tags) {
            materialSetTags.addAll(set);
        }
    }


    public Set<Material> getMaterials() {
        return materialSetTags;
    }

    public boolean hasBlock(Material block) {
        return materialSetTags.contains(block);
    }
}
