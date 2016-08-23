package novaz.command.fun;

import novaz.core.AbstractCommand;
import novaz.games.SlotMachine;
import novaz.main.Config;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

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
	public String execute(String[] args, IChannel channel, IUser author) {
		final SlotMachine slotMachine = new SlotMachine();
		final IMessage msg = bot.sendMessage(channel, "Slotmachine preparing!");
		bot.timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					if (slotMachine.gameInProgress()) {
						slotMachine.spin();
						msg.edit(slotMachine.toString());
					} else {
						msg.edit(slotMachine.toString() + Config.EOL + "Game done you %s %s");
						this.cancel();
					}
				} catch (Exception ignored) {
					this.cancel();
				}
			}
		}, 1000L, SPIN_INTERVAL);
		return "";
	}
}