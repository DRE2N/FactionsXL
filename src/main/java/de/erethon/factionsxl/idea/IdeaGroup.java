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
package de.erethon.factionsxl.idea;

import de.erethon.commons.gui.GUIButton;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import java.util.ArrayList;
import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel Saukel
 */
public enum IdeaGroup {

    /*CENTRALIZATION(new ItemStack(Material.STRUCTURE_VOID), FMessage.IDEA_GROUP_CENTRALIZATION, CentralizationIdea.class, true),
    DIPLOMACY(new ItemStack(Material.PAPER), FMessage.IDEA_GROUP_DIPLOMACY, DiplomacyIdea.class, true),
    ECONOMY(new ItemStack(Material.IRON_INGOT), FMessage.IDEA_GROUP_ECONOMY, EconomyIdea.class, true),
    MERCENARY(GUIButton.GUI_SWORD, FMessage.IDEA_GROUP_MERCENARY, MercenaryIdea.class, true),
    RELIGION(new ItemStack(Material.TOTEM), FMessage.IDEA_GROUP_RELIGION, ReligionIdea.class, true),*/
    SETTLER(GUIButton.PLAYER_HEAD, FMessage.IDEA_GROUP_SETTLER, SettlerIdea.class, true),/*
    TRADE(new ItemStack(Material.STORAGE_MINECART), FMessage.IDEA_GROUP_TRADE, TradeIdea.class, false)*/;

    private ItemStack icon;
    private FMessage name;
    private Class handler;
    private boolean standard;

    <E extends Enum<E>> IdeaGroup(ItemStack icon, FMessage name, Class<E> handler, boolean standard) {
        this.icon = icon;
        this.name = name;
        this.handler = handler;
        this.standard = standard;
    }

    /* Getters and setters */
    /**
     * @return
     * the inventory GUI icon
     */
    public ItemStack getIcon() {
        return icon.clone();
    }

    /**
     * @return
     * the name of the idea group
     */
    public String getName() {
        return name.getMessage();
    }

    /**
     * @return
     * the name of the idea group
     */
    public <E extends Enum<E>> Class<E> getHandler() {
        return handler;
    }

    /**
     * @return
     * if the idea can be obtained without special conditions
     */
    public boolean isStandard() {
        return standard;
    }

    /* Actions */
    public boolean select(Faction faction) {
        boolean has = faction.getIdeaGroups().contains(this);
        if (has) {
            faction.getIdeaGroups().remove(this);
            ArrayList<Idea> toRemove = new ArrayList<>();
            for (Idea idea : faction.getIdeas()) {
                if (idea.getGroup() == this) {
                    toRemove.add(idea);
                }
            }
            faction.getIdeas().removeAll(toRemove);
            return false;
        } else {
            faction.getIdeaGroups().add(this);
            return true;
        }
    }

    /* Statics */
    /**
     * @param icon
     * the icon to check
     * @return
     * the group that is represented by this icon
     */
    public static IdeaGroup getByIcon(ItemStack icon) {
        for (IdeaGroup group : values()) {
            if (icon.getType() == group.getIcon().getType()) {
                return group;
            }
        }
        return null;
    }

    /**
     * @param name
     * the name to check
     * @return
     * the group that has this name
     */
    public static IdeaGroup getByName(String name) {
        for (IdeaGroup group : values()) {
            if (group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }

    /**
     * @param string
     * the String to check
     * @return
     * An idea
     */
    public static Idea ideaValueOf(String string) {
        for (IdeaGroup group : values()) {
            for (Object ideaObj : group.getHandler().getEnumConstants()) {
                Idea idea = (Idea) ideaObj;
                if (idea.toString().equals(string)) {
                    return idea;
                }
            }
        }
        return null;
    }

    /**
     * @param icon
     * the icon to check
     * @return
     * the idea that is represented by this icon
     */
    public static Idea getIdeaByIcon(ItemStack icon) {
        for (IdeaGroup group : values()) {
            for (Object ideaObj : group.getHandler().getEnumConstants()) {
                Idea idea = (Idea) ideaObj;
                if (idea.getIcon().getType() == icon.getType()) {
                    return idea;
                }
            }
        }
        return null;
    }

    /**
     * @param name
     * the name to check
     * @return
     * the idea that has this name
     */
    public static Idea getIdeaByName(String name) {
        for (IdeaGroup group : values()) {
            for (Object ideaObj : group.getHandler().getEnumConstants()) {
                Idea idea = (Idea) ideaObj;
                if (idea.getName().equals(name)) {
                    return idea;
                }
            }
        }
        return null;
    }

}
