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
package de.erethon.factionsxl.scoreboard;

import com.google.common.base.Splitter;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

/**
 * Adapted from FactionsUUID by drtshock.
 *
 * @author Trent Hensler, Daniel Saukel
 */
public class BufferedObjective {

    private static final int MAX_LINE_LENGTH = 48;

    private final Scoreboard scoreboard;
    private final String baseName;

    private Objective current;
    private List<Team> currentTeams = new ArrayList<>();
    private String title;
    private DisplaySlot displaySlot;

    private int objPtr;
    private int teamPtr;
    private boolean requiresUpdate = false;

    private final Map<Integer, String> contents = new HashMap<>();

    public BufferedObjective(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
        this.baseName = createBaseName();

        current = scoreboard.registerNewObjective(getNextObjectiveName(), "dummy", "f_objective");
    }

    private String createBaseName() {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        while (builder.length() < 14) {
            builder.append(Integer.toHexString(random.nextInt()));
        }
        return builder.toString().substring(0, 14);
    }

    public void setTitle(String title) {
        if (this.title == null || !this.title.equals(title)) {
            this.title = title;
            requiresUpdate = true;
        }
    }

    public void setDisplaySlot(DisplaySlot slot) {
        this.displaySlot = slot;
        current.setDisplaySlot(slot);
    }

    public void setAllLines(List<String> lines) {
        if (lines.size() != contents.size()) {
            contents.clear();
        }

        for (int i = 0; i < lines.size(); i++) {
            setLine(lines.size() - i, lines.get(i));
        }
    }

    public void setLine(int lineNumber, String content) {
        if (content.length() > MAX_LINE_LENGTH) {
            content = content.substring(0, MAX_LINE_LENGTH);
        }
        content = ChatColor.translateAlternateColorCodes('&', content);

        if (contents.get(lineNumber) == null || !contents.get(lineNumber).equals(content)) {
            contents.put(lineNumber, content);
            requiresUpdate = true;
        }
    }

    // Hides the objective from the display slot until flip() is called
    public void hide() {
        if (displaySlot != null) {
            scoreboard.clearSlot(displaySlot);
        }
    }

    public void flip() {
        if (!requiresUpdate) {
            return;
        }
        requiresUpdate = false;

        Objective buffer = scoreboard.registerNewObjective(getNextObjectiveName(), "dummy");
        buffer.setDisplayName(title);

        List<Team> bufferTeams = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : contents.entrySet()) {
            if (entry.getValue().length() > 32) {
                Team team = scoreboard.registerNewTeam(getNextTeamName());
                bufferTeams.add(team);

                Iterator<String> split = Splitter.fixedLength(32).split(entry.getValue()).iterator();

                team.setPrefix(split.next());
                String name = split.next();
                if (split.hasNext()) { // We only guarantee two splits
                    team.setSuffix(split.next());
                }

                team.addEntry(name);
                buffer.getScore(name).setScore(entry.getKey());

            } else {
                buffer.getScore(entry.getValue()).setScore(entry.getKey());
            }
        }

        if (displaySlot != null) {
            buffer.setDisplaySlot(displaySlot);
        }

        // Unregister _ALL_ the old things
        current.unregister();

        Iterator<Team> it = currentTeams.iterator();
        while (it.hasNext()) {
            it.next().unregister();
            it.remove();
        }

        current = buffer;
        currentTeams = bufferTeams;
    }

    private String getNextObjectiveName() {
        return baseName + "_" + ((objPtr++) % 2);
    }

    private String getNextTeamName() {
        return baseName.substring(0, 10) + "_" + ((teamPtr++) % 999999);
    }

}
