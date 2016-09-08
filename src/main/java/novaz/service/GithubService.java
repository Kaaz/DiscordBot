package novaz.service;

import novaz.core.AbstractService;
import novaz.main.NovaBot;

/**
 * Created on 8-9-2016
 */
public class GithubService extends AbstractService {


	public GithubService(NovaBot b) {
		super(b);
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
