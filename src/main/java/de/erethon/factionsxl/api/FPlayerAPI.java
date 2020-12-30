package de.erethon.factionsxl.api;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.api.exceptions.NoFPlayerException;
import de.erethon.factionsxl.board.Region;
import de.erethon.factionsxl.entity.Relation;
import de.erethon.factionsxl.entity.Request;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.player.FPlayerCache;
import de.erethon.factionsxl.player.FPlayerSetting;
import de.erethon.factionsxl.protection.EntityProtectionListener;
import org.bukkit.entity.Player;

/**
 * Contains player-related methods
 */
public class FPlayerAPI {

    FactionsXL plugin = FactionsXL.getInstance();
    FPlayerCache fPlayerCache = plugin.getFPlayerCache();

    /**
     * Gets the corresponding FPlayer from a bukkit player.
     * Only works if the player is online!
     *
     * There are a lot of methods in that API that do not require
     * an FPlayer!
     * @param player a bukkit player
     * @return the {@link FPlayer}
     */
    public FPlayer getFPlayer(Player player) {
        FPlayer fPlayer = fPlayerCache.getByPlayer(player);
        if (fPlayer == null) {
            throw new NoFPlayerException("The FPlayer " + player.getName() + " does not exist. Make sure the player is online.");
        }
        return fPlayer;
    }

    /**
     * Checks if two players can damage each other.
     * This is directional! There are some situations were Player A can attack Player B,
     * but Player B can not attack Player A.
     * @param attacker the attacking player
     * @param defender the player that gets attacked
     * @return true if the players can attack each other, false if not
     */
    public boolean canAttack(Player attacker, Player defender) {
        FPlayer fpattacker = fPlayerCache.getByPlayer(attacker);
        FPlayer fpdefender = fPlayerCache.getByPlayer(defender);
        Faction aFaction = fpattacker.getFaction();
        Faction dFaction = fpdefender.getFaction();
        if (dFaction == null) {
            return true;
        }
        Region region = plugin.getBoard().getByLocation(defender.getLocation());
        if (region == null || region.isNeutral()) {
            return true;
        }
        if (aFaction == null && (region.getOwner() == dFaction || region.getOccupant() != null && region.getOccupant() == dFaction)) {
            return false;
        }
        Faction owner = region.getOwner();
        if ( region.getOccupant() != null) {
            Faction occupant = region.getOccupant();
            if (occupant == aFaction) {
                return true;
            }
        }
        Relation rel = owner.getRelation(aFaction);
        return !rel.isProtected() || (rel == Relation.ENEMY && region.isAttacked());
    }

    /**
     * Add a new {@link Request} to the player.
     * The player will not get notified until you use {@link Request#send()}
     * @param player the player that gets the request
     * @param request see {@link Request} for details
     */
    public void addRequest(Player player, Request request) {
        fPlayerCache.getByPlayer(player).getRequests().add(request);
    }

    /**
     * @deprecated not implemented
     */
    @Deprecated
    public void changeFPlayerSetting(Player player, FPlayerSetting setting) {
    }

    /**
     * @deprecated not implemented
     */
    @Deprecated
    public boolean getFPlayerSetting(Player player, FPlayerSetting setting) {
        return false;
    }
}
