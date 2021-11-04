package org.multiground.mglinker;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.leonhard.storage.Json;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Set;


@Plugin(id = "mglinker",
        name = "MGLinker",
        description = "Link",
        version = "1.0.1",
        authors = {"GiftShower_"},
        dependencies = {
            @Dependency(id = "luckperms")
        }
)
public class Mglinker {
    private final ProxyServer server;
    private final Logger logger;
    private final Path folder;
    private final Json config;

    Vertx vertx = Vertx.vertx();

    @Inject
    public Mglinker(ProxyServer server, Logger logger, @DataDirectory Path folder) {
        this.server = server;
        this.logger = logger;
        this.folder = folder;
        this.config = new CSfHandler().loadConfig(folder);
        logger.info("Initializing complete!");
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event){
        server.getEventManager().register(this, new SyncALot(server));
        server.getScheduler()
                .buildTask(this, () -> {
                    Verticle myVerticle = new TcpServer(server, config);
                    vertx.deployVerticle(myVerticle);
                }).schedule();
    }

    @Subscribe
    public void onStop(ProxyShutdownEvent event) {
        for(String id: vertx.deploymentIDs()){
            vertx.undeploy(id, res -> {
                if(res.succeeded()){
                    logger.info("Undeployment Successful.");
                } else {
                    logger.info("Failed");
                }
            });
        }

        vertx.close();
    }
}
