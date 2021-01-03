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

package de.erethon.factionsxl.menu.faction;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.vignette.api.InventoryGUI;
import de.erethon.vignette.api.SingleInventoryGUI;
import de.erethon.vignette.api.VignetteAPI;
import de.erethon.vignette.api.component.InventoryButtonBuilder;
import de.erethon.vignette.api.layout.CenteredInventoryLayout;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FMenuRegion {

    FactionsXL plugin = FactionsXL.getInstance();
    private InventoryGUI gui = new SingleInventoryGUI("F MENU REGION");

    public void open(Player p) {
        FPlayer fPlayer = plugin.getFPlayerCache().getByPlayer(p);
        Region rg = fPlayer.getLastRegion();
        Faction faction = rg.getOwner();
        gui.setLayout(new CenteredInventoryLayout(gui, 54));
        gui.setCloseListener(closeEvent -> onClose());
        String owner = FMessage.CMD_REGION_OWNER.getMessage() + (rg.getOwner() != null ? rg.getOwner().getName() : "Wilderness");
        String occupant =FMessage.CMD_REGION_OCCUPIER.getMessage() + (rg.getOccupant() != null ? rg.getOccupant().getName() : "None");
        String influence = FMessage.CMD_REGION_INFLUENCE.getMessage() + rg.getInfluence();
        String type =  FMessage.CMD_REGION_TYPE.getMessage() + rg.getType().getName();
        String pop = FMessage.CMD_REGION_POPULATION.getMessage() + rg.getType().getName();
        String more = "&7&oKlicken für mehr Info...";

        gui.add(new InventoryButtonBuilder()
                .icon(Material.GREEN_CONCRETE)
                .title(rg.getName())
                .lines(owner, occupant, influence, type, pop, more)
                .onInteract(e -> plugin.getServer().dispatchCommand((CommandSender) p, "fxl region"))
                .build()
        );

        VignetteAPI.register(gui);
        gui.open(p);
    }

    public void onClose() {
        VignetteAPI.unregister(gui);
    }
}
