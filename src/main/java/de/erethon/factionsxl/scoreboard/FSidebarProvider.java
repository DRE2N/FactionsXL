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
package de.erethon.factionsxl.scoreboard;

import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.War;

import java.util.List;

/**
 * Adapted from FactionsUUID by drtshock.
 *
 * @author Trent Hensler, Daniel Saukel
 */
public abstract class FSidebarProvider {

    public abstract String getTitle(FPlayer fplayer);

    public abstract List<String> getLines(FPlayer fplayer);

    public String replaceTags(FPlayer fPlayer, String string) {
        if (fPlayer.hasFaction()) {
            string = ParsingUtil.replaceFactionPlaceholders(string, fPlayer.getFaction());
        }
        string = ParsingUtil.replacePlayerPlaceholders(string, fPlayer);
        return string;
    }

    public String replaceTags(Faction faction, FPlayer fPlayer, String string) {
        string = ParsingUtil.replaceFactionPlaceholders(string, faction);
        string = ParsingUtil.replacePlayerPlaceholders(string, fPlayer);
        string = ParsingUtil.replaceRelationPlaceholders(string, fPlayer, faction);
        return string;
    }

    public String replaceTags(War war, FPlayer fPlayer, String string) {
        string = ParsingUtil.replaceWarPlaceholders(string, war, fPlayer);
        string = ParsingUtil.replacePlayerPlaceholders(string, fPlayer);
        return string;
    }

}
