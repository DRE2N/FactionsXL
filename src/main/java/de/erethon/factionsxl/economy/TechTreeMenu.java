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

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.building.Building;
import de.erethon.factionsxl.building.BuildingManager;
import hu.trigary.advancementcreator.Advancement;
import hu.trigary.advancementcreator.AdvancementFactory;
import hu.trigary.advancementcreator.shared.ItemObject;
import hu.trigary.advancementcreator.trigger.ImpossibleTrigger;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class TechTreeMenu {

    FactionsXL plugin = FactionsXL.getInstance();
    BuildingManager manager = plugin.getBuildingManager();
    AdvancementFactory factory = new AdvancementFactory(plugin, true, false);
    Set<NamespacedKey> addLater = new HashSet<>();
    Set<NamespacedKey> loaded = new HashSet<>();
    Advancement root;
    Advancement root2;
    boolean addedAll = false;

    public void load() {
        Bukkit.reloadData();
        try {
            root = factory.getRoot("factionbuilding/root", "Faction Buildings", "123", Material.PLAYER_HEAD, "block/lime_glazed_terracotta");
            root2 = factory.getRoot("regionbuilding/root", "Region Buildings", "123", Material.PLAYER_HEAD, "block/blue_glazed_terracotta");
        } catch (Exception ignored) { }

        addAdvancements();
    }

    public void addAdvancements() {
        Set<Building> factionBuildings = new HashSet<>();
        Set<Building> regionBuildings = new HashSet<>();
        for (Building building : manager.getBuildings()) {
            if (building.isFactionBuilding()) {
                factionBuildings.add(building);
            } else {
                regionBuildings.add(building);
            }
        }
        loaded.add(root.getId());
        for (Building building : factionBuildings) {
            NamespacedKey parent;
            if (building == null) {
                continue;
            }
            boolean hasReqs = false;
            NamespacedKey key = new NamespacedKey(plugin, "factionbuilding/" + building.getId());
            if (Bukkit.getAdvancement(key) != null) {
                continue;
            }
            if (building.getRequiredBuildings() == null || building.getRequiredBuildings().isEmpty() || building.getRequiredBuildings().get(0) == null || manager.getByID(building.getRequiredBuildings().get(0)) == null) {
                parent = root.getId();
            } else {
                parent = new NamespacedKey(plugin, "factionbuilding/" + manager.getByID(building.getRequiredBuildings().get(0)).getId());
                hasReqs = true;
            }
            if (hasReqs && Bukkit.getAdvancement(parent) == null) {
                addLater.add(key);
                MessageUtil.log("Required advancement not yet loaded. Added " + key.getKey() + " to queue.");
                continue;
            }
            Material icon = building.getIcon();
            if (icon == null) {
                icon = Material.BARRIER;
            }
            TextComponent description = new TextComponent();
            for (String line : building.getDescription()) {
                TextComponent tmp = new TextComponent(MessageUtil.parse(line));
                tmp.addExtra("\n");
                description.addExtra(tmp);
            }
            new Advancement(key, new ItemObject().setItem(icon),
                    new TextComponent(MessageUtil.parse(building.getName()  + "          <reset>")), description)
                    .addTrigger("dummy", new ImpossibleTrigger())
                    .makeChild(parent)
                    .setFrame(Advancement.Frame.GOAL)
                    .setHidden(false)
                    .activate(false);
            MessageUtil.log("Added: " + building.getId() + " (Parent: " + parent.getKey() + ")");
            loaded.add(key);
            addLater.remove(key);
        }

        loaded.add(root2.getId());
        for (Building building : regionBuildings) {
            NamespacedKey parent;
            if (building == null) {
                continue;
            }
            boolean hasReqs = false;
            NamespacedKey key = new NamespacedKey(plugin, "regionbuilding/" + building.getId());
            if (Bukkit.getAdvancement(key) != null) {
                continue;
            }
            if (building.getRequiredBuildings() == null || building.getRequiredBuildings().isEmpty() || building.getRequiredBuildings().get(0) == null || manager.getByID(building.getRequiredBuildings().get(0)) == null) {
                parent = root2.getId();
            } else {
                parent = new NamespacedKey(plugin, "regionbuilding/" + manager.getByID(building.getRequiredBuildings().get(0)).getId());
                hasReqs = true;
            }
            if (hasReqs && Bukkit.getAdvancement(parent) == null) {
                addLater.add(key);
                MessageUtil.log("Required advancement not yet loaded. Added " + key.getKey() + " to queue.");
                continue;
            }
            Material icon = building.getIcon();
            if (icon == null) {
                icon = Material.BARRIER;
            }
            TextComponent description = new TextComponent();
            for (String line : building.getDescription()) {
                TextComponent tmp = new TextComponent(MessageUtil.parse(line));
                tmp.addExtra("\n");
                description.addExtra(tmp);
            }
            new Advancement(key, new ItemObject().setItem(icon),
                    new TextComponent(MessageUtil.parse(building.getName() + "          <reset>")), description)
                    .addTrigger("dummy", new ImpossibleTrigger())
                    .makeChild(parent)
                    .setFrame(Advancement.Frame.GOAL)
                    .setHidden(false)
                    .activate(false);
            MessageUtil.log("Added: " + building.getId() + " (Parent: " + parent.getKey() + ")");

            addLater.remove(key);
        }
        Bukkit.reloadData();
        if (addLater.isEmpty()) {
            addedAll = true;
            MessageUtil.log("Tree successfully constructed.");
        } else {
            addAdvancements();
        }
    }

    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        MessageUtil.log("Deleting " + directoryToBeDeleted.getName());
        return directoryToBeDeleted.delete();
    }

}
