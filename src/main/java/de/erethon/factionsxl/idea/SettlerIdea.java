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
package de.erethon.factionsxl.idea;

import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.economy.Resource;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.legacygui.GUIButton;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Saukel
 */
public enum SettlerIdea implements Idea {

    COLONIZATION(GUIButton.PLAYER_HEAD, FMessage.IDEA_NAME_SETTLER_COLONIZATION, FMessage.IDEA_DESC_SETTLER_COLONIZATION, new HashMap<Resource, Integer>() {
        {
            //put(Resource.COAL, 400);
            put(Resource.GRAVEL, 250);
            //put(Resource.OAK, 200);
            //put(Resource.CLAY, 600);
            //put(Resource.MANPOWER, 10000);
            put(Resource.TAXES, 10000);
        }
    });

    public static final IdeaGroup GROUP = IdeaGroup.SETTLER;

    private ItemStack icon;
    private FMessage name;
    private FMessage description;
    private Map<Resource, Integer> price = new HashMap<>();

    SettlerIdea(ItemStack icon, FMessage name, FMessage description, Map<Resource, Integer> price) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getName() {
        return name.getMessage();
    }

    @Override
    public String[] getDescription() {
        return description.getMessage().split("<br>");
    }

    @Override
    public Map<Resource, Integer> getPrice() {
        return price;
    }

    @Override
    public boolean purchase(Faction faction) {
        boolean canAfford = faction.chargeResource(price);
        if (canAfford) {
            faction.getIdeas().add(this);
        }
        return canAfford;
    }

    @Override
    public IdeaGroup getGroup() {
        return GROUP;
    }

}
