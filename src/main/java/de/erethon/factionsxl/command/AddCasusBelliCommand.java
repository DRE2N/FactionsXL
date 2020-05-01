/*
 *
 *  * Copyright (C) 2017-2020 Daniel Saukel, Malfrador
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.erethon.factionsxl.command;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.CasusBelli;
import de.erethon.factionsxl.war.CasusBelli.Type;
import java.util.Date;
import org.bukkit.command.CommandSender;

/**
 * @author Daniel Saukel
 */
public class AddCasusBelliCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public AddCasusBelliCommand() {
        setCommand("addCasusBelli");
        setAliases("addCB", "acb");
        setMinArgs(3);
        setMaxArgs(3);
        setHelp(FMessage.HELP_ADD_CASUS_BELLI.getMessage());
        setPermission(FPermission.ADD_CASUS_BELLI.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Faction subject = plugin.getFactionCache().getByName(args[1]);
        if (subject == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_FACTION.getMessage(), args[1]);
            return;
        }
        Type type = Type.valueOf(args[2]);
        if (type == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_CASUS_BELLI.getMessage(), args[2]);
            return;
        }
        Faction target = plugin.getFactionCache().getByName(args[3]);
        if (target == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_FACTION.getMessage(), args[3]);
            return;
        }
        CasusBelli cb = new CasusBelli(type, target, new Date(System.currentTimeMillis() + FConfig.MONTH));
        subject.getCasusBelli().add(cb);
        ParsingUtil.sendMessage(sender, FMessage.CMD_ADD_CASUS_BELLI_SUCCESS.getMessage(), type.toString(), target.getName(), subject.getName());
    }

}
