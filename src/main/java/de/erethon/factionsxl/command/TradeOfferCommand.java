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
package de.erethon.factionsxl.command;

import de.erethon.commons.misc.NumberUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.economy.Resource;
import de.erethon.factionsxl.economy.TradeOffer;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.legacygui.PageGUI;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.erethon.factionsxl.gui.StandardizedGUI.*;
import static de.erethon.factionsxl.legacygui.GUIButton.PLACEHOLDER;
import static de.erethon.factionsxl.legacygui.GUIButton.setDisplay;

/**
 * @author Daniel Saukel
 */
public class TradeOfferCommand extends FCommand implements Listener, InventoryHolder {

    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();

    private String tradeOfferChoosePriceAndAmount = ChatColor.GREEN.toString() + ChatColor.BOLD.toString()
            + FMessage.TRADE_OFFER_AMOUNT.getMessage() + ": " + ChatColor.DARK_AQUA.toString() + "&v1 " + ChatColor.GREEN.toString()
            + ChatColor.BOLD.toString() + FMessage.TRADE_PRICE.getMessage() + ": " + ChatColor.DARK_AQUA.toString() + "&v2";
    private String plus = ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + "+";
    private String minus = ChatColor.DARK_RED.toString() + ChatColor.BOLD.toString() + "-";
    private String amount = ChatColor.WHITE + FMessage.TRADE_OFFER_AMOUNT.getMessage() + " ";
    private String price = ChatColor.WHITE + FMessage.TRADE_PRICE.getMessage() + " ";

    private PageGUI chooseResource;
    private Inventory chooseExport;
    private ItemStack[] choosePriceAndAmount;

    public List<TradeOffer> finishedOffers = new ArrayList<>();
    private Map<Integer, Faction> factionBySlot = new HashMap<>();
    private Map<HumanEntity, TradeOffer> creatingOffer = new HashMap<>();

    public TradeOfferCommand() {
        setCommand("tradeOffer");
        setMinArgs(0);
        setMaxArgs(7);
        setHelp(FMessage.HELP_TRADE_OFFER.getMessage());
        setPermission(FPermission.TRADE_OFFER.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);

        Bukkit.getPluginManager().registerEvents(this, plugin);

        chooseResource = chooseResource();
        chooseExport = chooseExport();

        ItemStack amountInc100 = setDisplay(UP_ALT, amount + plus + 100);
        ItemStack amountInc10 = setDisplay(UP_ALT, amount + plus + 10);
        ItemStack amountInc1 = setDisplay(UP_ALT, amount + plus + 1);
        ItemStack amountDec100 = setDisplay(DOWN_ALT, amount + minus + 100);
        ItemStack amountDec10 = setDisplay(DOWN_ALT, amount + minus + 10);
        ItemStack amountDec1 = setDisplay(DOWN_ALT, amount + minus + 1);
        ItemStack priceInc1000 = setDisplay(UP, price + plus + 1000);
        ItemStack priceInc100 = setDisplay(UP, price + plus + 100);
        ItemStack priceInc10 = setDisplay(UP, price + plus + 10);
        ItemStack priceInc1 = setDisplay(UP, price + plus + 1);
        ItemStack priceInc01 = setDisplay(UP, price + plus + 0.10);
        ItemStack priceInc001 = setDisplay(UP, price + plus + 0.01);
        ItemStack priceDec1000 = setDisplay(DOWN, price + minus + 1000);
        ItemStack priceDec100 = setDisplay(DOWN, price + minus + 100);
        ItemStack priceDec10 = setDisplay(DOWN, price + minus + 10);
        ItemStack priceDec1 = setDisplay(DOWN, price + minus + 1);
        ItemStack priceDec01 = setDisplay(DOWN, price + minus + 0.10);
        ItemStack priceDec001 = setDisplay(DOWN, price + minus + 0.01);
        choosePriceAndAmount = new ItemStack[]{
            amountInc100, amountInc10, amountInc1,
            priceInc1000, priceInc100, priceInc10, priceInc1, priceInc01, priceInc001,
            amountDec100, amountDec10, amountDec1,
            priceDec1000, priceDec100, priceDec10, priceDec1, priceDec01, priceDec001,
            PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER,
            CONTINUE,
            PLACEHOLDER, PLACEHOLDER, PLACEHOLDER, PLACEHOLDER
        };
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        if (args.length == 2) {
            int i = NumberUtil.parseInt(args[1], -1);
            if (i != -1 && finishedOffers.size() > i) {
                finishedOffers.get(i).accept(sender);
                return;
            }
        }
        Player player = (Player) sender;
        Faction creator = null;
        if (args.length >= 2) {
            creator = factions.getByName(args[1]);
        } else {
            creator = factions.getByMember((Player) sender);
        }
        if (creator == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_FACTION.getMessage(), args.length >= 2 ? args[1] : "none");
            return;
        }
        if (!creator.isPrivileged(sender)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }

        if (args.length < 3) {
            player.openInventory(choosePartner(creator));
            return;
        }

        Faction partner = factions.getByName(args[2]);
        if (partner == null) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_SUCH_FACTION.getMessage(), args[2]);
            return;
        } else if (partner == creator) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_CANNOT_TRADE_WITH_ITSELF.getMessage());
            return;
        }

        if (args.length < 4) {
            player.openInventory(chooseExport);
            return;
        }

        boolean export = args[3].startsWith("e") || args[3].startsWith("E");
        Faction importer = export ? partner : creator;
        Faction exporter = export ? creator : partner;

        if (args.length < 5) {
            chooseResource.open(player);
            return;
        }

        Resource good = Resource.getByName(args[4]);

        if (args.length < 6) {
            player.openInventory(choosePriceAndAmount(0, 0));
            return;
        }

        int amount = NumberUtil.parseInt(args[5]);
        double price = NumberUtil.parseDouble(args[6]);

        if (args.length < 8) {
            player.openInventory(choosePriceAndAmount(amount, price));
            return;
        }

        if (args[7].equals("-confirm")) {
            TradeOffer offer = new TradeOffer(good, amount, importer, exporter, price);
            finishedOffers.add(offer);
            if (export) {
                offer.setExporterAccepted(true);
            } else {
                offer.setImporterAccepted(true);
            }
            if (offer.check(sender)) {
                offer.send();
            }
        }
    }

    private Inventory choosePartner(Faction creator) {
        int size = (int) (9 * Math.ceil(((double) factions.getActive().size() / 9)));
        Inventory gui = Bukkit.createInventory(this, size, FMessage.TRADE_OFFER_CHOOSE_PARTNER.getMessage());
        int i = 0;
        for (Faction faction : factions.getActive()) {
            ItemStack icon = faction.getBannerStack();
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(creator.getRelation(faction).getColor() + faction.getName());
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            icon.setItemMeta(meta);
            gui.setItem(i, icon);
            factionBySlot.put(i, faction);
            i++;
        }
        return gui;
    }

    private PageGUI chooseResource() {
        PageGUI gui = new PageGUI(FMessage.TRADE_OFFER_CHOOSE_RESOURCE.getMessage());
        for (Resource resource : Resource.values()) {
            ItemStack icon = null;
            if (resource.isPhysical()) {
                icon = resource.getIcon();
            } else {
                icon = new ItemStack(Material.BARRIER);
            }
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + resource.getName());
            icon.setItemMeta(meta);
            gui.addButton(icon);
        }
        return gui;
    }

    private Inventory chooseExport() {
        Inventory gui = Bukkit.createInventory(this, 9, FMessage.TRADE_OFFER_CHOOSE_EXPORT.getMessage());
        ItemStack exportIcon = E.clone();
        ItemMeta exMeta = exportIcon.getItemMeta();
        exMeta.setDisplayName(ChatColor.GREEN + FMessage.TRADE_EXPORT.getMessage());
        exportIcon.setItemMeta(exMeta);
        ItemStack importIcon = I.clone();
        ItemMeta imMeta = importIcon.getItemMeta();
        imMeta.setDisplayName(ChatColor.GREEN + FMessage.TRADE_IMPORT.getMessage());
        importIcon.setItemMeta(imMeta);
        gui.setItem(3, exportIcon);
        gui.setItem(5, importIcon);
        return gui;
    }

    private Inventory choosePriceAndAmount(int amount, double price) {
        Inventory gui = Bukkit.createInventory(this, 27, tradeOfferChoosePriceAndAmount.replace("&v1", String.valueOf(amount)).replace("&v2", String.valueOf(price)));
        gui.setContents(choosePriceAndAmount);
        return gui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title.equals(FMessage.TRADE_OFFER_CHOOSE_PARTNER.getMessage())) {
            onClickChoosePartner(event);
        } else if (title.equals(FMessage.TRADE_OFFER_CHOOSE_RESOURCE.getMessage())) {
            onClickChooseResource(event);
            return;
        } else if (title.equals(FMessage.TRADE_OFFER_CHOOSE_EXPORT.getMessage())) {
            onClickChooseExport(event);
        } else if (title.startsWith(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + FMessage.TRADE_OFFER_AMOUNT.getMessage())) {
            onClickChoosePriceAndAmount(event);
        } else {
            return;
        }
        event.setCancelled(true);
        PageGUI.playSound(event);
    }

    private void onClickChoosePartner(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Faction creator = factions.getByMember(player);
        Faction partner = factionBySlot.get(event.getSlot());
        if (creator == null || partner == null) {
            return;
        }
        creatingOffer.put(player, new TradeOffer(creator, partner));
        performCommand(event.getWhoClicked(), creator.getName() + " " + partner.getName());
    }

    private void onClickChooseExport(InventoryClickEvent event) {
        TradeOffer offer = creatingOffer.get(event.getWhoClicked());
        if (offer == null) {
            return;
        }
        Faction creator = offer.getImporter();
        Faction partner = offer.getExporter();
        String exImport = null;
        if (event.getSlot() == 3) {
            exImport = "export";
            offer.setExporter(creator);
            offer.setImporter(partner);
            offer.setExporterAccepted(true);
            offer.setImporterAccepted(false);
        } else if (event.getSlot() == 5) {
            exImport = "import";
            offer.setExporter(partner);
            offer.setImporter(creator);
            offer.setExporterAccepted(false);
            offer.setImporterAccepted(true);
        }
        performCommand(event.getWhoClicked(), creator.getName() + " " + partner.getName() + " " + exImport);
    }

    private void onClickChooseResource(InventoryClickEvent event) {
        if (event.getSlot() > 44 && event.getSlot() < 54) {
            return;
        }

        TradeOffer offer = creatingOffer.get(event.getWhoClicked());
        if (offer == null) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        ItemStack button = event.getCurrentItem();
        if (button == null) {
            return;
        }
        Resource resource = Resource.getByIcon(button);
        if (resource == null) {
            return;
        }
        offer.setGood(resource);

        Faction creator = offer.hasExporterAccepted() ? offer.getExporter() : offer.getImporter();
        Faction partner = offer.hasExporterAccepted() ? offer.getImporter() : offer.getExporter();
        String exImport = offer.hasExporterAccepted() ? "export" : "import";
        performCommand(event.getWhoClicked(), creator.getName() + " " + partner.getName() + " " + exImport + " " + resource.name());
    }

    private void onClickChoosePriceAndAmount(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        TradeOffer offer = creatingOffer.get(player);
        if (offer == null) {
            return;
        }
        Faction creator = offer.hasExporterAccepted() ? offer.getExporter() : offer.getImporter();
        Faction partner = offer.hasExporterAccepted() ? offer.getImporter() : offer.getExporter();
        String exImport = offer.hasExporterAccepted() ? "export" : "import";
        int rAmount = readAmountFromTitle(event.getView().getTitle());
        BigDecimal rPrice = readPriceFromTitle(event.getView().getTitle());

        ItemStack button = event.getCurrentItem();
        if (button == null) {
            return;
        }

        if (button.equals(CONTINUE)) {
            performCommand(player, creator.getName() + " " + partner.getName() + " " + exImport + " " + offer.getGood().name() + " " + rAmount + " " + rPrice + " -confirm");
            if (offer.check(player)) {
                player.closeInventory();
            }
            creatingOffer.remove(player);

        } else {
            String name = button.getItemMeta().getDisplayName();
            if (name.startsWith(amount)) {
                if (name.startsWith(amount + plus)) {
                    rAmount += NumberUtil.parseInt(name.replace(amount + plus, new String()));
                } else if (name.startsWith(amount + minus)) {
                    rAmount -= NumberUtil.parseInt(name.replace(amount + minus, new String()));
                }
            } else if (name.startsWith(price)) {
                if (name.startsWith(price + plus)) {
                    rPrice = rPrice.add(new BigDecimal(name.replace(price + plus, new String())));
                } else if (name.startsWith(price + minus)) {
                    rPrice = rPrice.subtract(new BigDecimal(name.replace(price + minus, new String())));
                }
            }
            performCommand(event.getWhoClicked(), creator.getName() + " " + partner.getName() + " " + exImport + " " + offer.getGood().name() + " " + rAmount + " " + rPrice);
        }
    }

    private int readAmountFromTitle(String title) {
        String amount = title.replace(ChatColor.GREEN.toString() + ChatColor.BOLD.toString()
                + FMessage.TRADE_OFFER_AMOUNT.getMessage() + ": " + ChatColor.DARK_AQUA.toString(), new String());
        amount = amount.split(" ")[0];
        return NumberUtil.parseInt(amount);
    }
    // TODO: Broken
    private BigDecimal readPriceFromTitle(String title) {
        String price = title.split(ChatColor.DARK_AQUA.toString())[2];
        return new BigDecimal(price);
    }

    private final String COMMAND = FCommandCache.LABEL + " tradeOffer ";

    private void performCommand(HumanEntity human, String args) {
        if (!(human instanceof Player)) {
            return;
        }
        Player player = (Player) human;
        player.performCommand(COMMAND + args);
    }

    @Override
    public Inventory getInventory() {
        return chooseExport;
    }
}
