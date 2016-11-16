package discordbot.role;

import java.awt.*;

/**
 * Created on 19-9-2016
 */
public class MemberShipRole {

	private final String name;
	private final Color color;
	private final long membershipTime;
	private final boolean hoisted;

	public MemberShipRole(String name, Color color, long membershipTime) {
		this.name = name;
		this.color = color;
		this.hoisted = false;
		this.membershipTime = membershipTime;
	}

	public MemberShipRole(String name, Color color, long membershipTime, boolean hoisted) {
		this.name = name;
		this.color = color;
		this.hoisted = hoisted;
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

	public boolean isHoisted() {
		return hoisted;
	}
}
