package uk.co.n3fs.globaltab;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

public class GlobalTabCommand implements Command {

    private final GlobalTab plugin;

    public GlobalTabCommand(GlobalTab plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NonNull CommandSource source, String[] args) {
        if (!source.hasPermission("globaltab.command")) {
            source.sendMessage(TextComponent.of("You don't have access to this command.").color(TextColor.RED));
            return;
        }

        if (args.length > 0) {
            if (args[0].equals("reload")) {
                plugin.reload();
                source.sendMessage(TextComponent.of("Reloaded the plugin.").color(TextColor.LIGHT_PURPLE));
                return;
            }
        }

        source.sendMessage(TextComponent.of("Usage: /globaltab <reload>").color(TextColor.RED));
    }
}