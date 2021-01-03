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
package de.erethon.factionsxl.gui;

import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.legacygui.GUIButton;
import de.erethon.headlib.HeadLib;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import static de.erethon.factionsxl.legacygui.GUIButton.*;

/**
 * @author Daniel Saukel
 */
public interface StandardizedGUI {

    /* Text Components */
    public static final TextComponent CANCEL = new TextComponent(ChatColor.DARK_RED + FMessage.MISC_CANCEL.getMessage());
    public static final TextComponent CONFIRM = new TextComponent(ChatColor.GREEN + FMessage.MISC_ACCEPT.getMessage());
    public static final TextComponent DENY = new TextComponent(ChatColor.DARK_RED + FMessage.MISC_DENY.getMessage());

    /* Raw skulls */
    public static final ItemStack UP_ALT = HeadLib.setSkullOwner(PLAYER_HEAD, "e4d7b07b-59fc-4f77-b08b-b0446048dcd4", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNThmZTI1MWE0MGU0MTY3ZDM1ZDA4MWMyNzg2OWFjMTUxYWY5NmI2YmQxNmRkMjgzNGQ1ZGM3MjM1ZjQ3NzkxZCJ9fX0=");
    public static final ItemStack DOWN_ALT =  HeadLib.setSkullOwner(PLAYER_HEAD, "ccd469f7-1df1-42f9-8915-15de387906e4", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWI3Y2U2ODNkMDg2OGFhNDM3OGFlYjYwY2FhNWVhODA1OTZiY2ZmZGFiNmI1YWYyZDEyNTk1ODM3YTg0ODUzIn19fQ==");
    public static final ItemStack UP =  HeadLib.setSkullOwner(PLAYER_HEAD, "ff1654b0-10f2-48b6-9c05-483b75f6549e", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDQ4Yjc2OGM2MjM0MzJkZmIyNTlmYjNjMzk3OGU5OGRlYzExMWY3OWRiZDZjZDg4ZjIxMTU1Mzc0YjcwYjNjIn19fQ==");
    public static final ItemStack DOWN =  HeadLib.setSkullOwner(PLAYER_HEAD, "9afa272b-ca4a-4502-8073-c4be1b741ebc", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmRhZGQ3NTVkMDg1MzczNTJiZjdhOTNlM2JiN2RkNGQ3MzMxMjFkMzlmMmZiNjcwNzNjZDQ3MWY1NjExOTRkZCJ9fX0=");
    public static final ItemStack EXCLAMATION =  HeadLib.setSkullOwner(PLAYER_HEAD, "165c8c0f-d1ba-4c9e-9836-cbff5b40ba80", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmE1M2JkZDE1NDU1MzFjOWViYjljNmY4OTViYzU3NjAxMmY2MTgyMGU2ZjQ4OTg4NTk4OGE3ZTg3MDlhM2Y0OCJ9fX0=");
    public static final ItemStack QUESTION =  HeadLib.setSkullOwner(PLAYER_HEAD, "210665a1-0f17-4353-b85a-426e2cb651e2", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTE2M2RhZmFjMWQ5MWE4YzkxZGI1NzZjYWFjNzg0MzM2NzkxYTZlMThkOGY3ZjYyNzc4ZmM0N2JmMTQ2YjYifX19");
    public static final ItemStack E =  HeadLib.setSkullOwner(PLAYER_HEAD, "96eaca3a-0f22-484f-bad7-46153597e191", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJiMjczN2VjYmY5MTBlZmUzYjI2N2RiN2Q0YjMyN2YzNjBhYmM3MzJjNzdiZDBlNGVmZjFkNTEwY2RlZiJ9fX0=");
    public static final ItemStack I =  HeadLib.setSkullOwner(PLAYER_HEAD, "2ab25358-deaa-46b7-b351-d9732a448ff1", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDYxNzhhZDUxZmQ1MmIxOWQwYTM4ODg3MTBiZDkyMDY4ZTkzMzI1MmFhYzZiMTNjNzZlN2U2ZWE1ZDMyMjYifX19");
    public static final ItemStack SOLDIER = HeadLib.setSkullOwner(PLAYER_HEAD, "f59b9e38-8c9d-4917-8d9e-6b5aad457ec5", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjQ1MzAyNzY3NDE0ODUyNzM4MDFlMWIxMWYzYTNmZGNmMGRkMjA2ZjE0NTI1NWRmNGY1YTIwMjAyOGEwNjMifX19");
    public static final ItemStack MAILBOX =  HeadLib.setSkullOwner(PLAYER_HEAD, "244678f1-df93-49b4-916f-47bddd112e80", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIyODE1Yjk5YzEzYmZjNTViNGM1YzI5NTlkMTU3YTYyMzNhYjA2MTg2NDU5MjMzYmMxZTRkNGY3ODc5MmM2OSJ9fX0=");

    /* StandardizedGUI buttons */
    public static final ItemStack CONTINUE = GUIButton.setDisplay(new ItemStack(Material.BARRIER), FMessage.MISC_CONTINUE.getMessage());
    public static final ItemStack DISABLED = GUIButton.setDisplay(new ItemStack(Material.BARRIER), FMessage.ERROR_ECON_DISABLED.getMessage());

    /* Blank items that show meta stuff by default */
    public static final ItemStack GUI_SWORD = Init.GUI_SWORD;
    public static final ItemStack GUI_WATER_BOTTLE = Init.GUI_WATER_BOTTLE;

    static class Init {

        static final ItemStack GUI_SWORD = new ItemStack(Material.IRON_SWORD);
        static final ItemStack GUI_WATER_BOTTLE = new ItemStack(Material.POTION);

        static {
            ItemMeta swordMeta = GUI_SWORD.getItemMeta();
            swordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            GUI_SWORD.setItemMeta(swordMeta);

            PotionMeta watMeta = (PotionMeta) GUI_WATER_BOTTLE.getItemMeta();
            watMeta.setBasePotionData(new PotionData(PotionType.WATER));
            watMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            GUI_WATER_BOTTLE.setItemMeta(watMeta);
        }

    }

    public static void addHeader(Inventory gui) {
        gui.setItem(0, BACK);
        gui.setItem(1, PLACEHOLDER);
        gui.setItem(2, PLACEHOLDER);
        gui.setItem(3, PLACEHOLDER);
        gui.setItem(4, PLACEHOLDER);
        gui.setItem(5, PLACEHOLDER);
        gui.setItem(6, PLACEHOLDER);
        gui.setItem(7, PLACEHOLDER);
        gui.setItem(8, PLACEHOLDER);
    }

    public static void clearHeader(Inventory gui) {
        int i = 9;
        do {
            gui.clear(i);
            i++;
        } while (i < gui.getSize());
    }

    /**
     * Opens the StandardizedGUI to a player
     *
     * @param player the player
     */
    public void open(Player player);

}
