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
import de.erethon.factionsxl.event.DataReloadEvent;
import de.erethon.factionsxl.player.FPermission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;

/**
 * @author Daniel Saukel
 */
public class ReloadCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public ReloadCommand() {
        setCommand("reload");
        setMinArgs(0);
        setMaxArgs(0);
        setHelp(FMessage.HELP_RELOAD.getMessage());
        setPermission(FPermission.RELOAD.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        PluginManager plugins = Bukkit.getPluginManager();

        DataReloadEvent event = new DataReloadEvent();
        plugins.callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        plugin.onDisable();
        plugin.loadCore();

        MessageUtil.sendPluginTag(sender, plugin);
        MessageUtil.sendCenteredMessage(sender, FMessage.CMD_RELOAD_DONE.getMessage());
    }

}
