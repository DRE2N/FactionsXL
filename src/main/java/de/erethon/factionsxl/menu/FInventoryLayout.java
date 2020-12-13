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

import de.erethon.vignette.api.InventoryGUI;
import de.erethon.vignette.api.component.Component;
import de.erethon.vignette.api.layout.Layout;
import de.erethon.vignette.api.layout.SingleInventoryLayout;

public class FInventoryLayout extends SingleInventoryLayout {

    private InventoryGUI gui;

    protected Component<?, InventoryGUI>[] components;
    protected int slot;

    public FInventoryLayout(InventoryGUI gui, int size) {
        super(gui, size);
    }

    protected FInventoryLayout(InventoryGUI gui, FInventoryLayout layout) {
        super(gui, layout);
    }

    @Override
    public boolean set(int slot, Component<?, InventoryGUI> component) {
        return super.set(slot, component);
    }

    @Override
    public int nextSlot() {
        slot++;
        if (slot >= getSize()) {
            slot = -1;
        }
        return slot;
    }

    @Override
    public Layout<InventoryGUI> copy(InventoryGUI inventoryGUI) {
        return new FInventoryLayout(gui, this);
    }
}
