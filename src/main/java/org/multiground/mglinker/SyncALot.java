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
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
        String beforeParse = String.format("[%s]-%s%s > %s", sendAt, prefix.toArray()[0]
                , event.getPlayer().getUsername(), content);
        //split by color pattern.
        String[] afterParse = (String[]) COLOR_PATTERN.matcher(beforeParse).results().toArray();
        Boolean isStyle = false;
        String writecolor="`";
        List<String> writeStyle = null;
        Component result = Component.text("");
        String[] stylings = {"k", "l", "m", "n", "o", "r"};
        for (String s : afterParse) {
            if (!writecolor.equals("`")){
                Component cmp = Component.text(s).color(TextColor.color(colorParser(writecolor.substring(1))));
                if(isStyle){
                    for(String st: writeStyle) {
                        cmp.style().merge(typeParser(st));
                    }
                }
                result.append(cmp);
            }
            if (s.matches(COLOR_PATTERN.pattern())) {
                for(String s1: stylings){
                    if(s.endsWith(s1)){
                        isStyle = true;
                        if(s.endsWith("r")) {
                            Objects.requireNonNull(writeStyle).clear();
                            writecolor = "`";
                            break;
                        }
                        else writeStyle.add(s);
                        break;
                    }
                }
                if(!isStyle) writecolor = s;
            }
        }
        for(Player p: server.getAllPlayers()){
            p.sendMessage(result);
        }
    }


    private Style typeParser(String styleCode){
        switch (styleCode) {
            case "&k" -> {
                return Style.style(TextDecoration.OBFUSCATED);
            }
            case "&l" -> {
                return Style.style(TextDecoration.BOLD);
            }
            case "&m" -> {
                return Style.style(TextDecoration.STRIKETHROUGH);
            }
            case "&n" -> {
                return Style.style(TextDecoration.UNDERLINED);
            }
            case "&o" -> {
                return Style.style(TextDecoration.ITALIC);
            }
            default -> {
                return Style.empty();
            }
        }
    }

    private TextColor colorParser(String colorCode){
        if(colorCode == "&0"){
            return TextColor.color(0, 0, 0);
        }
        else if(colorCode == "&1"){
            return TextColor.color(0, 0, 170);
        }
        else if(colorCode == "&2"){
            return TextColor.color(0, 170, 0);
        }
        else if(colorCode == "&3"){
            return TextColor.color(0, 170, 170);
        }
        else if(colorCode == "&4"){
            return TextColor.color(170, 0, 0);
        }
        else if(colorCode == "&5"){
            return TextColor.color(170, 0, 170);
        }
        else if(colorCode == "&6"){
            return TextColor.color(255, 170, 0);
        }
        else if(colorCode == "&7"){
            return TextColor.color(170, 170, 170);
        }
        else if(colorCode == "&8"){
            return TextColor.color(85, 85, 85);
        }
        else if(colorCode == "&9"){
            return TextColor.color(85, 85, 255);
        }
        else if(colorCode == "&a"){
            return TextColor.color(85, 255, 85);
        }
        else if(colorCode == "&b"){
            return TextColor.color(85, 255, 255);
        }
        else if(colorCode == "&c"){
            return TextColor.color(255, 85, 85);
        }
        else if(colorCode =="&d"){
            return TextColor.color(255, 85, 255);
        }
        else if(colorCode == "&e"){
            return TextColor.color(255,255,85);
        }
        else return TextColor.color(255, 255, 255);
    }
}