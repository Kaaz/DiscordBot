package novaz.core;

import novaz.db.model.OServiceVariable;
import novaz.db.table.TServiceVariables;
import novaz.db.table.TServices;
import novaz.main.NovaBot;
import novaz.util.TimeUtil;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractService {
	protected NovaBot bot;
	private Map<String, OServiceVariable> cache;

	public AbstractService(NovaBot b) {
		bot = b;
		cache = new HashMap<>();
	}

	/**
	 * Start the service
	 */
	public final void start() {
		long lastRun = Long.parseLong("0" + getData("last_run"));
		long now = System.currentTimeMillis();
		long next = lastRun + getDelayBetweenRuns();
		if (next <= now) {
			if (!shouldIRun()) {
				System.out.println("maybe not, it appears that I shouldn't run");
				return;
			}
			beforeRun();
			run();
			afterRun();
			saveData("last_run", now);
		} else {
			System.out.println("I'm gonna run " + TimeUtil.getRelativeTime(next / 1000L, false));
		}
	}

	/**
	 * gets data for a certain key and caches it
	 *
	 * @param key key used
	 * @return the value of the key
	 */
	private String getData(String key) {
		return getDataObject(key).value;
	}

	/**
	 * gets data for a certain key and caches it
	 *
	 * @param key key used
	 * @return the database row object for
	 */
	private OServiceVariable getDataObject(String key) {
		if (!cache.containsKey(key)) {
			cache.put(key, TServiceVariables.findBy(getIdentifier(), key));
		}
		return cache.get(key);
	}


	/**
	 * saves service data
	 *
	 * @param key   the key
	 * @param value Any value converted to string
	 */
	public void saveData(String key, Object value) {
		OServiceVariable dataObject = getDataObject(key);
		dataObject.variable = key;
		dataObject.serviceId = TServices.getCachedId(getIdentifier());
		dataObject.value = String.valueOf(value);
		TServiceVariables.insertOrUpdate(dataObject);
	}

	/**
	 * The identifier of the service. This is used to reference the service and the key to store data with.
	 *
	 * @return the identifier of the service
	 */
	public abstract String getIdentifier();

	/**
	 * miliseconds it should wait befor eattempting anothe run
	 *
	 * @return delay in miliseconds
	 */
	public abstract long getDelayBetweenRuns();

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
