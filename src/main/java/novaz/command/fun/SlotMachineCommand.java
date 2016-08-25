package novaz.command.fun;

import novaz.core.AbstractCommand;
import novaz.games.SlotMachine;
import novaz.games.slotmachine.Slot;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;

import java.util.TimerTask;

/**
 * Created on 23-8-2016
 */
public class SlotMachineCommand extends AbstractCommand {

	public final long SPIN_INTERVAL = 2000L;

	public SlotMachineCommand(NovaBot bot) {
		super(bot);
	}


	@Override
	public String getDescription() {
		return "Feeling lucky? try the slotmachine! You might just win a hand full of air!";
	}

	@Override
	public String getCommand() {
		return "slot";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"slot      //displays info and payout table",
				"slot play //plays the game",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		if (args.length >= 1 && args[0].equals("play")) {
			final SlotMachine slotMachine = new SlotMachine();
			final IMessage msg = bot.sendMessage(channel, slotMachine.toString());
			bot.timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						if (slotMachine.gameInProgress()) {
							slotMachine.spin();
							msg.edit(slotMachine.toString());
						} else {
							Slot slot = slotMachine.winSlot();
							String gameresult = "";
							if (slot != null) {
								gameresult = "You rolled 3 **" + slot.getName() + "** and won **" + slot.getTriplePayout() + "**";
							} else {
								gameresult = "Aw you lose, better luck next time!";
							}
							msg.edit(slotMachine.toString() + Config.EOL + gameresult);
							this.cancel();
						}
					} catch (DiscordException e) {
						if (!e.getErrorMessage().contains("502")) {
							bot.sendErrorToMe(e, "blackjackgame", author.getID());
						}
					} catch (Exception e) {
						bot.sendErrorToMe(e, "blackjackgame", author.getID());
						this.cancel();
					}
				}
			}, 1000L, SPIN_INTERVAL);
		} else {
			String ret = "The slotmachine!" + Config.EOL;
			ret += "payout is as follows: " + Config.EOL;
			for (Slot s : Slot.values()) {
				ret += String.format("%1$s %1$s %1$s = %2$s" + Config.EOL, s.getEmote(), s.getTriplePayout());
			}
			ret += "type **slot play** to give it a shot!";
			return ret;
		}
		return "";
	}
}