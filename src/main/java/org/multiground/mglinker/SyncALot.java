package org.multiground.mglinker;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class SyncALot{
    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)([§&])[0-9A-FK-OR]");

    private final ProxyServer server;

    public SyncALot(ProxyServer server){
        this.server = server;
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onChat(PlayerChatEvent event){
        String content = event.getMessage();
        String sendAt = "";
        for(RegisteredServer c: server.getAllServers()){
            Optional<ServerConnection> conn = event.getPlayer().getCurrentServer();
            if(conn.isPresent() && Objects.equals(conn.get().getServerInfo().getName(),
                    c.getServerInfo().getName())) {
                sendAt = c.getServerInfo().getName();
            }
        }
        event.setResult(PlayerChatEvent.ChatResult.denied());
        content = COLOR_PATTERN.matcher(content).replaceAll("");
        String toSend = "["+sendAt+"] " + "<" + event.getPlayer().getGameProfile().getName() + "> " + content;
        for(Player p: server.getAllPlayers()){
            p.sendMessage(Component.text(toSend));
        }
    }
}
