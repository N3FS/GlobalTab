package uk.co.n3fs.globaltab;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;

public class TabEntryFormatter {

    private final ProxyServer server;

    public TabEntryFormatter(GlobalTab plugin) {
        this.server = plugin.server;
    }

    public TextComponent formatPlayerTab(String raw, Player player) {

        raw = raw.replace("%username%", player.getUsername());
        raw = raw.replace("%prefix%", getPrefixFromUsername(player.getUsername()));
        raw = raw.replace("%suffix%", getSuffixFromUsername(player.getUsername()));
        raw = raw.replace("%server%", getCurrentServer(player));

        return LegacyComponentSerializer.legacy().deserialize(raw, '&');
    }

    public TextComponent formatCustomTab(String raw, Player player) {

        raw = raw.replace("%username%", player.getUsername());
        raw = raw.replace("%prefix%", getPrefixFromUsername(player.getUsername()));
        raw = raw.replace("%suffix%", getSuffixFromUsername(player.getUsername()));
        raw = raw.replace("%server%", getCurrentServer(player));
        raw = raw.replace("%ping%", String.valueOf(player.getPing()));
        raw = raw.replace("%playercount%", String.valueOf(server.getPlayerCount()));
        raw = raw.replace("%localplayercount%", getServerPlayerCount(player));
        raw = raw.replace("%totalmaxplayer%", String.valueOf(server.getConfiguration().getShowMaxPlayers()));
        raw = raw.replace("%motd%", server.getConfiguration().getMotdComponent().toString());
        raw = raw.replace("%uuid%", player.getUniqueId().toString());
        raw = raw.replace("%ip%", player.getRemoteAddress().toString());
        raw = raw.replace("%balance%", getBalance(player));

        return LegacyComponentSerializer.legacy().deserialize(raw, '&');
    }

    private String getCurrentServer(Player player) {
        if (player.getCurrentServer().isPresent())
            return player.getCurrentServer().get().getServerInfo().getName();
        else
            return "null";
    }

    private String getBalance(Player player) {
        if (GlobalTab.playerBalances.containsKey(player.getUsername()))
            return String.valueOf(GlobalTab.playerBalances.get(player.getUsername()));
        else
            return "null";
    }

    private String getServerPlayerCount(Player player) {
        RegisteredServer server = null;

        if (player.getCurrentServer().isPresent())
            return String.valueOf(player.getCurrentServer().get().getServer().getPlayersConnected().size());
        else
            return "null";
    }

    private String getPrefixFromUsername(String username) {
        if (isLPAvailable()) {
            User lpUser = LuckPermsProvider.get().getUserManager().getUser(username);
            if (lpUser != null) {
                return lpUser.getCachedData().getMetaData(QueryOptions.defaultContextualOptions()).getPrefix();
            }
        }
        return "";
    }

    private String getSuffixFromUsername(String username) {
        if (isLPAvailable()) {
            User lpUser = LuckPermsProvider.get().getUserManager().getUser(username);
            if (lpUser != null) {
                return lpUser.getCachedData().getMetaData(QueryOptions.defaultContextualOptions()).getSuffix();
            }
        }
        return "";
    }

    private boolean isLPAvailable() {
        return server.getPluginManager().isLoaded("luckperms");
    }
}