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

package de.erethon.factionsxl.war;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.gui.GUIButton;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.entity.Request;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.faction.LegalEntity;
import de.erethon.factionsxl.gui.StandardizedGUI;
import de.erethon.factionsxl.util.ParsingUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Malfrador
 */

public class WarRequest extends Request {

    WarParty WP;
    private int subjectId;
    private int objectId;
    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();

    public WarRequest (Faction leader, Faction invited, WarParty WarParty ) {
        WP = WarParty;
        subject = leader;
        object = invited;
        expiration = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        object.getRequests().add(this);
    }

    public WarRequest (Map<String, Object> args) {
        subjectId = (int) args.get("subject");
        objectId = (int) args.get("object");
        expiration = (long) args.get("expiration");
    }

    @Override
    public void send() {
        ClickEvent onClickConfirm = new ClickEvent(ClickEvent.Action.RUN_COMMAND, getAcceptCommand());
        TextComponent confirm = new TextComponent(ChatColor.GREEN + FMessage.MISC_ACCEPT.getMessage());
        confirm.setClickEvent(onClickConfirm);

        ClickEvent onClickDeny = new ClickEvent(ClickEvent.Action.RUN_COMMAND, getDenyCommand());
        TextComponent deny = new TextComponent(ChatColor.DARK_RED + FMessage.MISC_DENY.getMessage());
        deny.setClickEvent(onClickDeny);

        getObject().getRequests().add(this);
        Set<WarParty> allWPs = getSubject().getWarParties();
        for (WarParty wp : allWPs) {
            if (wp.getLeader() == getSubject()) {
                WP = wp;
            }
        }

        sendSubjectMessage();
        sendObjectMessage();
        for (Player player : object.getRequestAuthorizedPlayers(getClass()).getOnlinePlayers()) {
            MessageUtil.sendMessage(player, confirm, new TextComponent(" "), deny);
        }
    }

    @Override
    public void confirm() {
        if (getObject().getRelation(getSubject()) == null) {
            for (Player player : object.getRequestAuthorizedPlayers(getClass()).getOnlinePlayers()) {
                MessageUtil.sendMessage(player, FMessage.WAR_REQUEST_NO_ALLIANCE.getMessage());
            }
            return;
        }
        MessageUtil.broadcastMessage(getObject().getRelation(getSubject()).getFormatted());
        if (!(getObject().getRelation(getSubject()) == Relation.ALLIANCE)) {
            for (Player player : object.getRequestAuthorizedPlayers(getClass()).getOnlinePlayers()) {
                MessageUtil.sendMessage(player, FMessage.WAR_REQUEST_NO_ALLIANCE.getMessage());
            }
            return;
        }
        Set<WarParty> allWPs = getSubject().getWarParties();
        for (WarParty wp : allWPs) {
            if (wp.getLeader() == getSubject()) {
                WP = wp;
            }
        }
        WP.joinWar((LegalEntity) object);
        WP.removeInvited((Faction) object);

        object.getRequests().remove(this);
        ParsingUtil.broadcastMessage(FMessage.WAR_REQUEST_WAR_JOINED.getMessage(), getObject(), WP.getEnemy().getLeader());
        purge();
    }

    @Override
    public ItemStack getButton(Player player) {
        return GUIButton.setDisplay(StandardizedGUI.MAILBOX, FMessage.WAR_REQUEST_TITLE.getMessage());
    }

    @Override
    public String getAcceptCommand() {
        return "/f confirmWarRequest " + WP.getName();
    }

    @Override
    public String getDenyCommand() {
        return "/f confirmWarRequest " + WP.getName() + " -cancel";
    }

    @Override
    public void sendSubjectMessage() {
        Faction f = getSubject();
        f.sendMessage(FMessage.WAR_REQUEST_INVITE.getMessage(), getObject(), WP.getEnemy().getLeader());
    }

    @Override
    public boolean isExpired() {
        return expiration <= System.currentTimeMillis();
    }

    @Override
    public void sendObjectMessage() {
        Faction f = getObject();
        f.sendMessage(FMessage.WAR_REQUEST_RECEIVED.getMessage(), getSubject(), WP.getEnemy().getLeader());
    }

    @Override
    public Faction getSubject() {
        if (subject == null) {
            subject = FactionsXL.getInstance().getFactionCache().getById(subjectId);
        }
        return (Faction) subject;
    }

    @Override
    public Faction getObject() {
        if (object == null) {
            object = FactionsXL.getInstance().getFactionCache().getById(objectId);
        }
        return (Faction) object;
    }

    /**
     * Removes all WarRequests that involve both the subject and the object
     */
    public void purge() {
        for (WarRequest req : getSubject().getRequests(WarRequest.class)) {
            if ((req.getObject() == getObject() && req.getSubject() == getSubject()) || (req.getObject() == getSubject() && req.getSubject() == getObject())) {
                getSubject().getRequests().remove(req);
            }
        }
        for (WarRequest req : getObject().getRequests(WarRequest.class)) {
            if ((req.getObject() == getObject() && req.getSubject() == getSubject()) || (req.getObject() == getSubject() && req.getSubject() == getObject())) {
                getObject().getRequests().remove(req);
            }
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> args = new HashMap<>();
        args.put("subject", getSubject().getId());
        args.put("object", getObject().getId());
        args.put("expiration", expiration);
        return args;
    }
}
