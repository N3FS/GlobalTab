package uk.co.n3fs.globaltab;

import com.google.common.io.ByteArrayDataInput;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;

public class GlobalTabListener {

    private final GlobalTab plugin;

    public GlobalTabListener(GlobalTab plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onReload(ProxyReloadEvent event) {
        plugin.reload();
    }

    @Subscribe
    public void onLeave(DisconnectEvent event) {
        if (plugin.server.getPlayerCount() > 0) {
            for (int i = 0; i < plugin.server.getPlayerCount(); i++) {
                Player currentPlayerToProcess = (Player) plugin.server.getAllPlayers().toArray()[i];
                currentPlayerToProcess.getTabList().removeEntry(event.getPlayer().getUniqueId());
            }
        }
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(new LegacyChannelIdentifier("GlobalTab"))) {
            return;
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }

        ByteArrayDataInput in = event.dataAsDataStream();
        String subChannel = in.readUTF();

        if (subChannel.equals("Balance")) {
            String packet[] = in.readUTF().split(":");
            String username = packet[0];
            Double balance = Double.parseDouble(packet[1]);
            if (GlobalTab.playerBalances.containsKey(username)) {
                GlobalTab.playerBalances.replace(username, balance);
            } else {
                GlobalTab.playerBalances.put(username, balance);
            }
        }
    }

}
