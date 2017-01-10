package discordbot.service;

import discordbot.core.AbstractService;
import discordbot.main.BotContainer;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import javax.security.auth.login.LoginException;
import java.util.concurrent.TimeUnit;

/**
 * Bot restart service, designed so another bot can restart this one if necessary.
 * @author nija123098
 */
public class BotRestartService extends AbstractService implements IListener<MessageReceivedEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
    private IDiscordClient client;
    public BotRestartService(BotContainer b) {
        super(b);
        try {
            client = new ClientBuilder().withToken(Config.RESTART_BOT_TOKEN).login();
            client.getDispatcher().registerListener(this);
        }catch (Exception e){
            LOGGER.error("Error starting Robot Restart Service!", e);
        }
    }
    @Override
    public String getIdentifier() {
        return "bot_restart_service";
    }
    @Override
    public long getDelayBetweenRuns() {
        return TimeUnit.HOURS.toMillis(1);
    }
    @Override
    public boolean shouldIRun() {
        return true;
    }
    @Override
    public void beforeRun() {

    }
    @Override
    public void run() throws Exception {
        RequestBuffer.request(() -> {
            try {
                this.client.logout();
            } catch (DiscordException e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(TimeUnit.SECONDS.toMillis(6));
        RequestBuffer.request(() -> {
            try {
                this.client.login();
            } catch (DiscordException e) {
                e.printStackTrace();
            }
        });
    }
    @Override
    public void afterRun() {

    }
    @Override
    public void handle(MessageReceivedEvent event) {
        if (!event.getMessage().getContent().equals("Emily restart please!")){
            return;
        }
        IUser user = event.getMessage().getAuthor();
        if (this.bot.getShardFor(event.getMessage().getGuild().getID()).security.getSimpleRank(new User() {
            @Override
            public String getName() {
                return user.getName();
            }

            @Override
            public String getDiscriminator() {
                return user.getDiscriminator();
            }

            @Override
            public String getAvatarId() {
                return user.getAvatar();
            }

            @Override
            public String getAvatarUrl() {
                return user.getAvatarURL();
            }

            @Override
            public String getDefaultAvatarId() {
                return user.getAvatar();
            }

            @Override
            public String getDefaultAvatarUrl() {
                return user.getAvatarURL();
            }

            @Override
            public String getEffectiveAvatarUrl() {
                return user.getAvatarURL();
            }

            @Override
            public boolean hasPrivateChannel() {
                return false;
            }

            @Override
            public RestAction<PrivateChannel> openPrivateChannel() {
                return new RestAction.EmptyRestAction<PrivateChannel>(this.getPrivateChannel());
            }

            @Override
            public PrivateChannel getPrivateChannel() {
                return null;
            }

            @Override
            public boolean isBot() {
                return user.isBot();
            }

            @Override
            public JDA getJDA() {
                return null;
            }

            @Override
            public boolean isFake() {
                return false;
            }

            @Override
            public String getAsMention() {
                return user.mention();
            }

            @Override
            public String getId() {
                return user.getID();
            }
        }).isAtLeast(SimpleRank.BOT_ADMIN)){
            DiscordBot[] discordBots = this.bot.getShards();
            for (int i = 0; i < discordBots.length; i++) {
                if (!discordBots[i].isReady()){
                    restart(i, false);
                }
            }
        }
    }
    private void restart(int shardId, boolean retry){
        try {
            this.bot.restartShard(shardId);
        } catch (InterruptedException | LoginException e) {
            if (retry){
                LOGGER.error("Error encountered during manual restart of bot!  Already attempted first restart!", e);
            }else{
                LOGGER.error("Error encountered during manual restart of bot!  Attempting one more time on shard " + shardId + ".", e);
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(6));
                } catch (InterruptedException ex) {
                    LOGGER.warn("Thread Exception while attempting to manually restart bot!", ex);
                }
                restart(shardId, true);
            }
        } catch (RateLimitedException e) {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(6));
                restart(shardId, false);
            } catch (InterruptedException ex) {
                LOGGER.warn("Thread Exception while attempting to manually restart bot!", ex);
            }
        }
    }
}
