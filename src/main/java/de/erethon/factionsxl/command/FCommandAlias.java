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

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author Daniel Saukel
 */
public class FCommandAlias implements CommandExecutor {

    FCommandCache commands;

    public FCommandAlias(FCommandCache commands) {
        this.commands = commands;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("home")) {
            commands.playerHome.onExecute((String[]) ArrayUtils.add(args, 0, "playerHome"), sender);
        } else if (command.getName().equalsIgnoreCase("setHome")) {
            commands.setPlayerHome.onExecute((String[]) ArrayUtils.add(args, 0, "setHome"), sender);
        }
        return true;
    }

}
