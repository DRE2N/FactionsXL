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
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Board;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.economy.Resource;
import de.erethon.factionsxl.economy.StatusEffect;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.population.PopulationLevel;
import de.erethon.factionsxl.util.ParsingUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Daniel Saukel
 */
public class RegionCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();
    FConfig config = plugin.getFConfig();
    Board board = plugin.getBoard();

    public RegionCommand() {
        setCommand("region");
        setAliases("province", "rg");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.HELP_REGION.getMessage());
        setPermission(FPermission.REGION.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        if (!(sender instanceof Player) && args.length == 1) {
            displayHelp(sender);
            return;
        }
        Player player = (Player) sender;
        Region region = null;
        World world = player.getWorld();
        if (args.length >= 2) {
            region = board.getByName(args[1]);
            if (region == null) {
                int id = 0;
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException exception) {
                    ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_REGION.getMessage(), args[1]);
                    return;
                }
                region = board.getById(id);
            }
        } else {
            region = board.getByLocation(((Player) sender).getLocation());
        }
        if (region == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_LAND_WILDERNESS.getMessage());
            return;
        }
        Faction faction = region.getOwner();
        ChatColor c = faction != null && sender instanceof Player ? faction.getRelation(factions.getByMember((Player) sender)).getColor() : Relation.PEACE.getColor();

        BaseComponent[] id = MessageUtil.parse(c + "&l=== " + region.getName() + " ===");
        HoverEvent tagHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Region-ID: " + region.getId()).create());
        for (BaseComponent component : id) {
            component.setHoverEvent(tagHover);
        }
        MessageUtil.sendCenteredMessage(sender, id);
        MessageUtil.sendCenteredMessage(sender, "&6____________________________________________________");
        MessageUtil.sendMessage(sender, FMessage.CMD_REGION_OWNER.getMessage() + c + (faction != null ? faction.getLongName() : "None"));
        if (region.isNeutral()) {
            Faction senderFaction = sender instanceof Player ? factions.getByMember((Player) sender) : null;
            MessageUtil.sendMessage(sender, FMessage.CMD_REGION_PRICE.getMessage() + c + region.getClaimPrice(senderFaction));
        }
        if (!(region.getCoreFactions().containsKey(region.getOwner())) && !region.isNeutral()) {
            double amount = region.getClaimPrice(faction) * config.getPriceCoreMultiplier();
            MessageUtil.sendMessage(sender, FMessage.CMD_REGION_PRICE_CORE.getMessage() + c + String.valueOf(amount));
        }
        MessageUtil.sendMessage(sender,FMessage.CMD_REGION_INFLUENCE.getMessage() + c + region.getInfluence() + "%");
        if (region.isAttacked()) {
            MessageUtil.sendMessage(sender, "&aEinfluss für Besetzung: &7" + config.getInfluenceNeeded() + "%");
        }
        if (region.getOccupant() != null) {
            MessageUtil.sendMessage(sender, FMessage.CMD_REGION_OCCUPIER.getMessage() + c + region.getOccupant().getName());
        }
        ArrayList<BaseComponent> adjacentList = new ArrayList<>(Arrays.asList(TextComponent.fromLegacyText("§6Angrenzend: ")));
        boolean adjacentFirst = true;
        for (Region rg : region.getNeighbours()) {
            if (!adjacentFirst) {
                adjacentList.addAll(Arrays.asList(TextComponent.fromLegacyText(ChatColor.GOLD + ", ")));
            }
            adjacentFirst = false;
            adjacentList.addAll(Arrays.asList(TextComponent.fromLegacyText(rg.getName())));
        }
        MessageUtil.sendMessage(player, adjacentList.toArray(new BaseComponent[]{}));
        BaseComponent[] income1 = TextComponent.fromLegacyText(FMessage.CMD_REGION_TYPE.getMessage());
        BaseComponent[] income2 = TextComponent.fromLegacyText(c + region.getType().getName() + " (" + region.getLevel() + ")");
        BaseComponent[] incomeHover = new BaseComponent[]{};
        boolean first = true;
        for (Entry<Resource, Integer> entry : region.getResources().entrySet()) {
            String legacy = c + "+" + entry.getValue() + " " + entry.getKey().getName();
            incomeHover = (BaseComponent[]) ArrayUtils.addAll(incomeHover, TextComponent.fromLegacyText((first ? new String() : "\n") + legacy));
            first = false;
        }
        HoverEvent incomeHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, incomeHover);
        for (BaseComponent comp : income2) {
            comp.setHoverEvent(incomeHoverEvent);
        }
        BaseComponent[] income = (BaseComponent[]) ArrayUtils.addAll(income1, income2);
        MessageUtil.sendMessage(sender, income);

        MessageUtil.sendMessage(sender, FMessage.CMD_REGION_POPULATION.getMessage() + c + region.getTotalPopulation() + "/" + region.getType().getMaxPopulation(region.getLevel()));

        if (sender instanceof Player) {
            ArrayList<BaseComponent> cores = new ArrayList<>(Arrays.asList(TextComponent.fromLegacyText(FMessage.CMD_REGION_CORES.getMessage())));
            boolean first1 = true;
            for (Map.Entry<Faction, Date> core : region.getCoreFactions().entrySet()) {
                Faction coreFaction = core.getKey();
                Date date = core.getValue();
                Relation relation = faction != null ? faction.getRelation(coreFaction) : Relation.PEACE;
                if (!first1) {
                    cores.addAll(Arrays.asList(TextComponent.fromLegacyText(ChatColor.GOLD + ", ")));
                }
                first1 = false;
                BaseComponent[] relComps = TextComponent.fromLegacyText(relation.getColor() + coreFaction.getName());
                HoverEvent onHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GRAY + date.toString()));
                for (BaseComponent relComp : relComps) {
                    relComp.setHoverEvent(onHover);
                }
                cores.addAll(Arrays.asList(relComps));
            }
            MessageUtil.sendMessage(sender, cores.toArray(new BaseComponent[]{}));

            ArrayList<BaseComponent> claims = new ArrayList<>(Arrays.asList(TextComponent.fromLegacyText(FMessage.CMD_REGION_CLAIMS.getMessage())));
            boolean first2 = true;
            for (Map.Entry<Faction, Date> core : region.getClaimFactions().entrySet()) {
                Faction claimFaction = core.getKey();
                Date date = core.getValue();
                Relation relation = faction != null ? faction.getRelation(claimFaction) : Relation.PEACE;
                if (!first2) {
                    claims.addAll(Arrays.asList(TextComponent.fromLegacyText(ChatColor.GOLD + ", ")));
                }
                first2 = false;
                BaseComponent[] relComps = TextComponent.fromLegacyText(relation.getColor() + claimFaction.getName());
                HoverEvent onHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GRAY + date.toString()));
                for (BaseComponent relComp : relComps) {
                    relComp.setHoverEvent(onHover);
                }
                claims.addAll(Arrays.asList(relComps));
            }
            MessageUtil.sendMessage(sender, claims.toArray(new BaseComponent[]{}));
            for (PopulationLevel level : region.getPopulation().keySet()) {
                MessageUtil.sendMessage(sender, "Lvl: " + level.toString() + " Val: " + region.getPopulation().get(level));
            }
            for (StatusEffect effect : region.getEffects()) {
                MessageUtil.sendMessage(sender, "Effect: " + effect.getProductionModifier().get(Resource.STONE));
            }
        }
    }

}
