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

package discordbot.games.game2048;

import discordbot.games.GameTurn;

public class Game2048Turn extends GameTurn {
    private Game2048Direction direction = Game2048Direction.UNKNOWN;

    public Game2048Turn() {

    }

    public Game2048Direction getDirection() {
        return direction;
    }

    @Override
    public boolean parseInput(String input) {
        if (input != null) {
            this.direction = Game2048Direction.fromString(input);
            return direction.equals(Game2048Direction.UNKNOWN);
        }
        return false;
    }

    @Override
    public String getInputErrorMessage() {
        return "Expecting a direction: up, down, left, right (or u/d/l/r)";
    }
}
