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
	public long delayBetweenRuns() {
		return 900_000;
	}

	@Override
	public boolean shouldIRun() {
		return false;
	}

	@Override
	public void beforeRun() {

	}

	@Override
	public void run() {

	}

	@Override
	public void afterRun() {

	}
}
