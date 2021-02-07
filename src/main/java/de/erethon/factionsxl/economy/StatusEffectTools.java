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

package de.erethon.factionsxl.economy;

import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.population.PopulationLevel;
import org.bukkit.Effect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusEffectTools {

    public static double getTotalMemberModifier(Faction faction) {
        return combine((StatusEffect[]) faction.getEffects().toArray()).getMemberModifier();
    }

    public static double getTotalRegionModifier(Faction faction) {
        return combine((StatusEffect[]) faction.getEffects().toArray()).getRegionModifier();
    }

    public static double getTotalManpowerModifier(Faction faction) {
        return combine((StatusEffect[]) faction.getEffects().toArray()).getManpowerModifier();
    }

    public static double getTotalStabilityModifier(Faction faction) {
        return combine((StatusEffect[]) faction.getEffects().toArray()).getStabilityModifier();
    }

    public static double getTotalExhaustionModifier(Faction faction) {
        return combine((StatusEffect[]) faction.getEffects().toArray()).getExhaustionModifier();
    }

    public static double getTotalDamageModifier(Faction faction) {
        return combine((StatusEffect[]) faction.getEffects().toArray()).getAttackDamageModifier();
    }

    public static double getTotalDamageModifier(Region region) {
        return combine((StatusEffect[]) region.getEffects().toArray()).getAttackDamageModifier();
    }

    public static double getTotalShieldModifier(Faction faction) {
        return combine((StatusEffect[]) faction.getEffects().toArray()).getShieldModifier();
    }

    public static double getTotalShieldModifier(Region region) {
        return combine((StatusEffect[]) region.getEffects().toArray()).getShieldModifier();
    }

    public static int getTotalPrestige(Faction faction) {
        return combine((StatusEffect[]) faction.getEffects().toArray()).getPrestige();
    }

    public static int getTotalAllianceLimitBuff(Faction faction) {
        return combine((StatusEffect[]) faction.getEffects().toArray()).getAllianceLimitBuff();
    }

    public static int getTotalShipLimitBuff(Faction faction) {
        return combine((StatusEffect[]) faction.getEffects().toArray()).getTransportShipLimit();
    }

    public static int getTotalShipLimitBuff(Region region) {
        return combine((StatusEffect[]) region.getEffects().toArray()).getTransportShipLimit();
    }

    public static int getTotalCoachLimitBuff(Faction faction) {
        return combine((StatusEffect[]) faction.getEffects().toArray()).getTransportCoachLimit();
    }

    public static int getTotalCoachLimitBuff(Region region) {
        return combine((StatusEffect[]) region.getEffects().toArray()).getTransportCoachLimit();
    }

    public static int getTotalAirshipLimitBuff(Faction faction) {
        return combine((StatusEffect[]) faction.getEffects().toArray()).getTransportAirshipLimit();
    }

    public static int getTotalAirshipLimitBuff(Region region) {
        return combine((StatusEffect[]) region.getEffects().toArray()).getTransportAirshipLimit();
    }

    public static Map<Resource, Double> getTotalResourceConsumption(Region region) {
        return combine((StatusEffect[]) region.getEffects().toArray()).getConsumptionModifier();
    }

    public static Map<Resource, Double> getTotalResourceProduction(Region region) {
        return combine((StatusEffect[]) region.getEffects().toArray()).getProductionModifier();
    }

    public static Map<Resource, Integer> getTotalResourceProductionBuff(Region region) {
        return combine((StatusEffect[]) region.getEffects().toArray()).getProductionBuff();
    }

    public static Map<PopulationLevel, Integer> getTotalHappinessBuff(Region region) {
        return combine((StatusEffect[]) region.getEffects().toArray()).getHappinessBuff();
    }

    public static Map<Effect, Integer> getTotalMinecraftEffects(Region region) {
        return combine((StatusEffect[]) region.getEffects().toArray()).getMinecraftEffects();
    }

    /**
     * Combines several StatusEffects. Careful: Does not check if they all have the same origin/expiration/regionModifier state
     * @param args an array of StatusEffects
     * @return the combined StatusEffect of all those effects
     */
    public static StatusEffect combine(StatusEffect... args) {
        List<StatusEffect> effects = Arrays.asList(args);
        StatusEffect result = new StatusEffect(effects.get(0).getOrigin(), effects.get(0).isRegionModifier(), effects.get(0).getExpiration());
        for (StatusEffect effect : effects) {
            add(result, effect);
            effects.remove(effect);
        }
        return result;
    }

    public static StatusEffect add(StatusEffect one, StatusEffect two) {
        StatusEffect result = new StatusEffect(one.getOrigin(), one.isRegionModifier(), one.getExpiration());
        result.setDisplayName(one.getDisplayName());
        double member = one.getMemberModifier() + two.getMemberModifier();
        double region = one.getRegionModifier() + two.getRegionModifier();
        double manpower = one.getManpowerModifier() + two.getManpowerModifier();
        double stability = one.getStabilityModifier() + two.getStabilityModifier();
        double exhaustion = one.getExhaustionModifier() + two.getExhaustionModifier();
        double damage = one.getAttackDamageModifier() + two.getAttackDamageModifier();
        double shield = one.getShieldModifier() + two.getShieldModifier();
        int prestige = one.getPrestige() + two.getPrestige();
        int alliances = one.getAllianceLimitBuff() + two.getAllianceLimitBuff();
        int ships = one.getTransportShipLimit() + two.getTransportShipLimit();
        int coaches = one.getTransportCoachLimit() + two.getTransportCoachLimit();
        int airships = one.getTransportAirshipLimit() + two.getTransportAirshipLimit();

        Map<Resource, Double> consumption = new HashMap<>(one.getConsumptionModifier());
        for (Map.Entry<Resource, Double> entry : two.getConsumptionModifier().entrySet()) {
            consumption.put(entry.getKey(), consumption.get(entry.getKey()) + entry.getValue());
        }

        Map<Resource, Double> production = new HashMap<>(one.getProductionModifier());
        for (Map.Entry<Resource, Double> entry : two.getProductionModifier().entrySet()) {
            production.put(entry.getKey(), production.get(entry.getKey()) + entry.getValue());
        }

        Map<Resource, Integer> productionBuff = new HashMap<>(one.getProductionBuff());
        for (Map.Entry<Resource, Integer> entry : two.getProductionBuff().entrySet()) {
            productionBuff.put(entry.getKey(), productionBuff.get(entry.getKey()) + entry.getValue());
        }

        Map<PopulationLevel, Integer> happiness = new HashMap<>(one.getHappinessBuff());
        for (Map.Entry<PopulationLevel, Integer> entry : two.getHappinessBuff().entrySet()) {
            happiness.put(entry.getKey(), happiness.get(entry.getKey()) + entry.getValue());
        }

        Map<Effect, Integer> effects = new HashMap<>(one.getMinecraftEffects());
        for (Map.Entry<Effect, Integer> entry : two.getMinecraftEffects().entrySet()) {
            effects.put(entry.getKey(), effects.get(entry.getKey()) + entry.getValue());
        }

        result.setMemberModifier(member);
        result.setRegionModifier(region);
        result.setManpowerModifier(manpower);
        result.setStabilityModifier(stability);
        result.setExhaustionModifier(exhaustion);
        result.setAttackDamageModifier(damage);
        result.setShieldModifier(shield);
        result.setPrestige(prestige);
        result.setAllianceLimitBuff(alliances);
        result.setTransportShipLimit(ships);
        result.setTransportCoachLimit(coaches);
        result.setTransportAirshipLimit(airships);
        result.setConsumptionModifier(consumption);
        result.setProductionModifier(production);
        result.setProductionBuff(productionBuff);
        result.setHappinessBuff(happiness);
        result.setMinecraftEffects(effects);

        return result;
    }
}
