package ru.leonidm.millida.rating.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.entity.RatingPlayer;
import ru.leonidm.millida.rating.api.gui.Gui;
import ru.leonidm.millida.rating.api.repository.RatingPlayerRepository;
import ru.leonidm.millida.rating.config.api.GuiConfig;
import ru.leonidm.millida.rating.config.api.Rewards;

public class RewardsGui implements Gui {

    private final RatingPlayerRepository repository;
    private final Rewards rewards;
    private final GuiConfig guiConfig;

    public RewardsGui(@NotNull RatingPlayerRepository repository, @NotNull Rewards rewards,
                      @NotNull GuiConfig guiConfig) {
        this.repository = repository;
        this.rewards = rewards;
        this.guiConfig = guiConfig;
    }

    @Override
    public void openInventory(@NotNull Player player) {
        if (!guiConfig.isEnabled()) {
            return;
        }

        RewardsGuiHolder holder = new RewardsGuiHolder(guiConfig);
        Inventory inventory = Bukkit.createInventory(holder, 54, guiConfig.getTitle());
        holder.setInventory(inventory);

        RatingPlayer ratingPlayer = repository.findRatingPlayer(player.getUniqueId());
        int streak = ratingPlayer != null ? ratingPlayer.getStreak() : 0;

        for (int i = 0; i < 54; i++) {
            ItemStack itemStack = guiConfig.getCustomIcon(i);
            if (itemStack != null) {
                inventory.setItem(i, itemStack);
            }
        }

        for (int i = 0; i < 28; i++) {
            int day = i + 1;
            ItemStack itemStack = guiConfig.getRewardIcon(day);
            if (i < streak) {
                itemStack.setType(guiConfig.getCompletedIcon(day));
            }

            int x = i % 7 + 1;
            int y = i / 7 + 1;

            inventory.setItem(x + y * 9, itemStack);
        }

        player.openInventory(inventory);
    }

}
