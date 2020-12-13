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

package de.erethon.factionsxl.menu;

import de.erethon.commons.chat.MessageUtil;
import de.erethon.vignette.api.component.InventoryButton;
import org.bukkit.entity.Player;

public class FNumberInputMenu extends FMenu {

    private InventoryButton acceptButton = FMenuButton.ACCEPT.getButton();
    private InventoryButton denyButton = FMenuButton.DENY.getButton();
    private InventoryButton[] upButtons = new InventoryButton[9];
    private InventoryButton[] numbers = new InventoryButton[32];
    private InventoryButton[] downButtons =  new InventoryButton[9];

    public FNumberInputMenu(String title, Player player) {
        super(title, player);
        generateButtons();
    }

    public void generateButtons() {
        int p = 0;
        for (int i = 9; i <= 17; i++) {
            p++;
            if (i == 15) {
                continue;
            }
            InventoryButton button = FMenuButton.UP_ARROW.getButton();
            int finalP = p;
            button.setInteractionListener(listener -> increment(finalP));
            numbers[p] = button;
            set(i, button);
        }
        p = 0;
        for (int i = 18; i <= 26; i++) {
            p++;
            if (i == 24) {
                InventoryButton dotButton = FMenuButton.DOT.getButton();
                numbers[p] = dotButton;
                set(i, dotButton);
                continue;
            }
            InventoryButton numberButton = FMenuButton.ZERO.getButton();
            numbers[p] = numberButton;
            set(i, numberButton);
        }
        p = 0;
        for (int i = 27; i <= 35; i++) {
            p++;
            if (i == 33) {
                continue;
            }
            InventoryButton button = FMenuButton.DOWN_ARROW.getButton();
            int finalP = p;
            MessageUtil.log("Decr listener " + p);
            button.setInteractionListener(listener -> decrement(finalP));
            numbers[p] = button;
            set(i, button);
        }
        set(37, denyButton);
        set(43, acceptButton);
    }

    public void increment(int position) {
        MessageUtil.broadcastMessage("Increment: " + position);
    }

    public void decrement(int position) {
        MessageUtil.broadcastMessage("Decrement: " + position);
    }

    public void enter() {

    }

    public InventoryButton getAcceptButton() {
        return acceptButton;
    }

    public InventoryButton getDenyButton() {
        return denyButton;
    }
}
