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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.concurrent.ArrayBlockingQueue;

import io.github.msc42.maze.Game;

class AccelerometerEventListener implements SensorEventListener {

    protected static final int[][] DIRECTIONS = {
            {Game.LEFT, Game.UP, Game.RIGHT, Game.DOWN}, // Surface.ROTATION_0
            {Game.DOWN, Game.LEFT, Game.UP, Game.RIGHT}, // Surface.ROTATION_90
            {Game.RIGHT, Game.DOWN, Game.LEFT, Game.UP}, // Surface.ROTATION_180
            {Game.UP, Game.RIGHT, Game.DOWN, Game.LEFT}  // Surface.ROTATION_270
    };

    private int mRotation;
    private float mSensitivity;
    private CurrentMoveDirection mCurrentMoveDirection;
    private ArrayBlockingQueue<Integer> mMotionQueue;


    protected AccelerometerEventListener(int rotation, float sensitivity, CurrentMoveDirection currentMoveDirection,
                                         ArrayBlockingQueue<Integer> motionQueue) {
        this.mRotation = rotation;
        this.mSensitivity = sensitivity;
        this.mCurrentMoveDirection = currentMoveDirection;
        this.mMotionQueue = motionQueue;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];

            if (x > mSensitivity && x > Math.abs(y)) {
                setCurrentMoveDirection(DIRECTIONS[mRotation][0]);
            } else if (y < -mSensitivity && -y > Math.abs(x)) {
                setCurrentMoveDirection(DIRECTIONS[mRotation][1]);
            } else if (x < -mSensitivity && -x > Math.abs(y)) {
                setCurrentMoveDirection(DIRECTIONS[mRotation][2]);
            } else if (y > mSensitivity && y > Math.abs(x)) {
                setCurrentMoveDirection(DIRECTIONS[mRotation][3]);
            } else {
                setCurrentMoveDirection(Game.NO_MOVE);
            }
        }
    }

    private void setCurrentMoveDirection(int newMoveDirection) {
        if (mCurrentMoveDirection.getCurrentMoveDirection() != newMoveDirection) {
            // without synchronization it could be possible that the AddCurrentDirectionToQueueThread
            // adds the old direction after the new direction in the queue
            synchronized (mCurrentMoveDirection) {
                mCurrentMoveDirection.setCurrentMoveDirection(newMoveDirection);
                try {
                    mMotionQueue.add(mCurrentMoveDirection.getCurrentMoveDirection());
                } catch (IllegalStateException e) {
                    // if queue is full, move is discarded
                }
            }
        }
    }
}
