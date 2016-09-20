package discordbot.role;

import java.awt.*;

/**
 * Created on 19-9-2016
 */
public class MemberShipRole {

	private final String name;
	private final Color color;
	private final long membershipTime;

	public MemberShipRole(String name, Color color, long membershipTime) {
		this.name = name;
		this.color = color;

		this.membershipTime = membershipTime;
	}

	public String getName() {
		return name;
	}

	public Color getColor() {
		return color;
	}

	public long getMembershipTime() {
		return membershipTime;
	}
}
