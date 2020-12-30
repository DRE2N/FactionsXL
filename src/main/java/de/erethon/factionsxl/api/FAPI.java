package de.erethon.factionsxl.api;

/**
 * The main class for the FactionAPI. Contains general helper methods,
 * as well as references to the sub-APIs.
 */
public class FAPI {

    private final FactionAPI factionAPI;
    private final FPlayerAPI fPlayerAPI;
    private final RegionAPI regionAPI;
    private final WarAPI warAPI;

    public FAPI() {
        factionAPI = new FactionAPI();
        fPlayerAPI = new FPlayerAPI();
        regionAPI = new RegionAPI();
        warAPI = new WarAPI();
    }

    /**
     * Contains all API methods related to Factions.
     */
    public FactionAPI getFactionAPI() {
        return factionAPI;
    }

    /**
     * Contains all API methods related to (F)Players.
     */
    public FPlayerAPI getFPlayerAPI() {
        return fPlayerAPI;
    }

    /**
     * Contains all API methods related to Regions.
     */
    public RegionAPI getRegionAPI() {
        return regionAPI;
    }

    /**
     * Contains all API methods related to War.
     */
    public WarAPI getWarAPI() {
        return warAPI;
    }
}
