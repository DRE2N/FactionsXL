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

package de.erethon.factionsxl.economy;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.building.Building;
import de.erethon.factionsxl.building.BuildingManager;
import hu.trigary.advancementcreator.Advancement;
import hu.trigary.advancementcreator.AdvancementFactory;
import hu.trigary.advancementcreator.shared.ItemObject;
import hu.trigary.advancementcreator.trigger.ImpossibleTrigger;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class TechTreeMenu {

    FactionsXL plugin = FactionsXL.getInstance();
    BuildingManager manager = plugin.getBuildingManager();
    AdvancementFactory factory = new AdvancementFactory(plugin, true, false);

    public void load() {
        Advancement root = factory.getRoot("fxl/root", "FXL", "123", Material.PLAYER_HEAD, "block/stone");
        for (Building building : manager.getBuildings()) {
            new Advancement(new NamespacedKey(plugin, "fxl/" + building.getId()), new ItemObject().setItem(Material.STONE),
                    new TextComponent(building.getName()), new TextComponent(building.getDescription().toString()))
                    .addTrigger("dummy", new ImpossibleTrigger())
                    .makeChild(root.getId())
                    .setFrame(Advancement.Frame.GOAL)
                    .activate(false);
        }
        Bukkit.reloadData();
    }

}
