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
package de.erethon.factionsxl.command.war;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.command.FCommand;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.CasusBelliMenu;
import de.erethon.factionsxl.war.WarParty;
import de.erethon.factionsxl.war.WarPartyRole;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * @author Daniel Saukel
 */

public class WarCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache cache = plugin.getFactionCache();

    public WarCommand() {
        setCommand("war");
        setMinArgs(1);
        setMaxArgs(2);
        setHelp(FMessage.HELP_WAR.getMessage());
        setPermission(FPermission.WAR.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        Faction object = plugin.getFactionCache().getByName(args[1]);
        if (object == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_FACTION.getMessage(), args[1]);
            return;
        }

        if (object.isInvincible()) {
            ParsingUtil.sendMessage(sender, "&cDu kannst diese Fraktion nicht angreifen!");
            return;
        }
        // TO DO!
        if (object.isInWar() && !(args.length == 3 && args[2].equalsIgnoreCase("-unsafe"))) {
           sender.sendMessage("Kriege gegen Fraktionen, die schon im Krieg sind, sind bis auf Weiteres deaktiviert!");
           return;
        }
        WarParty subject = null;
        Set<Faction> factions = plugin.getFactionCache().getByLeader(player);
        Faction f = cache.getByMember(player);
        if (!f.isAdmin(player)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }
        if (f.getStability() <= 20)  {
            MessageUtil.sendMessage(player, "&c Nicht genug Stabilität, um einen Krieg zu erklären.");
            return;
        }
        for (Faction faction : factions) {
            Relation relation = object.getRelation(faction);
            switch (relation) {
                case ALLIANCE:
                case COALITION:
                    ParsingUtil.sendMessage(sender, FMessage.ERROR_CANNOT_ATTACK_ALLIED_FACTION.getMessage());
                    return;
                case PERSONAL_UNION:
                    ParsingUtil.sendMessage(sender, FMessage.ERROR_PERSONAL_UNION_WITH_FACTION.getMessage(), faction, object);
                    return;
                case OWN:
                    ParsingUtil.sendMessage(sender, FMessage.ERROR_OWN_FACTION.getMessage());
                    return;
                case ENEMY:
                    player.performCommand("/fxl warstatus ");
            }
            if (faction.getMembers().contains(player)) {
                subject = new WarParty(faction, WarPartyRole.ATTACKER);
                break;
            }
        }
        if (subject == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_JOIN_FACTION.getMessage());
            return;
        }
        for (Faction faction : factions) {
            subject.addParticipant(faction);
        }
        new CasusBelliMenu().open(player, object);
        ParsingUtil.sendMessage(sender, FMessage.CMD_WAR_SELECTCB.getMessage());
    }

}
