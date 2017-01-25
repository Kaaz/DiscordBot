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

package discordbot.util;

public class GameUtil {
	public static final int MULTIPLICATION = 2;
	public static final int BASE = 3;

	public static long getXpFor(int level) {
		return getXpFor(level, MULTIPLICATION, BASE);
	}

	public static long getXpFor(int level, int multiIncremental, int multiBase) {
		return (multiIncremental * level * level) + multiBase * level;
	}
}
