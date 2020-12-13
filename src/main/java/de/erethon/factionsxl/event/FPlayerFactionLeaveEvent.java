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

package de.erethon.factionsxl.event;

import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Gets called when a player leaves a faction, was kicked from it or left the faction to create a vassal (via independence or command).
 * Also gets called when a player gets kicked for inactivity, if this is enabled.
 */
public class FPlayerFactionLeaveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    FPlayer fPlayer;
    Faction faction;

    public FPlayerFactionLeaveEvent(FPlayer fp, Faction f) {
        fPlayer = fp;
        faction = f;
    }

    /**
     *
     * @return the {@link FPlayer} that left the faction
     */
    public FPlayer getFPlayer() {
        return fPlayer;
    }

    /**
     *
     * @return the {@link Faction}
     */
    public Faction getFaction() {
        return faction;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
