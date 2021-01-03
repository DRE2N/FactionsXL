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
package de.erethon.factionsxl.scoreboard.sidebar;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.scoreboard.FSidebarProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Adapted from FactionsUUID by drtshock.
 *
 * @author Trent Hensler, Daniel Saukel
 */
public class FDefaultSidebar extends FSidebarProvider {

    FConfig config = FactionsXL.getInstance().getFConfig();

    @Override
    public String getTitle(FPlayer fplayer) {
        return replaceTags(fplayer, config.getDefaultScoreboardTitle());
    }

    @Override
    public List<String> getLines(FPlayer fplayer) {
        if (fplayer.hasFaction()) {
            return getOutput(fplayer, config.getScoreboardDefault());

        } else if (config.isScoreboardFactionlessEnabled()) {
            return getOutput(fplayer, config.getScoreboardFactionless());
        }

        return getOutput(fplayer, config.getScoreboardDefault()); // no faction, factionless-board disabled
    }

    public List<String> getOutput(FPlayer fplayer, List<String> content) {
        content = new ArrayList<>(content);
        ListIterator<String> it = content.listIterator();
        while (it.hasNext()) {
            it.set(replaceTags(fplayer, it.next()));
        }

        return content;
    }

}
