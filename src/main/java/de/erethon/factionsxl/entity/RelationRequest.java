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
package de.erethon.factionsxl.entity;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.gui.StandardizedGUI;
import de.erethon.factionsxl.legacygui.GUIButton;
import de.erethon.factionsxl.scoreboard.FTeamWrapper;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.CasusBelli;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Saukel
 */
public class RelationRequest extends Request {

    private int subjectId;
    private int objectId;
    private CommandSender sender;
    private Relation relation;

    public RelationRequest(CommandSender sender, Faction subject, Faction object, Relation relation) {
        this.sender = sender;
        this.subject = subject;
        this.object = object;
        this.relation = relation;
        expiration = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        boolean add = true;
        for (RelationRequest check : object.getRequests(RelationRequest.class)) {
            if (check.getSubject() == subject && check.getObject() == object && check.getRelation() == relation) {
                add = false;
            }
        }
        if (add) {
            object.getRequests().add(this);
        }
    }

    public RelationRequest(Map<String, Object> args) {
        subjectId = (int) args.get("subject");
        objectId = (int) args.get("object");
        relation = Relation.valueOf(Relation.class, (String) args.get("relation"));
        expiration = (long) args.get("expiration");
    }

    /**
     * @return
     * the CommandSender that did the request
     */
    public CommandSender getSender() {
        return sender;
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

    public Relation getRelation() {
        return relation;
    }

    public Relation getObjectRelation() {
        switch (relation) {
            case VASSAL:
                return Relation.LORD;
            case LORD:
                return Relation.VASSAL;
            default:
                return relation;
        }
    }

    /**
     * @return
     * true if the subject faction can pay for the request
     */
    public boolean checkSubject() {
        FConfig config = FactionsXL.getInstance().getFConfig();
        if (!config.isEconomyEnabled()) {
            return true;
        }
        return getSubject().getAccount().getBalance() >= config.getPriceRelation(relation);
    }

    /**
     * @return
     * true if the object faction can pay for the request
     */
    public boolean checkObject() {
        FConfig config = FactionsXL.getInstance().getFConfig();
        if (!config.isEconomyEnabled()) {
            return true;
        }
        return getObject().getAccount().getBalance() >= config.getPriceRelation(getObjectRelation());
    }

    @Override
    public void confirm() {
        if (relation == Relation.REAL_UNION) {
            FactionsXL.getInstance().getFactionCache().formRealUnion(getSubject(), getObject());
            ParsingUtil.broadcastMessage(FMessage.RELATION_UNITED.getMessage(), getSubject(), getObject());
            return;
        }

        FConfig config = FactionsXL.getInstance().getFConfig();
        if (config.isEconomyEnabled()) {
            if (!checkSubject()) {
                getSubject().sendMessage(FMessage.ERROR_NOT_ENOUGH_MONEY_FACTION.getMessage(), getSubject(), String.valueOf(config.getPriceRelation(relation)));
                getObject().sendMessage(FMessage.ERROR_NOT_ENOUGH_MONEY_FACTION.getMessage(), getSubject(), String.valueOf(config.getPriceRelation(relation)));
                return;
            }
            if (!checkObject()) {
                getSubject().sendMessage(FMessage.ERROR_NOT_ENOUGH_MONEY_FACTION.getMessage(), getObject(), String.valueOf(config.getPriceRelation(getObjectRelation())));
                getObject().sendMessage(FMessage.ERROR_NOT_ENOUGH_MONEY_FACTION.getMessage(), getObject(), String.valueOf(config.getPriceRelation(getObjectRelation())));
                return;
            }
            getSubject().getAccount().withdraw(config.getPriceRelation(relation));
            getObject().getAccount().withdraw(config.getPriceRelation(getObjectRelation()));
        }

        getSubject().getRelations().put(getObject(), relation);
        if (relation != Relation.LORD && relation != Relation.VASSAL) {
            getObject().getRelations().put(getSubject(), relation);
            ParsingUtil.broadcastMessage(FMessage.RELATION_CONFIRMED.getMessage(), getSubject(), getObject(), relation.getName());
        } else if (relation == Relation.VASSAL) {
            getObject().getRelations().put(getSubject(), Relation.LORD);
            getObject().setAllod(true);
            getObject().getCasusBelli().add(new CasusBelli(CasusBelli.Type.INDEPENDENCE, getSubject(), null));
            ParsingUtil.broadcastMessage(FMessage.RELATION_VASSALIZED.getMessage(), getSubject(), getObject());
        } else if (relation == Relation.LORD) {
            getObject().getRelations().put(getSubject(), Relation.VASSAL);
            getSubject().setAllod(true);
            getObject().getCasusBelli().add(new CasusBelli(CasusBelli.Type.INDEPENDENCE, getSubject(), null));
            ParsingUtil.broadcastMessage(FMessage.RELATION_VASSALIZED.getMessage(), getObject(), getSubject());
        }
        if (!getSubject().isVassal()) {
            getSubject().setAllod(true);
        }
        if (!getObject().isVassal()) {
            getObject().setAllod(true);
        }
        FTeamWrapper.updatePrefixes(getSubject());
        FTeamWrapper.updatePrefixes(getObject());
        purge();
    }

    /**
     * Removes all RelationRequests that involve both the subject and the object
     */
    public void purge() {
        for (RelationRequest req : getSubject().getRequests(RelationRequest.class)) {
            if ((req.getObject() == getObject() && req.getSubject() == getSubject()) || (req.getObject() == getSubject() && req.getSubject() == getObject())) {
                getSubject().getRequests().remove(req);
            }
        }
        for (RelationRequest req : getObject().getRequests(RelationRequest.class)) {
            if ((req.getObject() == getObject() && req.getSubject() == getSubject()) || (req.getObject() == getSubject() && req.getSubject() == getObject())) {
                getObject().getRequests().remove(req);
            }
        }
    }

    @Override
    public String getAcceptCommand() {
        return "/factionsxl relation " + getObject().getName() + " " + getSubject().getName() + " " + relation.toString();
    }

    @Override
    public String getDenyCommand() {
        return getAcceptCommand() + " -deny";
    }

    @Override
    public void sendSubjectMessage() {
        getSubject().sendMessage(FMessage.RELATION_WISH_OWN.getMessage(), sender, getObject(), relation.getName());// TODO getFormatted()
    }

    @Override
    public void sendObjectMessage() {
        getObject().sendMessage(FMessage.RELATION_WISH.getMessage(), getSubject(), relation.getName());// TODO getFormatted()
    }

    @Override
    public ItemStack getButton(Player player) {
        String title = ParsingUtil.parseMessage(player, FMessage.RELATION_REQUEST_BUTTON.getMessage(), getSubject(), getObject(), relation);
        String explanation = null;
        if (relation == Relation.LORD) {
            explanation = ParsingUtil.parseMessage(player, FMessage.RELATION_REQUEST_VASSAL.getMessage(), getSubject());
        } else if (relation == Relation.VASSAL) {
            explanation = ParsingUtil.parseMessage(player, FMessage.RELATION_REQUEST_VASSAL.getMessage(), getObject());
        }
        return explanation == null ? GUIButton.setDisplay(StandardizedGUI.MAILBOX, title) : GUIButton.setDisplay(StandardizedGUI.MAILBOX, title, explanation);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> args = new HashMap<>();
        args.put("subject", getSubject().getId());
        args.put("object", getObject().getId());
        args.put("relation", relation.name());
        args.put("expiration", expiration);
        return args;
    }

}
