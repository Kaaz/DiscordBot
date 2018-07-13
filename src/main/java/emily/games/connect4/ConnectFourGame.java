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

package emily.games.connect4;

import emily.games.meta.AbstractGame;
import emily.games.meta.GameState;
import emily.util.Emojibet;
import emily.util.Misc;
import net.dv8tion.jda.core.entities.User;

/**
 * Created on 9-9-2016
 */
public class ConnectFourGame extends AbstractGame<Connect4Turn> {

    public static final int ROWS = 6, COLS = 7;
    private C4Board board;

    public ConnectFourGame() {
        reset();
    }

    public void reset() {
        super.reset();
        board = new C4Board(COLS, ROWS);
    }

    @Override
    public String getCodeName() {
        return "cf";
    }

    @Override
    public String[] getReactions() {
        return new String[]{
                "1", "2", "3", "4", "5", "6", "7"
        };
    }

    @Override
    public String getFullname() {
        return "Connect Four";
    }

    @Override
    public int getTotalPlayers() {
        return 2;
    }

    @Override
    protected boolean isTheGameOver() {

        for (int j = 0; j < ROWS - 3; j++) {
            for (int i = 0; i < COLS; i++) {
                if (this.board.getValue(i, j) == getActivePlayerIndex() &&
                        this.board.getValue(i, j + 1) == getActivePlayerIndex() &&
                        this.board.getValue(i, j + 2) == getActivePlayerIndex() &&
                        this.board.getValue(i, j + 3) == getActivePlayerIndex()) {
                    setWinner(getActivePlayerIndex());
                    return true;
                }
            }
        }
        // verticalCheck
        for (int i = 0; i < COLS - 3; i++) {
            for (int j = 0; j < ROWS; j++) {
                if (this.board.getValue(i, j) == getActivePlayerIndex() &&
                        this.board.getValue(i + 1, j) == getActivePlayerIndex() &&
                        this.board.getValue(i + 2, j) == getActivePlayerIndex() &&
                        this.board.getValue(i + 3, j) == getActivePlayerIndex()) {
                    setWinner(getActivePlayerIndex());
                    return true;
                }
            }
        }
        // ascendingDiagonalCheck
        for (int i = 3; i < COLS; i++) {
            for (int j = 0; j < ROWS - 3; j++) {
                if (this.board.getValue(i, j) == getActivePlayerIndex() &&
                        this.board.getValue(i - 1, j + 1) == getActivePlayerIndex() &&
                        this.board.getValue(i - 2, j + 2) == getActivePlayerIndex() &&
                        this.board.getValue(i - 3, j + 3) == getActivePlayerIndex()) {
                    setWinner(getActivePlayerIndex());
                    return true;
                }
            }
        }
        // descendingDiagonalCheck
        for (int i = 3; i < COLS; i++) {
            for (int j = 3; j < ROWS; j++) {
                if (this.board.getValue(i, j) == getActivePlayerIndex() &&
                        this.board.getValue(i - 1, j - 1) == getActivePlayerIndex() &&
                        this.board.getValue(i - 2, j - 2) == getActivePlayerIndex() &&
                        this.board.getValue(i - 3, j - 3) == getActivePlayerIndex()) {
                    setWinner(getActivePlayerIndex());
                    return true;
                }
            }
        }
        for (int i = 0; i < COLS; i++) {
            if (board.canPlaceInColumn(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isValidMove(User player, Connect4Turn turnInfo) {
        return board.canPlaceInColumn(turnInfo.getColumnIndex());
    }

    @Override
    protected void doPlayerMove(User player, Connect4Turn turnInfo) {
        board.placeInColumn(turnInfo.getColumnIndex(), getActivePlayerIndex());
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("A Connect 4 game.\n");
        ret.append(board.toString());
        for (int i = 0; i < COLS; i++) {
            if (board.canPlaceInColumn(i)) {
                ret.append(Misc.numberToEmote(i + 1));
            } else {
                ret.append(Emojibet.NO_ENTRY);
            }
        }
        ret.append("\n\n");
        if (getGameState().equals(GameState.IN_PROGRESS) || getGameState().equals(GameState.READY)) {
            ret.append(board.intToPlayer(0)).append(" = ").append(getPlayer(0).getName()).append("\n");
            ret.append(board.intToPlayer(1)).append(" = ").append(getPlayer(1).getName()).append("\n");
            ret.append("It's the turn of ").append(getActivePlayer().getAsMention()).append("\n");
            ret.append("to play type **").append(getLastPrefix()).append("game <columnnumber>**");
        }
        if (getGameState().equals(GameState.OVER)) {
            if (getWinnerIndex() == getTotalPlayers()) {
                ret.append("Its over! And its a draw!");
            } else {
                ret.append("Its over! The winner is ").append(getPlayer(getWinnerIndex()).getAsMention());
            }
        }
        return ret.toString();
    }
}
