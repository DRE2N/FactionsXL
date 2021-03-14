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

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.misc.EnumUtil;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.FDebugLevel;
import de.erethon.factionsxl.util.ParsingUtil;
import org.apache.commons.lang.enums.EnumUtils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DebugCommand extends FCommand{

    public DebugCommand() {
        setCommand("debug");
        setAliases("dbg");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp("Internal debug command.");
        setPermission(FPermission.WORLD.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }
    @Override
    public void onExecute(String[] args, CommandSender commandSender) {
        if (args.length == 1) {
            List<String> levels = plugin.getDebugLevels().stream().map(Enum::toString).collect(Collectors.toList());
            MessageUtil.sendMessage(commandSender, "&aCurrent levels: &6" + ParsingUtil.collectionToString(levels));
        }
        if (args.length == 2) {
            List<String> levels = Arrays.stream(FDebugLevel.values()).map(Enum::toString).collect(Collectors.toList());
            if (!EnumUtil.isValidEnum(FDebugLevel.class, args[1].toUpperCase())) {
                MessageUtil.sendMessage(commandSender, "&cInvalid debug level. Debug levels: " + levels);
                return;
            }
            FDebugLevel newLevel = FDebugLevel.valueOf(args[1].toUpperCase());
            if (plugin.getDebugLevels().contains(newLevel)) {
                plugin.getDebugLevels().remove(newLevel);
                MessageUtil.sendMessage(commandSender, "&aRemoved debug level &6" + newLevel + "&a.");
                return;
            }
            plugin.getDebugLevels().add(newLevel);
            MessageUtil.sendMessage(commandSender, "&aAdded debug level &6" + newLevel + "&a.");
        }
    }
}
