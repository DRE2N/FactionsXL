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
package de.erethon.factionsxl.scoreboard;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.util.ParsingUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Adapted from FactionsUUID by drtshock.
 *
 * @author Trent Hensler, Daniel Saukel
 */
public class FTeamWrapper {

    FactionsXL plugin = FactionsXL.getInstance();

    private static final Map<Faction, FTeamWrapper> wrappers = new HashMap<>();
    private static final List<FScoreboard> tracking = new ArrayList<>();
    private static int factionTeamPtr;
    private static final Set<Faction> updating = new HashSet<>();

    private final Map<FScoreboard, Team> teams = new HashMap<>();
    private final String teamName;
    private final Faction faction;
    private final Set<OfflinePlayer> members = new HashSet<>();

    public static void applyUpdatesLater(final Faction faction) {
        FactionsXL plugin = FactionsXL.getInstance();

        if (!plugin.getFConfig().getDefaultScoreboardPrefixes()) {
            return;
        }

        if (updating.add(faction)) {
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    updating.remove(faction);
                    applyUpdates(faction);
                }
            });
        }
    }

    public static void applyUpdates(Faction faction) {
        FactionsXL plugin = FactionsXL.getInstance();

        if (!plugin.getFConfig().getDefaultScoreboardPrefixes()) {
            return;
        }

        if (updating.contains(faction)) {
            // Faction will be updated soon.
            return;
        }

        FTeamWrapper wrapper = wrappers.get(faction);
        Collection<OfflinePlayer> factionMembers = faction.getMembers().getOfflinePlayers();

        if (wrapper != null && !faction.isActive()) {
            // Faction was disbanded
            wrapper.unregister();
            wrappers.remove(faction);
            return;
        } else if (!faction.isActive()) {
            return;
        }

        if (wrapper == null) {
            wrapper = new FTeamWrapper(faction);
            wrappers.put(faction, wrapper);
        }

        for (OfflinePlayer player : wrapper.getPlayers()) {
            if (!player.isOnline() || !factionMembers.contains(player)) {
                // Player is offline or no longer in faction
                wrapper.removePlayer(player);
            }
        }

        for (OfflinePlayer fmember : factionMembers) {
            if (!fmember.isOnline()) {
                continue;
            }

            // Scoreboard might not have player; add him/her
            wrapper.addPlayer(fmember);
        }

        wrapper.updatePrefixes();
    }

    public static void updatePrefixes(Faction faction) {
        if (!wrappers.containsKey(faction)) {
            applyUpdates(faction);
        } else {
            wrappers.get(faction).updatePrefixes();
        }
    }

    protected static void track(FScoreboard fboard) {
        tracking.add(fboard);
        for (FTeamWrapper wrapper : wrappers.values()) {
            wrapper.add(fboard);
        }
    }

    protected static void untrack(FScoreboard fboard) {
        tracking.remove(fboard);
        for (FTeamWrapper wrapper : wrappers.values()) {
            wrapper.remove(fboard);
        }
    }

    private FTeamWrapper(Faction faction) {
        this.teamName = "faction_" + (factionTeamPtr++);
        this.faction = faction;

        for (FScoreboard fboard : tracking) {
            add(fboard);
        }
    }

    private void add(FScoreboard fboard) {
        Scoreboard board = fboard.getScoreboard();
        Team team = board.registerNewTeam(teamName);
        teams.put(fboard, team);

        for (OfflinePlayer player : getPlayers()) {
            team.addEntry(player.getName());
        }

        updatePrefix(fboard);
    }

    private void remove(FScoreboard fboard) {
        teams.remove(fboard).unregister();
    }

    private void updatePrefixes() {
        if (plugin.getFConfig().getDefaultScoreboardPrefixes()) {
            for (FScoreboard fboard : teams.keySet()) {
                updatePrefix(fboard);
            }
        }
    }

    private void updatePrefix(FScoreboard fboard) {
        if (plugin.getFConfig().getDefaultScoreboardPrefixes()) {
            FPlayer fplayer = fboard.getFPlayer();
            Team team = teams.get(fboard);

            String prefix = plugin.getFConfig().getNametagPrefix();
            prefix = ParsingUtil.replaceRelationPlaceholders(prefix, faction, fplayer);
            prefix = ParsingUtil.replaceFactionPlaceholders(prefix, faction);
            // Avoid client bug
            if (prefix.length() > 16) {
                prefix = prefix.substring(0, 15);
            }

            if (team.getPrefix() == null || !team.getPrefix().equals(prefix)) {
                team.setPrefix(prefix);
            }
        }
    }

    private void addPlayer(OfflinePlayer player) {
        if (members.add(player)) {
            for (Team team : teams.values()) {
                team.addEntry(player.getName());
            }
        }
    }

    private void removePlayer(OfflinePlayer player) {
        if (members.remove(player)) {
            for (Team team : teams.values()) {
                team.removeEntry(player.getName());
            }
        }
    }

    private Set<OfflinePlayer> getPlayers() {
        return new HashSet<>(this.members);
    }

    private void unregister() {
        for (Team team : teams.values()) {
            team.unregister();
        }
        teams.clear();
    }

}
