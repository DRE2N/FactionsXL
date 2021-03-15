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
package de.erethon.factionsxl;

import at.pavlov.cannons.API.CannonsAPI;
import at.pavlov.cannons.Cannons;
import com.griefcraft.lwc.LWC;
import de.erethon.commons.chat.MessageUtil;
import de.erethon.commons.compatibility.Internals;
import de.erethon.commons.javaplugin.DREPlugin;
import de.erethon.commons.javaplugin.DREPluginSettings;
import de.erethon.commons.misc.FileUtil;
import de.erethon.factionsxl.board.Board;
import de.erethon.factionsxl.board.dynmap.Atlas;
import de.erethon.factionsxl.chat.ChatListener;
import de.erethon.factionsxl.command.FCommandCache;
import de.erethon.factionsxl.command.FCommandCompleter;
import de.erethon.factionsxl.config.FConfig;
import de.erethon.factionsxl.config.FData;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.economy.IncomeTask;
import de.erethon.factionsxl.entity.RelationRequest;
import de.erethon.factionsxl.faction.FBull;
import de.erethon.factionsxl.faction.FMob;
import de.erethon.factionsxl.faction.FactionCache;
import de.erethon.factionsxl.player.AsyncPowerTask;
import de.erethon.factionsxl.player.FPermission;
import de.erethon.factionsxl.player.FPlayerCache;
import de.erethon.factionsxl.player.PlayerListener;
import de.erethon.factionsxl.protection.EntityProtectionListener;
import de.erethon.factionsxl.protection.LWCIntegration;
import de.erethon.factionsxl.protection.LandProtectionListener;
import de.erethon.factionsxl.util.BalanceCache;
import de.erethon.factionsxl.util.CoringHandler;
import de.erethon.factionsxl.util.FDebugLevel;
import de.erethon.factionsxl.war.*;
import de.erethon.factionsxl.war.demand.ItemDemand;
import de.erethon.factionsxl.war.demand.MoneyDemand;
import de.erethon.factionsxl.war.demand.RegionDemand;
import de.erethon.factionsxl.war.demand.RelationDemand;
import de.erethon.factionsxl.war.peaceoffer.PeaceOffer;
import de.erethon.factionsxl.war.peaceoffer.SeparatePeaceOffer;
import de.erethon.vignette.api.VignetteAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * The main class of FactionsXL.
 * It contains several important instances and the actions when the plugin is enabled / disabled.
 *
 * @author Daniel Saukel
 */
public class FactionsXL extends DREPlugin {

    private static FactionsXL instance;
    private LWC lwc;

    public static File BACKUPS;
    public static File BOARD;
    public static File BUILDINGS;
    public static File LANGUAGES;
    public static File PLAYERS;
    public static File DYNASTIES;
    public static File FACTIONS;
    public static File FEDERATIONS;
    public static File TRADE_LEAGUES;
    public static File WARS;
    private static File DEBUG;

    private FConfig fConfig;
    private FData fData;
    private FCommandCache fCommands;
    private FPlayerCache fPlayers;
    private FactionCache factions;
    private WarCache wars;
    private Board board;
    private ChatListener chatListener;
    private PlayerListener playerListener;
    private EntityProtectionListener entityProtectionListener;
    private LandProtectionListener landProtectionListener;
    private LWCIntegration lwcIntegration;
    private WarTNT warTNT;
    private WarHandler warHandler;
    private WarPoints warPoints;
    private CasusBelliManager casusBelliManager;
    private OccupationManager occupationManager;
    private CoringHandler core;
    private BukkitTask incomeTask;
    private BukkitTask powerTask;
    private BalanceCache balanceCache;
    private boolean debugEnabled = true;
    private List<FDebugLevel> debugLevels = new ArrayList<>();
    private PrintWriter out;
    private CannonsAPI cannonsAPI;

    public FactionsXL() {
        settings = DREPluginSettings.builder()
                .spigot(true)
                .paper(true)
                .economy(true)
                .metrics(true)
                .internals(Internals.v1_14_R1, Internals.v1_15_R1, Internals.v1_16_R1)
                .build();
    }

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(RelationRequest.class);
        ConfigurationSerialization.registerClass(WarRequest.class);
        ConfigurationSerialization.registerClass(PeaceOffer.class);
        ConfigurationSerialization.registerClass(SeparatePeaceOffer.class);
        ConfigurationSerialization.registerClass(MoneyDemand.class);
        ConfigurationSerialization.registerClass(RegionDemand.class);
        ConfigurationSerialization.registerClass(RelationDemand.class);
        ConfigurationSerialization.registerClass(ItemDemand.class);
        super.onEnable();
        initFolders();
        debugToFile("Enabling...");
        if (!compat.isSpigot() || !settings.getInternals().contains(compat.getInternals())) {
            MessageUtil.log(this, "&4This plugin requires Spigot 1.14.4-1.16.5 to work. It is not compatible with CraftBukkit and older versions.");
        }
        if (!compat.isPaper()) {
            MessageUtil.log(this, "Some features of FXL require Paper. Paper is a drop-in replacement for Spigot. Download it at papermc.io/downloads");
        }
        instance = this;
        debugLevels.add(FDebugLevel.ERROR);

        FPermission.register();
        VignetteAPI.init(this);
        loadCore();
        debugToFile("Enabled!");
    }

    @Override
    public void onDisable() {
        if (instance != null) {
            warTNT.restoreAll();
            saveData();
            backupData();
        }
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
        debugToFile("Disabled!");
        if (out != null) {
            out.close();
        }
    }

    // Initialize
    public void initFolders() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        BACKUPS = new File(getDataFolder(), "backups");
        if (!BACKUPS.exists()) {
            BACKUPS.mkdir();
        }

        BOARD = new File(getDataFolder(), "board");
        if (!BOARD.exists()) {
            BOARD.mkdir();
        }

        BUILDINGS = new File(getDataFolder(), "buildings");
        if (!BUILDINGS.exists()) {
            BUILDINGS.mkdir();
        }

        LANGUAGES = new File(getDataFolder(), "languages");
        if (!LANGUAGES.exists()) {
            LANGUAGES.mkdir();
        }

        PLAYERS = new File(getDataFolder(), "players");
        if (!PLAYERS.exists()) {
            PLAYERS.mkdir();
        }

        DYNASTIES = new File(getDataFolder(), "dynasties");
        if (!DYNASTIES.exists()) {
            DYNASTIES.mkdir();
        }

        FACTIONS = new File(getDataFolder(), "factions");
        if (!FACTIONS.exists()) {
            FACTIONS.mkdir();
        }

        FEDERATIONS = new File(getDataFolder(), "federations");
        if (!FEDERATIONS.exists()) {
            FEDERATIONS.mkdir();
        }

        TRADE_LEAGUES = new File(getDataFolder(), "tradeleagues");
        if (!TRADE_LEAGUES.exists()) {
            TRADE_LEAGUES.mkdir();
        }

        WARS = new File(getDataFolder(), "wars");
        if (!WARS.exists()) {
            WARS.mkdir();
        }

        if (debugEnabled) {
            DEBUG = new File(getDataFolder(), "debug.txt");
            if (!DEBUG.exists()) {
                try {
                    DEBUG.createNewFile();
                } catch (IOException exception) {
                }
            }
        }
    }

    public void loadCore() {
        // Load Config
        loadFConfig(new File(getDataFolder(), "config.yml"));
        if (fConfig.getAutoSaveInterval() != -1) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    saveData();
                }
            }.runTaskTimerAsynchronously(this, fConfig.getAutoSaveInterval(), fConfig.getAutoSaveInterval());
        }
        loadFData();
        loadFactions(FACTIONS, FEDERATIONS, TRADE_LEAGUES);
        loadBoard(BOARD);
        loadFPlayers();
        fPlayers.loadAll();
        board.loadAll();
        factions.loadAll();
        loadWars(WARS);
        loadWarHandler();
        loadWarPoints();
        loadCBManager();
        loadAtlas();
        loadOccupationManager();
        loadCoring();
        loadFCommands();
        loadChatListener();
        loadPlayerListener();
        loadEntityProtectionListener();
        loadLandProtectionListener();


        if (fConfig.isLWCEnabled()) {
            loadLWC();
        }
        loadWarTNT();
        startPowerTask();
        if (fConfig.isEconomyEnabled()) {
            startIncomeTask();
            createBalanceCache();
        }

        if (manager.isPluginEnabled("Cannons")) {
            loadCannonsAPI();
        }
        manager.registerEvents(new FBull(), this);
        manager.registerEvents(new FMob(), this);
        manager.registerEvents(new WarListener(), this);
        getCommand("factionsxl").setTabCompleter(new FCommandCompleter());

        new BukkitRunnable() {
            @Override
            public void run() {
                warHandler.updateTruce();
                warHandler.calculateWarStatus();
            }
        }.runTaskTimer(this, FConfig.MINUTE, FConfig.MINUTE * 5);

        new BukkitRunnable() {
            @Override
            public void run() {
                occupationManager.updateOccupationStatus();
            }
        }.runTaskTimer(this, 1, FConfig.SECOND * 60);
        new BukkitRunnable() {
            @Override
            public void run() {
                occupationManager.showTimers();
            }
        }.runTaskTimerAsynchronously(this, 40, 40);

        if (fConfig.isEconomyEnabled()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    balanceCache.run();
                }
            }.runTaskTimer(this, FConfig.SECOND * 30, FConfig.SECOND * 30);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                fPlayers.autoKick();
            }
        }.runTaskTimer(this, 200L, FConfig.HOUR);

        for (War war : wars.getWars()) {
            warHandler.relationFixer(war);
        }
    }


    public void saveData() {
        fData.save();
        board.saveAll();
        factions.saveAll();
        fPlayers.saveAll();
        wars.saveAll();
    }

    public void backupData() {
        File backupDir = new File(BACKUPS, String.valueOf(System.currentTimeMillis()));
        backupDir.mkdir();
        FileUtil.copyDir(BOARD, new File(backupDir, "board"), new String[]{});
        FileUtil.copyDir(PLAYERS, new File(backupDir, "players"), new String[]{});
        FileUtil.copyDir(DYNASTIES, new File(backupDir, "dynasties"), new String[]{});
        FileUtil.copyDir(FACTIONS, new File(backupDir, "factions"), new String[]{});
        FileUtil.copyDir(FEDERATIONS, new File(backupDir, "federations"), new String[]{});
        FileUtil.copyDir(TRADE_LEAGUES, new File(backupDir, "tradeleagues"), new String[]{});
        FileUtil.copyDir(WARS, new File(backupDir, "wars"), new String[]{});
        FileUtil.copyFile(new File(getDataFolder(), "config.yml"), new File(backupDir, "config.yml"));
        FileUtil.copyFile(FData.FILE, new File(backupDir, "data.yml"));
    }

    /* Getters and loaders */
    /**
     * @return
     * the plugin instance
     */
    public static FactionsXL getInstance() {
        return instance;
    }

    /**
     * @return
     * the loaded instance of FConfig
     */
    public FConfig getFConfig() {
        return fConfig;
    }

    /**
     * load / reload a new instance of FConfig
     */
    public void loadFConfig(File file) {
        fConfig = new FConfig(file);
    }

    /**
     * @return
     * the loaded instance of FData
     */
    public FData getFData() {
        return fData;
    }

    /**
     * @return
     * the CoringHandler
     */
    public CoringHandler getCoring() {
        return core;
    }

    /**
     * @return
     * the WarHandler
     */
    public WarHandler getWarHandler() {
        return warHandler;
    }

    /**
     * @return
     * the OccupationManager
     */
    public OccupationManager getOccupationManager() {
        return occupationManager;
    }

    /**
     * new OccupationManager
     */
    public void loadOccupationManager() {
        occupationManager = new OccupationManager();
    }

    /**
     * new CBManager
     */
    public void loadCBManager() {
       casusBelliManager = new CasusBelliManager();
    }

    /**
     * @return
     * the CBManager
     */
    public CasusBelliManager getCBManager() {
        return casusBelliManager;
    }

    /**
     * new CoringHandler
     */
    public void loadCoring() {
        core = new CoringHandler();
    }

    /**
     * load / reload a new instance of FData
     */
    public void loadFData() {
        fData = new FData();
    }

    /**
     * @return
     * the loaded instance of FCommandCache
     */
    @Override
    public FCommandCache getCommandCache() {
        return fCommands;
    }

    /**
     * load / reload a new instance of FCommandCache
     */
    public void loadFCommands() {
        fCommands = new FCommandCache(this);
        fCommands.register(this);
        fCommands.registerAliases();
    }

    /**
     * @return
     * the loaded instance of FPlayerCache
     */
    public FPlayerCache getFPlayerCache() {
        return fPlayers;
    }

    /**
     * load / reload a new instance of FPlayerCache
     */
    public void loadFPlayers() {
        fPlayers = new FPlayerCache();
    }

    /**
     * @return
     * the loaded instance of FactionCache
     */
    public FactionCache getFactionCache() {
        return factions;
    }

    /**
     * load / reload a new instance of FactionCache
     */
    public void loadFactions(File factionsDir, File federationsDir, File leaguesDir) {
        factions = new FactionCache(factionsDir, federationsDir, leaguesDir);
    }

    /**
     * @return
     * the loaded instance of Board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * load / reload a new instance of Board
     */
    public void loadBoard(File dir) {
        board = new Board(dir);
    }

    /**
     * load / reload a new instance of Atlas
     */
    public void loadAtlas() {
        Plugin plugin = manager.getPlugin("dynmap");
        if (plugin != null) {
            Atlas.init();
        } else {
            MessageUtil.log(this, FMessage.LOG_DYNMAP_NOT_ENABLED.getMessage());
        }
    }

    /**
     * @return
     * the loaded instance of WarCache
     */
    public WarCache getWarCache() {
        return wars;
    }

    /**
     * load / reload a new instance of WarCache
     */
    public void loadWars(File dir) {
        wars = new WarCache(this, dir);
    }

    /**
     * load / reload a new instance of WarHandler
     */
    public void loadWarHandler() {
        warHandler = new WarHandler();
    }

    public void loadCannonsAPI()
    {
        if (manager.isPluginEnabled("Cannons")) {
            Cannons c = Cannons.getPlugin();
            cannonsAPI = c.getCannonsAPI();
        }
    }

    public CannonsAPI getCannonsAPI() {
        return cannonsAPI;
    }

    /**
     * @return
     * the loaded instance of ChatListener
     */
    public ChatListener getChatListener() {
        return chatListener;
    }

    /**
     * load / reload a new instance of ChatListener and register it
     */
    public void loadChatListener() {
        if (chatListener != null) {
            HandlerList.unregisterAll(chatListener);
        }
        chatListener = new ChatListener();
        manager.registerEvents(chatListener, this);
    }

    /**
     * @return
     * the loaded instance of PlayerListener
     */
    public PlayerListener getPlayerListener() {
        return playerListener;
    }

    /**
     * load / reload a new instance of PlayerListener and register it
     */
    public void loadPlayerListener() {
        if (playerListener != null) {
            HandlerList.unregisterAll(playerListener);
        }
        playerListener = new PlayerListener(this);
        manager.registerEvents(playerListener, this);
    }

    /**
     * @return
     * the loaded instance of EntityProtectionListener
     */
    public EntityProtectionListener getEntityProtectionListener() {
        return entityProtectionListener;
    }

    /**
     * load / reload a new instance of EntityProtectionListener and register it
     */
    public void loadEntityProtectionListener() {
        if (entityProtectionListener != null) {
            HandlerList.unregisterAll(entityProtectionListener);
        }
        entityProtectionListener = new EntityProtectionListener();
        manager.registerEvents(entityProtectionListener, this);
    }

    /**
     * @return
     * the loaded instance of LandProtectionListener
     */
    public LandProtectionListener getLandProtectionListener() {
        return landProtectionListener;
    }

    /**
     * load / reload a new instance of LandProtectionListener and register it
     */
    public void loadLandProtectionListener() {
        if (landProtectionListener != null) {
            HandlerList.unregisterAll(landProtectionListener);
        }
        landProtectionListener = new LandProtectionListener();
        manager.registerEvents(landProtectionListener, this);
    }

    /**
     * @return
     * the loaded instance of LWCIntegration
     */
    public LWCIntegration getLWCIntegration() {
        return lwcIntegration;
    }

    /**
     * @return
     * the loaded instance of LWC
     */
    public LWC getLWC() {
        return lwc;
    }

    /**
     * load / reload the instance of LWC
     */
    public void loadLWC() {
        if (manager.isPluginEnabled("LWC")) {
            lwc = LWC.getInstance();
            PluginDescriptionFile desc = lwc.getPlugin().getDescription();
            lwcIntegration = new LWCIntegration();
            MessageUtil.log(this, "Successfully hooked into " + desc.getName() + " v" + desc.getVersion() + ".");
        } else {
            MessageUtil.log(this, "&4Could not find LWC.");
        }
    }

    /**
     * @return
     * the loaded instance of WarTNT
     */
    public WarTNT getWarTNT() {
        return warTNT;
    }

    /**
     * load / reload the instance of WarTNT
     */
    public void loadWarTNT() {
        warTNT = new WarTNT(fConfig.getWarExplosionTNTRestorationTime(), fConfig.getWarExplosionSiegeRestorationTime());
        manager.registerEvents(warTNT, this);
    }

    /**
     * @return
     * the AsyncPowerTask
     */
    public BukkitTask getPowerTask() {
        return powerTask;
    }

    /**
     * start a new AsyncPowerTask
     */
    public void startPowerTask() {
        long interval = fConfig.getPowerUpdateInterval();
        long passed = System.currentTimeMillis() - fData.lastPowerUpdate;
        double increase = fConfig.getPowerIncreaseRate();
        double decrease = fConfig.getPowerDecreaseRate();
        int maxPower = fConfig.getMaxPower();
        powerTask = new AsyncPowerTask(increase, decrease, maxPower).runTaskTimerAsynchronously(this, interval - passed, interval);
    }

    /**
     * @return
     * the IncomeTask
     */
    public BukkitTask getIncomeTask() {
        return incomeTask;
    }

    /**
     * @return
     * the balance cache
     */
    public BalanceCache getBalanceCache() {
        return balanceCache;
    }

    /**
     * a cache of player balances, to prevent too many DB queries when updating the scoreboard.
     */
    public void createBalanceCache() {
        balanceCache = new BalanceCache();
    }

    /**
     * the war point calculator
     */
    public void loadWarPoints() {
        warPoints = new WarPoints();
    }

    public WarPoints getWarPoints() {
        return warPoints;
    }

    /**
     * start a new IncomeTask
     */
    public void startIncomeTask() {
        long dayLength = fConfig.getDayLength();
        long passed = System.currentTimeMillis() - fData.lastNewDay;
        incomeTask = new IncomeTask().runTaskTimer(this, dayLength - passed, dayLength);
    }

    public void debugToFile(String message) {
        if (debugEnabled) {
            if (out == null) {
                try {
                    out = new PrintWriter(DEBUG);
                } catch (FileNotFoundException exception) {
                }
            }
            out.println(message);
        }
    }

    public void addDebugLevel(FDebugLevel level) {
        debugLevels.add(level);
    }

    public void removeDebugLevel(FDebugLevel level) {
        debugLevels.remove(level);
    }

    public List<FDebugLevel> getDebugLevels() {
        return debugLevels;
    }

    public static void debug(FDebugLevel level, String message) {
        instance.debugToFile("[" + level.toString() + "] " + message);
        if (instance.getDebugLevels().contains(level)) {
            System.out.println("[FXL | " + level.toString() + "] " + message);
            for (Player player : Bukkit.getOnlinePlayers()) {
                MessageUtil.sendMessage(player, "&8[&cDebug &8|&6 " + level + "&8] &7" + message);
            }
        }
    }

}
