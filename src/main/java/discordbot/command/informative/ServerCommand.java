package discordbot.command.informative;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mashape.unirest.http.Unirest;
import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.main.Launcher;
import discordbot.permission.SimpleRank;
import discordbot.util.DisUtil;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerCommand extends AbstractCommand {
	public ServerCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Information about the server";
	}

	@Override
	public String getCommand() {
		return "server";
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PUBLIC;
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		Guild guild = ((TextChannel) channel).getGuild();
		TextChannel defaultTxt = guild.getPublicChannel();
		if (!PermissionUtil.checkPermission((TextChannel) channel, guild.getSelfMember(), Permission.MESSAGE_EMBED_LINKS)) {
			return Template.get("permission_missing_embed");
		}
		if (bot.security.getSimpleRank(author, channel).isAtLeast(SimpleRank.BOT_ADMIN) && args.length > 0 && DisUtil.matchesGuildSearch(args[0])) {
			guild = DisUtil.findGuildBy(args[0], bot.getContainer());
			if (guild == null) {
				return Template.get("command_config_cant_find_guild");
			}
		}
		EmbedBuilder b = new EmbedBuilder();
		b.setAuthor(guild.getName(), guild.getIconUrl(), guild.getIconUrl());
		b.setThumbnail(guild.getIconUrl());

		b.setDescription(
				"Discord-id `" + guild.getId() + "`" + Config.EOL +
						"On shard `" + bot.getShardId() + "`" + Config.EOL +
						(PermissionUtil.checkPermission(guild, guild.getSelfMember(), Permission.ADMINISTRATOR)
								? Emojibet.POLICE + " Administrator" : "")
		);
		ImmutableSet<OnlineStatus> onlineStatus = Sets.immutableEnumSet(OnlineStatus.ONLINE, OnlineStatus.IDLE, OnlineStatus.DO_NOT_DISTURB);
		long online = guild.getMembers().stream().filter(member -> onlineStatus.contains(member.getOnlineStatus())).count();
		b.setColor(getAverageColor(guild.getIconUrl()));
		b.addField("Members", String.format("%s online\n%s in total", online, guild.getMembers().size()), true);
		b.addField("Channels", String.format("%s text channels\n%s voice channels", guild.getTextChannels().size(), guild.getVoiceChannels().size()), true);
		b.addField("Default channel", defaultTxt.getAsMention(), true);
		b.addField("Created by", String.format("%s\\#%s", guild.getOwner().getUser().getName(), guild.getOwner().getUser().getDiscriminator()), true);
		b.addField("My prefix", String.format("`%s`", DisUtil.getCommandPrefix(guild)), true);
		b.addField("Created On", new SimpleDateFormat("dd MMMM yyyy").format(new Date(guild.getCreationTime().toInstant().toEpochMilli())), true);
		b.setFooter(guild.getSelfMember().getEffectiveName(), channel.getJDA().getSelfUser().getAvatarUrl());
		channel.sendMessage(b.build()).queue();
		return "";
	}

	private Color getAverageColor(String url) {
		if (url == null) {
			return new Color(27, 137, 255);
		}
		try {
			BufferedImage img = ImageIO.read(Unirest.get(url).asBinary().getRawBody());
			int x0 = 0;
			int y0 = 0;
			int x1 = x0 + img.getWidth();
			int y1 = y0 + img.getHeight();
			long sumr = 0, sumg = 0, sumb = 0;
			for (int x = x0; x < x1; x++) {
				for (int y = y0; y < y1; y++) {
					Color pixel = new Color(img.getRGB(x, y));
					sumr += pixel.getRed();
					sumg += pixel.getGreen();
					sumb += pixel.getBlue();
				}
			}
			int num = img.getWidth() * img.getHeight();
			return new Color((int) sumr / num, (int) sumg / num, (int) sumb / num);
		} catch (Exception e) {
			Launcher.logToDiscord(e, "img-url", url);
			e.printStackTrace();
		}
		return new Color(27, 137, 255);
	}
}