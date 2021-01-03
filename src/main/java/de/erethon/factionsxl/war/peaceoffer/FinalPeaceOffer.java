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
package de.erethon.factionsxl.war.peaceoffer;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.legacygui.GUIButton;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.WarParty;
import de.erethon.factionsxl.war.WarPartyRole;
import de.erethon.factionsxl.war.demand.WarDemand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Daniel Saukel
 */
public class FinalPeaceOffer extends PeaceOffer {

    Double cost = 0.00;
    Boolean isOffer = false;
    FactionsXL plugin = FactionsXL.getInstance();

    public FinalPeaceOffer(War war, WarParty demanding, WarParty target, WarDemand... demands) {
        this.war = war;
        subject = demanding;
        object = target;
        this.demands = new ArrayList(Arrays.asList(demands));
        expiration = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        //object.getRequests().add(this);
        //
    }

    public FinalPeaceOffer(War war, Boolean isOfferBool, WarParty demanding, WarParty target, WarDemand... demands) {
        this.war = war;
        subject = demanding;
        object = target;
        isOffer = isOfferBool;
        this.demands = new ArrayList(Arrays.asList(demands));
        expiration = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        //object.getRequests().add(this);
        //
    }

    public FinalPeaceOffer(Map<String, Object> args) {
        war = FactionsXL.getInstance().getWarCache().getByDate((long) args.get("war"));
        subject = WarPartyRole.valueOf((String) args.get("subject")) == WarPartyRole.ATTACKER ? war.getAttacker() : war.getDefender();
        object = getSubject().getRole() == WarPartyRole.ATTACKER ? war.getDefender() : war.getAttacker();
        isOffer = (boolean) args.get("offer");
        demands = (List<WarDemand>) args.get("demands");
        expiration = (long) args.get("expiration");
    }

    @Override
    public WarParty getSubject() {
        return (WarParty) subject;
    }

    @Override
    public WarParty getObject() {
        return (WarParty) object;
    }

    @Override
    public boolean canPay() {
        boolean canPay = true;
        for (WarDemand demand : demands) {
            if (isOffer) {
                if (!demand.canPay(getSubject())) {
                    canPay = false;
                }
            }
            if (!isOffer) {
                if (!demand.canPay(getObject())) {
                    canPay = false;
                }
            }
        }
        return canPay;


    }
    @Override
    public void confirm() {
        if (isOffer) {
            demands.forEach(d -> d.pay(getObject(), getSubject()));
        }
        if (!isOffer) {
            demands.forEach(d -> d.pay(getSubject(), getObject()));
        }
        war.end();
        MessageUtil.broadcastMessage(" ");
        ParsingUtil.broadcastMessage(FMessage.WAR_ENDED.getMessage(), getObject().getLeader(), getSubject().getLeader());
        MessageUtil.broadcastMessage(" ");
            // TODO: Might break after government update
            // TODO: Add time modifier
        //purge();
    }

    @Override
    public void send() {
        if (!isOffer) {
            if (getCost() > getSubject().getPoints()) {
                for (Player player : subject.getRequestAuthorizedPlayers(getClass()).getOnlinePlayers()) {
                    MessageUtil.sendMessage(player, FMessage.WAR_DEMAND_NO_WARSCORE.getMessage());
                    MessageUtil.sendMessage(player, FMessage.WAR_DEMAND_WARSCORE_NEEDED.getMessage( String.valueOf(getSubject().getPoints()), String.valueOf(getCost())));
                }
                return;
            }
        }
        if (!canPay()) {
            for (Player player : subject.getRequestAuthorizedPlayers(getClass()).getOnlinePlayers()) {
                MessageUtil.sendMessage(player, FMessage.WAR_DEMAND_CANT_AFFORD.getMessage());
            }
            return;
        }
        ClickEvent onClickConfirm = new ClickEvent(ClickEvent.Action.RUN_COMMAND, getAcceptCommand());
        TextComponent confirm = new TextComponent(ChatColor.GREEN + FMessage.MISC_ACCEPT.getMessage());
        confirm.setClickEvent(onClickConfirm);

        ClickEvent onClickDeny = new ClickEvent(ClickEvent.Action.RUN_COMMAND, getDenyCommand());
        TextComponent deny = new TextComponent(ChatColor.DARK_RED + FMessage.MISC_DENY.getMessage());
        deny.setClickEvent(onClickDeny);

        boolean add = true;
        if (getObject().getRequests() == null) {
            getObject().initRequests();
        }
        for (PeaceOffer check : getObject().getRequests(PeaceOffer.class)) {
            if (check.getSubject() == subject && check.getObject() == object) {
                add = false;
                break;
            }
        }
        if (!(add)) {
            for (Player player : object.getRequestAuthorizedPlayers(getClass()).getOnlinePlayers()) {
                MessageUtil.sendMessage(player, FMessage.WAR_DEMAND_REQUEST_ALREADY_SENT.getMessage());
            }
        }
        if (add) {
            MessageUtil.log("Added new FinalPeaceOffer to " + getObject().getName());
            getObject().getRequests().add(this);
        }

        sendSubjectMessage();
        sendObjectMessage();
        for (Player player : object.getRequestAuthorizedPlayers(getClass()).getOnlinePlayers()) {
            MessageUtil.sendMessage(player, confirm, new TextComponent(" "), deny);
        }
    }

    @Override
    public String getAcceptCommand() {
        return "/f confirmPeace " + war.getStartDate().getTime() + " -acceptFinal";
    }

    @Override
    public String getDenyCommand() {
        return "/f confirmPeace " + war.getStartDate().getTime() + " -denyFinal";
    }

    @Override
    public void sendSubjectMessage() {
        for (Faction f : getSubject().getFactions()) {
            if (isOffer) {
                f.sendMessage(FMessage.WAR_OFFER_CHAT_TITLE.getMessage(getSubject().getName()));
            }
            else {
                f.sendMessage(FMessage.WAR_DEMAND_CHAT_TITLE.getMessage(getSubject().getName()));
            }
            f.sendMessage(" ");
            for (Object d : demands) {
                f.sendMessage(d.toString());
            }
            f.sendMessage(FMessage.WAR_DEMAND_CHAT_TOTAL_WARSCORE.getMessage(String.valueOf(getCost()), String.valueOf(getSubject().getPoints())));
            f.sendMessage(" ");
            f.sendMessage(FMessage.WAR_DEMAND_CHAT_SENT.getMessage());
        }
    }

    @Override
    public void sendObjectMessage() {
        for (Faction f : getObject().getFactions()) {
            if (isOffer) {
                f.sendMessage(FMessage.WAR_OFFER_CHAT_TITLE.getMessage(getSubject().getName()));
                f.sendMessage(" ");
                f.sendMessage(FMessage.WAR_OFFER_CHAT_EXPLANATION_1.getMessage());
                f.sendMessage(FMessage.WAR_OFFER_CHAT_EXPLANATION_2.getMessage());
                f.sendMessage(FMessage.WAR_DEMAND_CHAT_DEMANDS.getMessage());
            }
            else {
                f.sendMessage(FMessage.WAR_DEMAND_CHAT_TITLE.getMessage(getSubject().getName()));
                f.sendMessage(" ");
                f.sendMessage(FMessage.WAR_DEMAND_CHAT_EXPLANATION_1.getMessage());
                f.sendMessage(FMessage.WAR_DEMAND_CHAT_EXPLANATION_2.getMessage());
                f.sendMessage(FMessage.WAR_DEMAND_CHAT_DEMANDS.getMessage());
            }
            for (Object d : demands) {
                f.sendMessage(d.toString());
            }
            f.sendMessage(" ");
        }
    }

    @Override
    public ItemStack getButton(Player player) {
        String title = ParsingUtil.parseMessage(player, FMessage.WAR_GUI_BUTTON.getMessage(), getSubject());
        return GUIButton.setDisplay(new ItemStack(Material.WHITE_BANNER), title);
    }

    public double getCost() {
        cost = 0.00;
        for (WarDemand d : demands) {
            cost = cost + d.getWarscoreCost();
        }
        return Math.round(cost * 100.00) / 100.0;
    }

    public void purge() {
        Collection<FinalPeaceOffer> subjectOffers = getSubject().getRequests(FinalPeaceOffer.class);
        Collection<FinalPeaceOffer> objectOffers = getObject().getRequests(FinalPeaceOffer.class);
        if (objectOffers == null || subjectOffers == null) {
            return;
        }
        for (FinalPeaceOffer req : subjectOffers) {
            if ((req.getObject() == getObject() && req.getSubject() == getSubject()) || (req.getObject() == getSubject() && req.getSubject() == getObject())) {
                getSubject().getRequests().remove(req);
            }
        }
        for (FinalPeaceOffer req : objectOffers) {
            if ((req.getObject() == getObject() && req.getSubject() == getSubject()) || (req.getObject() == getSubject() && req.getSubject() == getObject())) {
                getObject().getRequests().remove(req);
            }
        }
    }

    public boolean isOffer() {
        return isOffer;
    }


    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> args = new HashMap<>();
        args.put("war", war.getStartDate().getTime());
        args.put("subject", getSubject().getRole().name());
        args.put("object", getObject().getRole().name());
        args.put("expiration", expiration);
        args.put("offer", isOffer);
        args.put("demands", demands);
        return args;
    }

}
