/*
 * Copyright (c) 2017-2019 Daniel Saukel
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
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.gui.StandardizedGUI;
import de.erethon.factionsxl.scoreboard.FScoreboard;
import de.erethon.factionsxl.util.ParsingUtil;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class War {

    private File file;
    private FileConfiguration config;
    private WarParty attacker;
    private WarParty defender;
    private CasusBelli cb;
    private Date startDate;

    public War(WarParty attacker, WarParty defender, CasusBelli cb) {
        this.attacker = attacker;
        this.defender = defender;
        this.cb = cb;
        startDate = Calendar.getInstance().getTime();
        this.file = new File(FactionsXL.WARS, System.currentTimeMillis() + ".yml");
    }

    public War(File file) {
        this.file = file;
        config = YamlConfiguration.loadConfiguration(file);
        attacker = new WarParty(ConfigUtil.getMap(config, "attacker"));
        defender = new WarParty(ConfigUtil.getMap(config, "defender"));
        cb = new CasusBelli(config.getConfigurationSection("casusBelli"));
        startDate = new Date(config.getLong("startDate"));
    }

    /* Getters */
    public WarParty getAttacker() {
        return attacker;
    }

    public WarParty getDefender() {
        return defender;
    }

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
        try {
            file.createNewFile();
        } catch (IOException exception) {
        }
        config = YamlConfiguration.loadConfiguration(file);
        WarCache wars = FactionsXL.getInstance().getWarCache();
        wars.getUnconfirmedWars().remove(this);
        wars.getWars().add(this);
        FScoreboard.updateAllProviders();
        System.out.println("War" + this + "confirmed!");
    }

    /* Serialization */
    public void save() {
        config.set("attacker", attacker.serialize());
        config.set("defender", defender.serialize());
        config.set("casusBelli", cb.serialize());
        config.set("startDate", startDate.getTime());
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
