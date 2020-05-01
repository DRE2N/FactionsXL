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
package de.erethon.factionsxl.player;

import de.erethon.commons.misc.NumberUtil;
import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.meta.BannerMeta;

/**
 * @author Daniel Saukel
 */
public class Dynasty {

    private int id;
    private File file;
    private FileConfiguration config;
    private String name;
    private String longName;
    private String desc;
    private BannerMeta banner;
    private long creationDate;
    private double prestige;

    public Dynasty(File file) {
        id = NumberUtil.parseInt(file.getName().replace(".yml", ""));
        this.file = file;
        config = YamlConfiguration.loadConfiguration(file);
    }

    /* Getters and setters */
    /**
     * @return
     * the dynasty's name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     * the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     * the faction's long name
     */
    public String getLongName() {
        return longName != null ? longName : name;
    }

    /**
     * @param name
     * the name to set
     */
    public void setLongName(String name) {
        longName = name;
    }

    /**
     * @return
     * the description of the faction
     */
    public String getDescription() {
        return desc;
    }

    /**
     * @param description
     * the description to set
     */
    public void setDescription(String description) {
        desc = description;
    }

    /**
     * @return
     * the faction's name
     */
    public BannerMeta getBanner() {
        return banner;
    }

    /**
     * @param banner
     * the banner to set
     */
    public void setBanner(BannerMeta banner) {
        this.banner = banner;
    }

    /**
     * @return
     * the creation date
     */
    public long getCreationDate() {
        return creationDate;
    }

    /**
     * @return
     * the prestige points
     */
    public double getPrestige() {
        return prestige;
    }

    /**
     * @param prestige
     * the prestige to set
     */
    public void setPrestige(double prestige) {
        this.prestige = prestige;
    }

}
