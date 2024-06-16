package ru.leonidm.millida.rating.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.service.GuiService;

public class RewardsCommand implements CommandExecutor {

    private final GuiService guiService;

    public RewardsCommand(@NotNull GuiService guiService) {
        this.guiService = guiService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String @NotNull [] args) {
        if (sender instanceof Player) {
            guiService.getRewardsGui().openInventory((Player) sender);
        }

        return true;
    }
}
