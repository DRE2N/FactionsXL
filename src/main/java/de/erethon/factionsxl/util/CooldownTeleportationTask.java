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
package de.erethon.factionsxl.util;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.misc.ProgressBar;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * @author Daniel Saukel
 */
public class CooldownTeleportationTask extends ProgressBar {

    FConfig config = FactionsXL.getInstance().getFConfig();
    Economy econ = FactionsXL.getInstance().getEconomyProvider();

    private Player player;
    private Location targetLocation;
    private Location location;
    private boolean teleport;
    private boolean charge;

    public CooldownTeleportationTask(Player player, Location targetLocation, boolean charge) {
        super(player, 10);
        this.player = player;
        this.targetLocation = targetLocation;
        location = player.getLocation();
    }

    @Override
    public void run() {
        super.run();
        if (player.getLocation().getBlockX() != location.getBlockX() || player.getLocation().getBlockY() != location.getBlockY() || player.getLocation().getBlockZ() != location.getBlockZ()) {
            cancel();
            MessageUtil.sendActionBarMessage(player, FMessage.ERROR_DO_NOT_MOVE.getMessage());
            return;
        }

        if (teleport) {
            if (config.isEconomyEnabled() && charge) {
                if (!econ.has(player, config.getPriceHomeWarp())) {
                    ParsingUtil.sendMessage(player, FMessage.ERROR_NOT_ENOUGH_MONEY.getMessage(), String.valueOf(config.getPriceHomeWarp()));
                    return;
                } else {
                    econ.withdrawPlayer(player, config.getPriceHomeWarp());
                }
            }
        }
    }
    @Override
    public void onFinish() {
        player.teleportAsync(targetLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

}
