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
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.player.FPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Malfrador
 */

public class ChatSpyCommand extends FCommand {

    public ChatSpyCommand() {
        setCommand("chatspy");
        setAliases("spy");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.CMD_CHATSPY_HELP.getMessage());
        setPermission("fxl.chatspy");
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player;
        FPlayer fplayer;
        FactionsXL plugin = FactionsXL.getInstance();
        if (!(sender instanceof Player)) {
            return;
        }
        player = (Player) sender;
        fplayer = plugin.getFPlayerCache().getByPlayer(player);
        if (fplayer.getData().getChatSpy()) {
            fplayer.getData().setChatSpy(false);
            MessageUtil.sendMessage(player, FMessage.CMD_CHATSPY_OFF.getMessage());
        }
        else {
            fplayer.getData().setChatSpy(true);
            MessageUtil.sendMessage(player, FMessage.CMD_CHATSPY_ON.getMessage());
        }
    }

}