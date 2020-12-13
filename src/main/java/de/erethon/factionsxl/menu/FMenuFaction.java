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

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.vignette.api.component.InventoryButton;
import de.erethon.vignette.api.component.InventoryButtonBuilder;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class FMenuFaction extends FMenu {

    FactionsXL plugin = FactionsXL.getInstance();
    Server server = plugin.getServer();

    Player viewer;
    FPlayer fPlayer;
    Faction faction;

    private InventoryButton home = new InventoryButtonBuilder()
            .icon(Material.COMPASS)
            .title("Home")
            .onInteract(i -> server.dispatchCommand(i.getPlayer(), "fxl home"))
            .build();

    private InventoryButton money = new InventoryButtonBuilder()
            .icon(Material.GOLD_INGOT)
            .title("Money")
            .onInteract(i -> new FNumberInputMenu("Numbah", i.getPlayer()).open(i.getPlayer()))
            .build();

    public FMenuFaction(Player player) {
        super("Faction", player);
        viewer = player;
        fPlayer = plugin.getFPlayerCache().getByPlayer(viewer);
        faction = plugin.getFactionCache().getByFPlayer(fPlayer);
        if (faction == null) {
            MessageUtil.sendMessage(viewer, "&cDu bist in keiner Fraktion.");
            close(viewer);
        }
        fill();
    }

    public void fill() {
        InventoryButton banner = new InventoryButton(faction.getBannerStack());
        banner.setLines(getFactionShort(faction));
        banner.setInteractionListener(i -> server.dispatchCommand(i.getPlayer(), "fxl show " + faction.getId()));
        set(4, banner);
        set(21, home);
        set(22, money);
    }
}
