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

package de.erethon.factionsxl.menu;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.GovernmentType;
import de.erethon.factionsxl.menu.faction.FMenuRegion;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.vignette.api.InventoryGUI;
import de.erethon.vignette.api.SingleInventoryGUI;
import de.erethon.vignette.api.VignetteAPI;
import de.erethon.vignette.api.component.Component;
import de.erethon.vignette.api.component.InventoryButton;
import de.erethon.vignette.api.component.InventoryButtonBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FMenu extends SingleInventoryGUI {

    FactionsXL plugin = FactionsXL.getInstance();

    private InventoryButton backButton = new InventoryButtonBuilder()
            .icon(Material.ARROW)
            .title(ChatColor.GOLD + "BACK")
            .build();
    private InventoryButton mainButton = new InventoryButtonBuilder()
            .icon(Material.CHEST)
            .title(ChatColor.GOLD + "Main")
            .onInteract(i -> new FMainMenu(i.getPlayer()).open(i.getPlayer()))
            .build();
    private InventoryButton playerButton = new InventoryButtonBuilder()
            .icon(Material.PLAYER_HEAD)
            .title(ChatColor.GOLD + "Player")
            .onInteract(i -> new FMenuPlayer().open(i.getPlayer()))
            .build();
    public InventoryButton factionButton = new InventoryButtonBuilder()
            .icon(Material.RED_BANNER)
            .title(ChatColor.GOLD + "Faction")
            .onInteract(i -> new FMenuFaction(i.getPlayer()).open(i.getPlayer()))
            .build();
    private InventoryButton regionButton = new InventoryButtonBuilder()
            .icon(Material.GRASS_BLOCK)
            .title(ChatColor.GOLD + "Region")
            .onInteract(i -> new FMenuRegion().open(i.getPlayer()))
            .build();
    private InventoryButton populationButton = new InventoryButtonBuilder()
            .icon(Material.VILLAGER_SPAWN_EGG)
            .title(ChatColor.GOLD + "Population")
            .lines(ChatColor.RED + "Aktuell nicht verfügbar.")
            .build();
    private InventoryButton settingsButton = new InventoryButtonBuilder()
            .icon(Material.COMPARATOR)
            .title(ChatColor.GOLD + "Settings")
            .build();

    FInventoryLayout layout;
    Player viewer;

    public FMenu(String title, Player player) {
        super(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "F" + ChatColor.WHITE + "XL" + ChatColor.GRAY + "] " + title);
        viewer = player;
        setLayout(new FInventoryLayout(this, 54));
        layout = (FInventoryLayout) getLayout();
        setCloseListener(closeEvent -> onClose());
        setup();
        VignetteAPI.register(this);
    }

    public void setup() {
        FPlayer fPlayer = plugin.getFPlayerCache().getByPlayer(viewer);
        Faction faction = plugin.getFactionCache().getByFPlayer(fPlayer);
        if (backButton.getInteractionListener() != null) {
            set(45, backButton);
        }
        set(48, mainButton);
        playerButton.setTitle(viewer.getDisplayName());
        playerButton.setLines(getPlayerShort(viewer));
        set(49, playerButton);
        if (faction != null) {
            factionButton.setTitle(faction.getName());
        }
        factionButton.setLines(getFactionShort(faction));
        set(50, factionButton);
        regionButton.setLines(getRegionShort(fPlayer));
        set(51, regionButton);
        set(52, populationButton);
        set(53, settingsButton);
    }

    public List<String> getPlayerShort(Player p) {
        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.GOLD + "Spieler seit: " + ChatColor.GRAY + new Date(p.getFirstPlayed()));
        lines.add(ChatColor.DARK_GRAY + "IP: " + p.getAddress());
        lines.add(ChatColor.DARK_GRAY + "Sprache: " + p.getLocale());
        lines.add(ChatColor.DARK_GRAY + "Client: " +  p.getClientBrandName() + " (" + p.getProtocolVersion() + ")");
        lines.add(ChatColor.DARK_GRAY + "Server-RP: " +  p.getResourcePackStatus());
        return lines;
    }

    public List<String> getFactionShort(Faction f) {
        List<String> lines = new ArrayList<>();
        if (f == null) {
            lines.add(ChatColor.RED + "Du bist in keiner Fraktion.");
            return lines;
        }
        lines.add(FMessage.CMD_SHOW_TAG.getMessage( ) + f.getShortName());
        lines.add(FMessage.CMD_SHOW_DESCRIPTION.getMessage() +  f.getDescription());
        if (plugin.getFConfig().isEconomyEnabled()) {
            lines.add(FMessage.CMD_SHOW_BALANCE.getMessage() + plugin.getEconomyProvider().format(f.getAccount().getBalance()));
        }
        String govType = f.getGovernmentType().getName();
        if (f.getGovernmentType() == GovernmentType.MONARCHY) {
            govType += " (" + (f.getDynasty() != null ? f.getDynasty().getName() : "&oInterregnum") + ")";
        }
        lines.add(FMessage.CMD_SHOW_GOVERNMENT_TYPE.getMessage() + govType);
        lines.add(FMessage.CMD_SHOW_CAPITAL.getMessage() + f.getCapital().getName());
        String power = String.valueOf(f.getPower());
        String provinces = String.valueOf(f.getRegions().size());
        int pop = 0;
        for (Region rg : f.getRegions()) {
            pop = pop + rg.getPopulation();
        }
        String population = String.valueOf(pop);
        lines.add(FMessage.CMD_SHOW_INFO.getMessage(power, provinces, population));
        lines.add(FMessage.CMD_SHOW_STABILITY.getMessage() + f.getStability());
        lines.add(" ");
        lines.add (ChatColor.GRAY + "Klicke hier, um zum Fraktions-Menü");
        lines.add(ChatColor.GRAY + "zu gelangen.");
        return lines;
    }

    public List<String> getRegionShort(FPlayer fp) {
        List<String> lines = new ArrayList<>();
        Region rg = fp.getLastRegion();
        if (rg == null) {
            lines.add(ChatColor.RED + "Keine Region gefunden.");
            return lines;
        }
        Faction faction = rg.getOwner();
        lines.add(FMessage.CMD_REGION_OWNER.getMessage() + (faction != null ? faction.getLongName() : "None"));
        if (rg.isNeutral()) {
            lines.add(FMessage.CMD_REGION_PRICE.getMessage() + rg.getClaimPrice(fp.getFaction()));
        }
        lines.add(FMessage.CMD_REGION_INFLUENCE.getMessage() + rg.getInfluence() + "%");
        lines.add(FMessage.CMD_REGION_TYPE.getMessage()  + rg.getType().getName() + " (" + rg.getLevel() + ")");
        lines.add(FMessage.CMD_REGION_POPULATION.getMessage() +  rg.getPopulation() + "/" + rg.getType().getMaxPopulation(rg.getLevel()));
        return lines;
    }

    public void set(int slot, Component<?, InventoryGUI> component) {
        layout.set(slot, component);
    }

    public InventoryButton getBackButton() {
        return backButton;
    }

    public InventoryButton getMainButton() {
        return mainButton;
    }

    public InventoryButton getPlayerButton() {
        return playerButton;
    }

    public InventoryButton getFactionButton() {
        return factionButton;
    }

    public InventoryButton getRegionButton() {
        return regionButton;
    }

    public InventoryButton getPopulationButton() {
        return populationButton;
    }

    public InventoryButton getSettingsButton() {
        return settingsButton;
    }

    public void onClose() {
        VignetteAPI.unregister(this);
    }
}
