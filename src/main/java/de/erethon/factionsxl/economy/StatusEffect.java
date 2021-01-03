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

import de.erethon.factionsxl.building.BuildSite;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

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
    private double memberModifier = 0;
    private double regionModifier = 0;
    private double manpowerModifier = 0;
    private int prestige = 0;

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
        }
        for (String key : args.keySet()) {
            if (key.contains("production.")) {
                String name = key.replace("production.", "");
                productionModifier.put(Resource.getByName(name), (double) args.get(key));
            }
        }
        for (String key : args.keySet()) {
            if (key.contains("resources.")) {
                String name = key.replace("resources.", "");
                productionBuff.put(Resource.getByName(name), (int) args.get(key));
            }
        }
        memberModifier = (double) args.get("members");
        regionModifier = (double) args.get("regions");
        manpowerModifier = (double) args.get("manpower");
        prestige = (int) args.get("prestige");
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
        if (isRegionModifier) {
            for (Resource resource : productionBuff.keySet()) {
                args.put("resources." + resource.getName(), productionBuff.get(resource));
            }
        }
        args.put("members", memberModifier);
        args.put("regions", regionModifier);
        args.put("manpower", manpowerModifier);
        args.put("prestige", prestige);
        return args;
    }
}
