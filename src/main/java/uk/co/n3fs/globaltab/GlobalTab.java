package uk.co.n3fs.globaltab;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Plugin(id = "globaltab-n3fs", name = "GlobalTabN3FS", version = "2.0", description = "A tab list plugin for Velocity", authors = {"Aang23", "md678685"})
public class GlobalTab {

    public static Map<String, Double> playerBalances = new HashMap<String, Double>();
    ProxyServer server;
    ConfigManager configManager;
    LuckPerms lpApi;
    private Logger logger;
    private GlobalTabScheduler scheduler;

    @Inject
    public GlobalTab(ProxyServer server, Logger logger, @DataDirectory Path configDir) {
        this.server = server;
        this.logger = logger;

        this.configManager = new ConfigManager(configDir, logger);
        this.scheduler = new GlobalTabScheduler(this);

        logger.info("Loading GlobalTab...");

        configManager.load();
        server.getCommandManager().register(new GlobalTabCommand(this), "globaltab");

        logger.info("GlobalTab loaded.");
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        server.getEventManager().register(this, new GlobalTabListener(this));
        server.getChannelRegistrar().register(new LegacyChannelIdentifier("GlobalTab"));
        scheduler.start();

        if (server.getPluginManager().isLoaded("luckperms")) {
            lpApi = LuckPermsProvider.get();
        }
    }

    void reload() {
        logger.info("Reloading GlobalTab...");
        configManager.load();
        scheduler.restart();
        logger.info("GlobalTab reloaded.");
    }

    ConfigManager.Settings getSettings() {
        return configManager.getSettings();
    }

}
