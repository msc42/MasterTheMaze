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

package io.github.msc42.maze;

/**
 * Represents an enemy point.
 *
 * @author Stefan Constantin
 */
class EnemyPoint implements Point {

    private int mCounter = 0;
    private int mCounterStep = 0;

    private int mCurrentX;
    private int mCurrentY;
    private int[] mStepX;
    private int[] mStepY;
    private int mStepInterval;


    /**
     * Creates a new enemy point.
     *
     * @param startX       The x coordinate of the first turn of the level.
     * @param startY       The y coordinate of the first turn of the level.
     * @param stepX        The steps of this enemy point at the x-coordinate
     * @param stepY        The steps of this enemy point at the y-coordinate
     * @param stepInterval The interval of the steps.
     */
    protected EnemyPoint(int startX, int startY, int[] stepX, int[] stepY, int stepInterval) {
        this.mCurrentX = startX;
        this.mCurrentY = startY;
        this.mStepX = stepX;
        this.mStepY = stepY;
        this.mStepInterval = stepInterval;
    }

    @Override
    public byte getPixelValue() {
        return GameConstants.LED_THREE_QUARTER;
    }

    protected int[] doStep() {
        if (mCounter < mStepInterval - 1) {
            mCounter++;
            return new int[]{mCurrentX, mCurrentY, mCurrentX, mCurrentY};
        } else {
            mCounter = 0;
            int oldX = mCurrentX;
            int oldY = mCurrentY;
            int[] newPosition = computeNewPosition();
            return new int[]{oldX, oldY, newPosition[0], newPosition[1]};
        }
    }

    private int[] computeNewPosition() {
        mCurrentX += mStepX[mCounterStep];
        mCurrentY += mStepY[mCounterStep];
        mCounterStep = (mCounterStep + 1) % mStepX.length;
        return new int[]{mCurrentX, mCurrentY};
    }
}