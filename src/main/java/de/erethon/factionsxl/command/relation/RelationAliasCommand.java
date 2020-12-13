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
package de.erethon.factionsxl.command.relation;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.command.FCommand;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public abstract class RelationAliasCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();

    public RelationAliasCommand() {
        setMinArgs(1);
        setMaxArgs(2);
        setPermission(FPermission.RELATION.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        if (!(sender instanceof Player) && args.length < 3) {
            displayHelp(sender);
            return;
        }
        Faction faction = sender instanceof Player ? factions.getByMember((Player) sender) : null;
        String subject = args.length == 3 ? args[1] : (faction != null ? faction.getName() : null);
        if (subject == null) {
            ParsingUtil.sendMessage(sender, args.length < 3 ? FMessage.ERROR_SPECIFY_FACTION.getMessage() : FMessage.ERROR_NO_SUCH_FACTION.getMessage(), args[1]);
            return;
        }
        String object = args.length == 3 ? args[2] : args[1];
        plugin.getCommandCache().relation.onExecute(new String[]{plugin.getCommandCache().relation.getCommand(), subject, object, getRelation().toString()}, sender);
    }

    public abstract Relation getRelation();

}
