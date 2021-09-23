package org.multiground.mglinker;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SyncALot{
    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)([§&])[0-9A-FK-OR]");

    private final ProxyServer server;
    private final LuckPerms luckPerms;
    public SyncALot(ProxyServer server){
        this.server = server;
        this.luckPerms = LuckPermsProvider.get();
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
        String playerGroup = luckPerms
                .getPlayerAdapter(Player.class)
                .getUser(event.getPlayer())
                .getPrimaryGroup();

        Set<String> prefix = Set.of("");

        if(!playerGroup.equals("default")){
            prefix = luckPerms
                    .getGroupManager()
                    .getGroup(playerGroup).getNodes(NodeType.PREFIX).stream()
                    .filter(NodeType.PREFIX::matches)
                    .map(NodeType.PREFIX::cast)
                    .map(PrefixNode::getMetaValue)
                    .collect(Collectors.toSet());
        }
        String toSend = String.format("[%s]-(%s%s) %s", sendAt, prefix, event.getPlayer().getUsername(), content.indexOf(0));
        for(Player p: server.getAllPlayers()){
            p.sendMessage(Component.text(toSend));
        }
    }
}
