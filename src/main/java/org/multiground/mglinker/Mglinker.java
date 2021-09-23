package org.multiground.mglinker;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(
        id = "@ID@",
        name = "@NAME@",
        version = "@VERSION@",
        authors = {"GiftShower_"}
)
public class Mglinker {
    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public Mglinker(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        logger.info("Initializing complete!");
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event){
        server.getEventManager().register(this, new SyncALot(server));
    }
}
