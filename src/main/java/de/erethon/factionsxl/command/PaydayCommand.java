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

import de.erethon.commons.misc.NumberUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.CoringHandler;
import de.erethon.factionsxl.war.WarHandler;
import org.bukkit.command.CommandSender;

/**
 * @author Daniel Saukel
 */
public class PaydayCommand extends FCommand {

    CoringHandler core =  FactionsXL.getInstance().getCoring();
    WarHandler wh = FactionsXL.getInstance().getWarHandler();

    public PaydayCommand() {
        setCommand("payday");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.HELP_PAYDAY.getMessage());
        setPermission(FPermission.PAYDAY.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        core.calculateCoringProgress();
        core.calculateClaimTime();
        wh.calculateWarStatus();
        wh.updateTruce();
        int i = args.length > 1 ? NumberUtil.parseInt(args[1], 1) : 1;
        do {
            for (Faction faction : FactionsXL.getInstance().getFactionCache().getActive()) {
                faction.getStorage().payday();
            }
            i--;
        } while (i > 0);
    }

}
