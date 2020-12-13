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

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.economy.FAccount;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayerCache;
import de.erethon.factionsxl.util.ParsingUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Daniel Saukel
 */
public class MoneyCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    Economy econ = plugin.getEconomyProvider();
    FPlayerCache fPlayerCache = plugin.getFPlayerCache();

    public MoneyCommand() {
        setCommand("money");
        setAliases("m", "econ", "economy");
        setMinArgs(1);
        setMaxArgs(3);
        setHelp(FMessage.HELP_MONEY.getMessage());
        setPermission(FPermission.MONEY.getNode());
        setPlayerCommand(true);
        setConsoleCommand(true);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        i = 1;
        Faction faction = getSenderFactionOrFromArg(sender, args, 1, true);
        if (faction == null) {
            return;
        }

        if (args.length <= i) {
            displayHelp(sender);
            return;
        }

        double amount = 0;
        if (args.length >= i + 2) {
            try {
                amount = Double.parseDouble(args[i + 1]);
            } catch (NumberFormatException exception) {
                ParsingUtil.sendMessage(sender, FMessage.ERROR_NOT_NUMERIC.getMessage(), args[3]);
                return;
            }
        }

        switch (args[i].toLowerCase()) {
            case "b":
            case "balance":
                ParsingUtil.sendMessage(sender, FMessage.CMD_MONEY_BALANCE.getMessage(), faction, faction.getAccount().getFormatted());
                break;
            case "d":
            case "deposit":
                deposit(sender, faction, amount);
                break;
            case "w":
            case "withdraw":
                withdraw(sender, faction, amount);
                break;
        }
    }

    public void deposit(CommandSender sender, Faction faction, double amount) {
        FAccount acc = faction.getAccount();
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (econ.has(player, amount)) {
                econ.withdrawPlayer(player, amount);
                acc.deposit(amount);
                faction.sendMessage(FMessage.CMD_MONEY_DEPOSIT_SUCCESS.getMessage(), player, econ.format(amount), faction);
                faction.sendMessage(FMessage.CMD_MONEY_BALANCE.getMessage(), faction, acc.getFormatted());
                fPlayerCache.getByPlayer(player).getData().addMoney(amount); // Stats
            } else {
                ParsingUtil.sendMessage(sender, FMessage.CMD_MONEY_DEPOSIT_FAIL.getMessage(), econ.format(amount));
            }
        } else {
            acc.deposit(amount);
            faction.sendMessage(FMessage.CMD_MONEY_DEPOSIT_SUCCESS.getMessage(), sender, econ.format(amount), faction);
            ParsingUtil.sendMessage(sender, FMessage.CMD_MONEY_BALANCE.getMessage(), faction, acc.getFormatted());
        }
    }

    public void withdraw(CommandSender sender, Faction faction, double amount) {
        if (faction.isInWar()) {
            ParsingUtil.sendMessage(sender, "&cIm Krieg kann kein Geld abgehoben werden.");
            return;
        }
        if (!faction.isPrivileged(sender)) {
            ParsingUtil.sendMessage(sender, FMessage.ERROR_NO_PERMISSION.getMessage());
            return;
        }

        FAccount acc = faction.getAccount();
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (acc.getBalance() >= amount) {
                acc.withdraw(amount);
                econ.depositPlayer(player, amount);
                faction.sendMessage(FMessage.CMD_MONEY_WITHDRAW_SUCCESS.getMessage(), player, econ.format(amount), faction);
                faction.sendMessage(FMessage.CMD_MONEY_BALANCE.getMessage(), faction, acc.getFormatted());
            } else {
                ParsingUtil.sendMessage(sender, FMessage.CMD_MONEY_WITHDRAW_FAIL.getMessage(), faction, econ.format(amount));
            }
        } else {
            acc.withdraw(amount);
            faction.sendMessage(FMessage.CMD_MONEY_WITHDRAW_SUCCESS.getMessage(), sender, econ.format(amount), faction);
            ParsingUtil.sendMessage(sender, FMessage.CMD_MONEY_BALANCE.getMessage(), faction, acc.getFormatted());
        }
    }

}
