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
package de.erethon.factionsxl.command;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.misc.EnumUtil;
import de.erethon.commons.misc.NumberUtil;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.board.Board;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.board.RegionType;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.util.LazyChunk;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.util.RegionProcessing;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Daniel Saukel
 */
public class WorldCommand extends FCommand {

    FactionsXL plugin = FactionsXL.getInstance();
    FactionCache factions = plugin.getFactionCache();
    Board board = plugin.getBoard();
    private static final BlockFace[] AXIS = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    public WorldCommand() {
        setCommand("world");
        setAliases("w");
        setMinArgs(0);
        setMaxArgs(5);
        setHelp(FMessage.HELP_WORLD.getMessage());
        setPermission(FPermission.WORLD.getNode());
        setPlayerCommand(true);
        setConsoleCommand(false);
    }

    @Override
    public void onExecute(String[] args, CommandSender sender) {
        String sub = new String();
        if (args.length != 1) {
            sub = args[1];
        }

        Player player = (Player) sender;
        Region region = board.getByLocation(player.getLocation());
        int i = 2;
        if (args.length >= 3 && board.getByName(args[2]) != null) {
            region = board.getByName(args[2]);
            i++;
        } else if (args.length >= 3 && board.getById(NumberUtil.parseInt(args[2], -1)) != null) {
            region = board.getById(NumberUtil.parseInt(args[2], -1));
            i++;
        }

        if (sub.equalsIgnoreCase("createProvince") || sub.equalsIgnoreCase("create") || sub.equalsIgnoreCase("c")) {
            create(player, region, args, i);

        } else if (sub.equalsIgnoreCase("deleteProvince") || sub.equalsIgnoreCase("delete") || sub.equalsIgnoreCase("d")) {
            delete(player, region);

        } else if (sub.equalsIgnoreCase("addChunk") || sub.equalsIgnoreCase("add") || sub.equalsIgnoreCase("a")) {
            addChunk(player, region, args, i);

        } else if (sub.equalsIgnoreCase("removeChunk") || sub.equalsIgnoreCase("r")) {
            removeChunk(player, region);

        } else if (sub.equalsIgnoreCase("renameProvince") || sub.equalsIgnoreCase("rename") || sub.equalsIgnoreCase("n")) {
            ParsingUtil.sendMessage(player, FMessage.CMD_WORLD_RENAMED.getMessage(), region.getName(), args[i]);
            region.setName(args[i]);

        } else if (sub.equalsIgnoreCase("setType") || sub.equalsIgnoreCase("t")) {
            setType(player, region, args, i);

        } else if (sub.equalsIgnoreCase("setLevel") || sub.equalsIgnoreCase("l")) {
            region.setLevel(NumberUtil.parseInt(args[i], 1));
            ParsingUtil.sendMessage(player, FMessage.CMD_WORLD_LEVEL.getMessage(), region, args[i]);

        } else if (sub.equalsIgnoreCase("addCore")) {
            Faction faction = factions.getAllByName(args[i]);
            if (faction != null) {
                region.getCoreFactions().putIfAbsent(faction, Calendar.getInstance().getTime());
            }
        } else if (sub.equalsIgnoreCase("addClaim")) {
            Faction faction = factions.getAllByName(args[i]);
            if (faction != null) {
                region.getClaimFactions().putIfAbsent(faction, Calendar.getInstance().getTime());
            }

        } else if (sub.equalsIgnoreCase("removeCore")) {
            Faction faction = factions.getAllByName(args[i]);
            if (faction != null) {
                region.getCoreFactions().remove(faction);
            }
        } else if (sub.equalsIgnoreCase("removeClaim")) {
            Faction faction = factions.getAllByName(args[i]);
            if (faction != null) {
                region.getClaimFactions().remove(faction);
            }

        } else if (sub.equalsIgnoreCase("calcAdjacent") || sub.equalsIgnoreCase("calc") || sub.equalsIgnoreCase("ca")) {
            calculateNeighbors(player, region);

        } else if (sub.equalsIgnoreCase("addAdjacent") || sub.equalsIgnoreCase("adda") || sub.equalsIgnoreCase("aa")) {
            addNeighbour(player, args);

        } else if (sub.equalsIgnoreCase("removeAdjacent") || sub.equalsIgnoreCase("rema") || sub.equalsIgnoreCase("ra")) {
            removeNeighbour(player, args);

        } else if (sub.equalsIgnoreCase("listAdjacent") || sub.equalsIgnoreCase("lista") || sub.equalsIgnoreCase("la")) {
            listNeighbours(player, region);

        } else if (sub.equalsIgnoreCase("setUnclaimable") || sub.equalsIgnoreCase("unclaimable") ||sub.equalsIgnoreCase("un")) {
            region.setUnclaimable(!region.isUnclaimable());
            MessageUtil.sendMessage(player, "&aUnclaimable: " + region.isUnclaimable());

        } else {
            MessageUtil.sendMessage(player, "&6/f &aw&8orld &5- &7Help");
            MessageUtil.sendMessage(player, "&6/f &aw c&8reate &7<Name> &5- &7Creates a new region.");
            MessageUtil.sendMessage(player, "&6/f &aw d&8elete &5- &7Deletes a region.");
            MessageUtil.sendMessage(player, "&6/f &awn a&8dd &7<Name> &5- &7Adds a chunk to a region. &7&oauto / radius <x> / line <x>");
            MessageUtil.sendMessage(player, "&6/f &aw r&8emove &5- &7Removes the current chunk from its region.");
            MessageUtil.sendMessage(player, "&6/f &aw &8re&an&8ame &7<Name> &5- &7Renames the region.");
            MessageUtil.sendMessage(player, "&6/f &aw &8set&aT&8ype &5- &7Sets the region type.");
            MessageUtil.sendMessage(player, "&6/f &aw &8set&aL&8evel &5- &7Sets the region level.");
            MessageUtil.sendMessage(player, "&6/f &aw &aaddCore &7<Faction> &5- &7Adds a core.");
            MessageUtil.sendMessage(player, "&6/f &aw &aaddClaim &7<Faction> &5- &7Adds a claim.");
            MessageUtil.sendMessage(player, "&6/f &aw &aremoveCore &7<Faction> &5- &7Removes a core.");
            MessageUtil.sendMessage(player, "&6/f &aw &aremoveClaim &7<Faction> &5- &7Removes a claim.");
            MessageUtil.sendMessage(player, "&6/f &aw &aa&8dd&aA&8djacent &7<Region> &5- &7Adds a adjacent region.");
            MessageUtil.sendMessage(player, "&6/f &aw &ar&8emove&aA&8djacent &7<Region> &5- &7Removes a adjacent region.");
            MessageUtil.sendMessage(player, "&6/f &aw &al&8ist&aA&8djacent &5- &7Lists all adjacent regions.");
            MessageUtil.sendMessage(player, "&6/f &aw &8set&aUn&8claimable &5- &7Changes the unclaimable state of the region");

        }
    }

    private void create(Player player, Region region, String[] args, int i) {
        if (region == null && args.length >= i + 1) {
            board.getRegions().add(new Region(args[i], player.getLocation().getChunk()));
            ParsingUtil.sendMessage(player, FMessage.CMD_WORLD_CREATE.getMessage(), args[2]);
        } else if (region != null) {
            ParsingUtil.sendMessage(player, FMessage.ERROR_NOT_WILDERNESS.getMessage());
        } else if (args.length < i + 1) {
            ParsingUtil.sendMessage(player, ChatColor.RED + "/f world createProvince [name]");
        }
    }

    private void delete(Player player, Region region) {
        if (region != null) {
            board.deleteRegion(region);
            ParsingUtil.sendMessage(player, FMessage.CMD_WORLD_DELETE.getMessage(ParsingUtil.getRegionName(player, region)));
        } else {
            ParsingUtil.sendMessage(player, FMessage.ERROR_NO_SUCH_REGION.getMessage());
        }
    }

    private void addChunk(Player player, Region region, String[] args, int i) {
        World world = player.getWorld();
        Chunk chunk = player.getLocation().getChunk();
        int x = chunk.getX();
        int z = chunk.getZ();
        boolean override = args.length > i + 2 ? args[i + 2].equalsIgnoreCase("-override") : false;

        if (region == null) {
            ParsingUtil.sendMessage(player, ChatColor.RED + "/f world addChunk [region name] [radius|line|auto] [value] ([-override])");
            return;
        }
        if (args.length >= i + 1) {
            if (args[i].equalsIgnoreCase("radius") || args[i].equalsIgnoreCase("r")) {
                int r = args.length > i + 1 ? NumberUtil.parseInt(args[i + 1]) : 1;
                x += -1 * r;
                z += -1 * r;
                while (z <= chunk.getZ() + r) {
                    if (board.isWilderness(world.getChunkAt(x, z)) || override) {
                        addChunk(region, world.getChunkAt(x, z), override);
                    }
                    x++;
                    if (x > chunk.getX() + r) {
                        x = chunk.getX() + -1 * r;
                        z++;
                    }
                }

            } else if (args[i].equalsIgnoreCase("line") || args[i].equalsIgnoreCase("l")) {
                int l = args.length > i + 1 ? NumberUtil.parseInt(args[i + 1]) : 1;
                BlockFace face = AXIS[Math.round(player.getLocation().getYaw() / 90F) & 0x3];
                if (face == BlockFace.NORTH) {
                    while (l >= 0) {
                        if (board.isWilderness(world.getChunkAt(x, z + l)) || override) {
                            addChunk(region, world.getChunkAt(x, z + l), override);
                        }
                        l--;
                    }
                } else if (face == BlockFace.EAST) {
                    while (l >= 0) {
                        if (board.isWilderness(world.getChunkAt(x - l, z)) || override) {
                            addChunk(region, world.getChunkAt(x - l, z), override);
                        }
                        l--;
                    }
                } else if (face == BlockFace.WEST) {
                    while (l >= 0) {
                        if (board.isWilderness(world.getChunkAt(x + l, z)) || override) {
                            addChunk(region, world.getChunkAt(x + l, z), override);
                        }
                        l--;
                    }
                } else if (face == BlockFace.SOUTH) {
                    while (l >= 0) {
                        if (board.isWilderness(world.getChunkAt(x, z - l)) || override) {
                            addChunk(region, world.getChunkAt(x, z - l), override);
                        }
                        l--;
                    }
                }

            } else if (args[i].equalsIgnoreCase("auto") || args[i].equalsIgnoreCase("a")) {
                FPlayer fPlayer = plugin.getFPlayerCache().getByPlayer(player);
                if (fPlayer.isAutoclaiming()) {
                    fPlayer.setAutoclaiming(null);
                    ParsingUtil.sendMessage(player, FMessage.CMD_WORLD_AUTOCLAIM_END.getMessage());
                    return;
                } else {
                    fPlayer.setAutoclaiming(region);
                    ParsingUtil.sendMessage(player, FMessage.CMD_WORLD_AUTOCLAIM_START.getMessage(), region);
                }
            }

        } else {
            addChunk(region, chunk, override);
        }
        ParsingUtil.sendMessage(player, FMessage.CMD_WORLD_CHUNK_ADDED.getMessage(), region);
    }

    private void removeChunk(Player player, Region region) {
        if (region != null) {
            removeChunk(region, player.getLocation().getChunk());
            ParsingUtil.sendMessage(player, FMessage.CMD_WORLD_CHUNK_REMOVED.getMessage(), region);
        } else {
            ParsingUtil.sendMessage(player, FMessage.ERROR_NO_SUCH_REGION.getMessage());
        }
    }

    private void addChunk(Region region, Chunk chunk, boolean override) {
        if (override) {
            Region old = board.getByChunk(chunk);
            if (old != null) {
                removeChunk(old, chunk);
            }
        }
        region.getChunks().add(new LazyChunk(chunk));
    }

    private void removeChunk(Region region, Chunk chunk) {
        LazyChunk remove = null;
        for (LazyChunk rChunk : region.getChunks()) {
            if (chunk.getX() == rChunk.getX() && chunk.getZ() == rChunk.getZ()) {
                remove = rChunk;
            }
        }
        region.getChunks().remove(remove);
    }

    private void setType(Player player, Region region, String[] args, int i) {
        if (args.length < i + 1) {
            ParsingUtil.sendMessage(player, ChatColor.RED + "/f world setType [region name] [type]");
        }
        if (region == null) {
            ParsingUtil.sendMessage(player, FMessage.ERROR_NO_SUCH_REGION.getMessage());
            return;
        }
        if (EnumUtil.isValidEnum(RegionType.class, args[i].toUpperCase())) {
            RegionType type = RegionType.valueOf(args[i].toUpperCase());
            region.setType(type);
            ParsingUtil.sendMessage(player, FMessage.CMD_WORLD_TYPE.getMessage(), region, type.getName());
        } else {
            ParsingUtil.sendMessage(player, FMessage.ERROR_NO_SUCH_REGION_TYPE.getMessage());
        }
    }

    private void handleFaction(Player player, Region region, Faction faction, String[] args, int i) {
        if (faction == null || region == null) {
            return;
        }
        if (args[i - 1].equalsIgnoreCase("addCore")) {
            region.getCoreFactions().put(faction, Calendar.getInstance().getTime());
            ParsingUtil.sendMessage(player, FMessage.FACTION_NEW_CLAIM.getMessage(), faction, region);
        } else if (args[i - 1].equalsIgnoreCase("addClaim")) {
            region.getClaimFactions().put(faction, Calendar.getInstance().getTime());
            ParsingUtil.sendMessage(player, FMessage.FACTION_NEW_CORE.getMessage(), faction, region);
        } else if (args[i - 1].equalsIgnoreCase("removeCore")) {
            region.getClaimFactions().remove(faction);
            ParsingUtil.sendMessage(player, FMessage.FACTION_LOST_CORE.getMessage(), faction, region);
        } else if (args[i - 1].equalsIgnoreCase("removeClaim")) {
            region.getClaimFactions().remove(faction);
            ParsingUtil.sendMessage(player, FMessage.FACTION_LOST_CLAIM.getMessage(), faction, region);
        }
    }

    private void listNeighbours(Player p, Region rgOwn) {
        MessageUtil.sendMessage(p, "&aAdjacent regions for &e" + rgOwn.getName() + "&a:");
        for (Region r : rgOwn.getNeighbours()) {
            if (!(r.getName() == null)) {
                MessageUtil.sendMessage(p, "&8 - &6" + r.getName());
            }
        }
    }

    private void addNeighbour(Player p, String[] args) {
        Region rgOwn = board.getByLocation(p.getLocation());
        Region rgAdd = board.getByName(args[2]);
        if (rgOwn == null || rgAdd == null) {
            MessageUtil.sendMessage(p, "&cInvalid region.");
            return;
        }
        rgOwn.addNeighbour(rgAdd);
        rgAdd.addNeighbour(rgOwn);
        MessageUtil.sendMessage(p, "&e" + rgAdd.getName() + "&a and &e" + rgOwn.getName() + "&a are now adjacent to each other.");
    }

    private void removeNeighbour(Player p, String[] args) {
        Region rgOwn = board.getByLocation(p.getLocation());
        Region rgRem = board.getByName(args[2]);
        if (rgOwn == null || rgRem == null) {
            MessageUtil.sendMessage(p, "&cInvalid region.");
            return;
        }
        rgRem.getNeighbours().remove(rgOwn);
        rgOwn.getNeighbours().remove(rgRem);
        MessageUtil.sendMessage(p, "&e" + rgRem.getName() + "&a and &e" + rgOwn.getName() + "&a are no longer adjacent.");
    }

    private void calculateNeighbors(Player p, Region rg) {
        if (rg == null) {
            MessageUtil.sendMessage(p, "&cInvalid region.");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                World w = rg.getWorld();
                int progress = 0;
                Collection<LazyChunk> chunks1 = rg.getChunks();
                Collection<ChunkSnapshot> allChunks = new CopyOnWriteArrayList<>();
                Collection<ChunkSnapshot> chunksAround = new CopyOnWriteArrayList<>();
                for (LazyChunk c : chunks1) {
                    progress++;
                    MessageUtil.sendActionBarMessage(p, "&aLoading Chunks... &6" + progress + "&8/&7" + rg.getSize());
                    chunksAround = c.getChunksAround(w);
                    allChunks.addAll(chunksAround);
                }
                progress = 0;
                for (ChunkSnapshot c : allChunks) {
                    int cx = c.getX()  * 16;
                    int cz = c.getZ() * 16;
                    Region rg2 = plugin.getBoard().getByLocation(new Location(w, cx, 100, cz));
                    MessageUtil.sendActionBarMessage(p, "&aCalculating adjacent regions... &6" + progress + "&8/&7" + allChunks.size());
                    if ( !(rg2 == null) && !(rg2.equals(rg)) && !(rg.getNeighbours().contains(rg2)) ) {
                        MessageUtil.sendMessage(p, "&aRegion found & added&8: &7" + rg2.getName());
                        rg.addNeighbour(rg2);
                        rg.save();
                    }
                }
            }
        });
    }


    private void calcAllNeighbours() {
        List<Region> allRgs = new CopyOnWriteArrayList<>();
        allRgs = board.getRegions();
        int size = 0;
        for (Region rg : allRgs) {
            size = size + rg.getSize();
        }
        MessageUtil.log("--- Now calculating adjacent regions for all regions. ---");
        MessageUtil.log("--> Regions: " + allRgs.size() + " Total chunks: " + size );
        MessageUtil.log("--> This will take approximately " + (size / 4) / 60 + " minutes or " + ((size / 4) / 60) / 60 + " hours." );

        // TODO: Do max 10 regions at the same time to prevent out of memory.

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, new RegionProcessing(allRgs), 0, 600);


    }

}
