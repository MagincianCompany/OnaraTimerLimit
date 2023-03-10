package magincian.manager;

import magincian.listeners.PlayerListener;
import magincian.main.OnaraTimeLimit;
import magincian.parsers.Parsers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ServerManager {

    private OnaraTimeLimit plugin;
    private int TIMERUN;
    public static Long PERMITEDSECONDS= 60L;
    public static Long RESETDELTASECONDS= 240L;
    public static LocalDateTime lastReset;
    public ServerManager(OnaraTimeLimit plugin,int TIMERUN,long PERMITEDSECONDS, long RESETDELTASECONDS)
    {

        this.TIMERUN=TIMERUN;
        this.PERMITEDSECONDS=PERMITEDSECONDS;
        this.RESETDELTASECONDS=RESETDELTASECONDS;

        this.plugin=plugin;
        loadPlayers();
        lastReset = LocalDateTime.now();

        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(plugin.getDataFolder().getPath()+"/data/" + "info.time")));
            lastReset= (LocalDateTime)in.readObject();
            System.out.println(lastReset);
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,() ->{

            for (Player p:Bukkit.getOnlinePlayers()) {
                if(!p.hasPermission("otl.nolimit"))
                    plugin.currentSecondsPerPlayerID.replace(p.getUniqueId(),plugin.currentSecondsPerPlayerID.get(p.getUniqueId())+TIMERUN);

            }
        },0,20*TIMERUN);//20 tiks = 1s

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,() ->{

            for (Player p:Bukkit.getOnlinePlayers()) {
                if(!p.hasPermission("otl.nolimit")) {

                    if (plugin.currentSecondsPerPlayerID.get(p.getUniqueId()) > PERMITEDSECONDS) {
                        PlayerListener.KickPlayerByTime(p,plugin);
                    } else if (plugin.currentSecondsPerPlayerID.get(p.getUniqueId()) > PERMITEDSECONDS - 10) {
                        Long r = PERMITEDSECONDS - plugin.currentSecondsPerPlayerID.get(p.getUniqueId()) + 1;
                        p.sendMessage(r.toString());
                    }
                }
            }
        },0,20*TIMERUN);//20 tiks = 1s

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,() ->{
            if(ChronoUnit.SECONDS.between(lastReset,LocalDateTime.now())>=RESETDELTASECONDS)
            {System.out.println("reset time");
                for (UUID uuid:plugin.currentSecondsPerPlayerID().keySet()) {
                    plugin.currentSecondsPerPlayerID.replace(uuid,0L);

                }
                lastReset = LocalDateTime.now();
                try {
                    BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(plugin.getDataFolder().getPath()+"/data/" + "info.time")));
                    out.writeObject(lastReset);
                    out.close();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        },0,20*TIMERUN);//20 tiks = 1s

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,() ->{
            savePlayers();
        },0,20*10);//cada minuto se guarda
    }

    public void savePlayer(UUID uuid)
    {
        try {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(plugin.getDataFolder().getPath()+"/data/" + uuid.toString() +".data")));
            out.writeLong(plugin.currentSecondsPerPlayerID.get(uuid));
            out.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void savePlayers()
    {
        File f = new File(plugin.getDataFolder().getPath()+"/data");

        // check if the directory can be created
        // using the specified path name
        if (f.mkdir() == true) {
            System.out.println("data directory has been created successfully");
        }

        for (UUID uuid:plugin.currentSecondsPerPlayerID().keySet()) {
            savePlayer(uuid);
        }

    }

    public void loadPlayer(UUID uuid)
    {
        try {
            BukkitObjectInputStream out = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(plugin.getDataFolder().getPath()+"/data/"+uuid.toString()+".data")));
            plugin.currentSecondsPerPlayerID().put(uuid, out.readLong());
            out.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public void loadPlayers()
    {
        for (UUID uuid:plugin.currentSecondsPerPlayerID().keySet()) {
            savePlayer(uuid);
        }
        File directoryPath = new File(plugin.getDataFolder().getPath()+"/data");

        String contents[] = directoryPath.list();
        for (String s : contents)
        {
            if(s.contains(".data")) {
                System.out.println(s);
                loadPlayer(UUID.fromString(s.replace(".data", "")));
            }

        }
    }

    private static  String parseMessageStringSpecificDiscord(String s)
    {
        s = s.replace("%red%","§c");
        return  s;
    }
    private static  String parseMessageStringSpecificMinecraft(String s)
    {
        s = s.replace("%red%","§c");
        return  s;
    }
    public static String parseMessageString(String s,MessageOut out)
    {
        /*
            %t% -> tiempo restante para el reinicio en segundos
            %tm% -> tiempo restante para el reinicio en segundos
            %th% -> tiempo restante para el reinicio en horas
            %td% -> tiempo restante para el reinicio en dias
            %tdate% -> dia y hora del reinicio
         */

        if(out == MessageOut.MINECRAFT)
            s = parseMessageStringSpecificMinecraft(s);
        else
            s = parseMessageStringSpecificDiscord(s);

        Float seconds =(RESETDELTASECONDS-(Long)ChronoUnit.SECONDS.between(lastReset,LocalDateTime.now()))/1f;
        System.out.println(seconds);
        s = s.replace("%t%",seconds.toString());
        s = s.replace("%tm%",((Float)(seconds/60f)).toString());
        s = s.replace("%th%",((Float)(seconds/3600f)).toString());
        s = s.replace("%td%",((Float)(seconds/86400f)).toString());

        if(s.contains("%playercount@"))
        {
            String code = "%playercount@"+ s.split("%playercount@")[1].split("%")[0] + "%";
            String arg = s.split("%playercount@")[1].split("%")[0];
            Integer n = Integer.valueOf(arg);
            s= s.replace(code,((Integer)
                    (Bukkit.getOnlinePlayers().size()+n))
                    .toString());
        }
        else
            s= s.replace("%playercount%",((Integer)Bukkit.getOnlinePlayers().size()).toString());
        LocalDateTime ldt = LocalDateTime.now();


        s = s.replace("%tdate%",
                Parsers.LocalDateTimeToString(ldt.plusSeconds(
                                ChronoUnit.SECONDS.between(
                                        lastReset,LocalDateTime.now())
                )));
        return s;
    }

    private String randomKickMessage()
    {
        Random rand = new Random();
        System.out.println(plugin.kickMessage.size());
        Integer r = rand.nextInt(plugin.kickMessage.size());
        return plugin.kickMessage.get(r);
    }
    public static String randomKickMessage(OnaraTimeLimit plugin)
    {

        Random rand = new Random();
        System.out.println(plugin.kickMessage.size());
        Integer r = rand.nextInt(plugin.kickMessage.size());
        return plugin.kickMessage.get(r);
    }
    public static String parseMessageStringContexted(String s, UUID uuid,MessageOut out)
    {
        /*
            %player% -> obtiene el nombre del jugador
        */

        s = parseMessageString(s,out);
        s = s.replace(
                "%player%",
                Bukkit.getPlayer(uuid).getName()
        );

        return s;
    }
}
