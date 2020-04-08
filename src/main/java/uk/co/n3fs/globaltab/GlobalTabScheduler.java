package uk.co.n3fs.globaltab;

import com.velocitypowered.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public class GlobalTabScheduler {

    private final GlobalTab plugin;
    private ScheduledTask task = null;

    public GlobalTabScheduler(GlobalTab plugin) {
        this.plugin = plugin;
    }

    void start() {
        task = plugin.server.getScheduler().buildTask(plugin, new TabUpdater(plugin))
                .delay(plugin.getSettings().getUpdateDelay(), TimeUnit.SECONDS)
                .repeat(plugin.getSettings().getUpdateDelay(), TimeUnit.SECONDS)
                .schedule();
    }

    void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    void restart() {
        this.stop();
        this.start();
    }

}
