package novaz.service;

import novaz.core.AbstractService;
import novaz.main.NovaBot;

/**
 * check for news on github
 */
public class GithubService extends AbstractService {


	public GithubService(NovaBot b) {
		super(b);
	}

	@Override
	public String getIdentifier() {
		return "bot_code_updates";
	}

	@Override
	public long getDelayBetweenRuns() {
		return 900_000;
	}

	@Override
	public boolean shouldIRun() {
		return true;
	}

	@Override
	public void beforeRun() {
		System.out.println("before running");
	}

	@Override
	public void run() {
		System.out.println("I'm running");
	}

	@Override
	public void afterRun() {
		System.out.println("after running");
	}
}
