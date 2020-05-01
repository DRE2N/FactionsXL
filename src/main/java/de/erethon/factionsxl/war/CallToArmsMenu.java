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
import de.erethon.commons.gui.PageGUI;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.faction.Faction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Daniel Saukel
 * TODO: Should get updated to Vignette. Not sure how to do the 3 different scrolling-menus
 */

public class CallToArmsMenu implements Listener {

    private WarParty attacker;
    private Faction attackerLeader;
    private Set<Faction> attackerCandidates = new HashSet<>();
    private Set<Faction> invitedAttackers = new HashSet<>();
    private WarParty defender;
    private CasusBelli cb;
    private PageGUI gui;
    private HumanEntity cachedViewer;
    private boolean isReopen = false;

    FactionsXL plugin = FactionsXL.getInstance();

    public CallToArmsMenu(WarParty attacker, Faction defender, CasusBelli cb) {

        Bukkit.getPluginManager().registerEvents(this, FactionsXL.getInstance());
        this.attacker = attacker;
        attackerLeader = (Faction) attacker.getLeader(); // TODO: Might break after government update
        attackerLeader.getRelatedFactions(Relation.ALLIANCE).forEach(f -> attackerCandidates.add(f));
        attackerLeader.getRelatedFactions(Relation.COALITION).forEach(f -> attackerCandidates.add(f));
        Faction mainTarget = defender.isVassal() ? defender.getLord() : defender;
        this.defender = new WarParty(mainTarget, WarPartyRole.DEFENDER);
        for (Entry<Faction, Relation> entry : mainTarget.getRelations().entrySet()) {
            Relation relation = entry.getValue();
            if (relation == Relation.ALLIANCE || relation == Relation.PERSONAL_UNION || relation == Relation.VASSAL) {
                this.defender.addParticipant(entry.getKey());
            }
        }
        gui = new PageGUI(FMessage.WAR_CALL_TO_ARMS_TITLE.getMessage(), 2);
        this.attacker.getFactions().forEach(f -> gui.addButton1(generateFactionButton(f, WarPartyRole.ATTACKER))); // Call to arms
        for (Faction faction : attackerCandidates) {// Allies and coalition partners
            if (!this.attacker.getFactions().contains(faction)) {
                gui.addButton2(generateFactionButton(faction, WarPartyRole.ATTACKER_CANDIDATE));
            }
        }
        this.defender.getFactions().forEach(f -> gui.addButton3(generateFactionButton(f, WarPartyRole.DEFENDER))); // Enemies
        this.cb = cb;
    }

    public void open(HumanEntity player) {
        cachedViewer = player;
        gui.open(player, 0, 0, 0);
    }

    private ItemStack generateFactionButton(Faction faction, WarPartyRole status) {
        ItemStack button = faction.getBannerStack();
        ItemMeta meta = button.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.setDisplayName(attackerLeader.getRelation(faction).getColor() + faction.getName());
        List<String> lore = new ArrayList<>();
        if (status != WarPartyRole.DEFENDER) {
            switch (attackerLeader.getRelation(faction)) {
                case ALLIANCE:
                    lore.add(FMessage.WAR_CALL_TO_ARMS_ALLY_1.getMessage(faction.getName()));
                    if (defender.getParticipants().contains(faction)) {
                        lore.add(FMessage.WAR_CALL_TO_ARMS_ALLY_2_ALLIED.getMessage());
                        lore.add(FMessage.WAR_CALL_TO_ARMS_ALLY_3_ALLIED.getMessage());
                    } else {
                        lore.add(FMessage.WAR_CALL_TO_ARMS_ALLY_2_NOT_ALLIED.getMessage());
                        lore.add(FMessage.WAR_CALL_TO_ARMS_ALLY_3_NOT_ALLIED.getMessage());
                        lore.add((status == WarPartyRole.ATTACKER_CANDIDATE ? FMessage.WAR_CALL_TO_ARMS_ADD : FMessage.WAR_CALL_TO_ARMS_REMOVE).getMessage());
                    }
                    break;
                case PERSONAL_UNION:
                case OWN:
                    lore.add(FMessage.WAR_CALL_TO_ARMS_OWN.getMessage(faction.getName()));
                    break;
                case VASSAL:
                    lore.add(FMessage.WAR_CALL_TO_ARMS_VASSAL_1.getMessage(faction.getName()));
                    // TODO if (vassal is stronger than lord) lore.add(FMessage.WAR_CALL_TO_ARMS_VASSAL_2_WEAKER.getMessage()
                    lore.add(FMessage.WAR_CALL_TO_ARMS_VASSAL_2_STRONGER.getMessage());
                    lore.add((status == WarPartyRole.ATTACKER_CANDIDATE ? FMessage.WAR_CALL_TO_ARMS_ADD : FMessage.WAR_CALL_TO_ARMS_REMOVE).getMessage());
                    break;
                case COALITION:
                    lore.add(FMessage.WAR_CALL_TO_ARMS_COALITION_1.getMessage(faction.getName()));
                    lore.add(FMessage.WAR_CALL_TO_ARMS_COALITION_2.getMessage());
                    lore.add((status == WarPartyRole.ATTACKER_CANDIDATE ? FMessage.WAR_CALL_TO_ARMS_ADD : FMessage.WAR_CALL_TO_ARMS_REMOVE).getMessage());
                    break;
            }
        } else {
            lore.add(FMessage.WAR_CALL_TO_ARMS_DEFENDER.getMessage(faction.getName()));
        }
        meta.setLore(lore);
        button.setItemMeta(meta);
        return button;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        int slot = event.getSlot();
        if (inventory == null || event.getWhoClicked() != cachedViewer || (slot > 8 & slot < 18 || slot > 26 & slot < 36 || slot >= 45)) {
            return;
        }
        ItemStack button = event.getCurrentItem();
        if (button == null || !button.hasItemMeta()) {
            return;
        }
        if (!(event.getView().getTitle().contains(FMessage.WAR_CALL_TO_ARMS_TITLE.getMessage()))) {
            return;
        }
        ItemMeta meta = button.getItemMeta();
        List<String> lore = meta.getLore();
        if (button.getItemMeta().getLore().contains(FMessage.WAR_CALL_TO_ARMS_ADD.getMessage())) {
            String name = ChatColor.stripColor(button.getItemMeta().getDisplayName());

            Faction f = plugin.getFactionCache().getByName(name);
            invitedAttackers.add(f);
            MessageUtil.sendMessage(event.getWhoClicked(), FMessage.WAR_CALL_TO_ARMS_ADDED_FACTION.getMessage(button.getItemMeta().getDisplayName()));
            MessageUtil.sendMessage(event.getWhoClicked(), attackerCandidates.toString());
            gui.removeButton2(button);
            gui.addButton1(button);

            lore.remove(FMessage.WAR_CALL_TO_ARMS_ADD.getMessage());
            lore.add(FMessage.WAR_CALL_TO_ARMS_REMOVE.getMessage());

            isReopen = true;
            gui.open(event.getWhoClicked(), 0, 0, 0);
        } else if (button.getItemMeta().getLore().contains(FMessage.WAR_CALL_TO_ARMS_REMOVE.getMessage())) {
            String name = ChatColor.stripColor(button.getItemMeta().getDisplayName());
            Faction f = plugin.getFactionCache().getByName(name);
            invitedAttackers.remove(f);
            MessageUtil.sendMessage(event.getWhoClicked(), FMessage.WAR_CALL_TO_ARMS_REMOVED_FACTION.getMessage(button.getItemMeta().getDisplayName()));

            lore.remove(FMessage.WAR_CALL_TO_ARMS_REMOVE.getMessage());
            lore.add(FMessage.WAR_CALL_TO_ARMS_ADD.getMessage());

            gui.removeButton1(button);
            gui.addButton2(button);
            isReopen = true;
            gui.open(event.getWhoClicked(), 0, 0, 0);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() != cachedViewer || !(event.getPlayer() instanceof Player)) {
            return;
        }
        if (!(event.getView().getTitle().equals(FMessage.WAR_CALL_TO_ARMS_TITLE.getMessage()))) {
            return;
        }
        if (isReopen) {
            isReopen = false;
            return;
        }
        for (Faction f : invitedAttackers) {
            attacker.addInvited(f);
        }
        War war = new War(attacker, defender, cb);
        FactionsXL.getInstance().getWarCache().getUnconfirmedWars().add(war);
        war.sendConfirmRequest((Player) event.getPlayer());
        HandlerList.unregisterAll(this);
    }

}
