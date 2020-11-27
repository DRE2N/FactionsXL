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

package de.erethon.factionsxl.config;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FTranslation  {

    FactionsXL plugin = FactionsXL.getInstance();
    File languageFile;
    FileConfiguration messages = null;

    public FTranslation() {
        languageFile = new File(plugin.getDataFolder() + "/items.yml");
        if (!languageFile.exists()) {
            InputStream jarURL = plugin.getClass().getResourceAsStream("/languages/names_en.yml");
            try {
                copyFile(jarURL, new File(plugin.getDataFolder() + "/languages/items.yml"));

            } catch (Exception e) {
                MessageUtil.log(e.toString());
            }
        }
        messages = YamlConfiguration.loadConfiguration(languageFile);
    }

    public String getTranslatedName(Material type) {
        String key = type.getKey().toString();
        key = key.replace("minecraft:", "");
        if (type.isBlock()) {
            key = "block-minecraft-" + key;
        }
        else if (type.isItem()) {
            key = "item-minecraft-" + key;
        }
        return messages.getString(key);
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