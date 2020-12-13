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

package de.erethon.factionsxl.building;

import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.player.FPlayerCache;
import org.bukkit.entity.Player;

public class FTouchHandler implements TouchHandler {

    FactionsXL plugin = FactionsXL.getInstance();

    @Override
    public void onTouch(Player player) {
        FPlayerCache fPlayers = plugin.getFPlayerCache();
        FPlayer fPlayer = fPlayers.getByPlayer(player);
        if (fPlayer.getLastRegion() == null) {
            return;
        }
        for (BuildSite buildSite : fPlayer.getLastRegion().getBuildings()) {
            if (buildSite.isInBuildSite(player)) {
                buildSite.checkProgress();
                buildSite.getBuilding().displayFrame(player, buildSite.getInteractive(), true);
            }
        }
    }
}
