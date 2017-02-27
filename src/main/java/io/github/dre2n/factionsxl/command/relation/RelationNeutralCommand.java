/*
 * Copyright (C) 2017 Daniel Saukel
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
package io.github.dre2n.factionsxl.command.relation;

import io.github.dre2n.commons.command.BRCommand;
import io.github.dre2n.factionsxl.FactionsXL;
import io.github.dre2n.factionsxl.config.FMessage;
import io.github.dre2n.factionsxl.faction.Faction;
import io.github.dre2n.factionsxl.faction.FactionCache;
import io.github.dre2n.factionsxl.faction.Relation;
import io.github.dre2n.factionsxl.player.FPermission;
import io.github.dre2n.factionsxl.util.ParsingUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class RelationNeutralCommand extends BRCommand {

    FactionCache factions = FactionsXL.getInstance().getFactionCache();

    public RelationNeutralCommand() {
        setCommand("neutral");
        setMinArgs(1);
        setMaxArgs(2);
        setHelp(FMessage.HELP_CMD_NEUTRAL.getMessage());
        setPermission(FPermission.RELATION.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        if (!(sender instanceof Player) && args.length < 3) {
            displayHelp(sender);
            return;
        }

        Faction faction = sender instanceof Player ? factions.getByMember((Player) sender) : null;
        Faction subject = args.length == 3 ? factions.getByName(args[1]) : (faction != null ? faction : null);
        if (subject == null) {
            ParsingUtil.sendMessage(sender, args.length < 3 ? FMessage.ERROR_SPECIFY_FACTION.getMessage() : FMessage.ERROR_NO_SUCH_FACTION.getMessage(), args[1]);
            return;
        }

        Faction object = args.length == 3 ? factions.getByName(args[2]) : factions.getByName(args[1]);
        if (object == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_FACTION.getMessage(), args.length == 3 ? args[2] : args[1]);
            return;
        }

        switch (subject.getRelation(object)) {
            case LORD:
                ParsingUtil.sendMessage(sender, FMessage.ERROR_VASSAL.getMessage(), subject);
                return;
            case OWN:
                ParsingUtil.sendMessage(sender, FMessage.ERROR_OWN_FACTION.getMessage(), subject, object);
                return;
            case PERSONAL_UNION:
                ParsingUtil.sendMessage(sender, FMessage.ERROR_PERSONAL_UNION_WITH_FACTION.getMessage(), subject, object);
                return;
            default:
                subject.getRelations().remove(object);
                object.getRelations().remove(subject);
                ParsingUtil.broadcastMessage(FMessage.RELATION_CONFIRMED.getMessage(), subject, object, Relation.PEACE.getName());
        }
    }

}