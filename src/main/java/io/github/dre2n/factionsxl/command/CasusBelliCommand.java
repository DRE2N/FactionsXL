/*
 * Copyright (C) 2017 Daniel Saukel
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
package io.github.dre2n.factionsxl.command;

import io.github.dre2n.factionsxl.FactionsXL;
import io.github.dre2n.factionsxl.config.FMessage;
import io.github.dre2n.factionsxl.faction.Faction;
import io.github.dre2n.factionsxl.player.FPermission;
import io.github.dre2n.factionsxl.util.ParsingUtil;
import io.github.dre2n.factionsxl.war.CasusBelli;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * @author Daniel Saukel
 */
public class CasusBelliCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();

    public CasusBelliCommand() {
        setCommand("casusBelli");
        setAliases("cb");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.HELP_CASUS_BELLI.getMessage());
        setPermission(FPermission.CASUS_BELLI.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Faction faction = getSenderFactionOrFromArg(sender, args, 1);
        if (faction == null) {
            return;
        }
        if (!FPermission.hasPermission(sender, FPermission.CASUS_BELLI_OTHERS) && !faction.getMembers().contains(sender) && !faction.isAdmin(sender)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }
        ParsingUtil.sendMessage(sender, FMessage.CMD_CASUS_BELLI_HEAD.getMessage());
        for (CasusBelli cb : faction.getCasusBelli()) {
            String msg = ChatColor.GOLD.toString();
            msg += cb.getTarget().getShortName() + " | ";
            msg += cb.getType() + " | ";
            msg += cb.getExpirationDate();
            ParsingUtil.sendMessage(sender, msg);
        }
    }

}
