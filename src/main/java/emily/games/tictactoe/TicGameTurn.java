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

package emily.games.tictactoe;

import emily.games.GameTurn;

public class TicGameTurn extends GameTurn {
    private int boardIndex = 0;

    public TicGameTurn() {

    }

    public TicGameTurn(int boardIndex) {

        this.boardIndex = boardIndex;
    }

    public int getBoardIndex() {
        return boardIndex;
    }

    @Override
    public boolean parseInput(String input) {
        if (input != null && input.matches("^[1-9]$")) {
            this.boardIndex = Integer.parseInt(input) - 1;
            return true;
        }
        return false;
    }

    @Override
    public String getInputErrorMessage() {
        return "Expecting a numeric input in range 1-9";
    }
}
