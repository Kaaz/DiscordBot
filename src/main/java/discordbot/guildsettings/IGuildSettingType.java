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

package discordbot.guildsettings;

import net.dv8tion.jda.core.entities.Guild;

public interface IGuildSettingType {

	/**
	 * Display name of the setting type
	 *
	 * @return name
	 */
	String typeName();

	/**
	 * Checks if a setting is valid
	 *
	 * @param guild the guild to check in
	 * @param value the new value
	 * @return does the value validate?
	 */
	boolean validate(Guild guild, String value);

	/**
	 * Converts the input to a value which can be used to store in the db
	 *
	 * @param guild the guild to check in
	 * @param value the input value
	 * @return a db-save value
	 */
	String fromInput(Guild guild, String value);

	/**
	 * Converts the db-save value back to a fancy db
	 *
	 * @param guild the guild to check in
	 * @param value the db-value to the fancy display
	 * @return a nicely formated value
	 */
	String toDisplay(Guild guild, String value);
}
