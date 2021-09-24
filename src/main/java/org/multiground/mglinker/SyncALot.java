package org.multiground.mglinker;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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

    @Subscribe
    public void onJoin(ServerConnectedEvent event){
        for(Player p: server.getAllPlayers()){
            p.getTabList().removeEntry(event.getPlayer().getGameProfile().getId());

            p.getTabList().addEntry(TabListEntry.builder()
                            .displayName(Component.text(event.getPlayer().getUsername()))
                            .profile(event.getPlayer().getGameProfile())
                            .gameMode(0)
                            .tabList(p.getTabList())
                            .latency(((int) event.getPlayer().getPing()))
                    .build());
        }
    }

    @Subscribe
    public void onChat(PlayerChatEvent event){
        System.out.println(event.getMessage());
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
        String toSend = String.format("[%s]-%s", sendAt, prefix.toArray()[0]);
        for(Player p: server.getAllPlayers()){
            p.sendMessage(Component.text(toSend).append(Component.text(String.format("%s > %s",
                    event.getPlayer().getUsername(), content)).color(TextColor.color(255,255,255))));
        }
    }
}