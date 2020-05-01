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
package de.erethon.factionsxl.chat;

import de.erethon.factionsxl.entity.Relation;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;

/**
 * @author Daniel Saukel
 */
public enum ChatChannel {

    ALLY(ChatColor.LIGHT_PURPLE, Relation.ALLIANCE_2, Relation.ALLIANCE),
    COALITION(ChatColor.RED, Relation.ALLIANCE_2, Relation.ALLIANCE, Relation.COALITION),
    FACTION(ChatColor.GREEN, Relation.ALLIANCE_2),
    LOCAL(ChatColor.GRAY, Relation.ALLIANCE, Relation.COALITION, Relation.PEACE, Relation.ENEMY),
    PUBLIC(ChatColor.WHITE, Relation.ALLIANCE, Relation.COALITION, Relation.PEACE, Relation.ENEMY),
    ROLEPLAY(ChatColor.GRAY, Relation.ALLIANCE, Relation.COALITION, Relation.PEACE, Relation.ENEMY);

    private ChatColor color;
    private List<Relation> relations;

    ChatChannel(ChatColor color, Relation... relations) {
        this.color = color;
        this.relations = Arrays.asList(relations);
    }

    /**
     * @return
     * the related factions that will receive this chat
     */
    public List<Relation> getRelations() {
        return relations;
    }

    /**
     * @return
     * the channel name
     */
    public String getFormatted() {
        return color + toString();
    }

    public static ChatChannel fromString(String string) {
        string = string.toLowerCase();
        switch (string) {
            case "a":
            case "ally":
            case "alliance":
            case "lord":
            case "pu":
            case "personalunion":
            case "vassal":
            case "vassals":
            case "v":
                return ALLY;
            case "c":
            case "coalition":
            case "w":
            case "war":
                return COALITION;
            case "f":
            case "faction":
            case "o":
            case "own":
                return FACTION;
            case "l":
            case "local":
            case "h":
            case "here":
                return LOCAL;
            default:
                return PUBLIC;
        }
    }

}
