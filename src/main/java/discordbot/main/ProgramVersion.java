package discordbot.main;

import discordbot.util.Misc;

/**
 * Created on 22-9-2016
 */
public class ProgramVersion {
	private int majorVersion;
	private int minorVersion;
	private int patchVersion;

	public ProgramVersion(int majorVersion, int minorVersion, int patchVersion) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.patchVersion = patchVersion;
	}

	private ProgramVersion(int majorVersion, int minorVersion) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.patchVersion = 0;
	}

	ProgramVersion(int majorVersion) {
		this.majorVersion = majorVersion;
		this.minorVersion = 0;
		this.patchVersion = 0;
	}

	public static ProgramVersion fromString(String version) {
		String[] parts = version.split("\\.");
		if (parts.length == 3) {
			return new ProgramVersion(Misc.parseInt(parts[0], 1), Misc.parseInt(parts[0], 0), Misc.parseInt(parts[0], 0));
		} else if (parts.length == 2) {
			return new ProgramVersion(Misc.parseInt(parts[0], 1), Misc.parseInt(parts[0], 0));
		} else if (parts.length == 1) {
			return new ProgramVersion(Misc.parseInt(parts[0], 1));
		}
		return new ProgramVersion(1);
	}

	/**
	 * Compares the version to another one
	 *
	 * @param version the version to compare it with
	 * @return true if this is higher than version
	 */
	public boolean isHigherThan(ProgramVersion version) {
		if (this.getMajorVersion() > version.getMajorVersion()) {
			return true;
		} else if (this.getMajorVersion() == version.getMajorVersion()) {
			if (this.getMinorVersion() > version.getMinorVersion()) {
				return true;
			} else if (this.getMinorVersion() == version.getMinorVersion()) {
				if (this.getPatchVersion() > version.getPatchVersion()) {
					return true;
				}
			}
		}
		return false;
	}

	public int getPatchVersion() {
		return patchVersion;
	}

	public int getMinorVersion() {
		return minorVersion;
	}

	public int getMajorVersion() {
		return majorVersion;
	}

	@Override
	public String toString() {
		return getMajorVersion() + "." + getMinorVersion() + "." + getPatchVersion();
	}
}
