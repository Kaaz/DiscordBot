/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package discordbot.db.model;

import discordbot.db.AbstractModel;

import java.util.EnumSet;

public class OUser extends AbstractModel {
	public int id;
	public String discord_id;
	public String name;
	public int commandsUsed;
	public int banned;

	private int permissionTotal;
	private EnumSet<PermissionNode> nodes;
	public int lastCurrencyRetrieval = 0;

	public OUser() {
		discord_id = "";
		id = 0;
		name = "";
		commandsUsed = 0;
		banned = 0;
		nodes = EnumSet.noneOf(PermissionNode.class);
		permissionTotal = 0;
	}

	public boolean hasPermission(PermissionNode node) {
		return nodes.contains(node);
	}

	public int getEncodedPermissions() {
		return permissionTotal;
	}

	public EnumSet<PermissionNode> getPermission() {
		return nodes;
	}

	public void setPermission(int total) {
		nodes = decode(total);
		permissionTotal = total;
	}

	public boolean addPermission(PermissionNode node) {
		if (nodes.contains(node)) {
			return false;
		}
		nodes.add(node);
		permissionTotal = encode();
		return true;
	}

	public boolean removePermission(PermissionNode node) {
		if (!nodes.contains(node)) {
			return false;
		}
		nodes.remove(node);
		permissionTotal = encode();
		return true;
	}

	private EnumSet<PermissionNode> decode(int code) {
		PermissionNode[] values = PermissionNode.values();
		EnumSet<PermissionNode> result = EnumSet.noneOf(PermissionNode.class);
		while (code != 0) {
			int ordinal = Integer.numberOfTrailingZeros(code);
			code ^= Integer.lowestOneBit(code);
			result.add(values[ordinal]);
		}
		return result;
	}

	private int encode() {
		int ret = 0;
		for (PermissionNode val : nodes) {
			ret |= 1 << val.ordinal();
		}
		return ret;
	}

	public enum PermissionNode {
		IMPORT_PLAYLIST("use youtube playlists"),
		BAN_TRACKS("ban tracks from the global playlist");
		private final String description;

		PermissionNode(String description) {

			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}
}
