package org.pennacap.chatmod;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;


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
        saveDefaultConfig();

    }
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        if (getConfig().getBoolean("isLockedDown")&& !event.getPlayer().hasPermission("chatmod.lockdownIgnore")){
            event.setCancelled(true);
            event.getPlayer().sendMessage(getConfig().getString("lockdown-error-message"));
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
