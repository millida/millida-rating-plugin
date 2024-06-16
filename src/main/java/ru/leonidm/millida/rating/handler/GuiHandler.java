package ru.leonidm.millida.rating.handler;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.v1.api.GuiConfig;
import ru.leonidm.millida.rating.gui.RewardsGuiHolder;

public final class GuiHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof RewardsGuiHolder) {
            event.setCancelled(true);

            GuiConfig config = ((RewardsGuiHolder) holder).getGuiConfig();

            Inventory clickedInventory = event.getClickedInventory();
            if (clickedInventory == inventory && event.getSlot() == config.getMessageSlot()) {
                HumanEntity whoClicked = event.getWhoClicked();
                whoClicked.sendMessage(config.getMessage());
                whoClicked.closeInventory();
            }
        }
    }
}
