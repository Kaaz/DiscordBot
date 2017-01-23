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

package discordbot.command;

/**
 * Limits the usage of commands by adding a cooldown to commands
 */
public interface ICommandCooldown {

	/**
	 * gets the cooldown of a command
	 *
	 * @return cooldown in seconds
	 */
	long getCooldownDuration();

	/**
	 * cooldown on what scale?
	 *
	 * @return scope of the cooldown
	 */
	CooldownScope getScope();
}
