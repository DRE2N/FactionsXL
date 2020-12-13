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
package de.erethon.factionsxl.command;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class SetColorCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public static final String RRGGBB = "#[0-9A-F]{6}";

    public SetColorCommand() {
        setCommand("setColor");
        setMinArgs(2);
        setMaxArgs(4);
        setHelp(FMessage.HELP_SET_COLOR.getMessage());
        setPermission(FPermission.SET_COLOR.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        i = 1;
        Faction faction = getSenderFactionOrFromArg(sender, args, 1, true);
        if (faction == null) {
            return;
        }

        if (!faction.isAdmin(sender) && !(sender instanceof Player)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }

        if (i == 2 && args.length < 4) {
            displayHelp(sender);
            return;
        }

        String fill = args[i];
        if (!fill.startsWith("#")) {
            fill = "#" + fill;
        }
        String line = args[i + 1];
        if (!line.startsWith("#")) {
            line = "#" + line;
        }
        if (!fill.matches(RRGGBB) || !line.matches(RRGGBB)) {
            displayHelp(sender);
            return;
        }
        String icon = null;
        if (args.length == i + 3) {
            icon = args[i + 2];
        }
        faction.setMapStyle(fill, line, icon);
        faction.sendMessage(FMessage.CMD_SET_COLOR_SUCCESS.getMessage(), sender, fill, line);
    }

}
