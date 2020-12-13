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
package de.erethon.factionsxl.command;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.misc.NumberUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.faction.GovernmentType;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.util.ParsingUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Daniel Saukel
 */
public class ShowCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();

    public ShowCommand() {
        setCommand("show");
        setAliases("who", "f");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.HELP_SHOW.getMessage());
        setPermission(FPermission.SHOW.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Faction faction = null;
        if (args.length == 1 && sender instanceof Player) {
            faction = factions.getByMember((Player) sender);
        } else if (args.length == 2) {
            faction = factions.getByName(args[1]);
            if (faction == null) {
                faction = factions.getById(NumberUtil.parseInt(args[1], -1));
            }
            if (faction == null) {
                FPlayer fPlayer = plugin.getFPlayerCache().getByName(args[1]);
                faction = fPlayer != null ? fPlayer.getFaction() : null;
            }
        }
        if (faction == null) {
            ParsingUtil.sendMessage(sender, args.length >= 2 ? FMessage.ERROR_NO_SUCH_FACTION.getMessage(args[1]) : FMessage.ERROR_SPECIFY_FACTION.getMessage());
            return;
        }
        showFaction((Player) sender, faction);
    }

    public void showFaction(Player player, Faction faction) {
        ChatColor c = faction.getRelation(factions.getByMember(player)).getColor();

        MessageUtil.sendCenteredMessage(player, c + "&l=== " + faction.getLongName() + (faction.isActive() ? new String() : " (\u271d)") + " ===");
        MessageUtil.sendCenteredMessage(player, "&6____________________________________________________");
        BaseComponent[] tag = TextComponent.fromLegacyText(FMessage.CMD_SHOW_TAG.getMessage() + c + faction.getShortName());
        HoverEvent tagHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("ID: " + faction.getId()).create());
        for (BaseComponent component : tag) {
            component.setHoverEvent(tagHover);
        }
        MessageUtil.sendMessage(player, tag);
        MessageUtil.sendMessage(player, FMessage.CMD_SHOW_DESCRIPTION.getMessage() + c + faction.getDescription());
        if (plugin.getFConfig().isEconomyEnabled()) {
            MessageUtil.sendMessage(player, FMessage.CMD_SHOW_BALANCE.getMessage() + c + plugin.getEconomyProvider().format(faction.getAccount().getBalance()));
        }
        String govType = faction.getGovernmentType().getName();
        if (faction.getGovernmentType() == GovernmentType.MONARCHY) {
            govType += " (" + (faction.getDynasty() != null ? faction.getDynasty().getName() : "&oInterregnum") + c + ")";
        }
        MessageUtil.sendMessage(player, FMessage.CMD_SHOW_GOVERNMENT_TYPE.getMessage() + c + govType);
        if (faction.isActive()) {
            MessageUtil.sendMessage(player, FMessage.CMD_SHOW_INVITATION.getMessage() + c + !faction.isOpen());
            MessageUtil.sendMessage(player, FMessage.CMD_SHOW_CAPITAL.getMessage() + c + faction.getCapital().getName());
            String power = String.valueOf(faction.getPower());
            String provinces = String.valueOf(faction.getRegions().size());
            int pop = 0;
            for (Region rg : faction.getRegions()) {
                pop = pop + rg.getTotalPopulation();
            }
            String population = String.valueOf(pop);
            MessageUtil.sendMessage(player, FMessage.CMD_SHOW_INFO.getMessage(c.toString(), power, provinces, population));
            MessageUtil.sendMessage(player, faction.getStabilityModifiers(c));

            ArrayList<BaseComponent> relList = new ArrayList<>(Arrays.asList(TextComponent.fromLegacyText(FMessage.CMD_SHOW_RELATIONS.getMessage())));
            boolean relFirst = true;
            for (Faction other : factions.getActive()) {
                Relation relation = faction.getRelation(other);
                if (relation != Relation.PEACE && relation != Relation.OWN) {
                    if (!relFirst) {
                        relList.addAll(Arrays.asList(TextComponent.fromLegacyText(ChatColor.GOLD + ", ")));
                    }
                    relFirst = false;
                    relList.addAll(Arrays.asList(relation.getFormatted(other)));
                }
            }
            MessageUtil.sendMessage(player, relList.toArray(new BaseComponent[]{}));

            String leader = faction.getAdmin() != null ? faction.getAdmin().getName() : "&oInterregnum";
            MessageUtil.sendMessage(player, FMessage.CMD_SHOW_LEADER.getMessage() + c + leader);

            ArrayList<BaseComponent> memList = new ArrayList<>(Arrays.asList(TextComponent.fromLegacyText(FMessage.CMD_SHOW_MEMBERS.getMessage(String.valueOf(faction.getMembers().size())))));
            boolean memFirst = true;
            for (OfflinePlayer member : faction.getMembers().getOfflinePlayers()) {
                Double memPower = plugin.getFData().power.get(member.getUniqueId());
                if (memPower == null) {
                    memPower = 0D;
                }
                String memPowerHover = FMessage.CMD_POWER.getMessage(ChatColor.GOLD + member.getName(), String.valueOf(memPower.intValue()));
                HoverEvent onHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(memPowerHover));
                BaseComponent[] components = TextComponent.fromLegacyText(c + plugin.getFPlayerCache().getByPlayer(member).getTitle() + " " + member.getName());
                for (BaseComponent component : components) {
                    component.setHoverEvent(onHover);
                }

                if (!memFirst) {
                    memList.addAll(Arrays.asList(TextComponent.fromLegacyText(ChatColor.GOLD + ", ")));
                }
                memFirst = false;
                memList.addAll(Arrays.asList(components));
            }
            MessageUtil.sendMessage(player, memList.toArray(new BaseComponent[]{}));

        } else {
            MessageUtil.sendMessage(player, FMessage.CMD_SHOW_FORMER_LEADERS.getMessage()
                    + c + ParsingUtil.namesToString(faction.getFormerAdmins().getOfflinePlayers(), ChatColor.GOLD));
        }
    }

}
