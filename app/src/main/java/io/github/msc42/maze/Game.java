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

import android.content.res.AssetManager;

import java.io.IOException;
import java.util.List;

/**
 * Represents a game.
 * A level is generated from a JSON file.
 * Rules for a JSON file are in the JavaDoc of the 'Level' class.
 *
 * @author Stefan Constantin
 */
public class Game {

    public static final int NO_MOVE = 0;
    public static final int UP = 1;
    public static final int RIGHT_UP = 2;
    public static final int RIGHT = 3;
    public static final int RIGHT_DOWN = 4;
    public static final int DOWN = 5;
    public static final int LEFT_DOWN = 6;
    public static final int LEFT = 7;
    public static final int LEFT_UP = 8;

    private AssetManager mAssetManager;
    private int mDifficulty;

    private int mCurrentLevelNumber = 1;
    private boolean mCurrentLevelRunning = true;

    private byte[] mLedMatrix = new byte[GameConstants.WIDTH * GameConstants.HEIGHT];

    private List<Point>[][] mPoints;
    private HeroPoint mHeroPoint;
    private List<EnemyPoint> mEnemyPoints;


    /**
     * Creates a new game.
     *
     * @param assetManager Provides access to this application's raw asset files.
     * @throws InvalidLevelException
     * @throws IOException
     */
    public Game(AssetManager assetManager, int difficulty) throws InvalidLevelException, IOException {
        this.mAssetManager = assetManager;
        this.mDifficulty = difficulty;
        try {
            loadLevel();
        } catch (IOException e) {
            end();
            throw new IOException(GameConstants.ERROR_IO);
        }
    }

    /**
     * Execute one turn of the game.
     *
     * @param move The direction of the players move.<br/>
     *             0: no move, 1: up, 2: right and up, 3: right, 4: right and down,
     *             5: down, 6: left and down, 7: left, 8: left and up
     * @return The values of every pixel of the matrix (GameConstants.HEIGHT * GameConstants.WIDTH))
     * @throws InvalidLevelException
     * @throws IOException
     */
    public byte[] doStep(int move) throws InvalidLevelException, IOException {
        if (mHeroPoint != null) {
            int[] heroTick = mHeroPoint.doStep(move);
            if (heroTick != null) {
                doStepHeroPoint(heroTick);
            }

            if (mCurrentLevelRunning) {
                for (EnemyPoint enemyPoint : mEnemyPoints) {
                    if (enemyPoint != null) {
                        doStepDynamicPoint(enemyPoint.doStep(), enemyPoint);
                    }
                }
            } else {
                mCurrentLevelNumber++;
                mCurrentLevelRunning = true;
                try {
                    loadLevel();
                } catch (IOException e) {
                    end();
                    throw new IOException(GameConstants.ERROR_IO);
                }
            }
        }
        return mLedMatrix;
    }

    /**
     * Clears the LED Matrix.
     *
     * @return the cleared LED Matrix
     */
    public byte[] clearLedMatrix() {
        for (int i = 0; i < GameConstants.WIDTH * GameConstants.HEIGHT; i++) {
            mLedMatrix[i] = GameConstants.LED_OUT;
        }
        return mLedMatrix;
    }

    private void loadLevel() throws InvalidLevelException, IOException {
        Level currentLevel = new Level(mCurrentLevelNumber, mAssetManager, this, mDifficulty);
        this.mPoints = currentLevel.getPoints();
        this.mEnemyPoints = currentLevel.getDynamicPoints();
        this.mHeroPoint = currentLevel.getHero();
        initLedMatrix();
    }

    private void initLedMatrix() {
        for (int i = 0; i < GameConstants.HEIGHT; i++) {
            for (int j = 0; j < GameConstants.WIDTH; j++) {
                if (mPoints[i][j].size() > 0 && mPoints[i][j].get(0) != null) {
                    mLedMatrix[i + j * GameConstants.WIDTH] = mPoints[i][j].get(0).getPixelValue();
                } else {
                    mLedMatrix[i + j * GameConstants.WIDTH] = GameConstants.LED_OUT;
                }
            }
        }
        if (mHeroPoint != null) {
            mLedMatrix[mHeroPoint.getX() + mHeroPoint.getY() * GameConstants.WIDTH] = mHeroPoint.getPixelValue();
        }
    }

    private void end() {
        this.mPoints = null;
        this.mEnemyPoints = null;
        this.mHeroPoint = null;
    }

    protected int[] getStepHeroPoint(int oldX, int oldY, int newX, int newY) {
        //order is important
        if (mPoints[newX][newY].size() > 0 &&
                mPoints[newX][newY].get(0) instanceof NextLevelPoint) {
            mCurrentLevelRunning = false;
            return new int[]{-1, -1};
        }

        if (mPoints[newX][newY].size() > 0 &&
                mPoints[newX][newY].get(0) instanceof TeleportPoint) {
            TeleportPoint teleportPoint = (TeleportPoint) mPoints[newX][newY].get(0);
            newX = teleportPoint.getDestinationX();
            newY = teleportPoint.getDestinationY();
            if (mPoints[newX][newY].size() > 0 &&
                    mPoints[newX][newY].get(0) instanceof NextLevelPoint) {
                mCurrentLevelRunning = false;
                return new int[]{-1, -1};
            }
        }

        if (mPoints[newX][newY].size() > 0 &&
                mPoints[newX][newY].get(0) instanceof BorderPoint) {
            return new int[]{-1, -1};
        }

        return new int[]{newX, newY};
    }

    private void doStepDynamicPoint(int[] state, Point point) {
        deleteCell(state[0], state[1], point);

        if (state[2] == mHeroPoint.getX() && state[3] == mHeroPoint.getY()) {
            doStepHeroPoint(mHeroPoint.die());
        }

        setCell(state[2], state[3], point);
    }

    private void deleteCell(int x, int y, Point point) {
        mPoints[x][y].remove(point);
        if (mPoints[x][y].size() == 0) {
            mLedMatrix[x + y * GameConstants.WIDTH] = GameConstants.LED_OUT;
        }
    }

    private void doStepHeroPoint(int[] state) {
        //HeroPoint is always alone in cell, therefore LED out is okay
        mLedMatrix[state[0] + state[1] * GameConstants.WIDTH] = GameConstants.LED_OUT;
        mLedMatrix[state[2] + state[3] * GameConstants.WIDTH] = mHeroPoint.getPixelValue();
    }

    private void setCell(int x, int y, Point point) {
        mPoints[x][y].add(point);
        mLedMatrix[x + y * GameConstants.WIDTH] = point.getPixelValue();
    }
}