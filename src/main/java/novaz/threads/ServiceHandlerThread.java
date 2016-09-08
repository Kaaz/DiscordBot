package novaz.threads;

import novaz.main.Launcher;
import novaz.main.NovaBot;

public class ServiceHandlerThread extends Thread {
	NovaBot bot;

	public ServiceHandlerThread(NovaBot bot) {
		super("ServiceHandler");
		this.bot = bot;
	}
	private void collectServices(){
	}

	@Override
	public void run() {
		long lastTime = System.nanoTime();
		while (!Launcher.killAllThreads) {
			try {
				try {
					if (bot != null) {
						System.out.println("BOT TICK BEEP BOOP");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				lastTime = System.nanoTime();
				sleep(600_000L);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
