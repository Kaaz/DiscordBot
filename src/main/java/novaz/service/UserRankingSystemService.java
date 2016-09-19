package novaz.service;

import novaz.core.AbstractService;
import novaz.main.NovaBot;

/**
 * updates the ranking of members within a guild
 */
public class UserRankingSystemService extends AbstractService {

	public UserRankingSystemService(NovaBot b) {
		super(b);
	}

	@Override
	public String getIdentifier() {
		return "user_role_ranking";
	}

	@Override
	public long getDelayBetweenRuns() {
		return 86400000L;
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