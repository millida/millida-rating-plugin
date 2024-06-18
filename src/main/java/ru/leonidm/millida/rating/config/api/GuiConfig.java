package ru.leonidm.millida.rating.config.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface GuiConfig {

    boolean isEnabled();

    @NotNull
    String getCommand();

    @NotNull
    String getTitle();

    @NotNull
    ItemStack getRewardIcon(int day);

    @NotNull
    Material getCompletedIcon(int day);

    int getMessageSlot();

    @NotNull
    String getMessage();

    @Nullable
    ItemStack getCustomIcon(int slot);

}
