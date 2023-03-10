package magincian.main;

import magincian.commands.CurrentTimeCommand;
import magincian.discordBot.DiscordBot;
import magincian.listeners.PlayerListener;
import magincian.manager.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.util.*;

public final class OnaraTimeLimit extends JavaPlugin {

    public static Map<UUID, Long> currentSecondsPerPlayerID;
    private PlayerListener playerListener;
    private ServerManager serverManager;

    public List<String> kickMessage;
    public Map<UUID, Long> currentSecondsPerPlayerID()
    {
        return OnaraTimeLimit.currentSecondsPerPlayerID;
    }

    @Override
    public void onEnable() {

        try {
            DiscordBot.inizialize(this);
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }

        saveDefaultConfig();
        getConfig();
        currentSecondsPerPlayerID = new HashMap<>();
        playerListener = new PlayerListener(this);
        serverManager = new ServerManager(this,
                getConfig().getInt("limiter.timeRun"),
                getConfig().getLong("limiter.permitedSeconds"),
                getConfig().getLong("limiter.resetDeltaSeconds"));

        kickMessage = getConfig().getStringList("phrase.kickByTime");

        System.out.println(getConfig().getInt("limiter.timeRun"));
        Bukkit.getPluginManager().registerEvents(playerListener,this);
         this.getCommand("ct").setExecutor(new CurrentTimeCommand());
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        DiscordBot.shutdown();
    }
}
