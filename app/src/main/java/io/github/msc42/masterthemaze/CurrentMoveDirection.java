// Copyright (C) 2016 Stefan Constantin
//
// This file is part of Master the maze.
//
// Master the maze is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Master the maze is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Master the maze. If not, see <http://www.gnu.org/licenses/>.

package io.github.msc42.masterthemaze;

import io.github.msc42.maze.Game;

/**
 * Represents the current move direction of the {@link  io.github.msc42.maze.HeroPoint hero point}
 *
 * @author Stefan Constantin
 */
final class CurrentMoveDirection {

    private int mCurrentMoveDirection = Game.NO_MOVE;
    private long mTimeLastMoveDirectionChange = System.currentTimeMillis();


    protected CurrentMoveDirection() {
        mTimeLastMoveDirectionChange = System.currentTimeMillis();
    }

    protected int getCurrentMoveDirection() {
        return mCurrentMoveDirection;
    }

    protected long getTimeLastMoveDirectionChange() {
        return mTimeLastMoveDirectionChange;
    }

    protected void setCurrentMoveDirection(int currentMoveDirection) {
        this.mCurrentMoveDirection = currentMoveDirection;
        mTimeLastMoveDirectionChange = System.currentTimeMillis();
    }

}