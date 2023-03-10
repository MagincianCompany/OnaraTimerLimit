package magincian.discordBot;

import magincian.main.OnaraTimeLimit;
import magincian.manager.MessageOut;
import magincian.manager.ServerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.security.auth.login.LoginException;
import java.util.UUID;

public abstract class DiscordBot {

    private static JDA bot;
    private static OnaraTimeLimit plugin;
    public static void inizialize(OnaraTimeLimit pplugin) throws LoginException {

        plugin= pplugin;
        bot = JDABuilder.createDefault(plugin.getConfig().getString("discord.token"))
                .build();

    }


    public static void changeActivity(Activity.ActivityType type,String message)
    {
        Activity a = Activity.of(type,message,"https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        bot.getPresence().setActivity(a);
    }

    public static void shutdown()
    {
        bot.shutdown();
    }

    public static void sendMessage(String message)
    {
        Guild g = bot.getGuildById(plugin.getConfig().getLong("discord.serverId"));
        TextChannel c = bot.getTextChannelById(plugin.getConfig().getLong("discord.channelId"));
        c.sendMessage(
                ServerManager.parseMessageString(
                        "```ansi\n"+
                        message+
                        "```",MessageOut.DISCORD)
        ).queue();

    }

    public static void sendMessageContexted(String message, UUID uuid)
    {
        Guild g = bot.getGuildById(plugin.getConfig().getLong("discord.serverId"));
        TextChannel c = bot.getTextChannelById(plugin.getConfig().getLong("discord.channelId"));
        c.sendMessage(
                ServerManager.parseMessageStringContexted(
                        "```ansi\n"+
                        message+
                        "```"
                        ,uuid, MessageOut.DISCORD)
        ).queue();

    }



}
