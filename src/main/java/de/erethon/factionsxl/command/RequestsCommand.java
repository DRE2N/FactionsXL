/*
 *
 *  * Copyright (C) 2017-2020 Daniel Saukel, Malfrador
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.erethon.factionsxl.command;

import de.erethon.commons.gui.PageGUI;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Request;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * @author Daniel Saukel
 */
public class RequestsCommand extends FCommand implements Listener {

    private Map<FPlayer, List<Request>> requests = new HashMap<>();

    public RequestsCommand() {
        setCommand("requests");
        setAliases("req");
        setMinArgs(0);
        setMaxArgs(0);
        setHelp(FMessage.HELP_REQUESTS.getMessage());
        setPermission(FPermission.REQUESTS.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        PageGUI gui = new PageGUI(FMessage.CMD_REQUESTS_TITLE.getMessage());
        Player player = (Player) sender;
        FPlayer fPlayer = getFSender(sender);
        List<Request> reqs = fPlayer.getAccessibleRequests();
        requests.put(fPlayer, reqs);
        reqs.forEach(r -> gui.addButton(r.getButton(player)));
        gui.open(player, 0);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        FPlayer fPlayer = plugin.getFPlayerCache().getByPlayer(player);
        int slot = event.getSlot();
        if (slot < 0 || slot > 44) {
            return;
        }
        PageGUI gui = PageGUI.getByInventory(event.getInventory());
        if (gui == null) {
            return;
        }
        int page = gui.getPages().indexOf(event.getClickedInventory());
        int i = slot + 45 * page;
        List<Request> reqs = requests.get(fPlayer);
        if (reqs == null || i >= reqs.size() || i < 0) {
            return;
        }
        Request req = reqs.get(i);
        player.closeInventory();
        requests.remove(fPlayer);
        req.send(false);
    }

}
