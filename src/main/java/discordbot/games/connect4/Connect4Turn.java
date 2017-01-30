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

import discordbot.games.GameTurn;

/**
 * Created on 2016-09-10.
 */
public class Connect4Turn extends GameTurn {
    private int columnIndex;

    public Connect4Turn() {

    }

    public Connect4Turn(int boardIndex) {

        this.columnIndex = boardIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    @Override
    public boolean parseInput(String input) {
        if (input != null && input.matches("^[1-7]$")) {
            this.columnIndex = Integer.parseInt(input) - 1;
            return true;
        }
        return false;
    }

    @Override
    public String getInputErrorMessage() {
        return "Expecting a numeric input between 1 and 7";
    }
}