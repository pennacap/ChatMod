package org.pennacap.chatmod;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


public final class ChatMod extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this,this);
        Bukkit.getPluginCommand("lockdown").setExecutor(
                (commandSender,command,string,strings)->{
                    if (!getConfig().getBoolean("isLockedDown")) {
                        if (getConfig().getBoolean("broadcast-lockdown")){
                            Bukkit.broadcastMessage(getConfig().getString("lockdown-broadcast"));
                        }
                        getConfig().set("isLockedDown",true);
                    }
                    commandSender.sendMessage(getConfig().getString("lockdown-message"));
                    return true;
                }
        );
        Bukkit.getPluginCommand("unlockdown").setExecutor(
                (commandSender,command,string,strings)->{
                    if (getConfig().getBoolean("isLockedDown")) {
                        if (getConfig().getBoolean("broadcast-lockdown")){
                            Bukkit.broadcastMessage(getConfig().getString("unlockdown-broadcast"));
                        }
                        getConfig().set("isLockedDown",false);
                    }
                    commandSender.sendMessage(getConfig().getString("unlockdown-message"));

                    return true;
                }
        );
        Bukkit.getPluginCommand("mute").setExecutor(
                ((sender, command, label, args) ->{
                    Player p;
                    if (args.length!=1){
                        sender.sendMessage(getConfig().getString("invalid-command-error").replace("{usage}",command.getUsage()));
                        return false;
                    }
                    if (Bukkit.getOnlinePlayers().stream().anyMatch(player->player.getName().equals(args[0])) ){
                        List<String> muted= getConfig().getStringList("muted");
                        muted.add(Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().equals(args[0])).collect(Collectors.toList()).get(0).getUniqueId().toString());
                        getConfig().set("muted",muted);
                        p = Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().equals(args[0])).collect(Collectors.toList()).get(0);


                    } else if ( Bukkit.getOnlinePlayers().stream().anyMatch(player->player.getUniqueId().toString().equals(args[0]))){
                        List<String> muted= getConfig().getStringList("muted");
                        muted.add(args[0]);
                        getConfig().set("muted",muted);
                        p = Bukkit.getOnlinePlayers().stream().filter(player -> player.getUniqueId().toString().equals(args[0])).collect(Collectors.toList()).get(0);

                    } else{
                        sender.sendMessage(getConfig().getString("invalid-person-error"));
                        return false;
                    }
                    sender.sendMessage(getConfig().getString("muted-message").replace("{user}",p.getName()));
                    if (getConfig().getBoolean("broadcast-mute")){
                        Bukkit.broadcastMessage(getConfig().getString("muted-broadcast").replace("{user}",p.getName()).replace("{moderator}",sender instanceof Player?((Player)sender).getName():"Console"));
                    }
                    return true;
                } )
        );
        Bukkit.getPluginCommand("unmute").setExecutor(
                ((sender, command, label, args) ->{
                    Player p;
                    if (args.length!=1){
                        sender.sendMessage(getConfig().getString("invalid-command-error").replace("{usage}",command.getUsage()));
                        return false;
                    }
                    if (Bukkit.getOnlinePlayers().stream().anyMatch(player->player.getName().equals(args[0])) ){

                        List<String> muted= getConfig().getStringList("muted");
                        muted.remove(Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().equals(args[0])).collect(Collectors.toList()).get(0).getUniqueId().toString());
                        getConfig().set("muted",muted);
                        p = Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().equals(args[0])).collect(Collectors.toList()).get(0);


                    } else if ( Bukkit.getOnlinePlayers().stream().anyMatch(player->player.getUniqueId().toString().equals(args[0]))){
                        List<String> muted= getConfig().getStringList("muted");
                        muted.remove(args[0]);
                        getConfig().set("muted",muted);
                        p = Bukkit.getOnlinePlayers().stream().filter(player -> player.getUniqueId().toString().equals(args[0])).collect(Collectors.toList()).get(0);

                    } else{
                        sender.sendMessage(getConfig().getString("invalid-person-error"));
                        return false;
                    }
                    sender.sendMessage(getConfig().getString("unmuted-message").replace("{user}",p.getName()));

                    return true;
                } )
        );
        saveDefaultConfig();
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.load(this.getTextResource("config.yml"));
            getConfig().setDefaults(config);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        if (getConfig().getBoolean("isLockedDown")&& !event.getPlayer().hasPermission("chatmod.lockdownIgnore")){
            event.setCancelled(true);
            event.getPlayer().sendMessage(getConfig().getString("lockdown-error-message"));
        }
        if (getConfig().getStringList("muted").contains(event.getPlayer().getUniqueId().toString())&&!event.getPlayer().hasPermission("chatmod.muteIgnore")){
            event.setCancelled(true);
            event.getPlayer().sendMessage(getConfig().getString("mute-error-message"));
        }
        if (event.getPlayer().hasPermission("chatmod.ignore")){
            return;
        }
        for (String word: getConfig().getStringList("blacklisted-words")){
            if (event.getMessage().toLowerCase().replace(" ","").replace("-","").contains(word.toLowerCase())){

                if (!getConfig().getBoolean("mask")){
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(getConfig().getString("blacklist-error-message"));
                    return;

                } else {

                    String message = event.getMessage();
                    int theIndex = 0;

                    while ((theIndex = message.toLowerCase().indexOf(word.toLowerCase().charAt(0),theIndex))!=-1){
                        int index = 0;

                        if (message.toLowerCase().substring(theIndex).replace("-","").replace(" ","").startsWith(word.toLowerCase())){

                            while (index!=word.length()){

                                if (message.charAt(theIndex)==word.toLowerCase()    .charAt(index)){
                                    index++;
                                }
                                StringBuilder buf = new StringBuilder(message);
                                buf.setCharAt(theIndex++,getConfig().getString("mask-char").charAt(0));
                                message = buf.toString();


                            }
                        } else{
                            theIndex++;
                        }
                    }
                    event.setMessage(message);

                }
            }
        }

    }

}
