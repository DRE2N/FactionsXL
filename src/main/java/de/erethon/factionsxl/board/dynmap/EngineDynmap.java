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
package de.erethon.factionsxl.board.dynmap;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Board;
import de.erethon.factionsxl.config.FConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.dynmap.utils.TileFlags;

import java.util.ArrayDeque;

/**
 * This is a modified version of mikeprimms plugin Dynmap-Factions taken from FactionsUUID by drtshock.
 *
 * @author Mike Primm, Trent Hensler, Daniel Saukel
 */
public abstract class EngineDynmap {

    FactionsXL plugin = FactionsXL.getInstance();
    Board board = plugin.getBoard();
    FConfig config = plugin.getFConfig();

    // -------------------------------------------- //
    // CONSTANTS
    // -------------------------------------------- //
    public final static int BLOCKS_PER_CHUNK = 16;
    public final static String DYNMAP_INTEGRATION = "\u00A7dDynmap Integration: \u00A7e";

    // -------------------------------------------- //
    // INSTANCE & CONSTRUCT
    // -------------------------------------------- //
    protected String layerName;
    protected boolean layerVisible;
    protected String prefix;
    protected String markersetString;

    public DynmapAPI dynmapApi;
    public MarkerAPI markerApi;
    public MarkerSet markerset;

    public void init() {
        // Should we even use dynmap?
        if (!config.isDynmapEnabled()) {
            if (this.markerset != null) {
                this.markerset.deleteMarkerSet();
                this.markerset = null;
            }
        }
    }

    // Thread Safe / Asynchronous: No
    public boolean updateCore() {
        // Get DynmapAPI
        this.dynmapApi = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
        if (this.dynmapApi == null) {
            severe("Could not retrieve the DynmapAPI.");
            return false;
        }

        // Get MarkerAPI
        this.markerApi = this.dynmapApi.getMarkerAPI();
        if (this.markerApi == null) {
            severe("Could not retrieve the MarkerAPI.");
            return false;
        }

        return true;
    }

    // Thread Safe / Asynchronous: Yes
    public TempMarkerSet createLayer() {
        TempMarkerSet ret = new TempMarkerSet();
        ret.label = layerName;
        ret.minimumZoom = config.getDynmapLayerMinimumZoom();
        ret.hideByDefault = !layerVisible;
        return ret;
    }

    // Thread Safe / Asynchronous: No
    public boolean updateLayer(TempMarkerSet temp) {
        this.markerset = this.markerApi.getMarkerSet(markersetString);
        if (this.markerset == null) {
            this.markerset = temp.create(this.markerApi, markersetString);
            if (this.markerset == null) {
                severe("Could not create the " + layerName + "Markerset/Layer");
                return false;
            }
        } else {
            temp.update(this.markerset);
        }
        return true;
    }

    // -------------------------------------------- //
    // UTIL & SHARED
    // -------------------------------------------- //
    public static String escapeHtml(String string) {
        StringBuilder out = new StringBuilder(Math.max(16, string.length()));
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    // Thread Safe / Asynchronous: Yes
    public static void info(String msg) {
        String message = DYNMAP_INTEGRATION + msg;
        MessageUtil.log(FactionsXL.getInstance(), message);
    }

    // Thread Safe / Asynchronous: Yes
    public static void severe(String msg) {
        String message = DYNMAP_INTEGRATION + ChatColor.RED.toString() + msg;
        MessageUtil.log(FactionsXL.getInstance(), message);
    }

    enum Direction {
        XPLUS, ZPLUS, XMINUS, ZMINUS
    }

    // Find all contiguous blocks, set in target and clear in source
    public int floodFillTarget(TileFlags source, TileFlags destination, int x, int y) {
        int cnt = 0;
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{x, y});

        while (!stack.isEmpty()) {
            int[] nxt = stack.pop();
            x = nxt[0];
            y = nxt[1];
            if (source.getFlag(x, y)) { // Set in src
                source.setFlag(x, y, false); // Clear source
                destination.setFlag(x, y, true); // Set in destination
                cnt++;
                if (source.getFlag(x + 1, y)) {
                    stack.push(new int[]{x + 1, y});
                }
                if (source.getFlag(x - 1, y)) {
                    stack.push(new int[]{x - 1, y});
                }
                if (source.getFlag(x, y + 1)) {
                    stack.push(new int[]{x, y + 1});
                }
                if (source.getFlag(x, y - 1)) {
                    stack.push(new int[]{x, y - 1});
                }
            }
        }
        return cnt;
    }

}
