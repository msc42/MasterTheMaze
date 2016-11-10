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
 * Represents a border point.
 *
 * @author Stefan Constantin
 */
class BorderPoint implements Point {

    /**
     * Creates a new border point.
     */
    protected BorderPoint() {
    }

    @Override
    public byte getPixelValue() {
        return GameConstants.LED_HALF;
    }
}