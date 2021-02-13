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

package de.erethon.factionsxl.building.effects;

import de.erethon.factionsxl.board.RegionType;
import de.erethon.factionsxl.building.BuildSite;
import de.erethon.factionsxl.building.effects.special.DropModifier;
import de.erethon.factionsxl.building.effects.special.MapMarker;
import de.erethon.factionsxl.economy.Resource;
import de.erethon.factionsxl.population.PopulationLevel;
import org.bukkit.Effect;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Malfrador
 *
 * A StatusEffect is a buff or debuff on a region or faction. The effects are usually applied by buildings. If the origin building gets damaged
 * or occupied, the effect will be removed
 */

public class StatusEffect implements ConfigurationSerializable  {

    private BuildSite origin;
    private boolean isRegionModifier;
    private long expiration = 0;
    private String displayName = "";
    private Map<Resource, Double> consumptionModifier = new HashMap<>();
    private Map<Resource, Double> productionModifier = new HashMap<>();
    private Map<Resource, Integer> productionBuff = new HashMap<>(); // not percentage based, just a flat buff.
    private Map<PopulationLevel, Integer> happinessBuff = new HashMap<>();
    private Map<Effect, Integer> minecraftEffects = new HashMap<>();
    private RegionType changeTypeTo;
    private double memberModifier = 0.00;
    private double regionModifier = 0.00;
    private double manpowerModifier = 0.00;
    private double stabilityModifier = 0.00;
    private double exhaustionModifier = 0.00;
    private double attackDamageModifier = 0.00;
    private double shieldModifier = 0.00;
    private int prestige = 0;
    private int allianceLimitBuff = 0;
    private int transportShipLimit = 0;
    private int transportAirshipLimit = 0;
    private int transportCoachLimit = 0;
    private String memberPermission;
    private SubEffect effect;

    public StatusEffect(boolean isRegionModifier, long expiration) {
        this.isRegionModifier = isRegionModifier;
        this.expiration = expiration;
    }

    public StatusEffect(BuildSite origin, boolean isRegionModifier, long expiration) {
        this.origin = origin;
        this.isRegionModifier = isRegionModifier;
        this.expiration = expiration;
    }

    public StatusEffect(Map<String, Object> args) {
        origin = (BuildSite) args.get("origin");
        displayName = (String) args.get("displayName");
        isRegionModifier = (boolean) args.get("regionModifier");
        expiration = (long) args.get("expiration");
        for (String key : args.keySet()) {
            if (key.contains("consumption.")) {
                String name = key.replace("consumption.", "");
                consumptionModifier.put(Resource.getByName(name), (double) args.get(key));
            }
            if (key.contains("production.")) {
                String name = key.replace("production.", "");
                productionModifier.put(Resource.getByName(name), (double) args.get(key));
            }
            if (key.contains("resources.")) {
                String name = key.replace("resources.", "");
                productionBuff.put(Resource.getByName(name), (int) args.get(key));
            }
            if (key.contains("effects.")) {
                String name = key.replace("effects.", "");
                minecraftEffects.put(Effect.valueOf(name), (int) args.get(key));
            }
            if (key.contains("happiness.")) {
                String name = key.replace("happiness.", "");
                happinessBuff.put(PopulationLevel.valueOf(name), (int) args.get(key));
            }
        }
        memberModifier = (double) args.get("members");
        regionModifier = (double) args.get("regions");
        manpowerModifier = (double) args.get("manpower");
        prestige = (int) args.get("prestige");
        if (args.containsKey("type")) {
            changeTypeTo = RegionType.valueOf((String) args.get("type"));
        }
        stabilityModifier = (double) args.get("stability");
        exhaustionModifier = (double) args.get("exhaustion");
        attackDamageModifier = (double) args.get("attackDamage");
        shieldModifier = (double) args.get("shield");
        allianceLimitBuff = (int) args.get("allianceLimit");
        transportShipLimit = (int) args.get("transportShipLimit");
        transportCoachLimit = (int) args.get("transportCoachLimit");
        transportAirshipLimit = (int) args.get("transportAirshipLimit");
        memberPermission = (String) args.get("permission");
        effect = loadEffect((String) args.get("specialEffect"));
    }

    public BuildSite getOrigin() {
        return origin;
    }

    public void setOrigin(BuildSite origin) {
        this.origin = origin;
    }

    public boolean isRegionModifier() {
        return isRegionModifier;
    }

    public void setRegionModifier(boolean regionMod) {
        isRegionModifier = regionMod;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public Map<Resource, Double> getConsumptionModifier() {
        return consumptionModifier;
    }

    public void setConsumptionModifier(Map<Resource, Double> consumptionModifier) {
        this.consumptionModifier = consumptionModifier;
    }

    public Map<Resource, Double> getProductionModifier() {
        return productionModifier;
    }

    public void setProductionModifier(Map<Resource, Double> productionModifier) {
        this.productionModifier = productionModifier;
    }

    public void setProductionBuff(Map<Resource, Integer> productionBuff) {
        this.productionBuff = productionBuff;
    }

    public Map<PopulationLevel, Integer> getHappinessBuff() {
        return happinessBuff;
    }

    public void setHappinessBuff(Map<PopulationLevel, Integer> happinessBuff) {
        this.happinessBuff = happinessBuff;
    }

    public double getMemberModifier() {
        return memberModifier;
    }

    public void setMemberModifier(double memberModifier) {
        this.memberModifier = memberModifier;
    }

    public double getRegionModifier() {
        return regionModifier;
    }

    public void setRegionModifier(double regionModifier) {
        this.regionModifier = regionModifier;
    }

    public double getManpowerModifier() {
        return manpowerModifier;
    }

    public void setManpowerModifier(double manpowerModifier) {
        this.manpowerModifier = manpowerModifier;
    }

    public int getPrestige() {
        return prestige;
    }

    public void setPrestige(int prestigeModifier) {
        this.prestige = prestigeModifier;
    }

    public Map<Resource, Integer> getProductionBuff() {
        return productionBuff;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<Effect, Integer> getMinecraftEffects() {
        return minecraftEffects;
    }

    public void setMinecraftEffects(Map<Effect, Integer> minecraftEffects) {
        this.minecraftEffects = minecraftEffects;
    }

    public RegionType getChangeTypeTo() {
        return changeTypeTo;
    }

    public void setChangeTypeTo(RegionType changeTypeTo) {
        this.changeTypeTo = changeTypeTo;
    }

    public double getStabilityModifier() {
        return stabilityModifier;
    }

    public void setStabilityModifier(double stabilityModifier) {
        this.stabilityModifier = stabilityModifier;
    }

    public double getExhaustionModifier() {
        return exhaustionModifier;
    }

    public void setExhaustionModifier(double exhaustionModifier) {
        this.exhaustionModifier = exhaustionModifier;
    }

    public double getAttackDamageModifier() {
        return attackDamageModifier;
    }

    public void setAttackDamageModifier(double attackDamageModifier) {
        this.attackDamageModifier = attackDamageModifier;
    }

    public double getShieldModifier() {
        return shieldModifier;
    }

    public void setShieldModifier(double shieldModifier) {
        this.shieldModifier = shieldModifier;
    }

    public int getAllianceLimitBuff() {
        return allianceLimitBuff;
    }

    public void setAllianceLimitBuff(int allianceLimitBuff) {
        this.allianceLimitBuff = allianceLimitBuff;
    }

    public int getTransportShipLimit() {
        return transportShipLimit;
    }

    public void setTransportShipLimit(int transportShipLimit) {
        this.transportShipLimit = transportShipLimit;
    }

    public int getTransportAirshipLimit() {
        return transportAirshipLimit;
    }

    public void setTransportAirshipLimit(int transportAirshipLimit) {
        this.transportAirshipLimit = transportAirshipLimit;
    }

    public int getTransportCoachLimit() {
        return transportCoachLimit;
    }

    public void setTransportCoachLimit(int transportCoachLimit) {
        this.transportCoachLimit = transportCoachLimit;
    }

    public String getMemberPermission() {
        return memberPermission;
    }

    public void setMemberPermission(String memberPermission) {
        this.memberPermission = memberPermission;
    }

    public SubEffect getEffect() {
        return effect;
    }

    public void setEffect(SubEffect effect) {
        this.effect = effect;
    }

    public SubEffect loadEffect(String data) {
        String[] split = data.split(":");
        switch (split[0]) {
            case "map":
                return new MapMarker(split[1]);
            case "drop":
                return new DropModifier(split[1]);
        }
        return null;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> args = new HashMap<>();
        args.put("origin" , origin);
        args.put("displayName", displayName);
        args.put("regionModifier", isRegionModifier);
        args.put("expiration", expiration);
        for (Resource resource : consumptionModifier.keySet()) {
            args.put("consumption." + resource.getName(), consumptionModifier.get(resource));
        }
        for (Resource resource : productionModifier.keySet()) {
            args.put("production." + resource.getName(), productionModifier.get(resource));
        }
        for (PopulationLevel pop : happinessBuff.keySet()) {
            args.put("happiness." + pop, happinessBuff.get(pop));
        }
        for (Effect eff : minecraftEffects.keySet()) {
            args.put("effects." + eff, minecraftEffects.get(eff));
        }
        if (isRegionModifier) {
            for (Resource resource : productionBuff.keySet()) {
                args.put("resources." + resource.getName(), productionBuff.get(resource));
            }
        }
        args.put("members", memberModifier);
        args.put("regions", regionModifier);
        args.put("manpower", manpowerModifier);
        args.put("prestige", prestige);
        args.put("type", changeTypeTo);
        args.put("stability", stabilityModifier);
        args.put("exhaustion", exhaustionModifier);
        args.put("attackDamage", attackDamageModifier);
        args.put("shield", shieldModifier);
        args.put("allianceLimit", allianceLimitBuff);
        args.put("transportShipLimit", transportShipLimit);
        args.put("transportCoachLimit", transportCoachLimit);
        args.put("transportAirshipLimit", transportAirshipLimit);
        args.put("permission", memberPermission);
        args.put("specialEffect", effect.save());
        return args;
    }
}
