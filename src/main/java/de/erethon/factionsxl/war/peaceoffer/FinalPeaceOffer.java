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
package de.erethon.factionsxl.war.peaceoffer;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.gui.GUIButton;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.WarParty;
import de.erethon.factionsxl.war.WarPartyRole;
import de.erethon.factionsxl.war.demand.WarDemand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
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
            Bukkit.broadcastMessage(getObject() + " -> " + getSubject());
        }
        if (!isOffer) {
            demands.forEach(d -> d.pay(getSubject(), getObject()));
        }
        war.end();
        MessageUtil.broadcastMessage(" ");
        MessageUtil.broadcastMessage("&aThe war between &6" + getObject().getName() + " &aand &6" + getSubject().getName() + "&a ended!");
        MessageUtil.broadcastMessage(" ");
            // TODO: Might break after government update
            // TODO: Add time modifier
        purge();
    }

    @Override
    public void send() {
        if (!isOffer) {
            if (getCost() > getSubject().getPoints()) {
                for (Player player : subject.getRequestAuthorizedPlayers(getClass()).getOnlinePlayers()) {
                    MessageUtil.sendMessage(player, "&cYou do not have the Warscore required to make these demands!");
                    MessageUtil.sendMessage(player, "&7Your Warscore&8: " + getSubject().getPoints() + " &7Needed&8: " + getCost());
                }
                return;
            }
        }
        if (!canPay()) {
            for (Player player : subject.getRequestAuthorizedPlayers(getClass()).getOnlinePlayers()) {
                MessageUtil.sendMessage(player, "&cYour faction can not afford this!");
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
                MessageUtil.sendMessage(player, "&cRequest already sent. They first need to accept or deny.");
            }
        }
        if (add) {
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
        return "/f confirmPeace " + war.getStartDate().getTime() + " -accept";
    }

    @Override
    public String getDenyCommand() {
        return "/f confirmPeace " + war.getStartDate().getTime() + " -deny";
    }

    @Override
    public void sendSubjectMessage() {
        for (Faction f : getSubject().getFactions()) {
            if (isOffer) {
                f.sendMessage("&8&m----------&r &f&lPeace offer&r &8&m----------");
            }
            else {
                f.sendMessage("&8&m----------&r &f&lPeace demand&r &8&m----------");
            }
            f.sendMessage(" ");
            for (Object d : demands) {
                f.sendMessage(d.toString());
            }
            f.sendMessage("&7Total Warscore&8: &5" + getCost() + "&8/&6" + getSubject().getPoints());
            f.sendMessage(" ");
            f.sendMessage("&aPeace request sent!");
        }
    }

    @Override
    public void sendObjectMessage() {
        for (Faction f : getObject().getFactions()) {
            if (isOffer) {
                f.sendMessage("&8&m----------&r &f&lPeace offer&r &8&m----------");
                f.sendMessage(" ");
                f.sendMessage("&7&oAccepting this request will end the war immediately.");
                f.sendMessage("&7&oand you will get the following from the demanding faction: ");
                f.sendMessage("&6&lOffers&8&l:");
            }
            else {
                f.sendMessage("&8&m----------&r &f&lPeace demand&r &8&m----------");
                f.sendMessage(" ");
                f.sendMessage("&7&oAccepting this request will end the war immediately.");
                f.sendMessage("&7&oBut you will need to pay the following to the demanding faction: ");
                f.sendMessage("&6&lDemands&8&l:");
            }
            for (Object d : demands) {
                f.sendMessage(d.toString());
            }
            f.sendMessage(" ");
        }
    }

    @Override
    public ItemStack getButton(Player player) {
        String title = ParsingUtil.parseMessage(player, "&f&lPeace request &8- &6" + getSubject().getName());
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
        // should remove the request. Does not persist restarts anyway.
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
