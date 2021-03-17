/*	
 * Written from 2015-2019 by Daniel Saukel	
 *	
 * To the extent possible under law, the author(s) have dedicated all	
 * copyright and related and neighboring rights to this software	
 * to the public domain worldwide.	
 *	
 * This software is distributed without any warranty.	
 *	
 * You should have received a copy of the CC0 Public Domain Dedication	
 * along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.	
 */
package de.erethon.factionsxl.legacygui;

import de.erethon.headlib.HeadLib;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * @author Daniel Saukel
 */
public class GUIButton {

    /* Raw ItemStacks*/
    public static ItemStack BLACK_STAINED_GLASS_PANE = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    public static ItemStack PLAYER_HEAD = new ItemStack(Material.PLAYER_HEAD);

    /* Raw skulls */
    public static final ItemStack LEFT = HeadLib.setSkullOwner(PLAYER_HEAD, "69b9a08d-4e89-4878-8be8-551caeacbf2a",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ViZjkwNzQ5NGE5MzVlOTU1YmZjYWRhYjgxYmVhZmI5MGZiOWJlNDljNzAyNmJhOTdkNzk4ZDVmMWEyMyJ9fX0=");
    public static final ItemStack RIGHT = HeadLib.setSkullOwner(PLAYER_HEAD, "15f49744-9b61-46af-b1c3-71c6261a0d0e",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI2ZjFhMjViNmJjMTk5OTQ2NDcyYWVkYjM3MDUyMjU4NGZmNmY0ZTgzMjIxZTU5NDZiZDJlNDFiNWNhMTNiIn19fQ==");

    /* GUI buttons */
    public static final ItemStack BACK = setDisplay(BLACK_STAINED_GLASS_PANE, ChatColor.GOLD.toString() + ChatColor.BOLD + "&6&lBACK");
    public static final ItemStack NEXT_PAGE = setDisplay(BLACK_STAINED_GLASS_PANE, ChatColor.GOLD.toString() + ChatColor.BOLD + "NEXT PAGE");
    public static final ItemStack PREVIOUS_PAGE = setDisplay(BLACK_STAINED_GLASS_PANE, ChatColor.GOLD.toString() + ChatColor.BOLD + "PREVIOUS PAGE");
    public static final ItemStack PLACEHOLDER = setDisplay(BLACK_STAINED_GLASS_PANE, ChatColor.RESET.toString());
    public static final ItemStack GUI_WATER_BOTTLE = setDisplay(BLACK_STAINED_GLASS_PANE, ChatColor.BLUE.toString() + "WATER");

    public static ItemStack setDisplay(ItemStack itemStack, String name, String... lore) {
        itemStack = itemStack.clone();
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

}
