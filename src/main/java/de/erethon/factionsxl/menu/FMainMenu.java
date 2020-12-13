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

package de.erethon.factionsxl.menu;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.vignette.api.component.InventoryButton;
import de.erethon.vignette.api.component.InventoryButtonBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FMainMenu extends FMenu {

    FactionsXL plugin = FactionsXL.getInstance();

    // TODO: Should be configurable
    private InventoryButton websiteButton = new InventoryButtonBuilder()
            .icon(Material.BOOK)
            .title(ChatColor.GOLD + "Website")
            .onInteract(e -> plugin.getServer().dispatchCommand(e.getPlayer(), "website"))
            .build();
    private InventoryButton mapButton = new InventoryButtonBuilder()
            .icon(Material.BOOK)
            .title(ChatColor.GOLD + "Livemap")
            .onInteract(e -> plugin.getServer().dispatchCommand(e.getPlayer(), "map"))
            .build();
    private InventoryButton wikiButton = new InventoryButtonBuilder()
            .icon(Material.BOOK)
            .title(ChatColor.GOLD + "Wiki")
            .onInteract(e -> plugin.getServer().dispatchCommand(e.getPlayer(), "wiki"))
            .build();
    private InventoryButton listButton = new InventoryButtonBuilder()
            .icon(Material.BOOK)
            .title(ChatColor.GOLD + "Fraktionen")
            .onInteract(e -> plugin.getServer().dispatchCommand(e.getPlayer(), "f list"))
            .build();

    public FMainMenu(Player player) {
        super("Menu", player);
        set(11, websiteButton);
        set(12, mapButton);
        set(13, wikiButton);
        set(14, listButton);
        getBackButton().setInteractionListener(null);
    }

}
