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

package de.erethon.factionsxl.integrations;

import de.erethon.factionsxl.event.FPlayerFactionJoinEvent;
import de.erethon.factionsxl.event.FPlayerFactionLeaveEvent;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.concurrent.CompletableFuture;

public class LuckPermsIntegration implements Listener {

    LuckPerms luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms.class).getProvider();
    UserManager userManager = luckPerms.getUserManager();
    GroupManager groupManager = luckPerms.getGroupManager();

    @EventHandler
    public void joinEvent(FPlayerFactionJoinEvent event) {
        String factionGroupID = "faction_" + event.getFaction().getId();
        Group factionGroup = groupManager.getGroup(factionGroupID);
        if (factionGroup == null) {
            groupManager.createAndLoadGroup(factionGroupID);
            groupManager.getGroup(factionGroupID);
        }
        CompletableFuture<User> userFuture = userManager.loadUser(event.getFPlayer().getUniqueId());
        userFuture.thenAcceptAsync(user -> {
            DataMutateResult result = user.data().add(Node.builder("group." + factionGroupID).build());
            userManager.saveUser(user);
        });
    }

    @EventHandler
    public void leaveEvent(FPlayerFactionLeaveEvent event) {
        String factionGroupID = "faction_" + event.getFaction().getId();
        CompletableFuture<User> userFuture = userManager.loadUser(event.getFPlayer().getUniqueId());
        userFuture.thenAcceptAsync(user -> {
            DataMutateResult result = user.data().remove(Node.builder("group." + factionGroupID).build());
            userManager.saveUser(user);
        });
    }
}
