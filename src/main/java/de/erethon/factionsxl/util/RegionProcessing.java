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

package de.erethon.factionsxl.util;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RegionProcessing extends BukkitRunnable {

    FactionsXL plugin = FactionsXL.getInstance();

    List<Region> queue = new CopyOnWriteArrayList<>();
    List<Region> all;
    int tasks = 0;


    public RegionProcessing(List<Region> rgs) {
        all = rgs;
        this.runTask(plugin);
    }
    @Override
    public void run() {
        if (queue.size() <=3 && tasks <= 3) {
            int filled = 0;
            for (Region r : all) {
                if (filled <= 3) {
                    filled++;
                    queue.add(r);
                }
            }
            MessageUtil.log("Filled region processing queue.");
        }

        if (queue.isEmpty()) {
            MessageUtil.log("Queue empty - Task finished. Please reload FXL/restart the Server.");
            this.cancel();
            return;
        }

        if (tasks >= 3) {
            MessageUtil.log("Too many parallel tasks; " + tasks + " / Queue size: " + queue.size() + " - skipping...");
            return;
        }
        calcNeighbours();
    }

    public void calcNeighbours() {

        for (Region rg : queue) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new BukkitRunnable() {

                @Override
                public void run() {
                    tasks++;
                    World w = rg.getWorld();
                    Collection<LazyChunk> chunks1 = new CopyOnWriteArrayList<>(rg.getChunks());
                    Collection<ChunkSnapshot> allChunks = new CopyOnWriteArrayList<>();
                    Collection<ChunkSnapshot> chunksAround;

                    for (LazyChunk c : chunks1) {
                        chunksAround = c.getChunksAround(w);
                        allChunks.addAll(chunksAround);
                    }

                    for (ChunkSnapshot c : allChunks) {
                        int cx = c.getX();
                        int cz = c.getZ();
                        int x;
                        int z;
                        if (!(cx == 0)) {
                            x = cx * 16;
                        }
                        else {
                            x = 1;
                        }
                        if (!(cz == 0)) {
                            z = cz * 16;
                        }
                        else {
                            z = 1;
                        }

                        Region rg2 = plugin.getBoard().getByLocation(new Location(w, x, 100, z));
                        if ( !(rg2 == null) && !(rg2.equals(rg)) && !(rg.getNeighbours().contains(rg2)) ) {
                            MessageUtil.log("Added " + rg2.getName() + " to adjacent regions of " + rg.getName());
                            rg.addNeighbour(rg2);
                        }
                    }

                    rg.save();
                    queue.remove(rg);
                    all.remove(rg);
                    tasks = tasks - 1;
                    allChunks.clear();
                    chunks1.clear();
                    MessageUtil.log("Region " + rg.getName() + " complete & saved.");
                }

            });
        }
    }
}
