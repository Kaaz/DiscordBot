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

import discordbot.main.Config;

public class C4Board {

    private C4Column[] cols;
    private int columnSize;

    public C4Board(int columns, int rows) {
        cols = new C4Column[columns];
        for (int i = 0; i < cols.length; i++) {
            cols[i] = new C4Column(rows);
        }
        this.columnSize = rows;
    }

    public int getValue(int column, int row) {
        return cols[column].getCol(row);
    }

    /**
     * @param index the column to place it in
     * @return can the player place it?
     */
    public boolean canPlaceInColumn(int index) {
        return cols[index] != null && cols[index].hasSpace();
    }

    /**
     * @param index  the column
     * @param player playerindex
     * @return success
     */
    public boolean placeInColumn(int index, int player) {
        return cols[index].place(player);
    }

    public String intToPlayer(int playerIndex) {
        switch (playerIndex) {
            case 0:
                return "\uD83D\uDD34";
            case 1:
                return "\uD83D\uDD35";
            default:
                return "\u26AA";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int height = 0; height < columnSize; height++) {
            for (C4Column col : cols) {
                sb.append(intToPlayer(col.getCol(height)));
            }
            sb.append(Config.EOL);
        }
        return sb.toString();
    }
}
