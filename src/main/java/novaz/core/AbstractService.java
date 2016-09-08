package novaz.core;

import novaz.main.NovaBot;

/**
 * Created on 8-9-2016
 */
public abstract class AbstractService {
	protected NovaBot bot;

	public AbstractService(NovaBot b) {
		bot = b;
	}

	/**
	 * miliseconds it should wait befor eattempting anothe run
	 *
	 * @return delay in miliseconds
	 */
	public abstract long delayBetweenRuns();

	/**
	 * Determines if the service should run
	 *
	 * @return should it run?
	 */
	public abstract boolean shouldIRun();

	/**
	 * called before run, so things can be prepared if needed
	 */
	public abstract void beforeRun();

	/**
	 * the actual logic of the service
	 */
	public abstract void run();

	/**
	 * called after run(), can be used to clean up things if needed
	 */
	public abstract void afterRun();
}
