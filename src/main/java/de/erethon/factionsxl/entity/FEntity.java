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
package de.erethon.factionsxl.entity;

import de.erethon.commons.player.PlayerCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Saukel
 */
public interface FEntity {

    /**
     * @return
     * the name of the entity
     */
    public String getName();

    /**
     * @param object
     * another FEntity
     * @return
     * the relation of this faction to the other one
     */
    public Relation getRelation(FEntity object);

    /**
     * @return
     * true if the faction is in war with the object
     */
    public boolean isInWar(FEntity object);

    /**
     * @param type
     * the type of the request
     * @return
     * a collection of players that may accept the request
     */
    public PlayerCollection getRequestAuthorizedPlayers(Class<? extends Request> type);

    /**
     * @return
     * a collection of requests
     */
    public List<Request> getRequests();

    /**
     * @return
     * a collection of requests of the respective type
     */
    public default <R extends Request> List<R> getRequests(Class<R> type) {
        List<R> requests = new ArrayList<>();
        for (Request request : getRequests()) {
            if (type.isInstance(request)) {
                requests.add((R) request);
            }
        }
        return requests;
    }

}
