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
package de.erethon.factionsxl.faction;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.config.ConfigUtil;
import de.erethon.commons.misc.EnumUtil;
import de.erethon.commons.misc.NumberUtil;
import de.erethon.commons.player.PlayerCollection;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.board.dynmap.DynmapStyle;
import de.erethon.factionsxl.building.BuildSite;
import de.erethon.factionsxl.building.effects.StatusEffect;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.economy.*;
import de.erethon.factionsxl.entity.FEntity;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.entity.Request;
import de.erethon.factionsxl.idea.Idea;
import de.erethon.factionsxl.idea.IdeaGroup;
import de.erethon.factionsxl.idea.IdeaMenu;
import de.erethon.factionsxl.player.Dynasty;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.population.PopulationLevel;
import de.erethon.factionsxl.population.PopulationMenu;
import de.erethon.factionsxl.population.SaturationLevel;
import de.erethon.factionsxl.scoreboard.FTeamWrapper;
import de.erethon.factionsxl.util.LazyChunk;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.CasusBelli;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.WarParty;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static java.lang.Math.round;

/**
 * Represents a faction.
 *
 * @author Daniel Saukel
 */
public class Faction extends LegalEntity {

    FactionsXL plugin = FactionsXL.getInstance();
    FConfig fConfig = plugin.getFConfig();

    File file;
    FileConfiguration config;
    boolean active;
    String mapFillColor = "#E0E0E0";
    String mapLineColor = "#FFFFFF";
    String mapIcon = "redflag";
    DynmapStyle dynmapStyle;
    boolean mapVisibility = true;
    boolean invincible = false;
    GovernmentType type;
    boolean open;
    double prestige;
    int stabilityBase;
    double exhaustion;
    double manpowerModifier;
    Location home;
    Hologram homeHolo;
    Region capital;
    long timeLastCapitalMove;
    long timeLastPeace;
    int scoreLastPeace;
    Set<LazyChunk> chunks = new HashSet<>();
    Set<Region> regions = new HashSet<>();
    PlayerCollection formerAdmins = new PlayerCollection();
    PlayerCollection mods = new PlayerCollection();
    PlayerCollection members = new PlayerCollection();
    PlayerCollection invited = new PlayerCollection();
    Map<Faction, Relation> relations = new HashMap<>();
    EconomyMenu economyMenu;
    TradeMenu tradeMenu;
    FStorage storage;
    Map<Resource, Integer> groceryList = new HashMap<>();
    Set<StatusEffect> effects = new HashSet<>();
    IdeaMenu ideaMenu;
    Set<IdeaGroup> ideaGroups = new HashSet<>();
    Set<Idea> ideas = new HashSet<>();
    Set<BuildSite> buildings = new HashSet<>();
    Set<CasusBelli> casusBelli = new HashSet<>();
    Set<War> callsToArms = new HashSet<>();
    boolean allod = true;

    public Faction(File file) {
        id = NumberUtil.parseInt(file.getName().replace(".yml", ""));
        this.file = file;
        config = YamlConfiguration.loadConfiguration(file);
        active = config.getBoolean("active");
    }

    public Faction(int id) {
        this.id = id;
        file = new File(FactionsXL.FACTIONS, id + ".yml");
        config = YamlConfiguration.loadConfiguration(file);
        active = config.getBoolean("active");
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
        if (active) {
            plugin.getFactionCache().removeEntity(this);
            plugin.getFactionCache().addEntity(this);
        }
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
     * @return
     * the Dynmap home icon
     */
    public String getMapIcon() {
        return mapIcon;
    }

    /**
     * @param fill
     * the Dynmap fill color to set
     * @param line
     * the Dynmap line color to set
     * @param icon
     * the Dynmap icon name to set
     */
    public void setMapStyle(String fill, String line, String icon) {
        if (fill.matches("#[0-9A-F]{6}") && line.matches("#[0-9A-F]{6}")) {
            mapFillColor = fill;
            mapLineColor = line;
            if (icon != null) {
                mapIcon = icon;
            }
            dynmapStyle = new DynmapStyle(DynmapStyle.DEFAULT_STYLE).setStrokeColor(mapLineColor).setFillColor(mapFillColor).setHomeMarker(mapIcon);
        }
    }

    /**
     * @return
     * the dynmap style of the faction
     */
    public DynmapStyle getDynmapStyle() {
        if (dynmapStyle == null) {
            dynmapStyle = new DynmapStyle(DynmapStyle.DEFAULT_STYLE).setStrokeColor(mapLineColor).setFillColor(mapFillColor).setHomeMarker(mapIcon);
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
     * if this faction can be attacked
     */
    public boolean isInvincible() {
        return invincible;
    }

    public long getTimeLastPeace() {
        return timeLastPeace;
    }

    public void setTimeLastPeace(long timeLastPeace) {
        this.timeLastPeace = timeLastPeace;
    }

    public int getScoreLastPeace() {
        return scoreLastPeace;
    }

    public void setScoreLastPeace(int scoreLastPeace) {
        this.scoreLastPeace = scoreLastPeace;
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
        double power = 0D;
        for (UUID member : members.getUniqueIds()) {
            Double d = plugin.getFData().power.get(member);
            if (d != null) {
                power += d;
            }
        }
        return (int) power;
    }

    /**
     * @return
     * the stability value
     */
    public int getStability() {
        int base = 50;
        int exhaustionExponent = 1;
        int sizeExponent = 1;
        int sizeExemptAmount = fConfig.getStabilityRegionExempt();
        double powerPerRegion = fConfig.getPowerPerRegion();
        double powerRegionRatio = getPower() / (regions.size() * powerPerRegion);
        double warExhaustion = Math.pow((-1 * exhaustion) * exhaustion, exhaustionExponent);
        double factionSize = Math.pow((-1 * (regions.size() - sizeExemptAmount)), sizeExponent);
        double power = getPower() / (regions.size() * powerPerRegion) * powerRegionRatio;
        int i = base + (int) round(warExhaustion + factionSize + power);

        if (!members.contains(admin)) {
            i = i - 25;
        }
        /*for (ResourceSubcategory category : ResourceSubcategory.values()) {
            i += isSubcategorySaturated(category).getStabilityBonus();
        }*/
        return i;
    }

    /**
     * @return
     * the stability value with all modifiers as hover texts
     */
    public BaseComponent[] getStabilityModifiers(ChatColor c) {
        int base = 50;
        int exhaustionExponent = 1;
        int sizeExponent = 1;
        int sizeExemptAmount = fConfig.getStabilityRegionExempt();
        double powerPerRegion = fConfig.getPowerPerRegion();
        double powerRegionRatio = getPower() / (regions.size() * powerPerRegion);
        double warExhaustion = Math.pow((-1 * exhaustion) * exhaustion, exhaustionExponent);
        double factionSize = Math.pow((-1 * (regions.size() - sizeExemptAmount)), sizeExponent);
        double pow = getPower() / (regions.size() * powerPerRegion) * powerRegionRatio;
        String stability = FMessage.CMD_SHOW_STABILITY.getMessage() + c + getStability();
        String b = FMessage.CMD_SHOW_STABILITY_MOD_BASE.getMessage() + color(base) + "\n";
        String exhaustion = ChatColor.RESET + FMessage.CMD_SHOW_STABILITY_MOD_EXHAUSTION.getMessage() + color((int) Math.round(warExhaustion)) + "\n";
        String size = ChatColor.RESET + FMessage.CMD_SHOW_STABILITY_MOD_PROVINCES.getMessage() + color((int) Math.round(factionSize)) + "\n";
        String adminNotMember = ChatColor.RESET + FMessage.CMD_SHOW_STABILITY_MOD_ABSENT_MONARCH.getMessage() + color(members.contains(admin) ? 0 : -25) + "\n";
        String power = ChatColor.RESET + FMessage.CMD_SHOW_STABILITY_MOD_POWER.getMessage();
        power += color((int) Math.round(pow)) + "" + ChatColor.DARK_GRAY + " (" + ChatColor.GRAY + Math.round(powerRegionRatio * 100) + "%" + ChatColor.DARK_GRAY + ")" + "\n";
        int i = 0;
        /*for (ResourceSubcategory category : ResourceSubcategory.values()) {
            i += isSubcategorySaturated(category).getStabilityBonus();
        }
        String wealth = ChatColor.RESET + FMessage.CMD_SHOW_STABILITY_MOD_WEALTH.getMessage() + color(i);*/

        BaseComponent[] msg = TextComponent.fromLegacyText(stability);
        for (BaseComponent component : msg) {
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(b + exhaustion + size + adminNotMember + power)));
        }
        return msg;
    }

    private String color(int i) {
        if (i > 0) {
            return ChatColor.GREEN + "+" + String.valueOf(i);
        } else if (i < 0) {
            return ChatColor.DARK_RED + String.valueOf(i);
        } else {
            return ChatColor.YELLOW + String.valueOf(i);
        }
    }

    /**
     * @param stability
     * the stability value to set
     */
    public void setStabilityBase(int stability) {
        this.stabilityBase = stability;
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
     * the amount of people without the manpower modifier
     */
    public int getPopulation() {
        int manpower = 0;
        for (Region region : regions) {
            manpower += region.getTotalPopulation();
        }
        return manpower;
    }

    /**
     * @return
     * the amount of people on a certain level
     */
    public int getPopulation(PopulationLevel level) {
        int total = 0;
        for (Region region : regions) {
            total += region.getPopulation(level);
        }
        return total;
    }

    /**
     * @return
     * the manpower value
     */
    public int getManpower() {
        int manpower = 0;
        for (Region region : regions) {
            manpower += region.getTotalPopulation() * (manpowerModifier / 100);
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
        timeLastCapitalMove = System.currentTimeMillis();
    }

    /**
     * @return
     * the time millis when the capital was moved for the last time
     */
    public long getTimeLastCapitalMove() {
        return timeLastCapitalMove;
    }

    /**
     * @return
     * a Set of all chunks owned by this faction
     */
    public Set<LazyChunk> getChunks() {
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
     * the territory worth of this faction (currently just amount of chunks, might get expanded)
     */
    public int getExpansion() {
        Set<Region> rg = regions;
        int value = 0;
        for (Region r : rg) {
            if (r.getCoreFactions().containsKey(this)) {
                value = round(value + (r.getSize() / (float) 2));       // Core Regions only count / 2
            }
            else {
                value = round(value + (r.getSize()));
            }
        }
        return value;
    }


    @Override
    public void setAdmin(OfflinePlayer admin) {
        formerAdmins.add(this.admin);
        super.setAdmin(admin);
        checkForPersonalUnions();
    }

    /**
     * @return
     * the players who used to be faction admins
     */
    public PlayerCollection getFormerAdmins() {
        return formerAdmins;
    }

    /**
     * Updates personal unions.
     */
    public void checkForPersonalUnions() {
        HashSet<Faction> toRemove = new HashSet<>();
        for (Entry<Faction, Relation> entry : relations.entrySet()) {
            if (entry.getValue() == Relation.PERSONAL_UNION && !entry.getKey().admin.equals(admin)) {
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
            if (faction.admin.equals(admin)) {
                relations.put(faction, Relation.PERSONAL_UNION);
                faction.relations.put(this, Relation.PERSONAL_UNION);
                ParsingUtil.broadcastMessage(FMessage.FACTION_PERSONAL_UNION_FORMED.getMessage(), this, faction, plugin.getFPlayerCache().getByUniqueId(admin).getName());
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
        return plugin.getFPlayerCache().getByUniqueId(admin);
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
     * a PlayerCollection of all mods as OfflinePlayers
     */
    public PlayerCollection getMods() {
        return mods;
    }

    /**
     * @return
     * a Set of all mods as FPlayerCache
     */
    public Set<FPlayer> getFMods() {
        HashSet<FPlayer> fPlayers = new HashSet<>();
        for (UUID member : mods.getUniqueIds()) {
            fPlayers.add(plugin.getFPlayerCache().getByUniqueId(member));
        }
        return fPlayers;
    }

    /**
     * @return
     * a Set of all mods that are online
     */
    public Collection<Player> getOnlineMods() {
        return mods.getOnlinePlayers();
    }

    @Override
    public PlayerCollection getRequestAuthorizedPlayers(Class<? extends Request> type) {
        PlayerCollection players = super.getRequestAuthorizedPlayers(type);
        players.addAll(mods);
        return players;
    }

    /**
     * @return
     * a Set of all members as OfflinePlayers
     */
    public PlayerCollection getMembers() {
        return members;
    }

    /**
     * @return
     * a Set of all members as FPlayerCache
     */
    public Collection<FPlayer> getFPlayers() {
        HashSet<FPlayer> fPlayers = new HashSet<>();
        for (UUID member : members.getUniqueIds()) {
            fPlayers.add(plugin.getFPlayerCache().getByUniqueId(member));
        }
        return fPlayers;
    }

    /**
     * @return
     * a Set of all members that are not mod or admin as OfflinePlayers
     */
    public Collection<OfflinePlayer> getNonPrivilegedMembers() {
        HashSet<OfflinePlayer> players = new HashSet<>();
        for (UUID member : members.getUniqueIds()) {
            if (!mods.contains(member) && !member.equals(admin)) {
                players.add(Bukkit.getOfflinePlayer(member));
            }
        }
        return players;
    }

    /**
     * @return
     * a Set of all members that are not mod or admin as FPlayerCache
     */
    public Collection<FPlayer> getNonPrivilegedFPlayers() {
        HashSet<FPlayer> fPlayers = new HashSet<>();
        for (UUID member : members.getUniqueIds()) {
            FPlayer fPlayer = plugin.getFPlayerCache().getByUniqueId(member);
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
    public Collection<Player> getOnlineMembers() {
        return members.getOnlinePlayers();
    }

    /**
     * @param relation
     * the relation type
     * @return
     * a Set of all related players that are online
     */
    public Collection<Player> getOnlineByRelation(Relation relation) {
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
    public PlayerCollection getInvitedPlayers() {
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
    public Relation getRelation(FEntity object) {
        if (object == null) {
            return Relation.PEACE;
        }
        Faction faction = null;
        if (object instanceof FPlayer) {
            if (((FPlayer) object).hasFaction()) {
                faction = ((FPlayer) object).getFaction();
            }
        } else if (object instanceof Faction) {
            faction = (Faction) object;
        }
        if (faction == null) {
            return Relation.PEACE;
        }

        Relation relation = null;
        if (relations.containsKey(faction)) {
            relation = relations.get(faction);
        } else if (faction == this) {
            return Relation.OWN;
        } else if (isInWar(faction)) {
            return Relation.ENEMY;
        } else if (getLords().contains(faction)) {
            return Relation.LORD;
        } else {
            relation = Relation.PEACE;
        }

        Faction lord = getLord();
        Faction objectLord = faction.getLord();
        if (objectLord != null) {
            if (objectLord == lord) {
                // If object & subject share the same lord => they get an ALLIANCE_2
                return Relation.ALLIANCE_2;
            } else if (objectLord.getRelation(this) == Relation.LORD) {
                // Object's lord is also lord of subject, but NOT subject's direct lord => subject must be an indirect vassal of object's lord
                return Relation.VASSAL;
            }
        }
        if (lord != null && !relation.doVassalsOverride()) {
            // If the subject's relation to the object is not supposed to override the lord's relation to the object, use the lord's...
            Relation lordRelation = lord.getRelation(faction);
            if (!lordRelation.doVassalsOverride()) {
                // ...but only if the lord's relation is supposed to override the vassal's
                return lordRelation;
            }
        }
        Faction objectOverlord = faction.getOverlord();
        if (relation == Relation.PEACE && getLords().contains(objectOverlord)) {
            return Relation.ALLIANCE_2;
        }
        if (relation == Relation.PEACE && relations.containsKey(objectOverlord)) {
            Relation overlordRelation = relations.get(objectOverlord);
            if (!overlordRelation.doVassalsOverride()) {
                return overlordRelation;
            }
        }

        return relation != null ? relation : Relation.PEACE;
    }

    /**
     * @param relation
     * the relation type
     * @return
     * all factions that have the specified relation to this faction
     */
    public Collection<Faction> getRelatedFactions(Relation relation) {
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
     * if the faction is a vassal
     */
    public boolean isVassal() {
        return getLord() != null;
    }

    /**
     * @return
     * the lord faction
     */
    public Faction getLord() {
        for (Entry<Faction, Relation> entry : relations.entrySet()) {
            if (entry.getValue() == Relation.LORD) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * @return
     * the independent faction that is, directly or indirectly, this faction's lord
     */
    public Faction getOverlord() {
        Faction current = this;
        Faction next = current.getLord();
        while (next != null) {
            current = next;
            next = current.getLord();
        }
        return current;
    }

    /**
     * @return
     * a list of all direct and indirect lords. The lower the index, the closer
     */
    public List<Faction> getLords() {
        Faction current = this;
        Faction next = current.getLord();
        List<Faction> lords = new ArrayList<>();
        while (next != null) {
            current = next;
            lords.add(current);
            next = current.getLord();
        }
        return lords;
    }

    /**
     * @return
     * the economy menu of the faction
     */
    public EconomyMenu getEconomyMenu() {
        return economyMenu;
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

    public Map<Resource, Integer> getTotalConsumableResources() {
        Map<Resource, Integer> resources = new HashMap<>();
        for (Region region : getRegions()) {
            resources.putAll(region.getConsumableResources());
        }
        return resources;
    }

    /**
     * @return
     * a set of StatusEffects that affect this faction
     */
    public Set<StatusEffect> getEffects() {
        return effects;
    }

    /**
     * @param resource
     * @return the modifier for this resource after applying all effects
     */
    public double getTotalModifierForResource(Resource resource) {
        double modifier = 0;
        for (StatusEffect effect : getEffects()) {
            if (effect.getProductionModifier().containsKey(resource)) {
                modifier = modifier + effect.getProductionModifier().get(resource);
            }
            if (effect.getConsumptionModifier().containsKey(resource)) {
                modifier = modifier + effect.getConsumptionModifier().get(resource);
            }
        }
        return modifier;
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
     * @return
     * the the faction-wide buildings this faction has completed.
     */
    public Set<BuildSite> getFactionBuildings() {
        return buildings;
    }

    /**
     * @return
     * the casus belli of this faction
     */
    public Set<CasusBelli> getCasusBelli() {
        return casusBelli;
    }

    /**
     * @return
     * if the faction is an allod
     */
    public boolean isAllod() {
        return allod;
    }

    /**
     * @param allod
     * set if the faction is an allod
     */
    public void setAllod(boolean allod) {
        this.allod = allod;
    }

    /**
     * @return
     * true if the faction is in war
     */
    public boolean isInWar() {
        return !plugin.getWarCache().getByFaction(this).isEmpty();
    }

    @Override
    public boolean isInWar(FEntity object) {
        Set<War> wars = plugin.getWarCache().getByFaction(this);
        if (object instanceof Faction) {
            for (War war : wars) {
                Set<Faction> factions = war.getAttacker().getFactions().contains(this) ? war.getDefender().getFactions() : war.getAttacker().getFactions();
                if (factions.contains((Faction) object)) {
                    return true;
                }
            }
        } else if (object instanceof FPlayer) {
            for (War war : wars) {
                Set<Faction> factions = war.getAttacker().getFactions().contains(this) ? war.getDefender().getFactions() : war.getAttacker().getFactions();
                for (Faction faction : factions) {
                    if (faction.getMembers().contains((FPlayer) object)) {
                        return true;
                    }
                }
            }
        }
        return false;
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
        return mods.contains(player.getUniqueId()) || admin.equals(player.getUniqueId()) || FPermission.hasPermission(sender, FPermission.BYPASS);
    }

    /**
     * @param fPlayer
     * the FPlayer to check
     * @return
     * true if the player is admin or mod
     */
    public boolean isPrivileged(FPlayer fPlayer) {
        return fPlayer.isMod(this) || admin.equals(fPlayer.getUniqueId()) || FPermission.hasPermission(fPlayer.getPlayer(), FPermission.BYPASS);
    }

    /**
     * @return
     * the war parties that this faction is part of
     */
    public Set<WarParty> getWarParties() {
        Set<WarParty> parties = new HashSet<>();
        for (War war : plugin.getWarCache().getByFaction(this)) {
            if (war.getAttacker().getFactions().contains(this)) {
                parties.add(war.getAttacker());
            } else if (war.getDefender().getFactions().contains(this)) {
                parties.add(war.getDefender());
            }
        }
        return parties;
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
        Player admin = Bukkit.getPlayer(this.admin);
        if (admin != null && !getOnlineMembers().contains(admin)) {
            ParsingUtil.sendMessage(admin, "&a[" + name + "] &r" + message, args);
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
        MessageUtil.sendMessage(player, confirm, new TextComponent(" "), deny);
    }

    /**
     * @param kicker
     * the CommandSender who kicked this member
     * @param member
     * the member to kick
     */
    public void kick(CommandSender kicker, OfflinePlayer member) {
        sendMessage(FMessage.FACTION_PLAYER_KICKED.getMessage(), member, kicker);
        members.remove(member.getUniqueId());
        mods.remove(member.getUniqueId());
    }

    /**
     * Kick by auto task
     *
     * @param member
     * the member to kick
     */
    public void kick(OfflinePlayer member) {
        sendMessage(FMessage.FACTION_PLAYER_KICKED_AUTO.getMessage(), member);
        members.remove(member.getUniqueId());
        mods.remove(member.getUniqueId());
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
                int oldAmount = storage.getGoods().get(entry.getKey()) != null ? storage.getGoods().get(entry.getKey()) : 0;
                storage.getGoods().put(entry.getKey(), oldAmount + entry.getValue());
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
            int oldAmount = storage.getGoods().get(type) != null ? storage.getGoods().get(type) : 0;
            storage.getGoods().put(type, oldAmount + amount);
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
        if (isVassal()) {
            plugin.getFactionCache().integrate(getLord(), this);
            return;
        }
        active = false;
        open = false;
        home = null;
        if (fConfig.areHologramsEnabled()) {
            homeHolo.delete();
        }
        capital = null;
        if (unclaim) {
            new HashSet<>(regions).forEach(r -> r.setOwner(null));
        }
        formerAdmins.add(admin);
        setAdmin(null);
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
        for (War war : plugin.getWarCache().getWars()) {
            war.getAttacker().getParticipants().remove(this);
            war.getDefender().getParticipants().remove(this);
        }
        plugin.getFactionCache().removeEntity(this);
        plugin.getFactionCache().addEntity(this);
        save();
    }

    /**
     * When the faction leader resigns / gets kicked automatically
     */
    public void doSuccession() {
        /*if (type == GovernmentType.MONARCHY) {
            // Dynasty member
        } else if (type == GovernmentType.REPUBLIC) {
            // Reelect
        } else if (type == GovernmentType.THEOCRACY) {
            // Random?
        }*/
        FPlayer newAdmin = null;
        for (OfflinePlayer mod : mods.getOfflinePlayers()) {
            FPlayer fPlayer = plugin.getFPlayerCache().getByPlayer(mod);
            if (newAdmin == null || fPlayer.getData().getTimeLastPlayed() > newAdmin.getData().getTimeLastPlayed() || mod.isOnline()) {
                newAdmin = fPlayer;
            }
        }
        if (newAdmin == null) {
            disband();
        }
    }

    /* Serialization */
    public void load() {
        name = config.getString("name");
        longName = config.getString("longName");
        shortName = config.getString("shortName");
        desc = config.getString("desc");
        anthem = config.getString("anthem");
        banner = (BannerMeta) config.get("banner");
        bannerType = Material.getMaterial(config.getString("bannerType", Material.WHITE_BANNER.name()));
        mapFillColor = config.getString("mapFillColor");
        mapLineColor = config.getString("mapLineColor");
        mapIcon = config.getString("mapIcon");
        mapVisibility = config.getBoolean("mapVisibility");
        invincible = config.getBoolean("invincible");
        creationDate = config.getLong("creationDate");
        type = GovernmentType.valueOf(config.getString("type"));
        open = config.getBoolean("open");
        stabilityBase = config.getInt("stability");
        exhaustion = config.getDouble("exhaustion");
        manpowerModifier = config.getDouble("manpowerModifier", fConfig.getDefaultManpowerModifier());
        setHome((Location) config.get("home"));
        capital = plugin.getBoard().getById(config.getInt("capital"));
        timeLastCapitalMove = config.getLong("timeLastCapitalMove", 0);
        if (config.contains("timeLastPeace")) {
            timeLastPeace = config.getLong("timeLastPeace", timeLastPeace);
        }
        if (config.contains("scoreLastPeace")) {
            scoreLastPeace = config.getInt("scoreLastPeace", scoreLastPeace);
        }

        admin = UUID.fromString(config.getString("admin"));
        mods.add(config.getStringList("mods"));
        members.add(config.getStringList("members"));

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
        economyMenu = new EconomyMenu(this);
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
        ConfigurationSection bs = config.getConfigurationSection("buildSites");
        if (bs != null) {
            for (String b : bs.getKeys(false)) {
                buildings.add(new BuildSite(config.getConfigurationSection("buildSites." + b)));
            }
        }
        ConfigurationSection cbs = config.getConfigurationSection("casusBelli");
        if (cbs != null) {
            for (String cb : cbs.getKeys(false)) {
                casusBelli.add(new CasusBelli(config.getConfigurationSection("casusBelli." + cb)));
            }
        }
        for (long date : config.getLongList("callsToArms")) {
            War war = plugin.getWarCache().getByDate(date);
            if (war != null) {
                callsToArms.add(war);
            }
        }
        allod = config.getBoolean("isAllod", true);
        requests = (List<Request>) config.getList("requests", new ArrayList<>());
        FactionsXL.debug("Loaded " + this);
    }

    public void save() {
        try {
            config.set("active", active);
            config.set("name", name);
            config.set("longName", longName);
            config.set("shortName", shortName);
            config.set("desc", desc);
            config.set("anthem", anthem);
            config.set("banner", banner);
            config.set("bannerType", bannerType.name());
            config.set("mapFillColor", mapFillColor);
            config.set("mapLineColor", mapLineColor);
            config.set("mapIcon", mapIcon);
            config.set("mapVisibility", mapVisibility);
            config.set("invincible", invincible);
            config.set("creationDate", creationDate);
            config.set("type", type.toString());
            config.set("open", open);
            config.set("stability", stabilityBase);
            config.set("exhaustion", exhaustion);
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
            config.set("timeLastCapitalMove", timeLastCapitalMove);
            config.set("timeLastPeace", timeLastPeace);
            config.set("scoreLastPeace", scoreLastPeace);
            config.set("admin", admin.toString());
            config.set("formerAdmins", formerAdmins.serialize());

            config.set("mods", mods.serialize());
            config.set("members", members.serialize());
            if (storage == null) {
                storage = new FStorage(this);
            }

            String relPath = "relations";
            if (!config.contains(relPath)) {
                config.createSection(relPath);
            }
            config.set("relations", null);
            for (Entry<Faction, Relation> entry : relations.entrySet()) {
                config.set(relPath + "." + entry.getKey().getId(), entry.getValue().toString());
            }

            config.set("storage", storage.serialize());
            for (Entry<Resource, Integer> entry : groceryList.entrySet()) {
                config.set("groceryList." + entry.getKey(), entry.getValue());
            }
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
            int i = 0;
            for (BuildSite b : buildings) {
                config.set("buildSites." + i, b.serialize());
                i++;
            }
            int i2 = 0;
            for (CasusBelli cb : casusBelli) {
                config.set("casusBelli." + i2, cb.serialize());
                i2++;
            }
            config.set("isAllod", allod);
            config.set("requests", requests);

            config.save(file);
        } catch (Exception exception) {
            MessageUtil.log(plugin, "An error occured while saving " + this);
            exception.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Faction{ID=" + id + "; name=" + name + "; active=" + active + "}";
    }

}
