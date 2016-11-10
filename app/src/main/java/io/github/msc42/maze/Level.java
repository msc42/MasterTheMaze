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
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a level.
 * <p/>
 * It generates levels from JSON files.
 * Rules for a level:<br/>
 * NextLevelPoint > TeleportPoint > BorderPoint > DynamicPoint<br/>
 * Consequently, NextLevelPoint and TeleportPoint secure mHero point
 * The number of the steps of an enemy point must be in x and y direction equal.
 * Neither a static point nor a dynamic point are allowed to go out of the field.
 * After all steps of an enemy point, it must be at its start position.
 * The destination of a teleport point must not have a border point.
 * The start position of the mHero point must not have a border point.
 * <p/>
 * A level can be correct, but maybe it cannot make sense or are to difficult, please test every level.
 *
 * @author Stefan Constantin
 */
class Level {

    protected static final String JSON_BEGIN_PATH_PREFIX = "/MasterTheMaze/Level";
    protected static final String JSON_FILE_NAME_PREFIX = "Level";
    protected static final String JSON_EXTENSION = ".json";
    protected static final String JSON_END_FILE_NAME = "End" + JSON_EXTENSION;

    protected static final String JSON_HEROPOINT_ID = "Heropoint";
    protected static final String JSON_NEXTLEVELPOINTS_ID = "Nextlevelpoints";
    protected static final String JSON_BORDERPOINTS_ID = "Borderpoints";
    protected static final String JSON_TELEPORTPOINTS_ID = "Teleportpoints";
    protected static final String JSON_ENEMYPOINTS_ID = "Enemypoints";

    protected static final String JSON_X_AXIS_ID = "x";
    protected static final String JSON_Y_AXIS_ID = "y";
    protected static final String JSON_X_AXIS_STEP_ID = "stepX";
    protected static final String JSON_Y_AXIS_STEP_ID = "stepY";
    protected static final String JSON_X_AXIS_DESTINATION_ID = "destinationX";
    protected static final String JSON_Y_AXIS_DESTINATION_ID = "destinationY";
    protected static final String JSON_X_AXIS_START_ID = "startX";
    protected static final String JSON_Y_AXIS_START_ID = "startY";
    protected static final String JSON_STEP_INTERVAL_ID = "stepInterval";

    private int mLevelNumber;
    private AssetManager mAssetManager;
    private Game mGame;
    private int mDifficulty;

    private boolean mEnd = false;

    private List<Point>[][] mPoints;
    private HeroPoint mHero;
    private List<EnemyPoint> mDynamicPoints;
    private List<int[]> mTeleportDestinations;


    protected Level(int levelNumber, AssetManager assetManager, Game game, int difficulty)
            throws InvalidLevelException, IOException {
        this.mLevelNumber = levelNumber;
        this.mAssetManager = assetManager;
        this.mGame = game;
        this.mDifficulty = difficulty;

        mPoints = new ArrayList[GameConstants.WIDTH][GameConstants.HEIGHT];
        for (int i = 0; i < GameConstants.HEIGHT; i++) {
            for (int j = 0; j < GameConstants.WIDTH; j++) {
                mPoints[i][j] = new ArrayList<Point>();
            }
        }

        String jsonFileContent = readJSONFile();

        if (jsonFileContent != null) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(jsonFileContent);
            } catch (JSONException e) {
                throw new InvalidLevelException(GameConstants.ERROR_JSON);
            }

            if (!mEnd) {
                //order is important
                addHero(jsonObject);

                addNextLevelPoints(jsonObject);

                mTeleportDestinations = new ArrayList<int[]>();
                addTeleportPoints(jsonObject);

                addBorderPoints(jsonObject);

                if (!checkTeleportDestination()) {
                    throw new InvalidLevelException(GameConstants.ERROR_TELEPORT_POSITION);
                }

                if (!checkHero()) {
                    throw new InvalidLevelException(GameConstants.ERROR_HERO_POSITION);
                }

                mDynamicPoints = new ArrayList<EnemyPoint>();
                addEnemyPoints(jsonObject);
            } else {
                addBorderPoints(jsonObject);
            }
        }
    }

    protected List<EnemyPoint> getDynamicPoints() {
        return mDynamicPoints;
    }

    protected List<Point>[][] getPoints() {
        return mPoints;
    }

    protected HeroPoint getHero() {
        return mHero;
    }

    private String readJSONFile() throws IOException {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            String file = Environment.getExternalStorageDirectory().toString()
                    + JSON_BEGIN_PATH_PREFIX + mLevelNumber + JSON_EXTENSION;
            try {
                InputStream inputStream = new FileInputStream(new File(file));
                return readJSONFromInputStream(inputStream);
            } catch (FileNotFoundException e) {
                return readJSONFromAsset();
            }
        }
        return readJSONFromAsset();
    }

    private String readJSONFromAsset() throws IOException {
        InputStream inputStream;
        try {
            inputStream = mAssetManager.open(JSON_FILE_NAME_PREFIX + mLevelNumber + JSON_EXTENSION);
        } catch (IOException e) {
            inputStream = mAssetManager.open(JSON_END_FILE_NAME);
            mEnd = true;
        }
        return readJSONFromInputStream(inputStream);
    }

    private String readJSONFromInputStream(InputStream inputStream) throws IOException {
        String jsonFileContent = "";
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            StringBuilder jsonFile = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                jsonFile.append(line);
            }
            jsonFileContent = jsonFile.toString();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                }
            }
        }
        return jsonFileContent;
    }

    private void addHero(JSONObject jsonObject) throws InvalidLevelException {
        try {
            JSONObject heroPoint = jsonObject.getJSONObject(JSON_HEROPOINT_ID);
            int x = heroPoint.getInt(JSON_X_AXIS_ID);
            int y = heroPoint.getInt(JSON_Y_AXIS_ID);
            int stepX = heroPoint.getInt(JSON_X_AXIS_STEP_ID);
            int stepY = heroPoint.getInt(JSON_Y_AXIS_STEP_ID);

            if (!staticCheck(x, y)) {
                throw new InvalidLevelException(GameConstants.ERROR_HERO_POSITION);
            }

            mHero = new HeroPoint(x, y, stepX, stepY, mGame);

        } catch (JSONException e) {
            throw new InvalidLevelException(GameConstants.ERROR_HERO);
        }
    }

    private void addNextLevelPoints(JSONObject jsonObject) throws InvalidLevelException {
        try {
            JSONArray nextLevelPoints = jsonObject.getJSONArray(JSON_NEXTLEVELPOINTS_ID);

            for (int i = 0; i < nextLevelPoints.length(); i++) {
                JSONObject nextLevelPoint = nextLevelPoints.getJSONObject(i);
                int x = nextLevelPoint.getInt(JSON_X_AXIS_ID);
                int y = nextLevelPoint.getInt(JSON_Y_AXIS_ID);

                if (!staticCheck(x, y)) {
                    throw new InvalidLevelException(GameConstants.ERROR_NEXT_POSITION);
                }

                mPoints[x][y].add(new NextLevelPoint());
            }
        } catch (JSONException e) {
            throw new InvalidLevelException(GameConstants.ERROR_NEXT);
        }
    }

    private void addTeleportPoints(JSONObject jsonObject) throws InvalidLevelException {
        try {
            JSONArray teleport = jsonObject.getJSONArray(JSON_TELEPORTPOINTS_ID);

            try {
                for (int i = 0; i < teleport.length(); i++) {
                    JSONObject nextLevelPoint = teleport.getJSONObject(i);
                    int x = nextLevelPoint.getInt(JSON_X_AXIS_ID);
                    int y = nextLevelPoint.getInt(JSON_Y_AXIS_ID);
                    int destinationX = nextLevelPoint.getInt(JSON_X_AXIS_DESTINATION_ID);
                    int destinationY = nextLevelPoint.getInt(JSON_Y_AXIS_DESTINATION_ID);

                    if (!staticCheck(x, y) || !staticCheck(destinationX, destinationY)) {
                        throw new InvalidLevelException(GameConstants.ERROR_TELEPORT_POSITION);
                    }

                    mPoints[x][y].add(new TeleportPoint(destinationX, destinationY));
                    mTeleportDestinations.add(new int[]{destinationX, destinationY});
                }
            } catch (JSONException e) {
                throw new InvalidLevelException(GameConstants.ERROR_TELEPORT);
            }
        } catch (JSONException e) {
            // teleport points are only optional
        }
    }

    private void addBorderPoints(JSONObject jsonObject) throws InvalidLevelException {
        try {
            JSONArray borderPoints = jsonObject.getJSONArray(JSON_BORDERPOINTS_ID);

            try {
                for (int i = 0; i < borderPoints.length(); i++) {
                    JSONObject borderPoint = borderPoints.getJSONObject(i);
                    int x = borderPoint.getInt(JSON_X_AXIS_ID);
                    int y = borderPoint.getInt(JSON_Y_AXIS_ID);

                    if (!staticCheck(x, y)) {
                        throw new InvalidLevelException(GameConstants.ERROR_BORDER_POSITION);
                    }

                    mPoints[x][y].add(new BorderPoint());
                }
            } catch (JSONException e) {
                throw new InvalidLevelException(GameConstants.ERROR_BORDER);
            }
        } catch (JSONException e) {
            // border points are only optional
        }
    }

    private void addEnemyPoints(JSONObject jsonObject) throws InvalidLevelException {
        try {
            JSONArray enemies = jsonObject.getJSONArray(JSON_ENEMYPOINTS_ID);

            try {
                for (int i = 0; i < enemies.length(); i++) {
                    JSONObject enemy = enemies.getJSONObject(i);
                    int startX = enemy.getInt(JSON_X_AXIS_START_ID);
                    int startY = enemy.getInt(JSON_Y_AXIS_START_ID);
                    int stepIntervall = enemy.getInt(JSON_STEP_INTERVAL_ID);
                    stepIntervall *= 2 * (mDifficulty + 1);

                    JSONArray stepXJSONArray = enemy.getJSONArray(JSON_X_AXIS_STEP_ID);
                    int[] stepX = new int[stepXJSONArray.length()];
                    for (int j = 0; j < stepXJSONArray.length(); j++) {
                        stepX[j] = stepXJSONArray.getInt(j);
                    }

                    JSONArray stepYJSONArray = enemy.getJSONArray(JSON_Y_AXIS_STEP_ID);
                    int[] stepY = new int[stepYJSONArray.length()];
                    for (int j = 0; j < stepYJSONArray.length(); j++) {
                        stepY[j] = stepYJSONArray.getInt(j);
                    }

                    if (stepX.length != stepY.length || !dynamicCheck(startX, startY, stepX, stepY)) {
                        throw new InvalidLevelException(GameConstants.ERROR_ENEMIES_POSITION);
                    }

                    EnemyPoint enemyPoint = new EnemyPoint(startX, startY, stepX, stepY, stepIntervall);
                    mDynamicPoints.add(enemyPoint);
                    mPoints[startX][startY].add(enemyPoint);

                }
            } catch (JSONException e) {
                throw new InvalidLevelException(GameConstants.ERROR_ENEMIES);
            }
        } catch (JSONException e) {
            // enemy points are only optional
        }
    }

    private boolean staticCheck(int x, int y) {
        return (x >= 0 && y >= 0 && x < GameConstants.WIDTH && y < GameConstants.HEIGHT);
    }

    private boolean dynamicCheck(int startX, int startY, int[] stepX, int[] stepY) {
        int x = startX;
        int y = startY;
        for (int i = 0; i < stepX.length; i++) {
            x += stepX[i];
            y += stepY[i];
            if (x < 0 || y < 0 || x >= GameConstants.WIDTH || y >= GameConstants.HEIGHT) {
                return false;
            }
        }

        return (x == startX && y == startY);
    }

    private boolean checkTeleportDestination() {
        for (int[] destination : mTeleportDestinations) {
            for (Point p : mPoints[destination[0]][destination[1]]) {
                if (p instanceof BorderPoint) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkHero() {
        for (Point p : mPoints[mHero.getX()][mHero.getY()]) {
            if (p instanceof BorderPoint) {
                return false;
            }
        }
        return true;
    }
}