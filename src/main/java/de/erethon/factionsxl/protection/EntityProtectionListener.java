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
package de.erethon.factionsxl.protection;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.board.RegionType;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPermission;
import static de.erethon.factionsxl.protection.EntityProtectionListener.Action.*;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.War;
import de.erethon.factionsxl.war.WarCache;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * @author Daniel Saukel
 */
public class EntityProtectionListener implements Listener {

    enum Action {
        ATTACK,
        BUILD,
        EQUIP,
        LEASH,
        SHEAR,
        SPLASH_POTION,
        TAME,
        UNLEASH
    }

    FactionsXL plugin = FactionsXL.getInstance();
    FConfig config = plugin.getFConfig();
    WarCache wc = plugin.getWarCache();

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (config.isExcludedWorld(event.getEntity().getWorld())) {
            return;
        }
        Player attacker = getDamageSource(event.getDamager());
        Entity eAttacker = attacker != null ? attacker : event.getDamager();
        Entity eDefender = event.getEntity();
        if (!(eAttacker instanceof Player)) {
            return;
        }
        if (!(eDefender instanceof Player)) {
            forbidIfInProtectedTerritory(attacker, eDefender, event, ATTACK);
            return;
        }
        Player defender = (Player) eDefender;
        Region region = plugin.getBoard().getByLocation(defender.getLocation());
        if (region != null && region.getType() == RegionType.WARZONE) {
            return;
        }
        Faction aFaction = plugin.getFactionCache().getByMember(attacker);
        Faction dFaction = plugin.getFactionCache().getByMember(defender);
        boolean truce = false;
        if (wc.getWarTogether(aFaction, dFaction) != null) {
            truce = wc.getWarTogether(aFaction, dFaction).getTruce();
        }

        Faction rFaction = region != null ? region.getOwner() : null;
        double shield = config.getTerritoryShield();
        if (region !=null && region.getOccupant() != null) {
            Faction occupant = region.getOccupant();
            if (occupant == aFaction) {
                return;
            }
        }
        if (aFaction != null && aFaction.getRelation(dFaction).isProtected() || truce) {
            ParsingUtil.sendActionBarMessage(attacker, FMessage.PROTECTION_CANNOT_ATTACK_PLAYER.getMessage(), dFaction);
            event.setCancelled(true);
        } else if (rFaction != null && rFaction.getRelation(dFaction).isProtected() && (aFaction == null || !aFaction.isInWar(dFaction))) {
            if (config.isTerritoryProtectionEnabled() && (!config.isCapitalProtectionEnabled()
                    || rFaction.getCapital().equals(plugin.getBoard().getByLocation(eDefender.getLocation())))) {
                ParsingUtil.sendActionBarMessage(attacker, (config.isCapitalProtectionEnabled() ? FMessage.PROTECTION_CANNOT_ATTACK_CAPITAL
                        : FMessage.PROTECTION_CANNOT_ATTACK_FACTION).getMessage(), rFaction);
                event.setCancelled(true);
            } else if (shield != 0) {
                event.setDamage(event.getDamage() - event.getDamage() * shield);
                ParsingUtil.sendActionBarMessage(attacker, FMessage.PROTECTION_DAMAGE_REDUCED.getMessage(), (int) (shield * 100), rFaction);
            }
        }
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent event) {
        Player owner = null;
        if (event.getOwner() instanceof Player) {
            owner = (Player) event.getOwner();
        }
        forbidIfInProtectedTerritory(owner, event.getEntity(), event, TAME);
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        forbidIfInProtectedTerritory(getDamageSource(event.getRemover()), event.getEntity(), event, ATTACK);
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        forbidIfInProtectedTerritory(event.getPlayer(), event.getEntity(), event, BUILD);
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        forbidIfInProtectedTerritory(event.getPlayer(), event.getRightClicked(), event, EQUIP);
    }

    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        forbidIfInProtectedTerritory(event.getPlayer(), event.getEntity(), event, SHEAR);
    }

    @EventHandler
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        forbidIfInProtectedTerritory(event.getPlayer(), event.getEntity(), event, LEASH);
    }

    @EventHandler
    public void onPlayerUnLeashEntity(PlayerUnleashEntityEvent event) {
        forbidIfInProtectedTerritory(event.getPlayer(), event.getEntity(), event, UNLEASH);
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        forbidIfInProtectedTerritory(getDamageSource(event.getAttacker()), event.getVehicle(), event, ATTACK);
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        ProjectileSource shooter = event.getPotion().getShooter();
        if (!(shooter instanceof Player)) {
            return;
        }
        if (event.getAffectedEntities().isEmpty()) {
            return;
        }
        forbidIfInProtectedTerritory((Player) shooter, event.getAffectedEntities().iterator().next(), event, SPLASH_POTION);
    }

    @EventHandler
    public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
        ProjectileSource shooter = event.getAreaEffectCloud().getSource();
        if (!(shooter instanceof Player)) {
            return;
        }
        forbidIfInProtectedTerritory((Player) shooter, event.getAreaEffectCloud(), event, SPLASH_POTION);
    }

    private void forbidIfInProtectedTerritory(Player attacker, Entity damaged, Cancellable event, Action action) {
        if (config.isExcludedWorld(damaged.getWorld())) {
            return;
        }
        if (attacker == null) {
            return;
        }
        if (FPermission.hasPermission(attacker, FPermission.BUILD)) {
            return;
        }
        if (damaged instanceof Monster) {
            return;
        }

        boolean living = damaged instanceof LivingEntity && damaged.getType() != EntityType.ARMOR_STAND;
        Region region = plugin.getBoard().getByLocation(damaged.getLocation());
        if (region == null || region.isNeutral()) {
            return;
        }


        Faction aFaction = plugin.getFactionCache().getByMember(attacker);
        Faction owner = region.getOwner();
        if ( region.getOccupant() != null) {
            Faction occupant = region.getOccupant();
            if (occupant == aFaction) {
                return;
            }
        }
        Relation rel = owner.getRelation(aFaction);
        if (!rel.canBuild()) {
            event.setCancelled(true);
            FMessage message = FMessage.PROTECTION_CANNOT_BUILD_FACTION;
            switch (action) {
                case ATTACK:
                    message = living ? FMessage.PROTECTION_CANNOT_ATTACK_FACTION : FMessage.PROTECTION_CANNOT_DESTROY_FACTION;
                    break;
                case BUILD:
                    message = FMessage.PROTECTION_CANNOT_BUILD_FACTION;
                    break;
                case EQUIP:
                    message = FMessage.PROTECTION_CANNOT_EQUIP_FACTION;
                    break;
                case LEASH:
                    message = FMessage.PROTECTION_CANNOT_LEASH_FACTION;
                    break;
                case SPLASH_POTION:
                    message = FMessage.PROTECTION_CANNOT_SPLASH_POTION_FACTION;
                    break;
                case SHEAR:
                    message = FMessage.PROTECTION_CANNOT_SHEAR_FACTION;
                    break;
                case TAME:
                    message = FMessage.PROTECTION_CANNOT_TAME_FACTION;
                    break;
                case UNLEASH:
                    message = FMessage.PROTECTION_CANNOT_UNLEASH_FACTION;
            }
            ParsingUtil.sendActionBarMessage(attacker, message.getMessage(), region.getOwner());
        }
    }

    public static Player getDamageSource(Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        } else if (damager instanceof Arrow) {
            ProjectileSource shooter = ((Arrow) damager).getShooter();
            if (shooter instanceof Player) {
                return (Player) shooter;
            }
        }
        return null;
    }

}
