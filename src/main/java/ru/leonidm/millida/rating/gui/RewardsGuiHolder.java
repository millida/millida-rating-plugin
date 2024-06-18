package ru.leonidm.millida.rating.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.api.GuiConfig;

public final class RewardsGuiHolder implements InventoryHolder {

    private final GuiConfig guiConfig;
    private Inventory inventory;

    public RewardsGuiHolder(@NotNull GuiConfig guiConfig) {
        this.guiConfig = guiConfig;
    }

    @NotNull
    public GuiConfig getGuiConfig() {
        return guiConfig;
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        if (inventory == null) {
            throw new IllegalStateException("Not initialized yet");
        }

        return inventory;
    }

    public void setInventory(@NotNull Inventory inventory) {
        if (this.inventory != null) {
            throw new IllegalStateException("Already initialized");
        }

        this.inventory = inventory;
    }
}
