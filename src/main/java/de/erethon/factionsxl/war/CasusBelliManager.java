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

package de.erethon.factionsxl.war;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.faction.Faction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CasusBelliManager {

    public void addClaimCB(Faction faction, Faction target) {
        int claims = 0;
        for (Region rg : target.getRegions()) {
            if (rg.getClaimFactions().containsKey(faction)) {
                claims++;
            }
        }
        if (claims > (target.getRegions().size() / 2)) {
            faction.getCasusBelli().add(new CasusBelli(CasusBelli.Type.SUBJAGATION, target, new Date(System.currentTimeMillis() + FConfig.MONTH)));
            faction.getCasusBelli().removeIf(cb -> (cb.getType().equals(CasusBelli.Type.CONQUEST)) && cb.getTarget().equals(target));
        } else {
            faction.getCasusBelli().add(new CasusBelli(CasusBelli.Type.CONQUEST, target, new Date(System.currentTimeMillis() + FConfig.MONTH)));
        }
    }

    public boolean hasCBAlready(Faction self, CasusBelli.Type type, Faction target) {
        for (CasusBelli casusBelli  : self.getCasusBelli()) {
            if (casusBelli.getTarget() == target && casusBelli.getType() == type) {
                return true;
            }
        }
        return false;
    }

    public void updateBorderFriction(Region region, Faction owner, Faction target) {
        FactionsXL plugin = FactionsXL.getInstance();
        if (plugin.getBoard().doShareBorder(owner, target)) {
            return;
        }
        List<CasusBelli> toRemove = new ArrayList<>();
        for (CasusBelli casusBelli : owner.getCasusBelli()) {
            if (casusBelli.getTarget() == target && casusBelli.getType() == CasusBelli.Type.BORDER_FRICTION) {
                if (plugin.getBoard().getBorderRegions(owner, target).isEmpty()) {
                    MessageUtil.log("Removing CB " + casusBelli.toString() + " from " + owner.getName() + " as its no longer valid.");
                    toRemove.add(casusBelli);
                }
            }
        }
        owner.getCasusBelli().removeAll(toRemove);

    }
}
