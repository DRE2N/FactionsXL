/*	
 * Written from 2015-2019 by Daniel Saukel	
 *	
 * To the extent possible under law, the author(s) have dedicated all	
 * copyright and related and neighboring rights to this software	
 * to the public domain worldwide.	
 *	
 * This software is distributed without any warranty.	
 *	
 * You should have received a copy of the CC0 Public Domain Dedication	
 * along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.	
 */
package de.erethon.factionsxl.legacygui;

import de.erethon.commons.javaplugin.DREPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

import static de.erethon.factionsxl.legacygui.GUIButton.*;

/**
 * @author Daniel Saukel
 */
public class PageGUICache implements Listener {

    private static PageGUICache instance;

    public static PageGUICache getInstance() {
        if (instance == null) {
            instance = new PageGUICache();
            Bukkit.getPluginManager().registerEvents(instance, DREPlugin.getInstance());
        }
        return instance;
    }

    Set<PageGUI> guis = new HashSet<>();

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        ItemStack button = event.getCurrentItem();
        if (button == null || button.getType() == Material.AIR) {
            return;
        }
        Inventory inventory = event.getInventory();
        PageGUI gui = PageGUI.getByInventory(inventory);
        if (gui == null) {
            return;
        }

        PageGUI.playSound(event);
        int index = gui.getPages().indexOf(inventory);
        HumanEntity player = event.getWhoClicked();
        if (button.equals(PLACEHOLDER)) {
            event.setCancelled(true);
        } else if (button.equals(NEXT_PAGE)) {
            event.setCancelled(true);
            PageGUI.playSound(event);
            gui.open(player, index + 1);
        } else if (button.equals(PREVIOUS_PAGE)) {
            event.setCancelled(true);
            PageGUI.playSound(event);
            gui.open(player, index - 1);
        } else if (!gui.isStealingAllowed()) {
            event.setCancelled(true);
            PageGUI.playSound(event);
        }
    }

}
