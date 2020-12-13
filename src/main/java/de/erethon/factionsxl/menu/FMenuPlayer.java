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

import de.erethon.vignette.api.InventoryGUI;
import de.erethon.vignette.api.SingleInventoryGUI;
import de.erethon.vignette.api.VignetteAPI;
import de.erethon.vignette.api.component.InventoryButtonBuilder;
import de.erethon.vignette.api.layout.FlowInventoryLayout;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FMenuPlayer {

    private InventoryGUI gui = new SingleInventoryGUI("F MENU FACTION");

    public void open(Player p) {
        gui.setLayout(new FlowInventoryLayout(gui, 54));
        gui.setCloseListener(closeEvent -> onClose());
        gui.add(new InventoryButtonBuilder()
                .icon(Material.PLAYER_HEAD)
                .onInteract(e -> e.getPlayer().sendMessage("Opened"))
                .build()
        );
        VignetteAPI.register(gui);
        gui.open(p);
    }

    public void onClose() {
        VignetteAPI.unregister(gui);
    }

}
