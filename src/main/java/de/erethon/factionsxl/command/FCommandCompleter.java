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

import de.erethon.commons.command.DRECommand;
import de.erethon.factionsxl.FactionsXL;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FCommandCompleter implements TabCompleter {

    FactionsXL plugin = FactionsXL.getInstance();
    FCommandCache commands = plugin.getCommandCache();

    // These commands do not show up in tab complete
    private static final List<String> INTERNAL_COMMANDS = new ArrayList<>(Arrays.asList(
            "confirmPeace",
            "confirmWar",
            "confirmWarRequest"
    ));

    // These commands use factions
    private static final List<String> FACTION_COMMANDS = new ArrayList<>(Arrays.asList(
            "s",
            "confirmWar",
            "confirmWarRequest"
    ));

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> cmds = new ArrayList<>();
        for (DRECommand cmd : commands.getCommands()) {
            if (sender.hasPermission(cmd.getPermission()) && !INTERNAL_COMMANDS.contains(cmd.getCommand())) {
                cmds.add(cmd.getCommand());
            }
        }
        List<String> completes = new ArrayList<>();

        if(args.length == 1) {
            for(String string : cmds) {
                if(string.toLowerCase().startsWith(args[0])) completes.add(string);
            }
            return completes;
        }
        return null;
    }
}
