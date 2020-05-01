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
package de.erethon.factionsxl.command;

import de.erethon.commons.gui.PageGUI;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.GovernmentType;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Daniel Saukel
 */
public class ListCommand extends FCommand implements Listener, InventoryHolder {

    FactionsXL plugin = FactionsXL.getInstance();
    Inventory gui;

    public ListCommand() {
        setCommand("list");
        setAliases("l");
        setMinArgs(0);
        setMaxArgs(1);
        setHelp(FMessage.HELP_LIST.getMessage());
        setPermission(FPermission.LIST.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        Player player = (Player) sender;
        Set<Faction> factions = null;
        if (args.length == 2 && args[1].equalsIgnoreCase("disbanded")) {
            factions = plugin.getFactionCache().getInactive();
        } else {
            factions = plugin.getFactionCache().getActive();
        }
        if (factions.isEmpty()) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_FACTIONS.getMessage());
            return;
        }

        int size = (int) (9 * Math.ceil(((double) factions.size() / 9)));
        gui = Bukkit.createInventory(this, size, FMessage.CMD_LIST_TITLE.getMessage());
        for (Faction faction : factions) {
            int members = faction.getMembers().contains(faction.getAdmin()) ? faction.getMembers().size() : faction.getMembers().size() + 1;
            ItemStack banner = new ItemStack(faction.getBannerType(), members);
            ItemMeta meta = faction.getBanner() != null ? faction.getBanner().clone() : banner.getItemMeta();
            meta.setDisplayName(ParsingUtil.getFactionName(player, faction));

            ChatColor c = faction.getRelation(plugin.getFactionCache().getByMember(player)).getColor();
            String leader = faction.getAdmin() != null ? faction.getAdmin().getName() : ChatColor.ITALIC + "Interregnum";
            String govType = faction.getGovernmentType().getName();
            if (faction.getGovernmentType() == GovernmentType.MONARCHY) {
                govType += " (" + (faction.getDynasty() != null ? faction.getDynasty().getName() : ChatColor.ITALIC + "Interregnum") + c + ")";
            }
            String power = String.valueOf(faction.getPower());
            String provinces = String.valueOf(faction.getRegions().size());
            meta.setLore(Arrays.asList(FMessage.CMD_SHOW_GOVERNMENT_TYPE.getMessage() + c + govType,
                    FMessage.CMD_SHOW_LEADER.getMessage() + c + leader,
                    FMessage.CMD_SHOW_INFO.getMessage(c.toString(), power, provinces)
            ));
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            banner.setItemMeta(meta);
            gui.addItem(banner);
        }
        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) {
            return;
        }
        event.setCancelled(true);

        PageGUI.playSound(event);
        ItemStack button = event.getCurrentItem();
        if (button != null && button.hasItemMeta() && button.getItemMeta().hasDisplayName()) {
            Faction faction = plugin.getFactionCache().getByName(ChatColor.stripColor(button.getItemMeta().getDisplayName()));
            if (faction == null) {
                faction = plugin.getFactionCache().getInactiveByName(ChatColor.stripColor(button.getItemMeta().getDisplayName()));
            }
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
            plugin.getCommandCache().show.showFaction(player, faction);
        }
    }

    @Override
    public Inventory getInventory() {
        return gui;
    }
}