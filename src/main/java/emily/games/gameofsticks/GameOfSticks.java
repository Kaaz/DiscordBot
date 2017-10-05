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

import emily.games.AbstractGame;
import emily.games.GameState;
import emily.main.BotConfig;
import net.dv8tion.jda.core.entities.User;

/**
 * Created on 9-9-2016
 */
public class GameOfSticks extends AbstractGame<GoSTurn> {

    private int sticksleft;

    public GameOfSticks() {
        reset();
    }

    public void reset() {
        super.reset();
        sticksleft = 20;
    }

    @Override
    public String getCodeName() {
        return "gos";
    }

    @Override
    public String[] getReactions() {
        return new String[]{
                "1", "2", "3"
        };
    }

    @Override
    public String getFullname() {
        return "Game of sticks";
    }

    @Override
    public int getTotalPlayers() {
        return 2;
    }

    @Override
    protected boolean isTheGameOver() {
        if (sticksleft == 0) {
            setWinner(1 - getActivePlayerIndex());
            return true;
        }

        return false;
    }

    @Override
    public boolean isValidMove(User player, GoSTurn turnInfo) {
        return turnInfo.getSubstract() <= 3 && (sticksleft - turnInfo.getSubstract()) >= 0;
    }

    @Override
    protected void doPlayerMove(User player, GoSTurn turnInfo) {
        sticksleft -= turnInfo.getSubstract();
    }

    @Override
    public String toString() {
        String ret = "A Game of sticks." + BotConfig.EOL;
        ret += "Take away a few sticks, the player to take away the last stick loses!" + BotConfig.EOL;
        ret += "There are currently **" + sticksleft + "** Sticks left";
        ret += BotConfig.EOL + BotConfig.EOL;
        if (getGameState().equals(GameState.IN_PROGRESS) || getGameState().equals(GameState.READY)) {
            ret += "It's the turn of " + getActivePlayer().getAsMention() + BotConfig.EOL;
            ret += "to play type **" + getLastPrefix() + "game 1-3**";
        } else if (getGameState().equals(GameState.OVER)) {
            ret += "Its over! The winner is " + getPlayer(getWinnerIndex()).getAsMention();
        }
        return ret;
    }
}
