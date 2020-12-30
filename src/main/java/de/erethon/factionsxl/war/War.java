/*
 * Copyright (C) 2017-2020 Daniel Saukel
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
package de.erethon.factionsxl.war;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.config.ConfigUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.api.event.WarDeclarationEvent;
import de.erethon.factionsxl.api.event.WarEndEvent;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.gui.StandardizedGUI;
import de.erethon.factionsxl.player.FPlayerCache;
import de.erethon.factionsxl.scoreboard.FScoreboard;
import de.erethon.factionsxl.util.ParsingUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Daniel Saukel
 */
public class War {

    FactionsXL plugin = FactionsXL.getInstance();
    FPlayerCache fPlayers = plugin.getFPlayerCache();

    private File file;
    private FileConfiguration config;
    private WarParty attacker;
    private WarParty defender;
    private boolean truce;
    private CasusBelli cb;
    private Date startDate;
    Map<OfflinePlayer, Double> participatingPlayers = new HashMap<>();; // Participation

    public War(WarParty attacker, WarParty defender, CasusBelli cb) {
        this.attacker = attacker;
        this.defender = defender;
        this.cb = cb;
        this.truce = false;
        startDate = Calendar.getInstance().getTime();
        this.participatingPlayers = new HashMap<>();
        this.file = new File(FactionsXL.WARS, System.currentTimeMillis() + ".yml");
    }

    public War(File file) {
        this.file = file;
        config = YamlConfiguration.loadConfiguration(file);
        attacker = new WarParty(ConfigUtil.getMap(config, "attacker"));
        defender = new WarParty(ConfigUtil.getMap(config, "defender"));
        truce = config.getBoolean("truce");
        cb = new CasusBelli(config.getConfigurationSection("casusBelli"));
        startDate = new Date(config.getLong("startDate"));
        for (String rawData : config.getStringList("participatingPlayers")) {
            String[] raw = rawData.split(":");
            participatingPlayers.put(Bukkit.getOfflinePlayer(UUID.fromString(raw[0])), Double.parseDouble(raw[1]));
        }
    }

    /* Getters */
    public WarParty getAttacker() {
        return attacker;
    }

    public WarParty getDefender() {
        return defender;
    }

    public boolean getTruce() {return truce;}

    public void setTruce(boolean t) {truce = t;}

    /**
     * Returns the defender if the party is the attacker;
     * the attacker if the party is the defender
     * and null if the party is anything else.
     *
     * @param party the party to check
     * @return the other party
     */
    public WarParty getEnemy(WarParty party) {
        if (party == attacker) {
            return defender;
        } else if (party == defender) {
            return attacker;
        }
        return null;
    }

    public CasusBelli getCasusBelli() {
        return cb;
    }

    public Date getStartDate() {
        return startDate;
    }

    public double getPlayerParticipation(OfflinePlayer player) {
        if (participatingPlayers == null || !participatingPlayers.containsKey(player)) {
            return 0;
        }
        return participatingPlayers.get(player);
    }

    public void addPlayerParticipation(OfflinePlayer player, double value) {
        double current;
        if (participatingPlayers.containsKey(player)) {
            current = participatingPlayers.get(player);
        } else {
            current = 0;
        }
        participatingPlayers.put(player, current + value);
    }

    public void addPlayerParticipation(OfflinePlayer player, WarPlayerAction action) {
        switch (action) {
            case KILL:
                addPlayerParticipation(player, 1);
                break;
            case PLACED_SIEGE:
                addPlayerParticipation(player, 0.4);
                break;
            case PLACED_TNT:
                addPlayerParticipation(player, 0.3);
                break;
            case DESTROYED_IMPORTANT_BLOCK:
                addPlayerParticipation(player, 0.2);
                break;
            case GRIEF:
                addPlayerParticipation(player, 0.0);
                break;
        }
    }

    /* Actions */
    /**
     * Requires Spigot API!
     * The confirmation request sent to a player after selecting allies.
     *
     * @param player
     * the player that receives the request
     */
    public void sendConfirmRequest(Player player) {
        MessageUtil.sendCenteredMessage(player, FMessage.WAR_DECLARATION_TITLE.getMessage());
        MessageUtil.sendCenteredMessage(player, FMessage.WAR_DECLARATION_CASUS_BELLI.getMessage() + cb.getType().toString());
        String allies = ParsingUtil.factionsToString(attacker.getFactions(), ChatColor.BLUE);
        MessageUtil.sendCenteredMessage(player, FMessage.WAR_DECLARATION_ALLIES.getMessage() + allies);
        MessageUtil.sendCenteredMessage(player, "&9&oInvited: " + ParsingUtil.factionsToString(attacker.getInvited(), ChatColor.ITALIC));
        String defenders = ParsingUtil.factionsToString(defender.getFactions(), ChatColor.RED);
        MessageUtil.sendCenteredMessage(player, FMessage.WAR_DECLARATION_DEFENDERS.getMessage() + defenders);

        ClickEvent onConfirmClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/factionsxl confirmWar " + startDate.getTime());
        BaseComponent confirm = StandardizedGUI.CONFIRM.duplicate();
        confirm.setClickEvent(onConfirmClick);
        ClickEvent onCancelClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/factionsxl confirmWar " + startDate.getTime() + " -cancel");
        BaseComponent cancel = StandardizedGUI.CANCEL.duplicate();
        cancel.setClickEvent(onCancelClick);
        MessageUtil.sendCenteredMessage(player, confirm, new TextComponent(" "), cancel);
    }

    /**
     * When a player confirms the war.
     */
    public void confirm() {
        WarDeclarationEvent event = new WarDeclarationEvent(attacker, defender, cb);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        try {
            file.createNewFile();
        } catch (IOException exception) {
        }
        config = YamlConfiguration.loadConfiguration(file);
        WarCache wars = FactionsXL.getInstance().getWarCache();
        wars.getUnconfirmedWars().remove(this);
        wars.getWars().add(this);
        this.truce = true;
        Set<Faction> factionSetA =  this.getAttacker().getFactions();
        Set<Faction> factionSetD = this.getDefender().getFactions();
        if (getCasusBelli().getType() == CasusBelli.Type.INDEPENDENCE) {
            Faction faction = (Faction) attacker.getLeader();
            faction.getRelations().remove((Faction) getCasusBelli().getTarget());
            ((Faction) getCasusBelli().getTarget()).getRelations().remove(faction);
            // Vasalls that declare independence should leave wars that they participate in because of their lord
            for (WarParty warParty : faction.getWarParties()) {
                if (warParty.getLeader().equals(getCasusBelli().getTarget())) {
                    warParty.leaveWar(faction);
                }
            }
        }
        // Set all relations to enemy
        for (Faction attacker : factionSetA) {
            for (Faction defender : factionSetD) {
                if (attacker != defender) { // To prevent self-declaration, for example for vassals
                    attacker.getRelations().put(defender, Relation.ENEMY);
                }
            }
        }
        FScoreboard.updateAllProviders();
        System.out.println("War" + this + "confirmed!");
    }
    public void end() {
        WarEndEvent event = new WarEndEvent(attacker, defender, cb);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        config = YamlConfiguration.loadConfiguration(file);
        WarCache wars = FactionsXL.getInstance().getWarCache();

        MessageUtil.broadcastMessage(" ");
        MessageUtil.broadcastCenteredMessage(FMessage.WAR_ENDED.getMessage(getAttacker().getName(), getDefender().getName()));
        MessageUtil.broadcastMessage(" ");

        cleanup();
        wars.getWars().remove(this);
        file.delete();
        System.out.println("War" + this + "ended!");
    }

    public void cleanup() {
        Set<Faction> factionSet =  this.getAttacker().getFactions();
        Set<Faction> factionSetD = this.getDefender().getFactions();
        // Set all relations to peace
        for (Faction f : factionSet) {
            for (Faction f2 : factionSetD) {
                f.getRelations().remove(f2);
                f2.getRelations().remove(f);
            }
        }
        // Remove Occupants from the Attacker
        for (Faction f : factionSet) {
            for (Region rg : f.getRegions()) {
                if (factionSetD.contains(rg.getOccupant())) {
                    rg.clearOccupant();
                }
            }
        }
        // Remove Occupants from the Defender
        for (Faction f : factionSetD) {
            for (Region rg : f.getRegions()) {
                if (factionSet.contains(rg.getOccupant())) {
                    rg.clearOccupant();
                }
            }
        }
    }

    /* Serialization */
    public void save() {
        config.set("attacker", attacker.serialize());
        config.set("defender", defender.serialize());
        config.set("truce", truce);
        config.set("casusBelli", cb.serialize());
        config.set("startDate", startDate.getTime());
        List<String> playerData = new ArrayList<>();
        for (OfflinePlayer p : participatingPlayers.keySet()) {
            String data = p.getUniqueId().toString() + ":" + participatingPlayers.get(p);
            playerData.add(data);
        }
        config.set("participatingPlayers", playerData);
        try {
            config.save(file);
        } catch (IOException exception) {
        }
    }

    @Override
    public String toString() {
        return "War{attacker=" + attacker.toString() + "defender=" + defender.toString() + "}";
    }

}
