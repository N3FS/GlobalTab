package uk.co.n3fs.globaltab;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;

import java.util.*;

public class TabUpdater implements Runnable {

    private final GlobalTab plugin;
    private final ProxyServer server;
    private final TabEntryFormatter formatter;

    public TabUpdater(GlobalTab plugin) {
        this.plugin = plugin;
        this.server = plugin.server;
        formatter = new TabEntryFormatter(plugin);
    }

    // I have no idea why this is needed - it might not be needed at all?
    private static void insertEntry(TabList list, TabListEntry entry, List<UUID> toKeep) {
        UUID inUUID = entry.getProfile().getId();
        List<UUID> existingEntries = new ArrayList<UUID>();
        Map<UUID, TabListEntry> cache = new HashMap<UUID, TabListEntry>();
        for (TabListEntry current : list.getEntries()) {
            existingEntries.add(current.getProfile().getId());
            cache.put(current.getProfile().getId(), current);
        }
        if (!existingEntries.contains(inUUID)) {
            list.addEntry(entry);
            toKeep.add(inUUID);
        } else {
            TabListEntry currentEntr = cache.get(inUUID);
            if (!currentEntr.getDisplayName().equals(entry.getDisplayName())) {
                list.removeEntry(inUUID);
                list.addEntry(entry);
            }
            toKeep.add(inUUID);
        }
    }

    @Override
    public void run() {
        if (server.getPlayerCount() > 0) {
            for (Player currentPlayer : server.getAllPlayers()) {
                if (!currentPlayer.getCurrentServer().isPresent() || !plugin.getSettings().isServerEnabled(currentPlayer.getCurrentServer().get()))
                    return;

                List<UUID> toKeep = new ArrayList<>();

                for (Player onlinePlayer : server.getAllPlayers()) {
                    TabListEntry currentEntry = TabListEntry.builder().profile(onlinePlayer.getGameProfile())
                            .displayName(formatter.formatPlayerTab(plugin.getSettings().getPlayerFormat(), onlinePlayer))
                            .tabList(currentPlayer.getTabList()).build();

                    insertEntry(currentPlayer.getTabList(), currentEntry, toKeep);
                }

                if (plugin.getSettings().getCustomTabEntries().size() > 0) {
                    List<String> customEntries = plugin.getSettings().getCustomTabEntries();

                    for (String customEntry : customEntries) {
                        GameProfile tabProfile = GameProfile.forOfflinePlayer("customTab" + customEntries.indexOf(customEntry));

                        TabListEntry currentEntry = TabListEntry.builder().profile(tabProfile)
                                .displayName(formatter.formatCustomTab(customEntry, currentPlayer))
                                .tabList(currentPlayer.getTabList()).build();

                        insertEntry(currentPlayer.getTabList(), currentEntry, toKeep);
                    }
                }

                for (TabListEntry current : currentPlayer.getTabList().getEntries()) {
                    if (!toKeep.contains(current.getProfile().getId()))
                        currentPlayer.getTabList().removeEntry(current.getProfile().getId());
                }

                currentPlayer.getTabList().setHeaderAndFooter(
                        formatter.formatCustomTab(plugin.getSettings().getHeader(), currentPlayer),
                        formatter.formatCustomTab(plugin.getSettings().getFooter(), currentPlayer));
            }
        }
    }

}