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

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.menu.FMainMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MenuCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public MenuCommand() {
        setCommand("menu");
        setHelp(FMessage.HELP_MAIN.getMessage());
        setPermission("fxl.menu");
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        MessageUtil.sendMessage(sender, "Opening GUI...");
        new FMainMenu((Player) sender).open((Player) sender);
    }

}
