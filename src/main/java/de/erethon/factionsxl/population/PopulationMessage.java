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

package de.erethon.factionsxl.population;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.config.DREConfig;
import de.erethon.factionsxl.FactionsXL;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * This class provides random messages based on population levels and happiness.
 */
public class PopulationMessage {

    FactionsXL plugin = FactionsXL.getInstance();
    Map<PopulationLevel, Map<HappinessLevel, Set<String>>> messages = new HashMap<>();
    File dataFile;
    FileConfiguration config;


    public PopulationMessage() {
        dataFile = new File(plugin.getDataFolder() + "/languages/population.yml");
        if (!dataFile.exists()) {
            InputStream jarURL = plugin.getClass().getResourceAsStream("/languages/population.yml");
            try {
                copyFile(jarURL, new File(plugin.getDataFolder() + "/languages/population.yml"));

            } catch (Exception e) {
                MessageUtil.log(e.toString());
            }
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
    }

    /**
     * Returns a random population response message
     * @param level the PopulationLevel. Beggars can respond differently than Noblemen
     * @param happiness the HappinessLevel. Villagers have different responses based on their happiness
     */
    public String getRandomMessage(PopulationLevel level, HappinessLevel happiness) {
        Set<String> strings = messages.get(level).get(happiness);
        return strings.stream().skip(new Random().nextInt(strings.size())).findFirst().orElse(null);
    }

    public void load() {
        for (PopulationLevel level : PopulationLevel.values()) {
            Map<HappinessLevel, Set<String>> levelMsgs = new HashMap<>();
            for (HappinessLevel happinessLevel : HappinessLevel.values()) {
                List<String> msgs = (List<String>) config.getList(level.toString() + "." + happinessLevel.toString());
                levelMsgs.put(happinessLevel, new HashSet<>(msgs));
            }
            messages.put(level, levelMsgs);
        }
    }

    public static void copyFile(InputStream in, File out) throws Exception {
        try (InputStream fis = in; FileOutputStream fos = new FileOutputStream(out)) {
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        }
    }

}
