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
 * Represents a hero point.
 *
 * @author Stefan Constantin
 */
class HeroPoint implements Point {

    private Game mGame;
    private int mCurrentX;
    private int mCurrentY;
    private int mStartX;
    private int mStartY;
    private int mStepX;
    private int mStepY;


    /**
     * Creates a new hero point.
     *
     * @param startX The x coordinate of the first turn of the level.
     * @param startY The y coordinate of the first turn of the level.
     * @param stepX  The steps of this enemy point at the x-coordinate
     * @param stepY  The steps of this enemy point at the y-coordinate
     * @param game   The game where the hero point is.
     */
    protected HeroPoint(int startX, int startY, int stepX, int stepY, Game game) {
        this.mStartX = startX;
        this.mStartY = startY;
        this.mStepX = stepX;
        this.mStepY = stepY;
        this.mGame = game;
        mCurrentX = startX;
        mCurrentY = startY;
    }

    @Override
    public byte getPixelValue() {
        return GameConstants.LED_MAX;
    }

    protected int getX() {
        return mCurrentX;
    }

    protected int getY() {
        return mCurrentY;
    }

    protected int[] doStep(int move) {
        switch (move) {
            case Game.UP:
                return moveUp();
            case Game.RIGHT_UP:
                return moveRightUp();
            case Game.RIGHT:
                return moveRight();
            case Game.RIGHT_DOWN:
                return moveRightDown();
            case Game.DOWN:
                return moveDown();
            case Game.LEFT_DOWN:
                return moveLeftDown();
            case Game.LEFT:
                return moveLeft();
            case Game.LEFT_UP:
                return moveLeftUp();
            default:
                return null;
        }
    }

    protected int[] die() {
        int[] heroPosition = {mCurrentX, mCurrentY, mStartX, mStartY};
        mCurrentX = mStartX;
        mCurrentY = mStartY;
        return heroPosition;
    }

    private int[] moveHelp(int[] newPosition) {
        if (newPosition[0] != -1) {
            int oldX = mCurrentX;
            int oldY = mCurrentY;
            mCurrentX = newPosition[0];
            mCurrentY = newPosition[1];
            return new int[]{oldX, oldY, mCurrentX, mCurrentY};
        }
        return null;
    }

    private int[] moveRight() {
        if (mCurrentX + mStepX < GameConstants.WIDTH) {
            int[] newPosition = mGame.getStepHeroPoint(mCurrentX, mCurrentY,
                    mCurrentX + mStepX, mCurrentY);
            return moveHelp(newPosition);
        }
        return null;
    }

    private int[] moveLeft() {
        if (mCurrentX - mStepX >= 0) {
            int[] newPosition = mGame.getStepHeroPoint(mCurrentX, mCurrentY,
                    mCurrentX - mStepX, mCurrentY);
            return moveHelp(newPosition);
        }
        return null;
    }

    private int[] moveUp() {
        if (mCurrentY - mStepY >= 0) {
            int[] newPosition = mGame.getStepHeroPoint(mCurrentX, mCurrentY,
                    mCurrentX, mCurrentY - mStepY);
            return moveHelp(newPosition);
        }
        return null;
    }

    private int[] moveDown() {
        if (mCurrentY + mStepY < GameConstants.HEIGHT) {
            int[] newPosition = mGame.getStepHeroPoint(mCurrentX, mCurrentY,
                    mCurrentX, mCurrentY + mStepY);
            return moveHelp(newPosition);
        }
        return null;
    }

    private int[] moveLeftUp() {
        if (mCurrentX - mStepX >= 0 && mCurrentY + mStepY >= 0) {
            int[] newPosition = mGame.getStepHeroPoint(mCurrentX, mCurrentY,
                    mCurrentX - mStepX, mCurrentY - mStepY);
            return moveHelp(newPosition);
        }
        return null;
    }

    private int[] moveLeftDown() {
        if (mCurrentX - mStepX >= 0 && mCurrentY + mStepY <= GameConstants.HEIGHT) {
            int[] newPosition = mGame.getStepHeroPoint(mCurrentX, mCurrentY,
                    mCurrentX - mStepX, mCurrentY + mStepY);
            return moveHelp(newPosition);
        }
        return null;
    }

    private int[] moveRightUp() {
        if (mCurrentX + mStepX <= GameConstants.WIDTH && mCurrentY - mStepY >= 0) {
            int[] newPosition = mGame.getStepHeroPoint(mCurrentX, mCurrentY,
                    mCurrentX + mStepX, mCurrentY - mStepY);
            return moveHelp(newPosition);
        }
        return null;
    }

    private int[] moveRightDown() {
        if (mCurrentX + mStepX <= GameConstants.WIDTH && mCurrentY + mStepY <= GameConstants.HEIGHT) {
            int[] newPosition = mGame.getStepHeroPoint(mCurrentX, mCurrentY,
                    mCurrentX + mStepX, mCurrentY + mStepY);
            return moveHelp(newPosition);
        }
        return null;
    }
}