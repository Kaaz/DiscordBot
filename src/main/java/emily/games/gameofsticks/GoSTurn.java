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

package emily.games.gameofsticks;

import emily.games.meta.GameTurn;

public class GoSTurn extends GameTurn {
    private int substract = 0;

    public GoSTurn() {

    }

    public GoSTurn(int boardIndex) {

        this.substract = boardIndex;
    }

    public int getSubstract() {
        return substract;
    }

    @Override
    public boolean parseInput(String input) {
        if (input != null && input.matches("^[1-3]$")) {
            this.substract = Integer.parseInt(input);
            return true;
        }
        return false;
    }

    @Override
    public String getInputErrorMessage() {
        return "Expecting a numeric input in range 1-3";
    }
}
