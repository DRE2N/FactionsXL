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
package de.erethon.factionsxl.config;

import de.erethon.commons.config.ConfigUtil;
import de.erethon.factionsxl.FactionsXL;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @author Daniel Saukel
 */
public class FData {

    public static final File FILE = new File(FactionsXL.getInstance().getDataFolder(), "data.yml");

    public FileConfiguration config = YamlConfiguration.loadConfiguration(FILE);

    public long lastNewDay;
    public long lastPowerUpdate;
    public Map<UUID, Double> power = new HashMap<>();

    public FData() {
        lastNewDay = config.getLong("lastNewDay");
        lastPowerUpdate = config.getLong("lastPowerUpdate");
        for (Entry<String, Object> entry : ConfigUtil.getMap(config, "power").entrySet()) {
            power.put(UUID.fromString(entry.getKey()), (Double) entry.getValue());
        }
    }

    public void save() {
        config.set("lastNewDay", lastNewDay);
        config.set("lastPowerUpdate", lastPowerUpdate);

        if (!config.contains("power")) {
            config.createSection("power");
        }
        ConfigurationSection powerSection = config.getConfigurationSection("power");
        for (Entry<UUID, Double> entry : power.entrySet()) {
            powerSection.set(entry.getKey().toString(), entry.getValue());
        }
        config.set("power", powerSection);

        try {
            config.save(FILE);
        } catch (IOException exception) {
        }
    }

}
