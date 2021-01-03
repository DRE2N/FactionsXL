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
package de.erethon.factionsxl.command.war;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.command.FCommand;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.WarCache;
import de.erethon.factionsxl.war.WarParty;
import de.erethon.factionsxl.war.demand.WarDemandWarPartyGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Malfrador
 */
public class PeaceCommand extends FCommand implements Listener {

    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();
    WarCache wars = plugin.getWarCache();

    public PeaceCommand() {
        setCommand("peace");
        setMinArgs(0);
        setMaxArgs(0);
        setHelp(FMessage.HELP_PEACE.getMessage());
        setPermission(FPermission.WAR.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
        /**Bukkit.getPluginManager().registerEvents(this, plugin);
         gui.setContents(new ItemStack[]{
         GUIButton.PLACEHOLDER,
         GUIButton.PLACEHOLDER,
         GUIButton.PLACEHOLDER,
         create,
         listReceived,
         listSent,
         GUIButton.PLACEHOLDER,
         GUIButton.PLACEHOLDER,
         GUIButton.PLACEHOLDER
         });**/
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        Faction f = factions.getByMember(player);
        boolean inWar = false;
        for (Faction faction : factions.getByLeader(player)) {
            if (faction.isInWar()) {
                inWar = true;
                break;
            }
        }
        if (!factions.getByMember(player).isInWar() && !inWar) {
            MessageUtil.sendMessage(sender, FMessage.ERROR_NOT_IN_WAR.getMessage());
            return;
        }
        if (!f.isAdmin(player)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }
        Set<Faction> ownFactions = factions.getByLeader(player);
        Set<WarParty> parties = new HashSet<>();
        ownFactions.forEach(fc -> f.getWarParties().forEach(p -> parties.add(p.getEnemy())));
        new WarDemandWarPartyGUI(plugin, ownFactions, parties.toArray(new WarParty[]{})).open(player);
        MessageUtil.sendMessage(player, "&6&o&lRIGHT CLICK&8: &f&lDemand");
        MessageUtil.sendMessage(player, "&6&o&lLEFT CLICK&8: &f&lOffer");
    }
}
