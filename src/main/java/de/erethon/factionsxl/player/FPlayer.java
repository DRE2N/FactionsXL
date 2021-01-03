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
package de.erethon.factionsxl.player;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.player.PlayerCollection;
import de.erethon.commons.player.PlayerUtil;
import de.erethon.commons.player.PlayerWrapper;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.chat.ChatChannel;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.FEntity;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.entity.Request;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.WarParty;
import de.erethon.factionsxl.war.demand.WarDemand;
import de.erethon.factionsxl.war.peaceoffer.PeaceOffer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a player.
 *
 * @author Daniel Saukel
 */
public class FPlayer implements FEntity, PlayerWrapper {

    FactionsXL plugin = FactionsXL.getInstance();

    private Player player;
    private UUID uuid;

    private ChatChannel chatChannel = ChatChannel.PUBLIC;
    private Region autoclaiming;
    private Region lastRegion;
    private PeaceOffer peaceOffer;
    private double lastPlayed;
    private List<Player> lastDamagers = new CopyOnWriteArrayList<>();

    private FPlayerData data;

    public FPlayer(Player player) {
        this.player = player;
        uuid = player.getUniqueId();
        loadPlayerData(FPlayerCache.getFile(uuid));
        data.setLastName(player.getName());
    }

    public FPlayer(UUID uuid) {
        this.uuid = uuid;
        loadPlayerData(FPlayerCache.getFile(uuid));
    }

    public FPlayer(String name) {
        this(PlayerUtil.getUniqueIdFromName(name));
    }

    /* Getters and setters */
    @Override
    public String getName() {
        return data.getLastName();
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public Player getPlayer() {
        return player;
    }


    /**
     * @return
     * the player
     */
    public boolean isOnline() {
        return player != null;
    }

    /**
     * @return
     * if the player is faction mod in any faction
     */
    public boolean isMod() {
        for (Faction faction : plugin.getFactionCache().getActive()) {
            if (isMod(faction)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param faction to check
     * @return
     * if the player is faction mod
     */
    public boolean isMod(Faction faction) {
        if (faction.getMods().contains(this)) {
            return true;
        }
        if (faction.isAdmin(uuid)) {
            return true;
        }
        return false;
    }

    @Override
    public PlayerCollection getRequestAuthorizedPlayers(Class<? extends Request> type) {
        PlayerCollection players = new PlayerCollection();
        players.add(uuid);
        return players;
    }

    @Override
    public List<Request> getRequests() {
        return data.getRequests();
    }

    /**
     * @return
     * a Collection of all accessible requests, including those of the factions the player owns
     */
    public List<Request> getAccessibleRequests() {
        List<Request> requests = new ArrayList<>();
        for (Request req : getRequests().toArray(new Request[]{})) {
            if (req.isExpired()) {
                getRequests().remove(req);
            } else {
                requests.add(req);
            }
        }
        for (Faction faction : FactionsXL.getInstance().getFactionCache().getByLeader(player)) {
            for (Request req : faction.getRequests().toArray(new Request[]{})) {
                if (req.isExpired()) {
                    faction.getRequests().remove(req);
                } else {
                    requests.add(req);
                }
            }
        }
        return requests;
    }

    /**
     * @return
     * the player's dynasty
     */
    public Dynasty getDynasty() {
        return plugin.getFPlayerCache().getDynastyByUUID(uuid);
    }

    /**
     * @return
     * the chat channel that this player is using
     */
    public ChatChannel getChatChannel() {
        return chatChannel;
    }

    /**
     * @param channel
     * the chat channel to set
     */
    public void setChatChannel(ChatChannel channel) {
        chatChannel = channel;
        ParsingUtil.sendMessage(player, FMessage.CHAT_CHANNEL_SWITCHED.getMessage(), channel.getFormatted());
    }

    /**
     * @return
     * true if the scoreboard is enabled
     */
    public boolean isScoreboardEnabled() {
        return data.isScoreboardEnabled();
    }

    /**
     * @param enabled
     * if the scoreboard is enabled
     */
    public void setScoreboardEnabled(boolean enabled) {
        data.setScoreboardEnabled(enabled);
    }

    /**
     * @return
     * if the scoreboard is enabled
     */
    public boolean areAnthemsEnabled() {
        return data.areAnthemsEnabled();
    }

    /**
     * @param enabled
     * if faction anthems shall be enabled
     */
    public void setAnthemsEnabled(boolean enabled) {
        data.setAnthemsEnabled(enabled);
    }

    /**
     * @return
     * the region to which the player adds chunks automatically
     */
    public Region getAutoclaimingRegion() {
        return autoclaiming;
    }

    /**
     * @return
     * if the player is currently autoclaiming
     */
    public boolean isAutoclaiming() {
        return autoclaiming != null;
    }

    /**
     * @param region
     * the region to claim automatically
     */
    public void setAutoclaiming(Region region) {
        autoclaiming = region;
    }

    /**
     * @return
     * the player's power value
     */
    public int getPower() {
        Double raw = plugin.getFData().power.get(uuid);
        return raw != null ? (int) Math.round(raw) : 0;
    }

    /**
     * @param power
     * the power value to set
     */
    public void setPower(double power) {
        plugin.getFData().power.put(uuid, power);
    }

    /**
     * @return
     * the player's title
     */
    public String getTitle() {
        return data.getTitle();
    }

    /**
     * @param title
     * the title to set
     */
    public void setTitle(String title) {
        data.setTitle(title);
    }

    /**
     * @return
     * the player's prefix as specified in the language file
     */
    public String getPrefix() {
        Faction faction = getFaction();
        if (faction != null && faction.isAdmin(uuid)) {
            return FMessage.CHAT_PREFIX_ADMIN.getMessage();
        } else if (faction != null && isMod(faction)) {
            return FMessage.CHAT_PREFIX_MOD.getMessage();
        } else {
            return FMessage.CHAT_PREFIX_MEMBER.getMessage();
        }
    }

    @Override
    public Relation getRelation(FEntity object) {
        Faction own = getFaction();
        return own != null ? own.getRelation(object) : Relation.PEACE;
    }

    @Override
    public boolean isInWar(FEntity object) {
        Faction own = getFaction();
        return own != null ? own.isInWar(object) : false;
    }

    public boolean isInWarParty(WarParty party) {
        return getFaction().getWarParties().contains(party);
    }

    public Location getHome() {
        return data.getHome();
    }

    public void setHome(Location location) {
        data.setHome(location);
    }

    public boolean checkHome() {
        return checkHome(getHome());
    }

    public boolean checkHome(Location home) {
        if (home == null) {
            return true;
        }
        Region region = plugin.getBoard().getByLocation(home);
        if (region == null || region.getOwner() == null) {
            return true;
        }
        if (region.getOwner().getRelation(this) == Relation.ENEMY) {
            return false;
        }
        if (!region.getOwner().getRelation(this).canBuild()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @return
     * the saved data
     */
    public FPlayerData getData() {
        return data;
    }

    /**
     * Load / reload a new instance of PlayerData
     */
    public void loadPlayerData(File file) {
        data = new FPlayerData(file);
    }

    /**
     * @return
     * the player's faction
     */
    public Faction getFaction() {
        return plugin.getFactionCache().getByFPlayer(this);
    }

    /**
     * @return
     * if the player is member of a faction.
     */
    public boolean hasFaction() {
        return plugin.getFactionCache().getByFPlayer(this) != null;
    }

    /**
     * @return
     * the last region the player was known to be inside
     */
    public Region getLastRegion() {
        if (player == null) {
            return null;
        }
        if (lastRegion == null) {
            lastRegion = plugin.getBoard().getByLocation(player.getLocation());
        }
        return lastRegion;
    }

    /**
     * @param region
     * the region to cache
     */
    public void setLastRegion(Region region) {
        lastRegion = region;
    }
    
    /**
     * @return
     * the cached PeaceOffer
     */
    public PeaceOffer getPeaceOffer() {
        return peaceOffer;
    }

    /**
     * Sets the PeaceOffer the player is creating
     *
     * @param peaceOffer the PeaceOffer
     */
    public void setPeaceOffer(PeaceOffer peaceOffer) {
        this.peaceOffer = peaceOffer;
    }

    /**
     * Sends a list of the player's war demands to them
     */
    public void listWarDemands() {
        listWarDemands(peaceOffer.getDemands());
    }

    /**
     * Sends a list of war demands to them
     *
     * @param demands
     * the WarDemands to send
     */
    public void listWarDemands(Collection<WarDemand> demands) {
        MessageUtil.sendMessage(player, FMessage.WAR_DEMAND_LIST.getMessage());
        if (demands.isEmpty()) {
            MessageUtil.sendMessage(player, FMessage.MISC_NONE.getMessage());
        }
        for (WarDemand demand : demands) {
            MessageUtil.sendMessage(player, "&8 - " + demand.toString());
        }
    }

    public List<Player> getLastDamagers() {
        return lastDamagers;
    }

    @Override
    public String toString() {
        return "FPlayer{name=" + getName() + "}";
    }

}
