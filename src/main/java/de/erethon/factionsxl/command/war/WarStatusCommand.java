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
package de.erethon.factionsxl.command.war;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.misc.SimpleDateUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.command.FCommand;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.scoreboard.FScoreboard;
import de.erethon.factionsxl.scoreboard.sidebar.FWarSidebar;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.WarCache;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * @author Daniel Saukel
 */
public class WarStatusCommand extends FCommand {

    WarCache wars = FactionsXL.getInstance().getWarCache();
    FConfig config = FactionsXL.getInstance().getFConfig();

    private String PLACEHOLDER = ChatColor.DARK_GRAY + " | " + ChatColor.BLUE;

    public WarStatusCommand() {
        setCommand("warStatus");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.HELP_WAR_STATUS.getMessage());
        setPermission(FPermission.WAR_STATUS.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        if (args.length == 2) {
            try {
                War war = wars.getByDate(Long.parseLong(args[1]));
                if (war != null) {
                    showWarInfo(sender, war);
                    return;
                }
            } catch (NumberFormatException exception) {
            }
        }
        Faction faction = getSenderFactionOrFromArg(sender, args, 1, true);
        if (faction == null) {
            return;
        }
        Set<War> wars = this.wars.getByFaction(faction);
        if (wars.isEmpty()) {
            ParsingUtil.sendMessage(sender, FMessage.CMD_WAR_STATUS_NO_WARS.getMessage(), faction);
            return;
        }
        ParsingUtil.sendMessage(sender, FMessage.CMD_WAR_STATUS_HEADLINE.getMessage(), faction);
        for (War fWar : wars) {
            String attacker = fWar.getAttacker().getLeader().getName();
            String defender = fWar.getDefender().getLeader().getName();
            String cb = fWar.getCasusBelli().getType().toString();
            String date = SimpleDateUtil.ddMMyyyyhhmm(fWar.getStartDate());
            String line = ChatColor.GOLD + "> " + ChatColor.BLUE + attacker + PLACEHOLDER + defender + PLACEHOLDER + cb + PLACEHOLDER + date;
            BaseComponent[] comps = TextComponent.fromLegacyText(line);
            ClickEvent onClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/factionsxl warStatus " + fWar.getStartDate().getTime());
            for (BaseComponent comp : comps) {
                comp.setClickEvent(onClick);
            }
            MessageUtil.sendMessage(sender, comps);
        }
    }

    public void showWarInfo(CommandSender sender, War war) {
        String attackerLeader = war.getAttacker().getLeader().getName();
        String attackers = ParsingUtil.factionsToString(war.getAttacker().getFactions(), ChatColor.GRAY);
        int attackerKills = war.getAttacker().kills;
        int attackerDeaths = war.getAttacker().deaths;
        double attackerKD = war.getAttacker().getKD();
        int attackerPoints = war.getAttacker().getPoints();
        String defenderLeader = war.getDefender().getLeader().getName();
        String defenders = ParsingUtil.factionsToString(war.getDefender().getFactions(), ChatColor.GRAY);
        int defenderKills = war.getDefender().kills;
        int defenderDeaths = war.getDefender().deaths;
        double defenderKD = war.getDefender().getKD();
        int defenderPoints = war.getDefender().getPoints();
        String cb = war.getCasusBelli().getType().toString();
        String date = SimpleDateUtil.ddMMyyyyhhmm(war.getStartDate());
        MessageUtil.sendCenteredMessage(sender, ChatColor.DARK_RED + attackerLeader + ChatColor.RED + " vs. " + ChatColor.DARK_RED + defenderLeader);
        MessageUtil.sendMessage(sender, FMessage.CMD_WAR_STATUS_CASUS_BELLI.getMessage() + cb);
        MessageUtil.sendMessage(sender, FMessage.CMD_WAR_STATUS_DATE.getMessage() + date);
        MessageUtil.sendMessage(sender, FMessage.CMD_WAR_STATUS_KILLPOINTS.getMessage(String.valueOf(war.getDefender().getPointsFromKills()), String.valueOf(config.getMaximumKillPoints())));
        MessageUtil.sendMessage(sender, FMessage.CMD_WAR_STATUS_ATTACKERS.getMessage() + attackers);
        MessageUtil.sendMessage(sender, FMessage.CMD_WAR_STATUS_KILLS_AND_DEATHS.getMessage(String.valueOf(attackerKills), String.valueOf(attackerDeaths), String.valueOf(attackerKD)));
        MessageUtil.sendMessage(sender, FMessage.CMD_WAR_STATUS_POINTS.getMessage() + String.valueOf(attackerPoints));
        MessageUtil.sendMessage(sender, FMessage.CMD_WAR_STATUS_DEFENDERS.getMessage() + defenders);
        MessageUtil.sendMessage(sender, FMessage.CMD_WAR_STATUS_KILLS_AND_DEATHS.getMessage(String.valueOf(defenderKills), String.valueOf(defenderDeaths), String.valueOf(defenderKD)));
        MessageUtil.sendMessage(sender, FMessage.CMD_WAR_STATUS_POINTS.getMessage() + String.valueOf(defenderPoints));
        FScoreboard.get((Player) sender).setTemporarySidebar(new FWarSidebar(war));
    }

}
