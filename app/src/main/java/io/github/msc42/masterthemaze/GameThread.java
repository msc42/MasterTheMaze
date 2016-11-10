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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;

import io.github.msc42.maze.Game;
import io.github.msc42.maze.GameConstants;
import io.github.msc42.maze.InvalidLevelException;

/**
 * Represents the thread, which controls the game.
 * It connects to the Bluetooth LED display.
 * It removes the oldest inserted move from the mMoveQueue, does a step in the game with this move
 * and sends the new game state to the connected Bluetooth display.
 *
 * @author Stefan Constantin
 */
final class GameThread extends Thread {

    private boolean mRunning = true;
    private boolean mPause = false;
    private boolean mStopReconnecting = false;
    private boolean mFirstAttemptToReconnect = true;

    private BluetoothSocket mBluetoothSocket;
    private OutputStream mOutputStream;

    private int mMaxFramesPerSecond;

    private Game mGame;
    private ArrayBlockingQueue<Integer> mMoveQueue;
    private BluetoothDevice mBluetoothDevice;
    private Handler mHandler;


    protected GameThread(Game game, ArrayBlockingQueue<Integer> moveQueue,
                         BluetoothDevice bluetoothDevice, Handler handler) {
        this.mGame = game;
        this.mMoveQueue = moveQueue;
        this.mBluetoothDevice = bluetoothDevice;
        this.mHandler = handler;
    }

    protected void pause() {
        mPause = true;
    }

    protected void restart() {
        mPause = false;
        interrupt();
    }

    protected void stopReconnecting() {
        mStopReconnecting = true;
        interrupt();
    }

    protected void terminate() {
        mRunning = false;
    }

    @Override
    public void run() {
        try {
            openConnection();
        } catch (BluetoothDisabledException e) {
            mHandler.sendEmptyMessage(Constants.DISABLED_BLUETOOTH_MESSAGE);
            return;
        } catch (RfcommSocketNotSupportedException e) {
            mHandler.sendEmptyMessage(Constants.RFCOMM_SOCKET_NOT_SUPPORTED_MESSAGE);
            return;
        } catch (IOException e) {
            mHandler.sendEmptyMessage(Constants.BT_CANNOT_ESTABLISH_CONNECTION_MESSAGE);
            closeConnection();
            return;
        }

        try {
            if (!doHandshake()) {
                mHandler.sendEmptyMessage(Constants.FAILED_HANDSHAKE_MESSAGE);
                mRunning = false;
            }
        } catch (BluetoothDisabledException e) {
            mHandler.sendEmptyMessage(Constants.DISABLED_BLUETOOTH_MESSAGE);
            mRunning = false;
        } catch (IOException e) {
            mHandler.sendEmptyMessage(Constants.BT_CANNOT_ESTABLISH_CONNECTION_MESSAGE);
            mRunning = false;
        }

        long sleepTimeUntilNextFrameShouldBeSent = 1000 / mMaxFramesPerSecond;

        mHandler.sendEmptyMessage(Constants.BT_ESTABLISHED_CONNECTION_MESSAGE);

        mMoveQueue.clear();

        while (mRunning) {
            if (mPause) {
                sleepTimeUntilNextFrameShouldBeSent = 0;
                disconnectIfConnectedAndSleep();
                continue;
            }

            sleepUntilNextFrameShouldBeSent(sleepTimeUntilNextFrameShouldBeSent);

            long timeStart = System.currentTimeMillis();

            int move = getNextMove();

            byte[] ledMatrix;
            try {
                ledMatrix = mGame.doStep(move);
            } catch (InvalidLevelException e) {
                sendStringMessage(e.getMessage());
                mRunning = false;
                break;
            } catch (IOException e) {
                mHandler.sendEmptyMessage(Constants.IO_ERROR_MESSAGE);
                mRunning = false;
                break;
            }

            mStopReconnecting = false;
            try {
                if (!sendDataWithHandlers(ledMatrix)) {
                    mRunning = false;
                }
            } catch (BluetoothDisabledException e) {
                mHandler.sendEmptyMessage(Constants.DISABLED_BLUETOOTH_MESSAGE);
                mRunning = false;
            }

            sleepTimeUntilNextFrameShouldBeSent = Math.max(0, System.currentTimeMillis() - timeStart);
        }

        clearLedAndCloseConnection();
    }

    private void disconnectIfConnectedAndSleep() {
        if (mBluetoothSocket.isConnected()) {
            closeConnection();
        }

        try {
            Thread.sleep(Constants.GAME_THREAD_PAUSE_SLEEPTIME);
        } catch (InterruptedException e) {
        }
    }

    private void sleepUntilNextFrameShouldBeSent(long duration) {
        if (duration < 1000 / mMaxFramesPerSecond) {
            try {
                Thread.sleep(1000 / mMaxFramesPerSecond - duration);
            } catch (InterruptedException e) {
            }
        }
    }

    private int getNextMove() {
        try {
            return mMoveQueue.remove();
        } catch (NoSuchElementException e) {
            return Game.NO_MOVE;
        }
    }

    private void sendStringMessage(String stringToSend) {
        Message msg = Message.obtain();
        msg.what = Constants.INVALID_LEVEL_MESSAGE;
        msg.obj = stringToSend;
        msg.setTarget(mHandler);
        msg.sendToTarget();
    }

    private void openConnection() throws BluetoothDisabledException, IOException,
            RfcommSocketNotSupportedException {
        initBluetoothSocket();

        try {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            mBluetoothSocket.connect();
        } catch (NullPointerException e) {
            throw new BluetoothDisabledException();
        }

        try {
            Thread.sleep(Constants.WAIT_TIME_AFTER_CONNECT);
        } catch (InterruptedException e) {
        }

        try {
            mOutputStream = mBluetoothSocket.getOutputStream();
        } catch (NullPointerException e) {
            throw new BluetoothDisabledException();
        }
    }

    private void initBluetoothSocket() throws BluetoothDisabledException,
            RfcommSocketNotSupportedException {
        mBluetoothSocket = null;
        try {
            // public BluetoothSocket createRfcommSocket(int channel) throws IOException in the class
            // BluetoothDevice is hidden (annotation @hide)
            // a workaround is to access the hidden function with the help of reflection
            Method m = mBluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            mBluetoothSocket = (BluetoothSocket) m.invoke(mBluetoothDevice, Constants.RFCOMM_PORT);
        } catch (NullPointerException e) {
            throw new BluetoothDisabledException();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            try {
                // public BluetoothSocket createRfcommSocket(int channel) throws IOException in the class
                // BluetoothDevice returns
                // new BluetoothSocket(BluetoothSocket.TYPE_RFCOMM, -1, true, true, this, channel, null);
                // instead of createRfcommSocket(int channel) the Bluetooth socket can be directly instantiated
                // however, the constructor is private
                // a workaround is to access the private constructor with the help of reflection
                Class[] constructorArgumentTypes = {int.class, int.class, boolean.class,
                        boolean.class, BluetoothDevice.class, int.class, ParcelUuid.class};
                Constructor<BluetoothSocket> constructor = (Constructor<BluetoothSocket>)
                        BluetoothSocket.class.getDeclaredConstructor(constructorArgumentTypes);
                constructor.setAccessible(true);
                int typeRfcomm = 1;
                // since Android API 23 the constant BluetoothSocket.TYPE_RFCOMM is public
                mBluetoothSocket = constructor.newInstance(typeRfcomm, -1, true, true,
                        mBluetoothDevice, Constants.RFCOMM_PORT, null);
            } catch (NullPointerException e1) {
                throw new BluetoothDisabledException();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e1) {
                throw new RfcommSocketNotSupportedException();
            }
        }
    }

    private boolean doHandshake() throws BluetoothDisabledException, IOException {
        return sendHandshake() && receiveHandshakeResponse();
    }

    private boolean sendHandshake() throws BluetoothDisabledException {
        byte[] handshake = new byte[5 + Constants.DISPLAY_APP_NAME.length()];
        handshake[0] = Constants.DISPLAY_PROTOCOL_VERSION;
        handshake[1] = (byte) GameConstants.WIDTH;
        handshake[2] = (byte) GameConstants.HEIGHT;
        handshake[3] = Constants.DISPLAY_COLOR_MODE;
        handshake[4] = (byte) Constants.DISPLAY_APP_NAME.length();
        System.arraycopy(Constants.DISPLAY_APP_NAME.getBytes(), 0, handshake, 5, Constants.DISPLAY_APP_NAME.length());

        return sendDataWithHandlers(handshake);
    }

    private boolean receiveHandshakeResponse() throws BluetoothDisabledException, IOException {
        InputStream inputStream;
        try {
            inputStream = mBluetoothSocket.getInputStream();
        } catch (NullPointerException e) {
            throw new BluetoothDisabledException();
        }

        int handshakeResponse = inputStream.read();
        int maxFramesPerSecondDisplay = inputStream.read();

        if (handshakeResponse != 0) {
            return false;
        }

        mMaxFramesPerSecond = Math.min(maxFramesPerSecondDisplay, Constants.MAX_FRAMES_PER_SECOND_GAME);

        return true;
    }

    private boolean sendDataWithHandlers(byte[] data) throws BluetoothDisabledException {
        if (!establishConnectionIfNecessary()) {
            return attemptToSendDataAgain(data);
        }

        try {
            if (data != null) {
                mOutputStream.write(data);
            }
        } catch (NullPointerException e) {
            throw new BluetoothDisabledException();
        } catch (IOException e) {
            return attemptToSendDataAgain(data);
        }

        return true;
    }

    private boolean establishConnectionIfNecessary() throws BluetoothDisabledException {
        boolean isConnected;
        try {
            isConnected = mBluetoothSocket.isConnected();
        } catch (NullPointerException e) {
            throw new BluetoothDisabledException();
        }

        if (!isConnected) {
            try {
                openConnection();
            } catch (RfcommSocketNotSupportedException e) {
                return false; //can not happen
            } catch (IOException e) {
                return false;
            }

            try {
                doHandshake();
            } catch (IOException e) {
                return false;
            }

            mHandler.sendEmptyMessage(Constants.BT_ESTABLISHED_CONNECTION_MESSAGE);
        }

        return true;
    }

    private boolean attemptToSendDataAgain(byte[] data) throws BluetoothDisabledException {
        if (mStopReconnecting) {
            mHandler.sendEmptyMessage(Constants.BT_IO_ERROR_MESSAGE);
            return false;
        }

        if (mFirstAttemptToReconnect) {
            mHandler.sendEmptyMessage(Constants.BT_BEGIN_IO_ERROR_MESSAGE);
            mFirstAttemptToReconnect = false;
        }

        closeConnection();

        try {
            Thread.sleep(Constants.TIME_BETWEEN_RECONNECTING_ATTEMPTS);
        } catch (InterruptedException e) {
        }


        if (sendDataWithHandlers(data)) {
            mHandler.sendEmptyMessage(Constants.BT_END_IO_ERROR_MESSAGE);
            mFirstAttemptToReconnect = true;
            return true;
        }

        return false;
    }

    private void clearLedAndCloseConnection() {
        try {
            Thread.sleep(1000 / mMaxFramesPerSecond);
        } catch (InterruptedException e) {
        }

        try {
            mOutputStream.write(mGame.clearLedMatrix());
        } catch (NullPointerException e) {
        } catch (IOException e) {
        }

        closeConnection();
    }

    private void closeConnection() {
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }
        } catch (NullPointerException e) {
        } catch (IOException e) {
        }

        try {
            if (mBluetoothSocket != null) {
                mBluetoothSocket.close();
            }
        } catch (NullPointerException e) {

        } catch (IOException e) {
        }
    }
}