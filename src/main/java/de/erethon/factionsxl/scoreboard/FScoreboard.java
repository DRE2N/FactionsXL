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
package de.erethon.factionsxl.scoreboard;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.scoreboard.sidebar.FDefaultSidebar;
import de.erethon.factionsxl.scoreboard.sidebar.FWarSidebar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapted from FactionsUUID by drtshock.
 *
 * @author Trent Hensler, Daniel Saukel
 */
public class FScoreboard {

    FactionsXL plugin = FactionsXL.getInstance();

    private static final Map<FPlayer, FScoreboard> fscoreboards = new HashMap<>();

    private final Scoreboard scoreboard;
    private final FPlayer fplayer;
    private final BufferedObjective bufferedObjective;
    private List<FSidebarProvider> defaultProviders = new ArrayList<>();
    private int currentProvider;
    private FSidebarProvider temporaryProvider;
    private boolean removed = false;

    public static void init(FPlayer fplayer) {
        FScoreboard fboard = new FScoreboard(fplayer);
        fscoreboards.put(fplayer, fboard);

        if (fplayer.hasFaction()) {
            FTeamWrapper.applyUpdates(fplayer.getFaction());
        }
        FTeamWrapper.track(fboard);
    }

    public static void remove(FPlayer fplayer) {
        FScoreboard fboard = fscoreboards.remove(fplayer);

        if (fboard != null) {
            fboard.removed = true;
            FTeamWrapper.untrack(fboard);
        }
    }

    public static FScoreboard get(FPlayer fplayer) {
        if (fscoreboards.get(fplayer) == null) {
            init(fplayer);
        }
        return fscoreboards.get(fplayer);
    }

    public static FScoreboard get(Player player) {
        return get(FactionsXL.getInstance().getFPlayerCache().getByPlayer(player));
    }

    private FScoreboard(FPlayer fplayer) {
        this.fplayer = fplayer;

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.bufferedObjective = new BufferedObjective(scoreboard);

        fplayer.getPlayer().setScoreboard(scoreboard);
    }

    protected FPlayer getFPlayer() {
        return fplayer;
    }

    protected Scoreboard getScoreboard() {
        return scoreboard;
    }

    public void updateProviders() {
        Faction faction = fplayer.getFaction();
        if (defaultProviders.isEmpty()) {
            defaultProviders.add(new FDefaultSidebar());
        } else if (defaultProviders.size() != 1) {
            FSidebarProvider standard = defaultProviders.get(0);
            defaultProviders.clear();
            defaultProviders.add(standard);
        }
        if (faction != null) {
            plugin.getWarCache().getByFaction(faction).forEach(f -> defaultProviders.add(new FWarSidebar(f)));
        }
    }

    public static void updateAllProviders() {
        fscoreboards.values().forEach(s -> s.updateProviders());
    }

    public void setSidebarVisibility(boolean visible) {
        bufferedObjective.setDisplaySlot(visible ? DisplaySlot.SIDEBAR : null);
    }

    public void setDefaultSidebar(int updateInterval) {
        updateProviders();
        if (temporaryProvider == null) {
            // We have no temporary provider; update the BufferedObjective!
            updateObjective();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (defaultProviders == null) {
                        cancel();
                        return;
                    }
                    if (defaultProviders.size() < currentProvider - 1) {
                        cancel();
                        return;
                    }
                    if (removed || !defaultProviders.contains(defaultProviders.get(currentProvider))) {
                        cancel();
                        return;
                    }

                    if (temporaryProvider == null) {
                        currentProvider++;
                        if (currentProvider >= defaultProviders.size()) {
                            currentProvider = 0;
                        }
                        updateObjective();
                    }
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
            }
        }.runTaskTimer(plugin, updateInterval, updateInterval);
    }

    public void setTemporarySidebar(final FSidebarProvider provider) {
        temporaryProvider = provider;
        updateObjective();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (removed) {
                    return;
                }

                if (temporaryProvider == provider) {
                    temporaryProvider = null;
                    updateObjective();
                }
            }
        }.runTaskLater(plugin, plugin.getFConfig().getScoreboardExpiration());
    }

    private void updateObjective() {
        FSidebarProvider provider = temporaryProvider != null ? temporaryProvider : defaultProviders.get(currentProvider);

        if (provider == null) {
            bufferedObjective.hide();

        } else {
            bufferedObjective.setTitle(provider.getTitle(fplayer));
            bufferedObjective.setAllLines(provider.getLines(fplayer));
            bufferedObjective.flip();
        }
    }

}
