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

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.player.FPlayerCache;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class TitleCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FPlayerCache fPlayers = plugin.getFPlayerCache();

    public TitleCommand() {
        setCommand("title");
        setMinArgs(2);
        setMaxArgs(2);
        setHelp(FMessage.HELP_TITLE.getMessage());
        setPermission(FPermission.TITLE.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        FPlayer fSender = getFSender(sender);
        if (sender instanceof Player && !fSender.isMod()) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }

        FPlayer fPlayer = fPlayers.getByName(args[1]);
        if (fPlayer == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_PLAYER.getMessage(), args[1]);
            return;
        }
        Faction faction = fPlayer.getFaction();
        if (sender instanceof Player && faction != fSender.getFaction() && !faction.isAdmin(fSender.getUniqueId())) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_PLAYER_NOT_IN_FACTION.getMessage(), args[1], fSender.getFaction().getName());
            return;
        }
        String title = args[2].replace("&", new String());
        fPlayer.setTitle(title);
        fPlayer.getFaction().sendMessage(FMessage.CMD_TITLE_SUCCESS.getMessage(), sender, fPlayer, title);
    }

}
