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
package de.erethon.factionsxl.economy;

import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.legacygui.GUIButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static de.erethon.factionsxl.economy.Resource.*;

/**
 * @author Daniel Saukel
 */
public enum ResourceSubcategory {

    // BUILDING
    WOOD(FMessage.RESOURCE_SUBCATEGORY_WOOD, OAK.getIconType(), true, ACACIA, BIRCH, DARK_OAK, JUNGLE, OAK, SPRUCE),
    GLASS(FMessage.RESOURCE_SUBCATEGORY_GLASS, Resource.GLASS.getIconType(), false, Resource.GLASS, QUARTZ, OBSIDIAN),
    ROADS(FMessage.RESOURCE_SUBCATEGORY_ROADS, GRAVEL.getIconType(), true, GRAVEL, Resource.STONE),
    STONE(FMessage.RESOURCE_SUBCATEGORY_STONE, Resource.STONE.getIconType(), false, ANDESITE, DIORITE, GRANITE, Resource.STONE, PURPUR, RED_SANDSTONE, YELLOW_SANDSTONE),
    // CLOTHES
    DYE(FMessage.RESOURCE_SUBCATEGORY_DYE, LAPIS_LAZULI.getIconType(), false, LAPIS_LAZULI, BEETROOT, INK, CACTUS),
    FUR(FMessage.RESOURCE_SUBCATEGORY_FUR, SHEEP.getIconType(), false, RABBIT, SHEEP),
    LEATHER(FMessage.RESOURCE_SUBCATEGORY_LEATHER, Material.LEATHER, true, COW, HORSE, PIG, SHEEP),
    // FOOD
    MEAT(FMessage.RESOURCE_SUBCATEGORY_MEAT, PUFFERFISH.getIconType(), false, CHICKEN, COW, HORSE, PIG, RABBIT, SHEEP, CLOWNFISH, PUFFERFISH),
    STAPLES(FMessage.RESOURCE_SUBCATEGORY_STAPLES, WHEAT.getIconType(), true, APPLE, BEETROOT, CARROT, MELON, POTATO, PUMPKIN, WHEAT, MUSHROOMS, CODFISH, SALMON),
    SPICES(FMessage.RESOURCE_SUBCATEGORY_SPICES, SALT.getIconType(), false, COCOA, SUGAR, SALT, NETHER_WART),
    WATER(FMessage.RESOURCE_SUBCATEGORY_WATER, Resource.WATER.getIconType(), true, Resource.WATER),
    // HEATING
    HEATING(FMessage.RESOURCE_SUBCATEGORY_HEATING, COAL.getIconType(), true, COAL, ACACIA, BIRCH, DARK_OAK, JUNGLE, OAK, SPRUCE),
    // JEWELRY
    JEWELRY(FMessage.RESOURCE_SUBCATEGORY_JEWELRY, DIAMOND.getIconType(), false, GOLD, DIAMOND, EMERALD, LAPIS_LAZULI, QUARTZ, REDSTONE, OBSIDIAN, PRISMARINE),
    // MILITARY
    SMITHERY(FMessage.RESOURCE_SUBCATEGORY_SMITHERY, IRON.getIconType(), false, GOLD, IRON),
    CANNONING(FMessage.RESOURCE_SUBCATEGORY_CANNONING, SULPHUR.getIconType(), false, IRON, SULPHUR),
    RIDERS(FMessage.RESOURCE_SUBCATEGORY_RIDERS, HORSE.getIconType(), false, HORSE),
    // SCIENCE
    ALCHEMY(FMessage.RESOURCE_SUBCATEGORY_ALCHEMY, REDSTONE.getIconType(), false, SULPHUR, GOLD, REDSTONE, Resource.WATER),
    LITERATURE(FMessage.RESOURCE_SUBCATEGORY_LITERATURE, Material.WRITABLE_BOOK, false, INK, PAPER);

    private FMessage name;
    private Material icon;
    private boolean basic;
    private Resource[] resources;

    ResourceSubcategory(FMessage name, Material icon, boolean basic, Resource... resources) {
        this.name = name;
        this.icon = icon;
        this.basic = basic;
        this.resources = resources;
    }

    public ItemStack getIcon() {
        return this == WATER ? GUIButton.GUI_WATER_BOTTLE.clone() : new ItemStack(icon);
    }

    public String getName() {
        return name.getMessage();
    }

    public Resource[] getResources() {
        return resources;
    }

    /**
     * Basic resources are those that the people definitely need in order to stay alive.
     *
     * @return
     * if the resources are basic
     */
    public boolean isBasic() {
        return basic;
    }

    /* Statics */
    /**
     * @param icon
     * the icon to check
     * @return
     * the subcategory that is represented by this icon
     */
    public static ResourceSubcategory getByIcon(ItemStack icon) {
        for (ResourceSubcategory category : values()) {
            if (icon.getType() == category.getIcon().getType() && icon.getDurability() == category.getIcon().getDurability()) {
                return category;
            }
        }
        return null;
    }

}
