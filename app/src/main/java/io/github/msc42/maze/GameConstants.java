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
 * Represents important constants of the game.
 *
 * @author Stefan Constantin
 */
final public class GameConstants {

    public static final byte LED_OUT = 0;
    public static final byte LED_QUARTER = 63;
    public static final byte LED_HALF = 127;
    public static final byte LED_THREE_QUARTER = (byte) 191;
    public static final byte LED_MAX = (byte) 255;

    public static final int WIDTH = 24; // 0 < WIDTH
    public static final int HEIGHT = 24; // 0 <= HEIGHT

    public static final String ERROR_IO = "ERROR_IO";
    public static final String ERROR_JSON = "JSON_ERROR";
    public static final String ERROR_HERO = "JSON_ERROR_HERO";
    public static final String ERROR_HERO_POSITION = "JSON_ERROR_HERO_POSITION";
    public static final String ERROR_ENEMIES = "JSON_ERROR_ENEMIES";
    public static final String ERROR_ENEMIES_POSITION = "JSON_ERROR_ENEMIES_POSITION";
    public static final String ERROR_BORDER = "JSON_ERROR_BORDER";
    public static final String ERROR_BORDER_POSITION = "JSON_ERROR_BORDER_POSITION";
    public static final String ERROR_TELEPORT = "JSON_ERROR_TELEPORT";
    public static final String ERROR_TELEPORT_POSITION = "JSON_ERROR_TELEPORT_POSITION";
    public static final String ERROR_NEXT = "JSON_ERROR_NEXT";
    public static final String ERROR_NEXT_POSITION = "JSON_ERROR_NEXT_POSITION";


    private GameConstants() {
    }
}
