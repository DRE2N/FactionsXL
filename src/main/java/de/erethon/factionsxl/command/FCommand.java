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
package de.erethon.factionsxl.command;

import de.erethon.commons.command.DRECommand;
import de.erethon.commons.misc.NumberUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public abstract class FCommand extends DRECommand {

    FactionsXL plugin = FactionsXL.getInstance();

    protected int i = 1;

    public FPlayer getFSender(CommandSender sender) {
        return plugin.getFPlayerCache().getByName(sender.getName());
    }

    public FPlayer getFSenderOrFromArg(CommandSender sender, String[] args, int index) {
        FPlayer fSender = null;
        if (args.length > index) {
            fSender = plugin.getFPlayerCache().getByName(args[index]);
            if (fSender == null) {
                ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_PLAYER.getMessage(), args[index]);
            }
        } else if (sender instanceof Player) {
            fSender = plugin.getFPlayerCache().getByPlayer((Player) sender);
        } else {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_SPECIFY_PLAYER.getMessage());
        }
        return fSender;
    }

    public Faction getSenderFaction(CommandSender sender) {
        if (getFSender(sender) == null) {
            return null;
        }
        return plugin.getFactionCache().getByFPlayer(getFSender(sender));
    }

    public Faction getSenderFactionOrFromArg(CommandSender sender, String[] args, int index) {
        return getSenderFactionOrFromArg(sender, args, index, false);
    }

    public Faction getSenderFactionOrFromArg(CommandSender sender, String[] args, int index, boolean i) {
        Faction faction = null;
        if (args.length > index) {
            faction = plugin.getFactionCache().getByName(args[index]);
            if (faction == null) {
                int id = NumberUtil.parseInt(args[index], -1);
                if (id != -1) {
                    faction = plugin.getFactionCache().getById(id);
                }
            }
            if (faction != null && i) {
                this.i++;
            }

        }

        if (faction == null && sender instanceof Player) {
            faction = getSenderFaction(sender);
        }

        if (faction == null) {
            if (args.length > index) {
                ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_FACTION.getMessage(), args[index]);
            } else {
                ParsingUtil.sendMessage(sender, FMessage.ERROR_SPECIFY_FACTION.getMessage());
            }
        }
        return faction;
    }

}
