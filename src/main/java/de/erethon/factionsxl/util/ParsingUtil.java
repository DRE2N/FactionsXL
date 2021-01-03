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
package de.erethon.factionsxl.util;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.FEntity;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.WarParty;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Partially adapted from FactionsOne by Sataniel.
 *
 * @author Daniel Saukel
 */
public enum ParsingUtil {

    FACTION_ADMIN("%faction_admin%"),
    FACTION_BALANCE("%faction_balance%"),
    FACTION_CAPITAL("%faction_capital%"),
    FACTION_DESCRIPTION("%faction_description%"),
    FACTION_GOVERNMENT_TYPE("%faction_gov_type%"),
    FACTION_LONG_TAG("%faction_long_tag%"),
    FACTION_MEMBER_LIST("%faction_member_list%"),
    FACTION_MOD_LIST("%faction_mod_list%"),
    FACTION_ONLINE_COUNT("%faction_online%"),
    FACTION_POWER("%faction_power%"),
    FACTION_PLAYER_COUNT("%faction_player_count%"),
    FACTION_PLAYER_LIST("%faction_player_list%"),
    FACTION_PROVINCE_COUNT("%faction_province_count%"),
    FACTION_SHORT_TAG("%faction_short_tag%"),
    FACTION_STABILITY("%faction_stability%"),
    FACTION_TAG("%faction_tag%"),
    FEDERATION_TAG("%federation_tag%"),
    PLAYER_BALANCE("%player_balance%"),
    PLAYER_DYNASTY("%player_dynasty%"),
    PLAYER_NAME("%player_name%"),
    PLAYER_POWER("%player_power%"),
    PLAYER_PREFIX("%player_prefix%"),
    PLAYER_TITLE("%player_title%"),
    REGION_CORE_LIST("%region_core_list%"),
    REGION_CLAIM_LIST("%region_claim_list%"),
    REGION_LEVEL("%region_level%"),
    REGION_NAME("%region_name%"),
    REGION_OWNER("%region_owner%"),
    REGION_POPULATION("%region_population%"),
    REGION_SIZE("%region_size%"),
    REGION_TYPE("%region_type%"),
    RELATION("%relation%"),
    RELATION_COLOR("%relation_color%"),
    WAR_ATTACKER("%war_attacker%"),
    WAR_ATTACKER_DEATHS("%war_attacker_deaths%"),
    WAR_ATTACKER_FIGHTS("%war_attacker_fights%"),
    WAR_ATTACKER_KD("%war_attacker_kd%"),
    WAR_ATTACKER_KILLS("%war_attacker_kills%"),
    WAR_ATTACKER_LIST("%war_attacker_list%"),
    WAR_ATTACKER_POINTS("%war_attacker_points%"),
    WAR_CASUS_BELLI("%war_cb%"),
    WAR_DEFENDER("%war_defender%"),
    WAR_DEFENDER_DEATHS("%war_defender_deaths%"),
    WAR_DEFENDER_FIGHTS("%war_defender_fights%"),
    WAR_DEFENDER_KD("%war_defender_kd%"),
    WAR_DEFENDER_KILLS("%war_defender_kills%"),
    WAR_DEFENDER_LIST("%war_defender_list%"),
    WAR_DEFENDER_POINTS("%war_defender_points%"),
    WAR_ENEMY("%war_enemy%"),
    WAR_ENEMY_DEATHS("%war_enemy_deaths%"),
    WAR_ENEMY_FIGHTS("%war_enemy_fights%"),
    WAR_ENEMY_KD("%warenemy_kd%"),
    WAR_ENEMY_KILLS("%war_enemy_kills%"),
    WAR_ENEMY_LIST("%war_enemy_list%"),
    WAR_ENEMY_POINTS("%war_enemy_points%"),
    WAR_PLAYER_PARTY("%war_player_party%"),
    WAR_PLAYER_PARTY_DEATHS("%war_player_party_deaths%"),
    WAR_PLAYER_PARTY_FIGHTS("%war_player_party_fights%"),
    WAR_PLAYER_PARTY_KD("%war_player_party_kd%"),
    WAR_PLAYER_PARTY_KILLS("%war_player_party_kills%"),
    WAR_PLAYER_PARTY_LIST("%war_player_party_list%"),
    WAR_PLAYER_PARTY_POINTS("%war_player_party_points%"),
    // External
    PERM_PREFIX("%perm_prefix%"),
    PERM_SUFFIX("%perm_suffix%");

    private String placeholder;

    ParsingUtil(String placeholder) {
        this.placeholder = placeholder;
    }

    /* Getters and setters */
    /**
     * @return the placeholder
     */
    public String getPlaceholder() {
        return placeholder;
    }

    @Override
    public String toString() {
        return placeholder;
    }

    /* Statics */
    /**
     * Replace the placeholders that are relevant for the chat in a String automatically.
     *
     * @param string
     * the String that contains the placeholders
     * @param receiver
     * the faction the replacements are taken from
     */
    public static String replaceChatPlaceholders(String string, FPlayer sender, FPlayer receiver) {
        Faction faction = sender.getFaction();
        string = string.replace(PLAYER_NAME.getPlaceholder(), sender.getName());
        string = string.replace(PLAYER_PREFIX.getPlaceholder(), sender.getPrefix());
        string = string.replace(PLAYER_TITLE.getPlaceholder(), sender.getTitle());
        Relation relation = null;
        if (faction != null) {
            relation = faction.getRelation(receiver);
            string = string.replace(FACTION_LONG_TAG.getPlaceholder(), faction.getLongName());
            string = string.replace(FACTION_TAG.getPlaceholder(), faction.getName());
            string = string.replace(FACTION_SHORT_TAG.getPlaceholder(), faction.getShortName());
        } else {
            relation = Relation.PEACE;
            string = string.replace(FACTION_LONG_TAG.getPlaceholder(), FMessage.MISC_LONER.getMessage());
            string = string.replace(FACTION_TAG.getPlaceholder(), FMessage.MISC_LONER.getMessage());
            string = string.replace(FACTION_SHORT_TAG.getPlaceholder(), FMessage.MISC_LONER.getMessage());
        }
        string = string.replace(RELATION.getPlaceholder(), relation.getName());
        string = string.replace(RELATION_COLOR.getPlaceholder(), relation.getColor().toString());
        // External
        try {
            RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServicesManager().getRegistration(Chat.class);
            if (chatProvider != null) {
                Chat chat = chatProvider.getProvider();
                if (chat != null) {
                    string = string.replace(PERM_PREFIX.getPlaceholder(), chat.getPlayerPrefix(sender.getPlayer()));
                    string = string.replace(PERM_SUFFIX.getPlaceholder(), chat.getPlayerSuffix(sender.getPlayer()));
                }
            }
        } catch (NoClassDefFoundError error) {
        }
        if (sender.getPlayer().hasPermission("fxl.chat.color")) {
            return ChatColor.translateAlternateColorCodes('&', string);
        }
        else {
            return string;
        }
    }

    /**
     * Replace the faction placeholders in a String automatically.
     *
     * @param string
     * the String that contains the placeholders
     * @param faction
     * the faction the replacements are taken from
     */
    public static String replaceFactionPlaceholders(String string, Faction faction) {
        if (string.contains(FACTION_ADMIN.getPlaceholder()) && faction.getAdmin() != null) {
            string = string.replace(FACTION_ADMIN.getPlaceholder(), faction.getAdmin().getName());
        }
        if (string.contains(FACTION_BALANCE.getPlaceholder()) && FactionsXL.getInstance().getFConfig().isEconomyEnabled()) {
            string = string.replace(FACTION_BALANCE.getPlaceholder(), faction.getAccount().getFormatted());
        }
        string = string.replace(FACTION_CAPITAL.getPlaceholder(), faction.getCapital().getName());
        string = string.replace(FACTION_DESCRIPTION.getPlaceholder(), faction.getDescription());
        string = string.replace(FACTION_GOVERNMENT_TYPE.getPlaceholder(), faction.getGovernmentType().getName());
        string = string.replace(FACTION_LONG_TAG.getPlaceholder(), faction.getLongName());
        if (string.contains(FACTION_MEMBER_LIST.getPlaceholder())) {
            string = string.replace(FACTION_MEMBER_LIST.getPlaceholder(), namesToString(faction.getNonPrivilegedMembers()));
        }
        if (string.contains(FACTION_MOD_LIST.getPlaceholder())) {
            string = string.replace(FACTION_MOD_LIST.getPlaceholder(), collectionToString(faction.getMods().getNames()));
        }
        string = string.replace(FACTION_ONLINE_COUNT.getPlaceholder(), String.valueOf(faction.getOnlineMembers().size()));
        string = string.replace(FACTION_PLAYER_COUNT.getPlaceholder(), String.valueOf(faction.getMembers().size()));
        if (string.contains(FACTION_PLAYER_LIST.getPlaceholder())) {
            string = string.replace(FACTION_PLAYER_LIST.getPlaceholder(), collectionToString(faction.getMembers().getNames()));
        }
        string = string.replace(FACTION_POWER.getPlaceholder(), String.valueOf(faction.getPower()));
        string = string.replace(FACTION_PROVINCE_COUNT.getPlaceholder(), String.valueOf(faction.getRegions().size()));
        string = string.replace(FACTION_SHORT_TAG.getPlaceholder(), faction.getShortName());
        string = string.replace(FACTION_STABILITY.getPlaceholder(), String.valueOf(faction.getStability()));
        string = string.replace(FACTION_TAG.getPlaceholder(), faction.getName());
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Replace the region placeholders in a String automatically.
     *
     * @param string
     * the String that contains the placeholders
     * @param region
     * the region the replacements are taken from
     */
    public static String replaceRegionPlaceholders(String string, Region region) {
        if (string.contains(REGION_CLAIM_LIST.getPlaceholder())) {
            string = string.replace(REGION_CLAIM_LIST.getPlaceholder(), factionsToString(region.getClaimFactions().keySet()));
        }
        if (string.contains(REGION_CORE_LIST.getPlaceholder())) {
            string = string.replace(REGION_CORE_LIST.getPlaceholder(), factionsToString(region.getCoreFactions().keySet()));
        }
        string = string.replace(REGION_LEVEL.getPlaceholder(), String.valueOf(region.getLevel()));
        string = string.replace(REGION_NAME.getPlaceholder(), region.getName());
        string = string.replace(REGION_OWNER.getPlaceholder(), region.getOwner() != null ? region.getOwner().getName() : FMessage.MISC_WILDERNESS.getMessage());
        string = string.replace(REGION_POPULATION.getPlaceholder(), String.valueOf(region.getTotalPopulation()));
        string = string.replace(REGION_SIZE.getPlaceholder(), String.valueOf(region.getSize()));
        string = string.replace(REGION_TYPE.getPlaceholder(), region.getType().getName());
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Replace the relation placeholders in a String automatically.
     *
     * @param string
     * the String that contains the placeholders
     * @param standpoint
     * the standpoint of this Faction will be chosen for the relation purposes
     * @param object
     * the FPlayer to compare to the standpoint faction
     */
    public static String replaceRelationPlaceholders(String string, FEntity standpoint, FEntity object) {
        Relation relation = standpoint.getRelation(object);
        string = string.replace(RELATION.getPlaceholder(), relation.getName());
        string = string.replace(RELATION_COLOR.getPlaceholder(), relation.getColor().toString());
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Replace the player placeholders in a String automatically.
     *
     * @param string
     * the String that contains the placeholders
     * @param fPlayer
     * the scoreboard owner
     */
    public static String replacePlayerPlaceholders(String string, FPlayer fPlayer) {
        FactionsXL plugin = FactionsXL.getInstance();
        Economy econ = plugin.getEconomyProvider();

        if (string.contains(PLAYER_BALANCE.getPlaceholder()) && plugin.getFConfig().isEconomyEnabled()) {
            string = string.replace(PLAYER_BALANCE.getPlaceholder(), econ.format(plugin.getBalanceCache().getCachedBalance(fPlayer.getPlayer())));
        }
        string = string.replace(PLAYER_DYNASTY.getPlaceholder(), fPlayer.getDynasty() != null ? fPlayer.getDynasty().getName() : "None");
        string = string.replace(PLAYER_NAME.getPlaceholder(), fPlayer.getName());
        string = string.replace(PLAYER_POWER.getPlaceholder(), String.valueOf(fPlayer.getPower()));
        string = string.replace(PLAYER_PREFIX.getPlaceholder(), fPlayer.getPrefix());
        string = string.replace(PLAYER_TITLE.getPlaceholder(), fPlayer.getTitle() != null ? fPlayer.getTitle() : "None");

        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String replaceWarPlaceholders(String string, War war) {
        WarParty attacker = war.getAttacker();
        WarParty defender = war.getDefender();

        string = string.replace(WAR_CASUS_BELLI.getPlaceholder(), war.getCasusBelli().getType().toString());
        string = string.replace(WAR_ATTACKER.getPlaceholder(), attacker.getLeader().getName());
        string = string.replace(WAR_ATTACKER_DEATHS.getPlaceholder(), String.valueOf(attacker.deaths));
        string = string.replace(WAR_ATTACKER_FIGHTS.getPlaceholder(), String.valueOf(attacker.fights));
        string = string.replace(WAR_ATTACKER_KD.getPlaceholder(), String.valueOf(attacker.getKD()));
        string = string.replace(WAR_ATTACKER_KILLS.getPlaceholder(), String.valueOf(attacker.kills));
        string = string.replace(WAR_ATTACKER_LIST.getPlaceholder(), factionsToString(attacker.getFactions()));
        string = string.replace(WAR_ATTACKER_POINTS.getPlaceholder(), String.valueOf(attacker.getPoints()));
        string = string.replace(WAR_DEFENDER.getPlaceholder(), defender.getLeader().getName());
        string = string.replace(WAR_DEFENDER_DEATHS.getPlaceholder(), String.valueOf(defender.deaths));
        string = string.replace(WAR_DEFENDER_FIGHTS.getPlaceholder(), String.valueOf(defender.fights));
        string = string.replace(WAR_DEFENDER_KD.getPlaceholder(), String.valueOf(defender.getKD()));
        string = string.replace(WAR_DEFENDER_KILLS.getPlaceholder(), String.valueOf(defender.kills));
        string = string.replace(WAR_DEFENDER_LIST.getPlaceholder(), factionsToString(defender.getFactions()));
        string = string.replace(WAR_DEFENDER_POINTS.getPlaceholder(), String.valueOf(defender.getPoints()));

        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String replaceWarPlaceholders(String string, War war, FPlayer fPlayer) {
        WarParty playerParty = null;
        for (Faction faction : war.getAttacker().getFactions()) {
            if (faction.getMembers().contains(fPlayer)) {
                playerParty = war.getAttacker();
            }
        }
        if (playerParty == null) {
            for (Faction faction : war.getDefender().getFactions()) {
                if (faction.getMembers().contains(fPlayer)) {
                    playerParty = war.getDefender();
                }
            }
        }
        WarParty enemyParty = playerParty == war.getAttacker() ? war.getDefender() : war.getAttacker();

        if (playerParty != null) {
            string = string.replace(WAR_PLAYER_PARTY.getPlaceholder(), playerParty.getLeader().getName());
            string = string.replace(WAR_PLAYER_PARTY_DEATHS.getPlaceholder(), String.valueOf(playerParty.deaths));
            string = string.replace(WAR_PLAYER_PARTY_FIGHTS.getPlaceholder(), String.valueOf(playerParty.fights));
            string = string.replace(WAR_PLAYER_PARTY_KD.getPlaceholder(), String.valueOf(playerParty.getKD()));
            string = string.replace(WAR_PLAYER_PARTY_KILLS.getPlaceholder(), String.valueOf(playerParty.kills));
            string = string.replace(WAR_PLAYER_PARTY_LIST.getPlaceholder(), factionsToString(playerParty.getFactions()));
            string = string.replace(WAR_PLAYER_PARTY_POINTS.getPlaceholder(), String.valueOf(playerParty.getPoints()));
            string = string.replace(WAR_ENEMY.getPlaceholder(), enemyParty.getLeader().getName());
            string = string.replace(WAR_ENEMY_DEATHS.getPlaceholder(), String.valueOf(enemyParty.deaths));
            string = string.replace(WAR_ENEMY_FIGHTS.getPlaceholder(), String.valueOf(enemyParty.fights));
            string = string.replace(WAR_ENEMY_KD.getPlaceholder(), String.valueOf(enemyParty.getKD()));
            string = string.replace(WAR_ENEMY_KILLS.getPlaceholder(), String.valueOf(enemyParty.kills));
            string = string.replace(WAR_ENEMY_LIST.getPlaceholder(), factionsToString(enemyParty.getFactions()));
            string = string.replace(WAR_ENEMY_POINTS.getPlaceholder(), String.valueOf(enemyParty.getPoints()));
        }

        string = replaceWarPlaceholders(string, war);
        string = replacePlayerPlaceholders(string, fPlayer);
        string = replaceFactionPlaceholders(string, fPlayer.getFaction());

        return string;
    }

    public static String factionsToString(Collection<Faction> factions) {
        return factionsToString(factions, ChatColor.WHITE);
    }

    public static String factionsToString(Collection<Faction> factions, ChatColor comma) {
        String names = new String();
        boolean first = true;
        for (Faction faction : factions) {
            if (!first) {
                names += comma + ", ";
            } else {
                first = false;
            }
            names += faction.getName();
        }
        return names;
    }

    public static List<String> namesToList(Collection<FPlayer> fPlayers) {
        List<String> names = new ArrayList<>();
        for (FPlayer fPlayer : fPlayers) {
            names.add(fPlayer.getName());
        }
        return names;
    }

    public static String fNamesToString(Collection<FPlayer> fPlayers) {
        return fNamesToString(fPlayers, null);
    }

    public static String fNamesToString(Collection<FPlayer> fPlayers, ChatColor comma) {
        String names = new String();
        boolean first = true;
        for (FPlayer fPlayer : fPlayers) {
            if (!first) {
                names += (comma != null ? comma : new String()) + ", ";
            } else {
                first = false;
            }
            names += fPlayer.getName();
        }
        return names;
    }

    public static String namesToString(Collection<OfflinePlayer> players) {
        return namesToString(players, null);
    }

    public static String namesToString(Collection<OfflinePlayer> players, ChatColor comma) {
        String names = new String();
        boolean first = true;
        for (OfflinePlayer player : players) {
            if (!first) {
                names += (comma != null ? comma : new String()) + ", ";
            } else {
                first = false;
            }
            names += player.getName();
        }
        return names;
    }

    public static String collectionToString(Collection<String> strings) {
        return collectionToString(strings, null);
    }

    public static String collectionToString(Collection<String> strings, ChatColor comma) {
        String names = new String();
        boolean first = true;
        for (String string : strings) {
            if (!first) {
                names += (comma != null ? comma : new String()) + ", ";
            } else {
                first = false;
            }
            names += string;
        }
        return names;
    }

    public static String getPlayerName(Player subject, Player object) {
        return getPlayerName(subject, object, false);
    }

    public static String getPlayerName(Player subject, Player object, boolean title) {
        FactionsXL plugin = FactionsXL.getInstance();
        String objectTitle = null;
        if (title) {
            objectTitle = plugin.getFPlayerCache().getByPlayer(object).getTitle();
        }
        Faction subjectFaction = plugin.getFactionCache().getByMember(subject);
        Faction objectFaction = plugin.getFactionCache().getByMember(object);
        ChatColor color = subjectFaction != null ? subjectFaction.getRelation(objectFaction).getColor() : ChatColor.WHITE;
        return color + (title ? objectTitle + ' ' + object.getName() : object.getName());
    }

    public static String getFactionName(Player subject, Faction object) {
        Faction subjectFaction = FactionsXL.getInstance().getFactionCache().getByMember(subject);
        ChatColor color = subjectFaction != null ? subjectFaction.getRelation(object).getColor() : ChatColor.WHITE;
        String objectName = object != null ? object.getName() : new String();
        return color + objectName;
    }

    public static String getRegionName(Player subject, Region object) {
        if (object == null) {
            return FMessage.MISC_WILDERNESS.getMessage();
        }
        Faction subjectFaction = FactionsXL.getInstance().getFactionCache().getByMember(subject);
        Faction objectFaction = object.getOwner();
        ChatColor color = subjectFaction != null ? subjectFaction.getRelation(objectFaction).getColor() : ChatColor.WHITE;
        String name = objectFaction != null ? color + "[" + objectFaction.getName() + "] " : new String();
        return name + object.getName();
    }

    /**
     * Handles CommandSenders, Players, FPlayers, FactionCache and Regions as arguments
     */
    public static void broadcastMessage(String message, Object... args) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendMessage(player, message, args);
        }
        sendMessage(Bukkit.getConsoleSender(), message, args);
    }

    /**
     * Handles CommandSenders, OfflinePlayers, Players, FPlayers, FactionCache and Regions as arguments
     */
    public static void sendMessage(CommandSender sender, String message, Object... args) {
        MessageUtil.sendMessage(sender, parseMessage(sender, message, args));
    }

    /**
     * Handles CommandSenders, OfflinePlayers, Players, FPlayers, FactionCache and Regions as arguments
     */
    public static void sendActionBarMessage(Player player, String message, Object... args) {
        MessageUtil.sendActionBarMessage(player, parseMessage(player, message, args));
    }

    /**
     * Handles CommandSenders, OfflinePlayers, Players, FPlayers, FactionCache and Regions as arguments
     */
    public static String parseMessage(CommandSender sender, String message, Object... args) {
        FactionsXL plugin = FactionsXL.getInstance();
        FactionCache factions = plugin.getFactionCache();
        Faction subjectFaction = null;
        if (sender instanceof Player) {
            subjectFaction = factions.getByMember((Player) sender);
        }
        String messageParsed = message;
        int i = 0;
        for (Object arg : args) {
            i++;
            if (arg != null) {
                if (arg instanceof OfflinePlayer && !(arg instanceof Player)) {
                    arg = plugin.getFPlayerCache().getByPlayer((OfflinePlayer) arg);
                }
                if (arg instanceof String) {
                    messageParsed = messageParsed.replace("&v" + i, (String) arg);
                } else if (arg instanceof CommandSender) {
                    Faction objectFaction = null;
                    if (arg instanceof Player) {
                        objectFaction = factions.getByMember((Player) arg);
                    }
                    ChatColor color = subjectFaction == null ? ChatColor.WHITE : subjectFaction.getRelation(objectFaction).getColor();
                    messageParsed = messageParsed.replace("&v" + i, color + ((CommandSender) arg).getName());
                } else if (arg instanceof FPlayer) {
                    Faction objectFaction = factions.getByFPlayer(((FPlayer) arg));
                    ChatColor color = subjectFaction == null ? ChatColor.WHITE : subjectFaction.getRelation(objectFaction).getColor();
                    messageParsed = messageParsed.replace("&v" + i, color + ((FPlayer) arg).getName());
                } else if (arg instanceof Faction) {
                    Faction objectFaction = (Faction) arg;
                    ChatColor color = subjectFaction == null ? ChatColor.WHITE : subjectFaction.getRelation(objectFaction).getColor();
                    messageParsed = messageParsed.replace("&v" + i, color + objectFaction.getName());
                } else if (arg instanceof Region) {
                    Faction objectFaction = ((Region) arg).getOwner();
                    ChatColor color = subjectFaction == null ? ChatColor.WHITE : subjectFaction.getRelation(objectFaction).getColor();
                    messageParsed = messageParsed.replace("&v" + i, color + ((Region) arg).getName());
                } else {
                    messageParsed = messageParsed.replace("&v" + i, arg.toString());
                }

            } else {
                messageParsed = messageParsed.replace("&v" + i, "null");
            }
        }
        return messageParsed;
    }

}
