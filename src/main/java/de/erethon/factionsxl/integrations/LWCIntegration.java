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
package de.erethon.factionsxl.integrations;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.util.FDebugLevel;
import de.erethon.factionsxl.util.ParsingUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Adapted from FactionsLWCBridge by Sataniel.
 *
 * @author Daniel Saukel
 */
public class LWCIntegration extends JavaModule {

    FactionsXL plugin = FactionsXL.getInstance();
    LWC lwc = plugin.getLWC();

    public LWCIntegration() {
        lwc.getModuleLoader().registerModule(plugin, this);
    }

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (canBypass(event.getPlayer(), event.getProtection().getBlock())) {
            event.setResult(ALLOW);
        }
    }

    @Override
    public void onDestroyProtection(LWCProtectionDestroyEvent event) {
        if (canBypass(event.getPlayer(), event.getProtection().getBlock())) {
            event.getProtection().remove();
        }
    }

    @Override
    public void onRegisterProtection(LWCProtectionRegisterEvent event) {
        FPlayer fPlayer = plugin.getFPlayerCache().getByPlayer(event.getPlayer());
        Region region = plugin.getBoard().getByLocation(event.getBlock().getLocation());
        if (region == null || region.isNeutral()) {
            return;
        }
        Faction faction = region.getOwner();
        if (!faction.getRelation(fPlayer).canBuild()) {
            event.setCancelled(true);
            ParsingUtil.sendMessage(event.getPlayer(), FMessage.PROTECTION_CANNOT_REGISTER_FACTION.getMessage(), faction);
        }
    }

    public boolean canBypass(Player player, Block block) {
        Region region = plugin.getBoard().getByLocation(block.getLocation());
        if (region == null || region.isNeutral()) {
            return false;
        }
        Faction faction = region.getOwner();
        if (faction.isInWar(plugin.getFactionCache().getByMember(player)) && block.getType() == Material.FURNACE && region.getInfluence() <= 30) {
            FactionsXL.debug(FDebugLevel.PROTECTION, "Bypassing chest for player  " + player.getName() + " (Furnace & Influence below 30)");
            return true;
        }
        Faction playerFaction = plugin.getFPlayerCache().getByPlayer(player).getFaction();
        if (region.getOccupant() != null && playerFaction != null && playerFaction.equals(region.getOccupant())) {
            FactionsXL.debug(FDebugLevel.PROTECTION, "Bypassing chest for player  " + player.getName() + " (Occupied)");
            return true;
        }
        FactionsXL.debug(FDebugLevel.PROTECTION, "Bypassing chest for player  " + player.getName() + " (isAdmin: " + faction.isAdmin(player) + ")");
        return faction.isAdmin(player);
    }


}
