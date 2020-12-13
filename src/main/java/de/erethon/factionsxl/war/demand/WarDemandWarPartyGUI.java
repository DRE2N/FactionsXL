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
package de.erethon.factionsxl.war.demand;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.gui.WarPartyGUI;
import de.erethon.factionsxl.player.FPlayerCache;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.WarCache;
import de.erethon.factionsxl.war.WarParty;
import de.erethon.factionsxl.war.peaceoffer.FinalPeaceOffer;
import de.erethon.factionsxl.war.peaceoffer.SeparatePeaceOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

import java.util.Collection;

/**
 * @author Daniel Saukel
 */
public class WarDemandWarPartyGUI extends WarPartyGUI {

    private final FPlayerCache fPlayers;
    private final WarCache wars;

    private final Collection<Faction> ownFactions;

    public WarDemandWarPartyGUI(FactionsXL plugin, Collection<Faction> ownFactions, WarParty... parties) {
        super(plugin, FMessage.FACTION_SELECT.getMessage(), parties);
        fPlayers = plugin.getFPlayerCache();
        wars = plugin.getWarCache();
        this.ownFactions = ownFactions;
    }

    @Override
    public void onButtonClick(Player whoClicked, boolean left, boolean right, Faction buttonFaction) {
        WarParty offerTarget = null;
        for (WarParty party : buttonFaction.getWarParties()) {
            if (party.getLeader() == buttonFaction) {
                offerTarget = party;
            }
        }
        if (offerTarget != null) {
            War war = offerTarget.getWar();
            WarParty demanding = war.getEnemy(offerTarget);
            if (right) {
                fPlayers.getByPlayer(whoClicked).setPeaceOffer(new FinalPeaceOffer(war, demanding, offerTarget));
                MessageUtil.sendMessage(whoClicked, FMessage.WAR_DEMAND_CREATION_MENU_MAKE_DEMANDS.getMessage());
                wars.getWarDemandCreationMenu().open(whoClicked, buttonFaction, false);
            }
            if (left) {
                fPlayers.getByPlayer(whoClicked).setPeaceOffer(new FinalPeaceOffer(war, true, demanding, offerTarget));
                MessageUtil.sendMessage(whoClicked, FMessage.WAR_DEMAND_CREATION_MENU_MAKE_OFFER.getMessage());
                wars.getWarDemandCreationMenu().open(whoClicked, buttonFaction, true);
            }
        } else {
            War war = null;
            for (Faction f : ownFactions) {
                if (wars.getWarTogether(f, buttonFaction) != null) {
                    war = wars.getWarTogether(f, buttonFaction);
                }
            }
            if (war == null) {
                MessageUtil.sendMessage(whoClicked, FMessage.ERROR_NOT_IN_WAR.getMessage());
                return;
            }
            if (right) {
                fPlayers.getByPlayer(whoClicked).setPeaceOffer(new SeparatePeaceOffer(war, fPlayers.getByPlayer(whoClicked).getFaction() , buttonFaction, false));
                MessageUtil.sendMessage(whoClicked, FMessage.WAR_DEMAND_CREATION_MENU_MAKE_DEMANDS.getMessage());
                wars.getWarDemandCreationMenu().open(whoClicked, buttonFaction, false);
            }
            if (left) {
                fPlayers.getByPlayer(whoClicked).setPeaceOffer(new SeparatePeaceOffer(war, fPlayers.getByPlayer(whoClicked).getFaction(), buttonFaction, true));
                MessageUtil.sendMessage(whoClicked, FMessage.WAR_DEMAND_CREATION_MENU_MAKE_OFFER.getMessage());
                wars.getWarDemandCreationMenu().open(whoClicked, buttonFaction, true);
            }
        }
        HandlerList.unregisterAll(this);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

}
