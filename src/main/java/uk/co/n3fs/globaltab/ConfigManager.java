package uk.co.n3fs.globaltab;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.velocitypowered.api.proxy.ServerConnection;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConfigManager {

    private final Path configPath;
    private final Logger logger;
    private final ConfigurationLoader<CommentedConfigurationNode> loader;

    private Settings settings = null;

    ConfigManager(Path configDir, Logger logger) {
        this.configPath = configDir.resolve("globaltab.conf");
        this.logger = logger;
        this.loader = HoconConfigurationLoader.builder()
                .setPath(configPath)
                .build();
    }

    void load() {
        Settings oldSettings = settings;
        settings = null;

        boolean configExists = Files.exists(configPath);
        if (!configExists) {
            try {
                generateDefaultConfig();
                configExists = true;
            } catch (IOException e) {
                logger.error("Failed to generate the default config file.", e);
            }
        }

        if (configExists) {
            try {
                settings = loader.load().getValue(TypeToken.of(Settings.class));
            } catch (ObjectMappingException e) {
                logger.error("Config file is not valid.", e);
            } catch (IOException e) {
                logger.error("Failed to read the config file from disk.", e);
            }
        }

        if (settings == null) {
            if (oldSettings == null) {
                logger.warn("Using the built-in default settings, which is probably not ideal.");
                settings = new Settings();
            } else {
                logger.warn("Using the previously-loaded settings. These may be lost after a reboot!");
                settings = oldSettings;
            }
        }
    }

    private void generateDefaultConfig() throws IOException {
        try {
            if (!Files.exists(configPath.getParent())) {
                Files.createDirectory(configPath.getParent());
            }

            ConfigurationNode node = loader.createEmptyNode()
                    .setComment("The configuration for GlobalTab. After saving, run `/globaltab reload` for the new config to take effect.")
                    .setValue(TypeToken.of(Settings.class), new Settings());
            loader.save(node);
        } catch (ObjectMappingException e) {
            logger.error("Failed to save the default config due to an internal error.");
        }
    }

    public Settings getSettings() {
        return settings;
    }

    @ConfigSerializable
    public static class Settings {

        @Setting(comment = "The format for the tab list header.")
        private String header = "&4Welcome, &6%username%&4!";

        @Setting(comment = "The format for the tab list footer.")
        private String footer = "&4You're playing on the &6Super Cool Server Network&4!";

        @Setting(value = "update-delay", comment = "The interval between tab list refreshes, in seconds.")
        private int updateDelay = 1;

        @Setting(value = "player-format", comment = "The format of each player's entry on the tab list.")
        private String playerFormat = "%prefix% %username%";

        @Setting(value = "custom-tab-entries", comment = "A list of custom entries to add to the tab list. Leave blank to disable.")
        private List<String> customTabEntries = Lists.newArrayList("&3Your ping: &e%ping%", "&3Current server: &e%server%", "&3Balance : &e%balance%");

        @Setting(value = "disabled-servers", comment = "Players on these servers will see the original tab list rather than the custom tab list.")
        private List<String> disabledServers = Lists.newArrayList();

        public String getHeader() {
            return header;
        }

        public String getFooter() {
            return footer;
        }

        public int getUpdateDelay() {
            return updateDelay;
        }

        public String getPlayerFormat() {
            return playerFormat;
        }

        public List<String> getCustomTabEntries() {
            return customTabEntries;
        }

        public boolean isServerEnabled(ServerConnection server) {
            return !disabledServers.contains(server.getServerInfo().getName());
        }
    }
}