package magincian.listeners;

import magincian.discordBot.DiscordBot;
import magincian.main.OnaraTimeLimit;
import magincian.manager.ServerManager;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class PlayerListener implements Listener {

    private OnaraTimeLimit plugin;


    public PlayerListener(OnaraTimeLimit plugin){
        this.plugin=plugin;
    }

    @EventHandler
    public void onJoin(PlayerLoginEvent event)
    {
        System.out.println("New Player");
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if(!plugin.currentSecondsPerPlayerID.containsKey(uuid) && Bukkit.getPlayer(uuid).hasPermission("otl.nolimit"))
            plugin.currentSecondsPerPlayerID.put(uuid,0L);
        DiscordBot.changeActivity(Activity.ActivityType.PLAYING,
                ServerManager.parseMessageString("%playercount@1% Jugador(es)")
                );
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        System.out.println(event.getQuitMessage());


        DiscordBot.changeActivity(Activity.ActivityType.PLAYING,
                ServerManager.parseMessageString("%playercount@-1% Jugador(es)")
        );

    }

    public static void KickPlayerByTime(Player player,OnaraTimeLimit plugin)
    {
        String m = ServerManager.parseMessageStringContexted(
                ServerManager.randomKickMessage(plugin),player.getUniqueId());
        DiscordBot.sendMessageContexted(
                "```ansi\n" +
                "\u001b[1;31m %player% ha sido expulsado por límite de tiempo\n"+
                "```"
                ,player.getUniqueId());



        player.kickPlayer(m);

    }
}
