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
package de.erethon.factionsxl.entity;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.config.FMessage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Daniel Saukel
 */
public abstract class Request implements ConfigurationSerializable {

    protected FEntity subject;
    protected FEntity object;
    protected long expiration;

    /* Getters and setters */
    /**
     * @return
     * the FEntity that made the request
     */
    public FEntity getSubject() {
        return subject;
    }

    /**
     * @return
     * the FEntity that received the request
     */
    public FEntity getObject() {
        return object;
    }

    /**
     * @return
     * if the request is expired
     */
    public boolean isExpired() {
        return expiration <= System.currentTimeMillis();
    }

    /* Actions */
    /**
     * Sends the request to the object
     */
    public void send() {
        ClickEvent onClickConfirm = new ClickEvent(ClickEvent.Action.RUN_COMMAND, getAcceptCommand());
        TextComponent confirm = new TextComponent(ChatColor.GREEN + FMessage.MISC_ACCEPT.getMessage());
        confirm.setClickEvent(onClickConfirm);

        ClickEvent onClickDeny = new ClickEvent(ClickEvent.Action.RUN_COMMAND, getDenyCommand());
        TextComponent deny = new TextComponent(ChatColor.DARK_RED + FMessage.MISC_DENY.getMessage());
        deny.setClickEvent(onClickDeny);

        sendSubjectMessage();
        sendObjectMessage();
        for (Player player : object.getRequestAuthorizedPlayers(getClass()).getOnlinePlayers()) {
            MessageUtil.sendMessage(player, confirm, new TextComponent(" "), deny);
        }
    }

    /**
     * Sends the request to the object
     *
     * @param messages
     * if explaining messages should be sent to both subject and object
     */
    public void send(boolean messages) {
        ClickEvent onClickConfirm = new ClickEvent(ClickEvent.Action.RUN_COMMAND, getAcceptCommand());
        TextComponent confirm = new TextComponent(ChatColor.GREEN + FMessage.MISC_ACCEPT.getMessage());
        confirm.setClickEvent(onClickConfirm);

        ClickEvent onClickDeny = new ClickEvent(ClickEvent.Action.RUN_COMMAND, getDenyCommand());
        TextComponent deny = new TextComponent(ChatColor.DARK_RED + FMessage.MISC_DENY.getMessage());
        deny.setClickEvent(onClickDeny);

        if (messages) {
            sendSubjectMessage();
            sendObjectMessage();
        }
        for (Player player : object.getRequestAuthorizedPlayers(getClass()).getOnlinePlayers()) {
            MessageUtil.sendMessage(player, confirm, new TextComponent(" "), deny);
        }
    }

    /* Abstracts */
    /**
     * For when the object accepts the request.
     * Make sure to remove the requests from the object
     */
    public abstract void confirm();

    /**
     * @return
     * an ItemStack button for inventory GUIs
     */
    public abstract ItemStack getButton(Player player);

    /**
     */
    public abstract String getAcceptCommand();

    /**
     * @return
     * the command to deny the request
     */
    public abstract String getDenyCommand();

    /**
     * Sends a message to the subject player(s) that are online to inform them about the request
     */
    public abstract void sendSubjectMessage();

    /**
     * Sends a message to the object player(s) that are online to inform them about the request
     */
    public abstract void sendObjectMessage();

    @Override
    public String toString() {
        return getClass().getName() + "{subject=" + subject + "; object=" + object + "}";
    }

}
