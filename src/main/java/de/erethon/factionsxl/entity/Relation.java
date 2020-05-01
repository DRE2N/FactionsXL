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
package de.erethon.factionsxl.entity;

import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import static org.bukkit.ChatColor.*;

/**
 * An enumeration of relations between factions and players.
 *
 * @author Daniel Saukel
 */
public enum Relation {

    REAL_UNION(DARK_GREEN, true, true, true, FMessage.RELATION_REAL_UNION),
    ALLIANCE(LIGHT_PURPLE, true, true, false, FMessage.RELATION_ALLIANCE),
    ALLIANCE_2(BLUE, true, true, false, FMessage.RELATION_ALLIANCE, ALLIANCE),
    PERSONAL_UNION(YELLOW, true, true, true, FMessage.RELATION_PERSONAL_UNION, ALLIANCE),
    /**
     * vassal faction ===> Relation.LORD ===> lord faction
     */
    LORD(DARK_AQUA, true, true, true, FMessage.RELATION_LORD, ALLIANCE_2),
    OWN(GREEN, true, true, true, FMessage.RELATION_OWN, ALLIANCE_2, PERSONAL_UNION),
    /**
     * lord faction ===> Relation.VASSAL ===> vassal faction
     */
    VASSAL(AQUA, true, true, true, FMessage.RELATION_VASSAL, ALLIANCE_2),
    COALITION(DARK_PURPLE, true, true, false, FMessage.RELATION_COALITION),
    PEACE(WHITE, false, false, false, FMessage.RELATION_PEACE),
    ENEMY(RED, true, false, false, FMessage.RELATION_ENEMY);

    private ChatColor color;
    private boolean build;
    private boolean protection;
    private boolean vassalsOverride;
    private FMessage name;
    private Set<Relation> included = new HashSet<>();

    Relation(ChatColor color, boolean build, boolean protection, boolean vassalsOverride, FMessage name, Relation... included) {
        this.color = color;
        this.build = build;
        this.protection = protection;
        this.vassalsOverride = vassalsOverride;
        this.name = name;
        this.included.addAll(Arrays.asList(included));
    }

    /**
     * @return
     * the chat color
     */
    public ChatColor getColor() {
        return color;
    }

    /**
     * @return
     * if players may build
     */
    public boolean canBuild() {
        return build;
    }

    /**
     * @return
     * if players are protected from PvP
     */
    public boolean isProtected() {
        return protection;
    }

    /**
     * @return
     * true if vassals override this relation to other factions
     * for themselves instead of using the lord's relation
     */
    public boolean doVassalsOverride() {
        return vassalsOverride;
    }

    /**
     * @return
     * the name of the type
     */
    public String getName() {
        return color + name.getMessage();
    }

    /**
     * @return
     * the description
     */
    public String getDescription() {
        return color + ChatColor.BOLD.toString() + name.getMessage() + ChatColor.RESET + "\n" + FMessage.valueOf(name + "_DESC").getMessage();
    }

    /**
     * @return
     * the relations that are included in this relation
     */
    public Set<Relation> getIncludedRelations() {
        return included;
    }

    /**
     * @return
     * the relation formatted with hover description
     */
    public BaseComponent[] getFormatted() {
        BaseComponent[] components = TextComponent.fromLegacyText(getName());
        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(getDescription()));
        for (BaseComponent component : components) {
            component.setHoverEvent(hover);
        }
        return components;
    }

    /**
     * @return
     * the faction name formatted with hover description
     */
    public BaseComponent[] getFormatted(Faction faction) {
        BaseComponent[] components = TextComponent.fromLegacyText(color + faction.getName());
        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(getDescription()));
        ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/factionsxl show " + faction.getId());
        for (BaseComponent component : components) {
            component.setHoverEvent(hover);
            component.setClickEvent(click);
        }
        return components;
    }

    /**
     * @param string
     * the String to check
     * @return
     * the relation
     */
    public static Relation fromString(String string) {
        switch (string.toLowerCase()) {
            case "real_union":
            case "realunion":
            case "ru":
                return REAL_UNION;
            case "personal_union":
            case "personalunion":
            case "pu":
                return PERSONAL_UNION;
            case "vassal":
            case "v":
                return VASSAL;
            case "lord":
            case "l":
                return LORD;
            case "alliance":
            case "ally":
            case "a":
                return ALLIANCE;
            case "coalition":
            case "c":
                return COALITION;
            case "own":
            case "o":
                return OWN;
            case "peace":
            case "p":
            case "neutral":
            case "n":
                return PEACE;
            case "enemy":
            case "e":
                return ENEMY;
            default:
                return null;
        }
    }

}
