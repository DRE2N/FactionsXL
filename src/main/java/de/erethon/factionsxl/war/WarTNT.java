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
package de.erethon.factionsxl.war;

import at.pavlov.cannons.event.ProjectileImpactEvent;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Board;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.player.FPlayerCache;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.bukkit.Material.*;

/**
 * @author Daniel Saukel
 */
public class WarTNT implements Listener {

    FactionsXL plugin = FactionsXL.getInstance();
    Board board = plugin.getBoard();
    FPlayerCache fPlayers = plugin.getFPlayerCache();

    private long restoreTicksTNT;
    private long restoreTicksSiege;

    private List<Entity> fired = new ArrayList<>();
    private List<Location> cannonsTarget = new ArrayList<>();
    private CopyOnWriteArrayList<WeakReference<List<BlockState>>> blockListsTNT = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<WeakReference<List<BlockState>>> blockListsSiege = new CopyOnWriteArrayList<>();

    private static final Set<Material> NO_REGEN_INVENTORY = new HashSet<>(Arrays.asList(
            CHEST,
            TRAPPED_CHEST,
            FURNACE,
            DISPENSER,
            DROPPER,
            HOPPER,
            OBSERVER

    ));

    public WarTNT(long restoreTNT, long restoreSiege) {
        this.restoreTicksTNT = restoreTNT;
        this.restoreTicksSiege = restoreSiege;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.TNT) {
            return;
        }
        Region region = board.getByLocation(block.getLocation());
        if (region == null || region.isNeutral()) {
            return;
        }
        FPlayer fPlayer = fPlayers.getByPlayer(event.getPlayer());
        if (!fPlayer.isInWar(region.getOwner())) {
            return;
        }
        if (!region.isAttacked()) {
            return;
        }
        block.setType(Material.AIR);
        fired.add(block.getWorld().spawnEntity(block.getLocation().add(.5, 0, .5), EntityType.PRIMED_TNT));
    }

    @EventHandler
    public void onCannonImpact (ProjectileImpactEvent event) {
        cannonsTarget.add(event.getImpactLocation());
    }

    @EventHandler
    public void onCannonExplosion(BlockExplodeEvent event) {
        Location loc = event.getBlock().getLocation();
        if (loc.getWorld().getEnvironment() == World.Environment.NETHER) {  // Bed explosions in Nether are usually not caused by cannons.
            return;
        }
        Region region = board.getByLocation(loc);
        if (region == null ||region.getOwner() == null) {
            return;
        }
        if (!region.getOwner().isInWar() || region.getOccupant() != null) {
            return;
        }
        if (!region.isAttacked()) {
            return;
        }
        event.setYield(0);
        List<BlockState> blocks = new ArrayList<>();
        event.blockList().forEach(b -> blocks.add(b.getState()));
        new Task(blocks).runTaskTimer(plugin, restoreTicksSiege * 2, restoreTicksSiege);
        blockListsSiege.add(new WeakReference<>(blocks));
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if ( !fired.contains(event.getEntity())) {
            return;
        }
        Region region = board.getByLocation(event.getLocation());
        if (!region.isAttacked()) {
            return;
        }
        event.setYield(0);
        List<BlockState> blocks = new ArrayList<>();
        event.blockList().forEach(b -> blocks.add(b.getState()));
        new Task(blocks).runTaskTimer(plugin, 0, restoreTicksTNT);
        blockListsTNT.add(new WeakReference<>(blocks));
        fired.remove(event.getEntity());
    }


    public void restoreAll() {
        cleanBlockList();
        blockListsTNT.forEach(l -> l.get().forEach(b -> b.update(true)));
        blockListsSiege.forEach(l -> l.get().forEach(b -> b.update(true)));
    }

    private void cleanBlockList() {
        for (WeakReference ref : blockListsTNT) {
            if (ref.get() == null) {
                blockListsTNT.remove(ref);
            }
        }
        for (WeakReference ref : blockListsSiege) {
            if (ref.get() == null) {
                blockListsSiege.remove(ref);
            }
        }
    }

    public class Task extends BukkitRunnable {

        private List<BlockState> restore;

        Task(List<BlockState> restore) {
            this.restore = restore;
        }

        @Override
        public void run() {
            if (restore.isEmpty()) {
                cancel();
                cleanBlockList();
            }
            restore.get(0).update(true);
            Material mat = restore.get(0).getBlock().getType();
            if (NO_REGEN_INVENTORY.contains(mat)) {
                Location loc = restore.get(0).getLocation();
                Container cont = (Container) loc.getBlock().getState();
                cont.getInventory().clear();
            }
            restore.remove(0);
        }

    }

}
