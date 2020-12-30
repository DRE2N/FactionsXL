package de.erethon.factionsxl.api;

import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.api.event.WarTruceEndEvent;
import de.erethon.factionsxl.api.exceptions.NoFPlayerException;
import de.erethon.factionsxl.api.exceptions.NoFactionException;
import de.erethon.factionsxl.api.exceptions.NotInAnyWarPartyException;
import de.erethon.factionsxl.config.FMessage;
import de.erethon.factionsxl.faction.Faction;
import de.erethon.factionsxl.player.FPlayer;
import de.erethon.factionsxl.player.FPlayerCache;
import de.erethon.factionsxl.util.ParsingUtil;
import de.erethon.factionsxl.war.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class WarAPI {

    FactionsXL plugin = FactionsXL.getInstance();
    WarCache warCache = plugin.getWarCache();
    FPlayerCache fPlayerCache = plugin.getFPlayerCache();

    /**
     * @return a set of all wars that are currently ongoing
     */
    public Set<War> getWars() {
        return warCache.getWars();
    }

    /**
     * Only returns wars were the truce period is over.
     * @return a set of all active wars
     */
    public Set<War> getWars(boolean onlyActiveWars) {
        Set<War> wars = new HashSet<>(warCache.getWars());
        wars.removeIf(War::getTruce);
        return wars;
    }

    /**
     * @param faction the {@link Faction}
     * @return a set of all wars this {@link Faction} participates in.
     */
    public Set<War> getByFaction(Faction faction) {
        return warCache.getByFaction(faction);
    }

    /**
     * Only returns wars were the truce period is over.
     * @param faction the {@link Faction}
     * @return a set of all active wars this faction participates in.
     */
    public Set<War> getByFaction(Faction faction, boolean onlyActiveWars) {
        Set<War> wars = getByFaction(faction);
        wars.removeIf(War::getTruce);
        return wars;
    }

    /**
     * @param player the (bukkit) player
     * @return a set of all wars this player is in
     * @throws NoFPlayerException when the provided player does not have a valid {@link FPlayer}
     * @throws NoFactionException when the provided player is not in any faction
     */
    public Set<War> getByPlayer(Player player) throws NoFPlayerException, NoFactionException {
        FPlayer fPlayer = fPlayerCache.getByPlayer(player);
        if (fPlayer == null) {
            throw new NoFPlayerException("There is no valid FPlayer for " + player);
        }
        Faction faction = fPlayer.getFaction();
        if (faction == null) {
            throw new NoFactionException("The FPlayer " + fPlayer.getName() + " is not in a faction.");
        }
        return faction.getWarParties().stream().map(WarParty::getWar).collect(Collectors.toSet());
    }

    /**
     * Only returns active wars (=truce period over)
     * @param player the Player to check
     * @return a set of all active wars this player participates in.
     * @throws NoFPlayerException when the provided player does not have a valid {@link FPlayer}
     * @throws NoFactionException when the provided player is not in any faction
     */
    public Set<War> getByPlayer(Player player, boolean onlyActiveWars) throws NoFPlayerException, NoFactionException {
        Set<War> wars = getByPlayer(player);
        wars.removeIf(War::getTruce);
        return wars;
    }

    /**
     * @param faction the {@link Faction}
     * @return a set of WarParties that are at war with this faction's {@link WarParty}'s
     * @throws NotInAnyWarPartyException when the provided faction is not in any {@link WarParty}
     */
    public Set<WarParty> getEnemyWarParties(Faction faction) throws NotInAnyWarPartyException {
        Set<WarParty> enemies = new HashSet<>();
        if (faction.getWarParties() == null || faction.getWarParties().isEmpty()) {
            throw new NotInAnyWarPartyException("The faction " + faction.getName() + " is not in any WarParty.");
        }
        faction.getWarParties().forEach(wp -> enemies.add(wp.getEnemy()));
        return enemies;
    }

    /**
     * @param faction the {@link Faction}
     * @return a set of factions that are at war with this {@link Faction}
     * @throws NotInAnyWarPartyException when the provided faction is not in any {@link WarParty}
     */
    public Set<Faction> getEnemyFactions(Faction faction) throws NotInAnyWarPartyException {
        Set<Faction> enemies = new HashSet<>();
        if (faction.getWarParties() == null || faction.getWarParties().isEmpty()) {
            throw new NotInAnyWarPartyException("The faction " + faction.getName() + " is not in any WarParty.");
        }
        faction.getWarParties().forEach(wp -> enemies.addAll(wp.getEnemy().getFactions()));
        return enemies;
    }

    /**
     * @param warParty the {@link WarParty}
     * @return a set of players that are in this {@link WarParty}
     */
    public Set<Player> getOnlinePlayers(WarParty warParty) {
        Set<Player> players = new HashSet<>();
        warParty.getFactions().forEach(f -> players.addAll(f.getOnlineMembers()));
        return players;
    }

    /**
     * Creates a war with only the two factions participating.
     * In most cases, you want to use {@link #startWar(WarParty, WarParty, CasusBelli)} instead.
     * The war will start with a truce period as configured in the plugin config.
     * @param attacker the attacking faction
     * @param defender the defending faction
     * @param casusBelli the {@link CasusBelli} (reason) for the war.
     */
    public void startWar(Faction attacker, Faction defender, CasusBelli casusBelli) {
        WarParty attackerWP = new WarParty(attacker, WarPartyRole.ATTACKER);
        WarParty defenderWP = new WarParty(defender, WarPartyRole.DEFENDER);
        startWar(attackerWP, defenderWP, casusBelli);
    }

    /**
     * Creates a war from the two {@link WarParty}'s.
     * The war will start with a truce period as configured in the plugin config.
     * @param attacker the attacking {@link WarParty}
     * @param defender the defending {@link WarParty}
     * @param casusBelli the {@link CasusBelli} (reason) for the war.
     */
    public void startWar(WarParty attacker, WarParty defender, CasusBelli casusBelli) {
        War war = new War(attacker, defender, casusBelli);
        warCache.getUnconfirmedWars().add(war);
        war.confirm();
    }

    /**
     * Skips the truce time (plugin config) and starts the war.
     * @param war the {@link War}
     */
    public void skipTruce(War war) {
        war.setTruce(false);
        Faction attacker = (Faction) war.getAttacker().getLeader();
        Faction defender = (Faction) war.getDefender().getLeader();
        ParsingUtil.broadcastMessage(FMessage.WAR_TRUCE_ENDED.getMessage(), attacker, defender);

        WarTruceEndEvent event = new WarTruceEndEvent(war.getAttacker(), war.getDefender(), war.getCasusBelli());
        Bukkit.getPluginManager().callEvent(event);
    }

}
