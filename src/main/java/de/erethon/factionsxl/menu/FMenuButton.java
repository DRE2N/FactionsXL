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

import de.erethon.factionsxl.config.FMessage;
import de.erethon.vignette.api.component.InventoryButton;
import de.erethon.vignette.api.component.InventoryButtonBuilder;
import org.bukkit.Material;

public enum FMenuButton {
    
    // Numbers
    ZERO(Material.GRAY_DYE, "0"),
    ONE(Material.GRAY_DYE, "1"),
    TWO(Material.GRAY_DYE, "2"),
    THREE(Material.GRAY_DYE, "3"),
    FOUR(Material.GRAY_DYE, "4"),
    FIVE(Material.GRAY_DYE, "5"),
    SIX(Material.GRAY_DYE, "6"),
    SEVEN(Material.GRAY_DYE, "7"),
    EIGHT(Material.GRAY_DYE, "8"),
    NINE(Material.GRAY_DYE, "9"),

    // Symbols
    DOT(Material.GRAY_DYE, "."),
    ACCEPT(Material.GREEN_DYE, FMessage.MISC_ACCEPT.getMessage()),
    DENY(Material.RED_DYE, FMessage.MISC_DENY.getMessage()),
    UP_ARROW(Material.BLACK_DYE, "+"),
    DOWN_ARROW(Material.WHITE_DYE, "-");
    

    private Material icon;
    private String name;
    private String desc;


    FMenuButton(Material icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    FMenuButton(Material icon, String name, String desc) {
        this.icon = icon;
        this.name = name;
        this.desc = desc;
    }
    
    public InventoryButton getButton() {
        InventoryButton button = new InventoryButtonBuilder()
                .icon(icon)
                .title(name)
                .build();
        if (desc != null) {
            button.addLines(desc);
        }
        return button;
    }

}

