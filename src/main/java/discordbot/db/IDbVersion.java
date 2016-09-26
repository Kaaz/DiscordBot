package discordbot.db;

/**
 * A database upgrade interface, from what version to what version
 */
public interface IDbVersion {

	/**
	 * the version it upgrades from
	 *
	 * @return version
	 */
	public int getFromVersion();

	/**
	 * the version it upgrades to
	 *
	 * @return version
	 */
	public int getToVersion();

	public String[] getExecutes();
}
