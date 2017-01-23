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

package discordbot.games.connect4;

/**
 * Created on 9-9-2016
 */
public class C4Column {

	private int spaceAvailable;
	private int[] column;

	C4Column(int size) {
		this.spaceAvailable = size;
		column = new int[size];
		for (int i = 0; i < column.length; i++) {
			column[i] = -1;
		}
	}

	public boolean place(int player) {
		if (hasSpace()) {
			column[spaceAvailable - 1] = player;
			spaceAvailable--;
		}
		return false;
	}

	public boolean hasSpace() {
		return spaceAvailable > 0;
	}

	public int getCol(int colindex) {
		return column[colindex];
	}
}
