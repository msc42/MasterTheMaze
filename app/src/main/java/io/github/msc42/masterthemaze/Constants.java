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

/**
 * Represents important constants of the Master the maze app.
 *
 * @author Stefan Constantin
 */
final class Constants {

    // 0 <= MAX_FRAMES_PER_SECOND_GAME, emulator has problems with more than 12 FPS
    protected static final int MAX_FRAMES_PER_SECOND_GAME = 12;

    protected static final float ADD_CURRENT_DIRECTION_TO_QUEUE_FIRST_TIME_FACTOR = 2;

    protected static final int MOTION_QUEUE_SIZE = 5; // 1 <= MOTION_QUEUE_SIZE
    protected static final int RFCOMM_PORT = 16; // 1 <= RFCOMM_PORT <= 30 == BluetoothSocket.MAX_RFCOMM_CHANNEL
    protected static final int WAIT_TIME_AFTER_CONNECT = 1000; // 0 <= WAIT_TIME_AFTER_CONNECT
    protected static final int GAME_THREAD_PAUSE_SLEEPTIME = 1500; // 0 <= GAME_THREAD_PAUSE_SLEEPTIME
    protected static final int TIME_BETWEEN_RECONNECTING_ATTEMPTS = 2000; // 0 <= TIME_BETWEEN_RECONNECTING_ATTEMPTS

    // 0 <= ADD_CURRENT_DIRECTION_TO_QUEUE_WAIT_SLEEPTIME
    protected static final int ADD_CURRENT_DIRECTION_TO_QUEUE_WAIT_SLEEPTIME = 20;

    protected static final int DEFAULT_DIFFICULTY = 4; // DEFAULT_DIFFICULTY < MainActivity.difficulties.length
    protected static final boolean DEFAULT_MOTION = true;
    protected static final int SPEED_LEVEL_MULTIPLICATOR = 100; // 0 < SPEED_LEVEL_MULTIPLICATOR
    protected static final int DEFAULT_SPEED = 300; // DEFAULT_SPEED < MAX_SPEED_LEVEL * SPEED_LEVEL_MULTIPLICATOR
    protected static final int MIN_SENSITIVITY = 1; // 0 < MIN_SENSITIVITY <= MAX_SENSITIVITY
    protected static final int MAX_SENSITIVITY = 4; // MIN_SENSITIVITY <= MAX_SENSITIVITY
    protected static final int SENSITIVITY_MULTIPLICATOR = 4; // 0 < SENSITIVITY_MULTIPLICATOR

    // MIN_SENSITIVITY <= DEFAULT_SENSOR_SENSITIVITY <= MAX_SENSITIVITY
    protected static final float DEFAULT_SENSOR_SENSITIVITY = 3;

    protected static final int MILLISECONDS_TO_REPEAT_DISCOVERY = 14000;

    protected static final byte DISPLAY_PROTOCOL_VERSION = 1;
    protected static final byte DISPLAY_COLOR_MODE_RED = 0;
    protected static final byte DISPLAY_COLOR_MODE_GREEN = 1;
    protected static final byte DISPLAY_COLOR_MODE_BLUE = 2;

    // emulator supports only DISPLAY_COLOR_MODE_RED
    protected static final byte DISPLAY_COLOR_MODE = DISPLAY_COLOR_MODE_RED;

    protected static final String DISPLAY_APP_NAME = "Master the maze"; // DISPLAY_APP_NAME.length() <= Byte.MAX_VALUE

    protected static final String EXTRA_MESSAGES_PREFIX = "io.github.sgconstantin.masterthemaze.";
    protected static final String EXTRA_MESSAGE_MAC = EXTRA_MESSAGES_PREFIX + "MESSAGE_MAC";
    protected static final String EXTRA_MESSAGE_DEVICE_NAME = EXTRA_MESSAGES_PREFIX + "DEVICE_NAME";
    protected static final String EXTRA_MESSAGE_CONTROL = EXTRA_MESSAGES_PREFIX + "MESSAGE_CONTROL";
    protected static final String EXTRA_MESSAGE_SPEED = EXTRA_MESSAGES_PREFIX + "MESSAGE_SPEED";
    protected static final String EXTRA_MESSAGE_SENSITIVITY = EXTRA_MESSAGES_PREFIX + "MESSAGE_SENSITIVITY";
    protected static final String EXTRA_MESSAGE_DIFFICULTY = EXTRA_MESSAGES_PREFIX + "MESSAGE_DIFFICULTY";

    protected static final String PREFS_NAME = "Master the maze - preferences";
    protected static final String PREFS_MAC_NAME = "macName";
    protected static final String PREFS_MAC = "mac";
    protected static final String PREFS_MOTION = "motion";
    protected static final String PREFS_DIFFICULTY = "difficulty";
    protected static final String PREFS_SPEED = "speed";
    protected static final String PREFS_SENSITIVITY = "sensitivity";

    protected static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 1;
    protected static final int REQUEST_READING_EXTERNAL_STORAGE_PERMISSIONS = 2;
    protected static final int REQUEST_ENABLE_BT = 3;

    protected static final int INVALID_LEVEL_MESSAGE = 1;
    protected static final int IO_ERROR_MESSAGE = 2;
    protected static final int BT_ESTABLISHED_CONNECTION_MESSAGE = 3;
    protected static final int BT_CANNOT_ESTABLISH_CONNECTION_MESSAGE = 4;
    protected static final int FAILED_HANDSHAKE_MESSAGE = 5;
    protected static final int BT_BEGIN_IO_ERROR_MESSAGE = 6;
    protected static final int BT_END_IO_ERROR_MESSAGE = 7;
    protected static final int BT_IO_ERROR_MESSAGE = 8;
    protected static final int DISABLED_BLUETOOTH_MESSAGE = 9;
    protected static final int RFCOMM_SOCKET_NOT_SUPPORTED_MESSAGE = 10;


    private Constants() {
    }
}
