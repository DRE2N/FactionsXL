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
import de.erethon.factionsxl.faction.LegalEntity;
import de.erethon.factionsxl.legacygui.GUIButton;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.demand.WarDemand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * @author Daniel Saukel
 */
public class SeparatePeaceOffer extends PeaceOffer {

    FactionsXL plugin = FactionsXL.getInstance();
    Boolean isOffer = false;
    Double cost = 0.00;

    public SeparatePeaceOffer(War war, LegalEntity demanding, LegalEntity target, boolean isOfferBool, WarDemand... demands) {
        this.war = war;
        subject = demanding;
        object = target;
        isOffer = isOfferBool;
        expiration = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        this.demands = new ArrayList(Arrays.asList(demands));
    }

    public SeparatePeaceOffer(Map<String, Object> args) {
        new BukkitRunnable() {

            @Override
            public void run() {
                war =  FactionsXL.getInstance().getWarCache().getByDate((long) args.get("war"));
                subject = plugin.getFactionCache().getById((int) args.get("subject"));
                object = plugin.getFactionCache().getById((int) args.get("object"));
                isOffer = (boolean) args.get("offer");
                demands = (List<WarDemand>) args.get("demands");
                expiration = (long) args.get("expiration");
            }

        }.runTaskLater(this.plugin, 20);
    }

    @Override
    public Faction getSubject() {
        return (Faction) subject;
    }

    @Override
    public Faction getObject() {
        return (Faction) object;
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

    public double getCost() {
        cost = 0.00;
        for (WarDemand d : demands) {
            cost = cost + d.getWarscoreCost();
        }
        return Math.round(cost * 100.00) / 100.0;
    }

    @Override
    public void confirm() {
        if (isOffer) {
            demands.forEach(d -> d.pay(getObject(), getSubject()));
        }
        if (!isOffer) {
            demands.forEach(d -> d.pay(getSubject(), getObject()));
        }

        if (war.getAttacker().getFactions().contains(getObject())) {
            war.getAttacker().leaveWar(getObject());
        }
        if (war.getDefender().getFactions().contains(getObject())) {
            war.getDefender().leaveWar(getObject());
        }
        MessageUtil.broadcastMessage(" ");
        ParsingUtil.broadcastMessage(FMessage.WAR_ALLY_LEFT_WAR.getMessage(), getObject(), getSubject());
        MessageUtil.broadcastMessage(" ");
        // TODO: Might break after government update
        // TODO: Add time modifier
        purge();
    }

    @Override
    public void send() {
        ClickEvent onClickConfirm = new ClickEvent(ClickEvent.Action.RUN_COMMAND, getAcceptCommand());
        TextComponent confirm = new TextComponent(ChatColor.GREEN + FMessage.MISC_ACCEPT.getMessage());
        confirm.setClickEvent(onClickConfirm);

        ClickEvent onClickDeny = new ClickEvent(ClickEvent.Action.RUN_COMMAND, getDenyCommand());
        TextComponent deny = new TextComponent(ChatColor.DARK_RED + FMessage.MISC_DENY.getMessage());
        deny.setClickEvent(onClickDeny);

        boolean add = true;
        for (SeparatePeaceOffer check : getObject().getRequests(SeparatePeaceOffer.class)) {
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
            getObject().getRequests().add(this);
        }

        sendSubjectMessage();
        sendObjectMessage();
        for (Player player : object.getRequestAuthorizedPlayers(getClass()).getOnlinePlayers()) {
            MessageUtil.sendMessage(player, confirm, new TextComponent(" "), deny);
        }
    }

    @Override
    public ItemStack getButton(Player player) {
        String title = ParsingUtil.parseMessage(player, FMessage.WAR_GUI_BUTTON.getMessage(), getSubject());
        return GUIButton.setDisplay(new ItemStack(Material.WHITE_BANNER), title);
    }

    @Override
    public String getAcceptCommand() {
        return "/f confirmPeace " + war.getStartDate().getTime() + " -acceptSingle " + object.getName() + " " + subject.getName();
    }

    @Override
    public String getDenyCommand() {
        return "/f confirmPeace " + war.getStartDate().getTime() + " -denySingle " + object.getName() + " " + subject.getName();
    }

    @Override
    public void sendSubjectMessage() {
        Faction f = getSubject();
        if (isOffer) {
            f.sendMessage(FMessage.WAR_OFFER_CHAT_TITLE.getMessage());
        }
        else {
            f.sendMessage(FMessage.WAR_DEMAND_CHAT_TITLE.getMessage());
        }
        f.sendMessage(" ");
        for (Object d : demands) {
            f.sendMessage(d.toString());
        }
        f.sendMessage(" ");
        f.sendMessage(FMessage.WAR_DEMAND_CHAT_SENT.getMessage());

    }

    @Override
    public void sendObjectMessage() {
        Faction f = getObject();
        if (isOffer) {
            f.sendMessage(FMessage.WAR_OFFER_CHAT_TITLE.getMessage(getSubject().getName()));
            f.sendMessage(" ");
            f.sendMessage(FMessage.WAR_OFFER_CHAT_EXPLANATION_1_SEPARATE.getMessage());
            f.sendMessage(FMessage.WAR_OFFER_CHAT_EXPLANATION_2_SEPARATE.getMessage());
            f.sendMessage(FMessage.WAR_DEMAND_CHAT_DEMANDS.getMessage());
        } else {
            f.sendMessage(FMessage.WAR_DEMAND_CHAT_TITLE.getMessage(getSubject().getName()));
            f.sendMessage(" ");
            f.sendMessage(FMessage.WAR_DEMAND_CHAT_EXPLANATION_1_SEPARATE.getMessage());
            f.sendMessage(FMessage.WAR_DEMAND_CHAT_EXPLANATION_2_SEPARATE.getMessage());
            f.sendMessage(FMessage.WAR_DEMAND_CHAT_DEMANDS.getMessage());
        }
        for (Object d : demands) {
            f.sendMessage(d.toString());
        }
        f.sendMessage(" ");

    }

    public boolean isOffer() {
        return isOffer;
    }

    public void purge() {
        for (PeaceOffer req : getSubject().getRequests(SeparatePeaceOffer.class)) {
            if ((req.getObject() == getObject() && req.getSubject() == getSubject()) || (req.getObject() == getSubject() && req.getSubject() == getObject())) {
                getSubject().getRequests().remove(req);
            }
        }
        for (PeaceOffer req : getObject().getRequests(SeparatePeaceOffer.class)) {
            if ((req.getObject() == getObject() && req.getSubject() == getSubject()) || (req.getObject() == getSubject() && req.getSubject() == getObject())) {
                getObject().getRequests().remove(req);
            }
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> args = new HashMap<>();
        args.put("war", war.getStartDate().getTime());
        args.put("subject", getSubject().getId());
        args.put("object", getObject().getId());
        args.put("demands", demands);
        args.put("offer", isOffer);
        args.put("expiration", expiration);
        return args;
    }

}
