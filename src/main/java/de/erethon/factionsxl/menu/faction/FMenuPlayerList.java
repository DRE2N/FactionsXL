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

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.menu.FInventoryLayout;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.vignette.api.InventoryGUI;
import de.erethon.vignette.api.SingleInventoryGUI;
import de.erethon.vignette.api.VignetteAPI;
import de.erethon.vignette.api.action.Action;
import de.erethon.vignette.api.action.InteractionEvent;
import de.erethon.vignette.api.component.InventoryButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FMenuPlayerList {

    FactionsXL plugin = FactionsXL.getInstance();
    private final InventoryGUI gui = new SingleInventoryGUI("F MENU PLAYERS");

    public void open(Player p) {
        FPlayer fPlayer = plugin.getFPlayerCache().getByPlayer(p);
        Faction faction = fPlayer.getFaction();
        gui.setLayout(new FInventoryLayout(gui, 54));
        gui.setCloseListener(closeEvent -> onClose());

        for (UUID uuid : faction.getMembers()) {
            FPlayer fp = plugin.getFPlayerCache().getByUniqueId(uuid);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(plugin.getServer().getOfflinePlayer(uuid));
            InventoryButton button = new InventoryButton(head);
            button.setInteractionListener(this::interact);
            button.setTitle(fp.getName());
            List<String> lines = new ArrayList<>();
            lines.add(FMessage.CMD_POWER.getMessage() + ":&7 " + fp.getPower());
            lines.add("&aMod: &7 " + fp.isMod());
            lines.add(" ");
            lines.add("&7Linksklick, um Spieler-Stats anzusehen");
            lines.add("&c&oRechtsklick, um Spieler zu kicken.");
            button.setLines(lines);
            gui.add(button);
        }
        VignetteAPI.register(gui);
        gui.open(p);
    }

    public void interact(InteractionEvent event) {
        MessageUtil.broadcastMessage("Interacted " + event.getAction().toString());
        if (event.getAction() == Action.RIGHT_CLICK) {
            plugin.getServer().dispatchCommand(event.getPlayer(), "fxl kick " + event.getButton().getTitle());
        }
        if (event.getAction() == Action.LEFT_CLICK) {
            plugin.getServer().dispatchCommand(event.getPlayer(), "fxl stats " + event.getButton().getTitle());
        }
    }

    public void onClose() {
        VignetteAPI.unregister(gui);
    }

}
