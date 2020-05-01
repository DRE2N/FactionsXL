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
package de.erethon.factionsxl.player;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Board;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.board.RegionType;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.scoreboard.FScoreboard;
import de.erethon.factionsxl.scoreboard.sidebar.FInfoSidebar;
import de.erethon.factionsxl.util.LazyChunk;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.WarParty;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Daniel Saukel
 */
public class PlayerListener implements Listener {

    FactionsXL plugin;
    FPlayerCache fPlayers;
    Board board;
    FConfig fConfig;

    public static final String SPACE = " " + ChatColor.RESET;

    public PlayerListener(FactionsXL plugin) {
        this.plugin = plugin;
        fPlayers = plugin.getFPlayerCache();
        board = plugin.getBoard();
        fConfig = plugin.getFConfig();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FPlayer fPlayer = new FPlayer(player);
        fPlayers.addPlayer(fPlayer);

        Region region = fPlayer.getLastRegion();
        MessageUtil.sendActionBarMessage(player, getRegionName(player, region));

        if (fConfig.isScoreboardEnabledByDefault()) {
            FScoreboard.init(fPlayer);
            FScoreboard.get(fPlayer).setDefaultSidebar(fConfig.getScoreboardUpdateInterval());
            FScoreboard.get(fPlayer).setSidebarVisibility(fPlayer.isScoreboardEnabled());
        }

        if (!fPlayer.getAccessibleRequests().isEmpty()) {
            MessageUtil.sendCenteredMessage(player, FMessage.MISC_OPEN_REQUESTS.getMessage());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        FPlayer fPlayer = fPlayers.getByPlayer(event.getPlayer());
        FScoreboard.remove(fPlayer);
        fPlayers.removePlayer(fPlayer);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (fConfig.isExcludedWorld(event.getEntity().getWorld())) {
            return;
        }
        Player killedP = event.getEntity();
        Player killerP = event.getEntity().getKiller();
        FPlayer killedF = fPlayers.getByPlayer(killedP);
        FPlayer killerF = killerP != null ? fPlayers.getByPlayer(killerP) : null;
        Faction killedFc = killedF.getFaction();
        Faction killerFc = killerF != null ? killerF.getFaction() : null;
        double loss = fConfig.getPowerDeathLoss();
        double newPower = killedF.getPower() - loss;
        killedF.setPower(newPower < fConfig.getMinPower() ? fConfig.getMinPower() : newPower);
        // Gamerule: doImmediateRespawn needs to be true here!
        if (killerP != null) {
            if (killerFc != null) {
                for (WarParty w : killerFc.getWarParties()) {
                    if (w.getEnemy().getFactions().contains(killedFc)) {
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            public void run() {
                                killedP.teleport(killedFc.getHome());
                            }
                        }, 2);
                        break;
                    }
                }
            }
            if ((killerFc != null && !killerFc.isInWar(killedFc) || (killerFc != null && killerFc.isInWar(killedFc) && fConfig.isPowerGainInWar()))) {
                double newKPower = killerF.getPower() + loss;
                killerF.setPower(newKPower > fConfig.getMaxPower() ? fConfig.getMaxPower() : newKPower);
                ParsingUtil.sendMessage(killerP, FMessage.DEATH_PLAYER_KILL_KILLER.getMessage(), killedF, String.valueOf(loss), String.valueOf(killerF.getPower()));
            }
            ParsingUtil.sendMessage(killedP, FMessage.DEATH_PLAYER_KILL_KILLED.getMessage(), killerF, String.valueOf(loss), String.valueOf(killedF.getPower()));
        } else {
            ParsingUtil.sendMessage(killedP, FMessage.DEATH_DEFAULT_DEATH.getMessage(), String.valueOf(loss), String.valueOf(killedF.getPower()));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (fConfig.isExcludedWorld(event.getPlayer().getWorld())) {
            return;
        }
        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();
        if (fromChunk == toChunk) {
            return;
        }

        Player player = event.getPlayer();
        FPlayer fPlayer = fPlayers.getByPlayer(player);

        Region fromRegion = fPlayer.getLastRegion();
        Region toRegion = board.getByChunk(toChunk);
        fPlayer.setLastRegion(toRegion);

        if (fPlayer.isAutoclaiming() && toRegion == null) {
            fPlayer.getAutoclaimingRegion().getChunks().add(new LazyChunk(toChunk));
            ParsingUtil.sendMessage(player, FMessage.CMD_WORLD_CHUNK_ADDED.getMessage(), fPlayer.getAutoclaimingRegion());
            return;
        }

        if (fromRegion == toRegion) {
            return;
        }

        MessageUtil.sendActionBarMessage(event.getPlayer(), getRegionName(player, toRegion));
        if (toRegion != null) {
            Faction fromFaction = fromRegion != null ? fromRegion.getOwner() : null;
            Faction toFaction = toRegion != null ? toRegion.getOwner() : null;
            if (fromFaction != toFaction) {
                stopSound(player, fromFaction);
                if (toFaction != null && toFaction.getDescription() != null) {
                    MessageUtil.sendCenteredMessage(player, ParsingUtil.getFactionName(player, toFaction));
                    MessageUtil.sendCenteredMessage(player, toFaction.getDescription());
                    stopSound(player, toFaction);
                    player.playSound(player.getLocation(), toFaction.getAnthem(), 1, 1);
                    if (fPlayer.isScoreboardEnabled()) {
                        FScoreboard.get(player).setTemporarySidebar(new FInfoSidebar(toFaction));
                    }
                }
            }
        }
    }

    private void stopSound(Player player, Faction faction) {
        if (player == null || faction == null || faction.getAnthem() == null) {
            return;
        }
        player.stopSound(faction.getAnthem());
    }

    private String getRegionName(Player player, Region region) {
        String main = ParsingUtil.getRegionName(player, region);
        if (region == null || region.getType() != RegionType.WARZONE) {
            return main;
        } else {
            String warZone = ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() + "[" + FMessage.REGION_WAR_ZONE.getMessage().toUpperCase() + "]";
            return warZone + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + main + SPACE + SPACE + SPACE + SPACE + SPACE + SPACE + warZone;
        }
    }

}
