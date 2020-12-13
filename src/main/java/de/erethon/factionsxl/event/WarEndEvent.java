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

import de.erethon.factionsxl.war.CasusBelli;
import de.erethon.factionsxl.war.WarParty;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event gets fired when a war ends. Can be cancelled.
 */
public class WarEndEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    WarParty attacker;
    WarParty defender;
    CasusBelli casusBelli;
    private boolean cancelled;

    public WarEndEvent(WarParty a, WarParty d, CasusBelli cb) {
        attacker = a;
        defender = d;
        casusBelli = cb;
    }

    /**
     * @return the {@link WarParty} of the attacker
     */
    public WarParty getAttacker() {
        return attacker;
    }

    /**
     * @return the {@link WarParty} of the defender
     */
    public WarParty getDefender() {
        return defender;
    }

    /**
     * @return  the {@link CasusBelli} of the war
     */
    public CasusBelli getCasusBelli() {
        return casusBelli;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
