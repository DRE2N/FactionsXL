/*
 * Copyright (C) 2017 Daniel Saukel
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
package io.github.dre2n.factionsxl.faction;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import io.github.dre2n.commons.util.ConfigUtil;
import io.github.dre2n.commons.util.EnumUtil;
import io.github.dre2n.commons.util.NumberUtil;
import io.github.dre2n.factionsxl.FactionsXL;
import io.github.dre2n.factionsxl.board.Region;
import io.github.dre2n.factionsxl.board.dynmap.DynmapStyle;
import io.github.dre2n.factionsxl.config.FConfig;
import io.github.dre2n.factionsxl.config.FMessage;
import io.github.dre2n.factionsxl.economy.FAccount;
import io.github.dre2n.factionsxl.economy.FStorage;
import io.github.dre2n.factionsxl.economy.Resource;
import io.github.dre2n.factionsxl.economy.TradeMenu;
import io.github.dre2n.factionsxl.idea.Idea;
import io.github.dre2n.factionsxl.idea.IdeaGroup;
import io.github.dre2n.factionsxl.idea.IdeaMenu;
import io.github.dre2n.factionsxl.player.Dynasty;
import io.github.dre2n.factionsxl.player.FPermission;
import io.github.dre2n.factionsxl.player.FPlayer;
import io.github.dre2n.factionsxl.population.PopulationMenu;
import io.github.dre2n.factionsxl.relation.Relation;
import io.github.dre2n.factionsxl.relation.RelationParticipator;
import io.github.dre2n.factionsxl.scoreboard.FTeamWrapper;
import io.github.dre2n.factionsxl.util.ParsingUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents a faction.
 *
 * @author Daniel Saukel
 */
public class Faction extends LegalEntity implements RelationParticipator {

    FactionsXL plugin = FactionsXL.getInstance();
    FConfig fConfig = plugin.getFConfig();

    File file;
    FileConfiguration config;
    boolean active;
    String mapFillColor = "#E0E0E0";
    String mapLineColor = "#FFFFFF";
    DynmapStyle dynmapStyle;
    boolean mapVisibility = true;
    GovernmentType type;
    boolean open;
    double prestige;
    byte stability;
    double exhaustion;
    double manpowerModifier;
    Location home;
    Hologram homeHolo;
    Region capital;
    Set<Chunk> chunks = new HashSet<>();
    Set<Region> regions = new HashSet<>();
    OfflinePlayer admin;
    Set<OfflinePlayer> mods = new HashSet<>();
    Set<OfflinePlayer> members = new HashSet<>();
    Set<OfflinePlayer> invited = new HashSet<>();
    Map<Faction, Relation> relations = new HashMap<>();
    TradeMenu tradeMenu;
    FStorage storage;
    Map<Resource, Integer> groceryList = new HashMap<>();
    PopulationMenu populationMenu;
    IdeaMenu ideaMenu;
    Set<IdeaGroup> ideaGroups = new HashSet<>();
    Set<Idea> ideas = new HashSet<>();

    public Faction(File file) {
        id = NumberUtil.parseInt(file.getName().replace(".yml", ""));
        this.file = file;
        config = YamlConfiguration.loadConfiguration(file);
    }

    public Faction(int id) {
        this.id = id;
        file = new File(FactionsXL.FACTIONS, id + ".yml");
        config = YamlConfiguration.loadConfiguration(file);
    }

    /* Getters and setters */
    @Override
    public void setName(String name) {
        super.setName(name);
        FTeamWrapper.updatePrefixes(this);
    }

    @Override
    public void setBanner(ItemStack banner) {
        super.setBanner(banner);
        updateHomeHologram();
    }

    /**
     * @return
     * if the faction is active or disbanded
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active
     * set if the faction is active or disbanded
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return
     * the Dynmap fill color
     */
    public String getMapFillColor() {
        return mapFillColor;
    }

    /**
     * @return
     * the Dynmap line color
     */
    public String getMapLineColor() {
        return mapLineColor;
    }

    /**
     * @param fill
     * the Dynmap fill color to set
     * @param line
     * the Dynmap line color to set
     */
    public void setMapColor(String fill, String line) {
        if (fill.matches("#[0-9A-F]{6}") && line.matches("#[0-9A-F]{6}")) {
            mapFillColor = fill;
            mapLineColor = line;
            dynmapStyle = new DynmapStyle(DynmapStyle.DEFAULT_STYLE).setStrokeColor(mapLineColor).setFillColor(mapFillColor);
        }
    }

    /**
     * @return
     * the dynmap style of the faction
     */
    public DynmapStyle getDynmapStyle() {
        if (dynmapStyle == null) {
            dynmapStyle = new DynmapStyle(DynmapStyle.DEFAULT_STYLE).setStrokeColor(mapLineColor).setFillColor(mapFillColor);
        }
        return dynmapStyle;
    }

    /**
     * @return
     * the map visibility
     */
    public boolean isMapVisible() {
        return mapVisibility;
    }

    /**
     * @param visibility
     * set the faction to visible or hidden
     */
    public void setMapVisibility(boolean visibility) {
        mapVisibility = visibility;
    }

    /**
     * @return
     * the type of the government
     */
    public GovernmentType getGovernmentType() {
        return type;
    }

    /**
     * @param type
     * the type of the government to set
     */
    public void setGovernmentType(GovernmentType type) {
        this.type = type;
    }

    /**
     * @return
     * if the faction is open
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * @param open
     * set if the faction is open
     */
    public void setOpen(boolean open) {
        this.open = open;
    }

    /**
     * @return
     * the prestige value
     */
    public double getPrestige() {
        return prestige;
    }

    /**
     * @param prestige
     * the prestige value to set
     */
    public void setPrestige(double prestige) {
        this.prestige = prestige;
    }

    /**
     * @return
     * the power of all players
     */
    public int getPower() {
        Double power = 0D;
        for (OfflinePlayer member : members) {
            Double d = plugin.getFData().power.get(member.getUniqueId());
            if (d != null) {
                power += d;
            }
        }
        return power.intValue();
    }

    /**
     * @return
     * the stability value
     */
    public byte getStability() {
        return stability;
    }

    /**
     * @param stability
     * the stability value to set
     */
    public void setStability(byte stability) {
        this.stability = stability;
    }

    /**
     * @return
     * the war exhaustion value
     */
    public double getExhaustion() {
        return exhaustion;
    }

    /**
     * @param exhaustion
     * the war exhaustion value to set
     */
    public void setExhaustion(double exhaustion) {
        this.exhaustion = exhaustion;
    }

    /**
     * @return
     * the manpower value
     */
    public int getManpower() {
        int manpower = 0;
        for (Region region : regions) {
            manpower += region.getPopulation() * (manpowerModifier / 100);
        }
        return manpower;
    }

    /**
     * @return
     * the overall sympathy value
     */
    public int getSympathy() {
        return 0;
    }

    /**
     * @return
     * the home location of the faction
     */
    public Location getHome() {
        return home;
    }

    /**
     * @param home
     * the location to set
     */
    public void setHome(Location home) {
        this.home = home;
        updateHomeHologram();
    }

    /**
     * Updates the home hologram
     */
    public void updateHomeHologram() {
        if (!fConfig.areHologramsEnabled() || !active) {
            return;
        }
        // Run this 1 tick later sothat everything is loaded
        final Faction faction = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (homeHolo != null) {
                    homeHolo.delete();
                }
                homeHolo = HologramsAPI.createHologram(plugin, getHome().clone().add(0, 3, 0));
                homeHolo.appendItemLine(getBannerStack());
                for (String line : fConfig.getHomeHologramText()) {
                    homeHolo.appendTextLine(ParsingUtil.replaceFactionPlaceholders(line, faction));
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    /**
     * @return
     * the capital region
     */
    public Region getCapital() {
        return capital;
    }

    /**
     * @param capital
     * the capital region to set
     */
    public void setCapital(Region capital) {
        this.capital = capital;
    }

    /**
     * @return
     * a Set of all chunks owned by this faction
     */
    public Set<Chunk> getChunks() {
        return chunks;
    }

    /**
     * @return
     * a Set of all chunks owned by this faction
     */
    public Set<Region> getRegions() {
        return regions;
    }

    /**
     * @return
     * the admin of the faction
     */
    public OfflinePlayer getAdmin() {
        return admin;
    }

    /**
     * @param admin
     * the new admin to set
     */
    public void setAdmin(OfflinePlayer admin) {
        this.admin = admin;
        checkForPersonalUnions();
    }

    /**
     * Updates personal unions.
     */
    public void checkForPersonalUnions() {
        HashSet<Faction> toRemove = new HashSet<>();
        for (Entry<Faction, Relation> entry : relations.entrySet()) {
            if (entry.getValue() == Relation.PERSONAL_UNION && !entry.getKey().getAdmin().equals(admin)) {
                toRemove.add(entry.getKey());
            }
        }
        for (Faction key : toRemove) {
            relations.remove(key);
        }

        for (Faction faction : plugin.getFactionCache().getActive()) {
            if (faction == this) {
                continue;
            }
            if (faction.getAdmin().equals(admin)) {
                relations.put(faction, Relation.PERSONAL_UNION);
                faction.relations.put(this, Relation.PERSONAL_UNION);
                ParsingUtil.broadcastMessage(FMessage.FACTION_PERSONAL_UNION_FORMED.getMessage(), this, faction, admin);
            } else if (faction.getRelation(this) == Relation.PERSONAL_UNION) {
                faction.relations.remove(this);
            }
        }
    }

    /**
     * @return
     * the faction admin as an FPlayer
     */
    public FPlayer getFAdmin() {
        return plugin.getFPlayerCache().getByPlayer(admin);
    }

    /**
     * @return
     * the dynasty of the faction admin;
     * null if the faction is a republic or a theocracy
     */
    public Dynasty getDynasty() {
        return getFAdmin().getDynasty();
    }

    /**
     * @return
     * a Set of all mods as OfflinePlayers
     */
    public Set<OfflinePlayer> getMods() {
        return mods;
    }

    /**
     * @return
     * a Set of all mods as FPlayerCache
     */
    public Set<FPlayer> getFMods() {
        HashSet<FPlayer> fPlayers = new HashSet<>();
        for (OfflinePlayer member : mods) {
            fPlayers.add(plugin.getFPlayerCache().getByPlayer(member));
        }
        return fPlayers;
    }

    /**
     * @return
     * a Set of all mods that are online
     */
    public Set<Player> getOnlineMods() {
        HashSet<Player> online = new HashSet<>();
        for (OfflinePlayer player : mods) {
            if (player.isOnline()) {
                online.add(player.getPlayer());
            }
        }
        return online;
    }

    /**
     * @return
     * a Set of all members as OfflinePlayers
     */
    public Set<OfflinePlayer> getMembers() {
        return members;
    }

    /**
     * @return
     * a Set of all members as FPlayerCache
     */
    public Set<FPlayer> getFPlayers() {
        HashSet<FPlayer> fPlayers = new HashSet<>();
        for (OfflinePlayer member : members) {
            fPlayers.add(plugin.getFPlayerCache().getByPlayer(member));
        }
        return fPlayers;
    }

    /**
     * @return
     * a Set of all members that are not mod or admin as OfflinePlayers
     */
    public Set<OfflinePlayer> getNonPrivilegedMembers() {
        HashSet<OfflinePlayer> players = new HashSet<>();
        for (OfflinePlayer member : members) {
            if (!mods.contains(member) && !member.equals(admin)) {
                players.add(member);
            }
        }
        return players;
    }

    /**
     * @return
     * a Set of all members that are not mod or admin as FPlayerCache
     */
    public Set<FPlayer> getNonPrivilegedFPlayers() {
        HashSet<FPlayer> fPlayers = new HashSet<>();
        for (OfflinePlayer member : members) {
            FPlayer fPlayer = plugin.getFPlayerCache().getByPlayer(member);
            if (!isPrivileged(fPlayer)) {
                fPlayers.add(fPlayer);
            }
        }
        return fPlayers;
    }

    /**
     * @return
     * a Set of all members that are online
     */
    public Set<Player> getOnlineMembers() {
        HashSet<Player> online = new HashSet<>();
        for (OfflinePlayer player : members) {
            if (player.isOnline()) {
                online.add(player.getPlayer());
            }
        }
        return online;
    }

    /**
     * @param relation
     * the relation type
     * @return
     * a Set of all related players that are online
     */
    public Set<Player> getOnlineByRelation(Relation relation) {
        HashSet<Player> online = new HashSet<>();
        for (Faction faction : getRelatedFactions(relation)) {
            online.addAll(faction.getOnlineMembers());
        }
        return online;
    }

    /**
     * @return
     * a Set of all invited players
     */
    public Set<OfflinePlayer> getInvitedPlayers() {
        return invited;
    }

    /**
     * @return
     * a Map of all relations except peace (default)
     */
    public Map<Faction, Relation> getRelations() {
        return relations;
    }

    @Override
    public Relation getRelation(RelationParticipator object) {
        Faction faction = null;
        if (object instanceof FPlayer) {
            if (((FPlayer) object).hasFaction()) {
                faction = ((FPlayer) object).getFaction();
            }
        } else if (object instanceof Faction) {
            faction = (Faction) object;
        }

        if (relations.containsKey(faction)) {
            return relations.get(faction);
        } else if (faction == this) {
            return Relation.OWN;
        } else {
            return Relation.PEACE;
        }
    }

    /**
     * @param fPlayer
     * another fPlayer
     * @return
     * the relation of this faction to the faction of the player
     */
    public Relation getRelation(FPlayer fPlayer) {
        if (fPlayer.hasFaction()) {
            return getRelation(fPlayer.getFaction());
        } else {
            return Relation.PEACE;
        }
    }

    /**
     * @param relation
     * the relation type
     * @return
     * all factions that have the specified relation to this faction
     */
    public Set<Faction> getRelatedFactions(Relation relation) {
        HashSet<Faction> factions = new HashSet<>();
        for (Faction faction : plugin.getFactionCache().getActive()) {
            if (getRelation(faction) == relation || getRelation(faction).getIncludedRelations().contains(relation)) {
                factions.add(faction);
            }
        }
        return factions;
    }

    /**
     * @return
     * the trade menu of the faction
     */
    public TradeMenu getTradeMenu() {
        return tradeMenu;
    }

    /**
     * @return
     * the storage of the faction
     */
    public FStorage getStorage() {
        return storage;
    }

    /**
     * @param resource
     * the resource
     * @return
     * how much of the resource is imported (export = negative values)
     */
    public int getImportValue(Resource resource) {
        Integer eximport = groceryList.get(resource);
        if (eximport == null) {
            return 0;
        } else {
            return eximport;
        }
    }

    /**
     * @return
     * a Map of exports / imports
     */
    public Map<Resource, Integer> getGroceryList() {
        return groceryList;
    }

    /**
     * @return
     * the population menu
     */
    public PopulationMenu getPopulationMenu() {
        return populationMenu;
    }

    /**
     * @return
     * the idea menu
     */
    public IdeaMenu getIdeaMenu() {
        return ideaMenu;
    }

    /**
     * @return
     * the idea groups that this faction has chosen
     */
    public Set<IdeaGroup> getIdeaGroups() {
        return ideaGroups;
    }

    /**
     * @return
     * the ideas that this faction has bought
     */
    public Set<Idea> getIdeas() {
        return ideas;
    }

    /**
     * @param sender
     * the CommandSender to check
     * @return
     * true if the player is admin or mod
     */
    public boolean isPrivileged(CommandSender sender) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        return mods.contains(player) || admin.equals(player) || FPermission.hasPermission(sender, FPermission.BYPASS);
    }

    /**
     * @param fPlayer
     * the FPlayer to check
     * @return
     * true if the player is admin or mod
     */
    public boolean isPrivileged(FPlayer fPlayer) {
        return fPlayer.isMod(this) || admin.getUniqueId().equals(fPlayer.getUniqueId()) || FPermission.hasPermission(fPlayer.getPlayer(), FPermission.BYPASS);
    }

    /* Actions */
    /**
     * Sends a message to all players in this faction.
     *
     * @param message
     * the message to send
     */
    public void sendMessage(String message, Object... args) {
        for (Player player : getOnlineMembers()) {
            ParsingUtil.sendMessage(player, "&a[" + name + "] &r" + message, args);
        }
        if (admin.isOnline() && !getOnlineMembers().contains(admin.getPlayer())) {
            ParsingUtil.sendMessage(admin.getPlayer(), "&a[" + name + "] &r" + message, args);
        }
    }

    /**
     * Requires Spigot API!
     *
     * Sends an invitation to join this faction to the player.
     *
     * @param player
     * the player that will receive the invitation
     */
    public void sendInvitation(Player player) {
        ClickEvent onClickConfirm = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/factionsxl join " + name);
        TextComponent confirm = new TextComponent(ChatColor.GREEN + FMessage.MISC_ACCEPT.getMessage());
        confirm.setClickEvent(onClickConfirm);

        ClickEvent onClickDeny = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/factionsxl join " + name + " deny");
        TextComponent deny = new TextComponent(ChatColor.DARK_RED + FMessage.MISC_DENY.getMessage());
        deny.setClickEvent(onClickDeny);

        ParsingUtil.sendMessage(player, FMessage.FACTION_INVITE.getMessage(name));
        player.spigot().sendMessage(confirm, new TextComponent(" "), deny);
    }

    /**
     * Actions when a day passed.
     */
    public void payday() {
        storage.payday();
    }

    /**
     * Makes the faction pay the resource value for the resource
     *
     * @param goods
     * a Map of resources and their amount
     * @param modifier
     * a modifier for the price, e.g. for taxes
     * @return
     * true if the faction can afford the price, false if not
     */
    public boolean chargeMoneyForResource(Map<Resource, Integer> goods, double modifier) {
        double price = 0;
        for (Entry<Resource, Integer> entry : goods.entrySet()) {
            price += entry.getValue() * entry.getKey().getValue();
        }
        boolean canAfford = account.getBalance() >= price * modifier;
        if (canAfford) {
            account.withdraw(price);
            for (Entry<Resource, Integer> entry : goods.entrySet()) {
                storage.getGoods().put(entry.getKey(), storage.getGoods().get(entry.getKey()) + entry.getValue());
            }
        }
        return canAfford;
    }

    /**
     * Makes the faction pay the resource value for the resource
     *
     * @param type
     * the resource type
     * @param amount
     * the amount of the resource
     * @param modifier
     * a modifier for the price, e.g. for taxes
     * @return
     * true if the faction can afford the price, false if not
     */
    public boolean chargeMoneyForResource(Resource type, int amount, double modifier) {
        double price = amount * type.getValue();
        boolean canAfford = account.getBalance() >= price * modifier;
        if (canAfford) {
            account.withdraw(price * modifier);
            storage.getGoods().put(type, storage.getGoods().get(type) + amount);
        }
        return canAfford;
    }

    /**
     * Makes the faction pay anything with the resource
     *
     * @param price
     * a Map of resources and their amount
     * @return
     * true if the faction can afford the price, false if not
     */
    public boolean chargeResource(Map<Resource, Integer> price) {
        for (Entry<Resource, Integer> entry : price.entrySet()) {
            if (!canAfford(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        for (Entry<Resource, Integer> entry : price.entrySet()) {
            if (entry.getKey() == Resource.TAXES) {
                boolean has = account.getBalance() >= entry.getValue();
                account.withdraw(entry.getValue());
                return has;
            } else if (entry.getKey() == Resource.MANPOWER) {
                // TODO
            } else {
                storage.getGoods().put(entry.getKey(), storage.getGoods().get(entry.getKey()) - entry.getValue());
            }
        }
        return true;
    }

    /**
     * Makes the faction pay anything with the resource
     *
     * @param type
     * the resource type to withdraw
     * @param price
     * the amount of resources to withdraw
     * @return
     * true if the faction can afford the price, false if not
     */
    public boolean chargeResource(Resource type, int price) {
        if (!canAfford(type, price)) {
            return false;
        }
        if (type == Resource.TAXES) {
            boolean has = account.getBalance() >= price;
            account.withdraw(price);
            return has;
        } else if (type == Resource.MANPOWER) {
            // TODO
        } else {
            storage.getGoods().put(type, storage.getGoods().get(type) - price);
        }
        return true;
    }

    /**
     * @param good
     * a good
     * @param amount
     * the amount of the good
     * @return
     * true if the faction can afford it, false if not
     */
    public boolean canAfford(Resource good, int amount) {
        if (good == Resource.TAXES) {
            return account.getBalance() >= amount;
        } else if (good == Resource.MANPOWER) {
            return true; // TODO
        }
        return storage.getGoods().get(good) >= amount;
    }

    /**
     * Deactivates the faction.
     */
    public void disband() {
        disband(true);
    }

    /**
     * Deactivates the faction.
     *
     * @param unclaim
     * if the land owner of the land of this faction shall be set to null.
     */
    public void disband(boolean unclaim) {
        active = false;
        open = false;
        home = null;
        if (fConfig.areHologramsEnabled()) {
            homeHolo.delete();
        }
        capital = null;
        if (unclaim) {
            for (Region region : regions) {
                region.setOwner(null);
            }
        }
        regions.clear();
        admin = null;
        mods.clear();
        members.clear();
        invited.clear();
        chunks.clear();
        relations.clear();
        for (Faction faction : plugin.getFactionCache().getActive()) {
            faction.relations.remove(this);
        }
        ideaGroups.clear();
        ideas.clear();
        FTeamWrapper.applyUpdates(this);
    }

    /* Serialization */
    public void load() {
        active = config.getBoolean("active");
        name = config.getString("name");
        longName = config.getString("longName");
        desc = config.getString("desc");
        anthem = config.getString("anthem");
        banner = (BannerMeta) config.get("banner");
        bannerColor = (short) config.getInt("bannerColor");
        mapFillColor = config.getString("mapFillColor");
        mapLineColor = config.getString("mapLineColor");
        mapVisibility = config.getBoolean("mapVisibility");
        creationDate = config.getLong("creationDate");
        type = GovernmentType.valueOf(config.getString("type"));
        manpowerModifier = config.getInt("manpowerModifier", 0);
        setHome((Location) config.get("home"));
        capital = plugin.getBoard().getById(config.getInt("capital"));

        admin = Bukkit.getOfflinePlayer(UUID.fromString(config.getString("admin")));
        for (String mod : config.getStringList("mods")) {
            mods.add(Bukkit.getOfflinePlayer(UUID.fromString(mod)));
        }
        for (String member : config.getStringList("members")) {
            members.add(Bukkit.getOfflinePlayer(UUID.fromString(member)));
        }

        for (Entry<String, Object> entry : ConfigUtil.getMap(config, "relations").entrySet()) {
            relations.put(plugin.getFactionCache().getById(NumberUtil.parseInt(entry.getKey())), Relation.valueOf((String) entry.getValue()));
        }

        for (Region region : plugin.getBoard().getRegions()) {
            if (region.getOwner() == this) {
                regions.add(region);
            }
        }
        for (Region region : regions) {
            chunks.addAll(region.getChunks());
        }

        if (fConfig.isEconomyEnabled()) {
            account = new FAccount(this);
        }
        tradeMenu = new TradeMenu(this);

        storage = new FStorage(this, ConfigUtil.getMap(config, "storage"));
        if (storage == null) {
            storage = new FStorage(this);
        }

        for (Entry<String, Object> entry : ConfigUtil.getMap(config, "groceryList").entrySet()) {
            if (EnumUtil.isValidEnum(Resource.class, entry.getKey())) {
                groceryList.put(Resource.valueOf(entry.getKey()), (int) entry.getValue());
            }
        }

        populationMenu = new PopulationMenu(this);

        List<String> groups = config.getStringList("ideaGroups");
        if (groups != null) {
            for (String group : groups) {
                if (EnumUtil.isValidEnum(IdeaGroup.class, group)) {
                    ideaGroups.add(IdeaGroup.valueOf(group));
                }
            }
        }
        List<String> ideas = config.getStringList("ideas");
        if (ideas != null) {
            for (String idea : ideas) {
                this.ideas.add(IdeaGroup.ideaValueOf(idea));
            }
        }
        ideaMenu = new IdeaMenu(this);
    }

    public void save() {
        config.set("active", active);
        config.set("name", name);
        config.set("longName", longName);
        config.set("desc", desc);
        config.set("anthem", anthem);
        config.set("banner", banner);
        config.set("bannerColor", bannerColor);
        config.set("mapFillColor", mapFillColor);
        config.set("mapLineColor", mapLineColor);
        config.set("mapVisibility", mapVisibility);
        config.set("creationDate", creationDate);
        config.set("type", type.toString());
        if (!active) {
            try {
                config.save(file);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            return;
        }
        config.set("home", home);
        if (homeHolo != null) {
            homeHolo.delete();
        }
        config.set("capital", capital.getId());
        config.set("admin", admin.getUniqueId().toString());

        List<String> modIds = new ArrayList<>();
        for (OfflinePlayer mod : mods) {
            modIds.add(mod.getUniqueId().toString());
        }
        config.set("mods", modIds);
        List<String> memberIds = new ArrayList<>();
        for (OfflinePlayer member : members) {
            memberIds.add(member.getUniqueId().toString());
        }
        config.set("members", memberIds);
        if (storage == null) {
            storage = new FStorage(this);
        }

        String relPath = "relations";
        if (!config.contains(relPath)) {
            config.createSection(relPath);
        }
        for (Entry<Faction, Relation> entry : relations.entrySet()) {
            config.set(relPath + "." + entry.getKey().getId(), entry.getValue().toString());
        }

        config.set("storage", storage.serialize());
        for (Entry<Resource, Integer> entry : groceryList.entrySet()) {
            config.set("groceryList." + entry.getKey(), entry.getValue());
        }
        /* 
         * TODO: POPULATION
         */
        List<String> ideaGroupIds = new ArrayList<>();
        for (IdeaGroup ideaGroup : ideaGroups) {
            ideaGroupIds.add(ideaGroup.toString());
        }
        config.set("ideaGroups", ideaGroupIds);
        List<String> ideaIds = new ArrayList<>();
        for (Idea idea : ideas) {
            ideaIds.add(idea.toString());
        }
        config.set("ideas", ideaIds);

        try {
            config.save(file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
