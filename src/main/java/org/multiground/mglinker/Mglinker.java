package org.multiground.mglinker;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.leonhard.storage.Json;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "@ID@",
        name = "@NAME@",
        description = "Link",
        version = "@VERSION",
        authors = {"GiftShower_"},
        dependencies = {
            @Dependency(id = "luckperms")
        }
)
public class Mglinker {
    private final ProxyServer server;
    private final Logger logger;
    private final Path folder;

    @Inject
    public Mglinker(ProxyServer server, Logger logger, @DataDirectory Path folder) {
        this.server = server;
        this.logger = logger;
        this.folder = folder;
        Json config = new ConfHandler().loadConfig(folder);
        logger.info("Initializing complete!");
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event){
        server.getEventManager().register(this, new SyncALot(server));
    }
}
