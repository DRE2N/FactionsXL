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
package de.erethon.factionsxl.chat;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.player.FPlayerCache;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Daniel Saukel
 */
public class ChatListener implements Listener {

    FactionsXL plugin = FactionsXL.getInstance();
    FPlayerCache fPlayers = plugin.getFPlayerCache();
    FConfig fConfig = plugin.getFConfig();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        FPlayer fPlayer = fPlayers.getByPlayer(player);
        if (!fPlayer.hasFaction() && fPlayer.getChatChannel() != ChatChannel.PUBLIC && fPlayer.getChatChannel() != ChatChannel.LOCAL) {
            fPlayer.setChatChannel(ChatChannel.PUBLIC);
        }
        ChatChannel channel = fPlayer.getChatChannel();
        if ((channel == ChatChannel.PUBLIC || channel == ChatChannel.LOCAL) && !fConfig.isPublicChatHandled()) {
            return;
        }
        if (!fPlayer.getData().getPublicChat() && fPlayer.getChatChannel() == ChatChannel.PUBLIC) {
            MessageUtil.sendMessage(player, FMessage.CHAT_PUBLIC_DISABLED.getMessage());
            event.setCancelled(true);
        }

        if (!event.isCancelled()) {
            event.setCancelled(true);
            if (channel == ChatChannel.PUBLIC) {
                for (Player receiver : Bukkit.getOnlinePlayers()) {
                    String format = ParsingUtil.replaceChatPlaceholders(fConfig.getChatFormat(channel), fPlayer, fPlayers.getByPlayer(receiver));
                    if (fPlayers.getByPlayer(receiver).getData().getPublicChat()) {
                        MessageUtil.sendMessage(receiver, format + event.getMessage());
                    }
                }
            }
            else if (channel == ChatChannel.LOCAL) {
                BukkitRunnable chatTask = new BukkitRunnable() { // Needs to run on main thread.
                    @Override
                    public void run() {
                        for (Entity entity : player.getNearbyEntities(fConfig.getLocalChatRange(), fConfig.getLocalChatRange(), fConfig.getLocalChatRange())) {
                            if (entity instanceof Player) {
                                Player receiver = (Player) entity;
                                String format = ParsingUtil.replaceChatPlaceholders(fConfig.getChatFormat(channel), fPlayer, fPlayers.getByPlayer(receiver));
                                MessageUtil.sendMessage(receiver, format + event.getMessage());
                            }
                        }
                        String format = ParsingUtil.replaceChatPlaceholders(fConfig.getChatFormat(channel), fPlayer, fPlayer);
                        MessageUtil.sendMessage(player, format + event.getMessage());
                    }
                };
                chatTask.runTask(plugin);
            }
            else if (channel == ChatChannel.TEAM) {
                for (Player receiver : Bukkit.getOnlinePlayers()) {
                    if (receiver.hasPermission("fxl.teamchat")) {
                        String format = ParsingUtil.replaceChatPlaceholders(fConfig.getChatFormat(channel), fPlayer, fPlayers.getByPlayer(receiver));
                        MessageUtil.sendMessage(receiver, format + event.getMessage());
                    }
                }
            }
            else {
                for (Relation relation : channel.getRelations()) {
                    for (Player receiver : fPlayer.getFaction().getOnlineByRelation(relation)) {
                        String format = ParsingUtil.replaceChatPlaceholders(fConfig.getChatFormat(channel), fPlayer, fPlayers.getByPlayer(receiver));
                        MessageUtil.sendMessage(receiver, format + event.getMessage());
                    }
                }
            }
            MessageUtil.log("[FXL-Chat] [" + channel + "] " + player.getName() + ": " + event.getMessage());
            if (channel != ChatChannel.PUBLIC && channel != ChatChannel.TEAM) {
                for (Player team : Bukkit.getOnlinePlayers()) {
                    if (plugin.getFPlayerCache().getByPlayer(team).getData().getChatSpy()) {
                        MessageUtil.sendMessage(team, ChatColor.DARK_GRAY + "[" + ChatColor.YELLOW + channel + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY + player.getName() + ": " + event.getMessage());
                    }
                }
            }
        }
    }
}
