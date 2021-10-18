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
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.transformation.TransformationType;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;

import java.util.*;
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

        MiniMessage mm = MiniMessage.builder()
                .transformation(TransformationType.COLOR)
                .transformation(TransformationType.DECORATION)
                .transformation(TransformationType.RESET)
                .transformation(TransformationType.GRADIENT)
                .build();
        for(Player p: server.getAllPlayers()){
            //get prefix if group is not default.
            String disname;
            Set<String> prefix = Set.of("");
            String playerGroup = luckPerms
                    .getPlayerAdapter(Player.class)
                    .getUser(event.getPlayer())
                    .getPrimaryGroup();
            if(!playerGroup.equals("default")){
                prefix = luckPerms
                        .getGroupManager()
                        .getGroup(playerGroup).getNodes(NodeType.PREFIX).stream()
                        .filter(NodeType.PREFIX::matches)
                        .map(NodeType.PREFIX::cast)
                        .map(PrefixNode::getMetaValue)
                        .collect(Collectors.toSet());
            }

            p.getTabList().removeEntry(event.getPlayer().getGameProfile().getId());
            p.getTabList().addEntry(TabListEntry.builder()
                            .displayName( mm.parse(String.format("[%s][%s] %s", event.getServer().getServerInfo().getName(),prefix,event.getPlayer().getUsername())).asComponent())
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

        //Get all servers registered in proxy.
        for(RegisteredServer c: server.getAllServers()){
            //Get event player's current server.
            Optional<ServerConnection> conn = event.getPlayer().getCurrentServer();
            if(conn.isPresent() && Objects.equals(conn.get().getServerInfo().getName(),
                    c.getServerInfo().getName())) {
                sendAt = c.getServerInfo().getName();
            }
        }

        //Blocks current event.
        event.setResult(PlayerChatEvent.ChatResult.denied());

        String playerGroup = luckPerms
                .getPlayerAdapter(Player.class)
                .getUser(event.getPlayer())
                .getPrimaryGroup();

        Set<String> prefix = Set.of("");

        //get prefix if group is not default.
        if(!playerGroup.equals("default")){
            prefix = luckPerms
                    .getGroupManager()
                    .getGroup(playerGroup).getNodes(NodeType.PREFIX).stream()
                    .filter(NodeType.PREFIX::matches)
                    .map(NodeType.PREFIX::cast)
                    .map(PrefixNode::getMetaValue)
                    .collect(Collectors.toSet());
        }
        String toSend = String.format("[%s]-%s<reset>%s > %s", sendAt, prefix.toArray()[0]
                , event.getPlayer().getUsername(), content);

        MiniMessage mm = MiniMessage.builder()
                .transformation(TransformationType.COLOR)
                .transformation(TransformationType.DECORATION)
                .transformation(TransformationType.RESET)
                .transformation(TransformationType.GRADIENT)
                .build();

        for(Player p: server.getAllPlayers()){
            p.sendMessage(mm.parse(toSend).asComponent());
        }
    }
}
