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

import android.os.Process;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Represents a thread, which adds moves to the move queue in a given interval if the move
 * direction has not been changed since a given interval.
 *
 * @author Stefan Constantin
 */
final class AddCurrentDirectionToQueueThread extends Thread {

    private boolean mRunning = true;

    private ArrayBlockingQueue<Integer> mMoveQueue;
    private final CurrentMoveDirection mCurrentMoveDirection;
    private int mTimeBetweenMoves;


    protected AddCurrentDirectionToQueueThread(ArrayBlockingQueue<Integer> moveQueue,
                                               CurrentMoveDirection currentMoveDirection, int timeBetweenMoves) {
        this.mMoveQueue = moveQueue;
        this.mCurrentMoveDirection = currentMoveDirection;
        this.mTimeBetweenMoves = timeBetweenMoves;
    }

    protected void terminate() {
        mRunning = false;
        interrupt();
    }

    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);

        while (mRunning) {

            long timeStart = System.currentTimeMillis();

            if (timeStart - mCurrentMoveDirection.getTimeLastMoveDirectionChange()
                    >= Constants.ADD_CURRENT_DIRECTION_TO_QUEUE_FIRST_TIME_FACTOR * mTimeBetweenMoves) {
                // without synchronization it could be possible that the AddCurrentDirectionToQueueThread
                // adds the old direction after the new direction in the queue
                synchronized (mCurrentMoveDirection) {
                    try {
                        mMoveQueue.add(mCurrentMoveDirection.getCurrentMoveDirection());
                    } catch (IllegalStateException e) {
                        // if queue is full, move is discarded
                    }
                }

                try {
                    sleep(mTimeBetweenMoves - (System.currentTimeMillis() - timeStart));
                } catch (InterruptedException e) {
                }

            } else {
                try {
                    sleep(Constants.ADD_CURRENT_DIRECTION_TO_QUEUE_WAIT_SLEEPTIME);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}