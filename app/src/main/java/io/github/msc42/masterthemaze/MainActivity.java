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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Represents the main activity.
 *
 * @author Stefan Constantin
 */
public final class MainActivity extends AppCompatActivity {

    private SharedPreferences mSettings;

    private String mBluetoothDeviceName;
    private String mBluetoothAddress;
    private String[] mDifficulties;
    private int mDifficulty;
    private boolean mMotion;
    private String[] mSpeedLevels;
    private int mSpeed;
    private float mSensitivity;

    private TextView mLedsDeviceTextView;
    private TextView mDifficultyLevelBarLabel;
    private SeekBar mDifficultyLevelBar;
    private TextView mSpeedBarLabel;
    private SeekBar mSpeedBar;
    private TextView mSensitivityBarLabel;
    private SeekBar mSensitivityBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSettings = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        initBluetooth();
        initDifficulty();
        initSpeed();
        initSensitivity();
        initMotion();
    }

    private void initBluetooth() {
        Intent intent = getIntent();
        mBluetoothDeviceName = intent.getStringExtra(Constants.EXTRA_MESSAGE_DEVICE_NAME);
        mBluetoothAddress = intent.getStringExtra(Constants.EXTRA_MESSAGE_MAC);

        if (mBluetoothAddress == null) {
            mBluetoothDeviceName = mSettings.getString(Constants.PREFS_MAC_NAME, null);
            mBluetoothAddress = mSettings.getString(Constants.PREFS_MAC, null);
        }

        mLedsDeviceTextView = (TextView) findViewById(R.id.ledsDeviceTextView);

        if (mBluetoothAddress != null && mBluetoothAddress.length() > 0) {
            if (mBluetoothDeviceName == null || mBluetoothDeviceName.length() == 0) {
                mLedsDeviceTextView.setText(getString(
                        R.string.ledDisplaySet, getString(R.string.noLedDisplayName), mBluetoothAddress));
            } else {
                mLedsDeviceTextView.setText(getString(
                        R.string.ledDisplaySet, mBluetoothDeviceName, mBluetoothAddress));
            }
        }
    }

    private void initDifficulty() {
        mDifficulties = new String[]{getString(R.string.very_difficult),
                getString(R.string.difficult), getString(R.string.medium), getString(R.string.easy)};

        mDifficulty = mSettings.getInt(Constants.PREFS_DIFFICULTY, mDifficulties.length - 1);

        mDifficultyLevelBarLabel = (TextView) findViewById(R.id.difficultyLevelBarLabel);
        mDifficultyLevelBarLabel.setText(getString(
                R.string.difficultyLevelBarLabel, mDifficulties[mDifficulty]));

        mDifficultyLevelBar = (SeekBar) findViewById(R.id.difficultyLevelBar);
        mDifficultyLevelBar.incrementProgressBy(1);
        mDifficultyLevelBar.setMax(mDifficulties.length - 1);
        mDifficultyLevelBar.setProgress(mDifficulties.length - 1 - mDifficulty);

        mDifficultyLevelBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mDifficulty = mDifficulties.length - 1 - progress;
                mDifficultyLevelBarLabel.setText(getString(
                        R.string.difficultyLevelBarLabel, mDifficulties[mDifficulty]));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    // minimum speedLevel should be 1 although the app can handle speedLevel 1,
    // because it speedLevel 0 is difficult to explain
    private void initSpeed() {
        mSpeedLevels = new String[]{getString(R.string.very_fast), getString(R.string.fast),
                getString(R.string.normal), getString(R.string.slowly), getString(R.string.very_slowly)};

        mSpeed = mSettings.getInt(Constants.PREFS_SPEED, Constants.DEFAULT_SPEED);

        mSpeedBarLabel = (TextView) findViewById(R.id.speedBarLabel);
        mSpeedBarLabel.setText(getString(R.string.speedBarLabel,
                mSpeedLevels[(mSpeed / Constants.SPEED_LEVEL_MULTIPLICATOR) - 1]));

        mSpeedBar = (SeekBar) findViewById(R.id.speedBar);
        mSpeedBar.incrementProgressBy(1);
        mSpeedBar.setMax(mSpeedLevels.length - 1);
        mSpeedBar.setProgress(mSpeed / Constants.SPEED_LEVEL_MULTIPLICATOR - 1);

        mSpeedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int speedLevel, boolean fromUser) {
                mSpeed = (speedLevel + 1) * Constants.SPEED_LEVEL_MULTIPLICATOR;
                mSpeedBarLabel.setText(getString(R.string.speedBarLabel,
                        mSpeedLevels[(mSpeed / Constants.SPEED_LEVEL_MULTIPLICATOR) - 1]));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void initSensitivity() {
        mSensitivity = mSettings.getFloat(Constants.PREFS_SENSITIVITY, Constants.DEFAULT_SENSOR_SENSITIVITY);

        mSensitivityBarLabel = (TextView) findViewById(R.id.sensitivityBarLabel);
        mSensitivityBarLabel.setText(getString(R.string.sensitivityBarLabel, mSensitivity));

        mSensitivityBar = (SeekBar) findViewById(R.id.sensitivityBar);
        mSensitivityBar.incrementProgressBy(1);
        mSensitivityBar.setMax((Constants.MAX_SENSITIVITY - Constants.MIN_SENSITIVITY)
                * Constants.SENSITIVITY_MULTIPLICATOR);
        mSensitivityBar.setProgress((int) (mSensitivity * Constants.SENSITIVITY_MULTIPLICATOR)
                - (int) (Constants.MIN_SENSITIVITY * Constants.SENSITIVITY_MULTIPLICATOR));

        mSensitivityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int sensitivityLevel, boolean fromUser) {
                mSensitivity = (Constants.MIN_SENSITIVITY * Constants.SENSITIVITY_MULTIPLICATOR + sensitivityLevel)
                        / (float) Constants.SENSITIVITY_MULTIPLICATOR;
                mSensitivityBarLabel.setText(getString(R.string.sensitivityBarLabel, mSensitivity));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    // requires initSpeed  and Sensitivity are called before
    private void initMotion() {
        mMotion = mSettings.getBoolean(Constants.PREFS_MOTION, Constants.DEFAULT_MOTION);
        RadioButton motionControlRadioButton = (RadioButton) findViewById(R.id.motionControlRadioButton);
        RadioButton touchControlRadioButton = (RadioButton) findViewById(R.id.touchControlRadioButton);
        if (mMotion) {
            motionControlRadioButton.setChecked(true);
            touchControlRadioButton.setChecked(false);
            mSpeedBarLabel.setVisibility(View.VISIBLE);
            mSpeedBar.setVisibility(View.VISIBLE);
            mSensitivityBarLabel.setVisibility(View.VISIBLE);
            mSensitivityBar.setVisibility(View.VISIBLE);
        } else {
            touchControlRadioButton.setChecked(true);
            motionControlRadioButton.setChecked(false);
            mSpeedBarLabel.setVisibility(View.GONE);
            mSpeedBar.setVisibility(View.GONE);
            mSensitivityBarLabel.setVisibility(View.GONE);
            mSensitivityBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(Constants.PREFS_MAC_NAME, mBluetoothDeviceName);
        editor.putString(Constants.PREFS_MAC, mBluetoothAddress);
        editor.putInt(Constants.PREFS_DIFFICULTY, mDifficulty);
        editor.putInt(Constants.PREFS_SPEED, mSpeed);
        editor.putFloat(Constants.PREFS_SENSITIVITY, mSensitivity);
        editor.putBoolean(Constants.PREFS_MOTION, mMotion);
        editor.commit();
    }

    public void startGame(View v) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(Constants.EXTRA_MESSAGE_CONTROL, mMotion);

        if (mBluetoothDeviceName != null) {
            intent.putExtra(Constants.EXTRA_MESSAGE_DEVICE_NAME, mBluetoothDeviceName);
        } else {
            intent.putExtra(Constants.EXTRA_MESSAGE_DEVICE_NAME, getString(R.string.noLedDisplayName));
        }

        intent.putExtra(Constants.EXTRA_MESSAGE_MAC, mBluetoothAddress);
        intent.putExtra(Constants.EXTRA_MESSAGE_SPEED, mSpeed);
        intent.putExtra(Constants.EXTRA_MESSAGE_DIFFICULTY, mDifficulty);
        intent.putExtra(Constants.EXTRA_MESSAGE_SENSITIVITY, mSensitivity);
        startActivity(intent);
    }

    public void openGameDescription(View v) {
        Intent intent = new Intent(this, GameDescriptionActivity.class);
        startActivity(intent);
    }

    public void searchDevice(View v) {
        Intent intent = new Intent(this, SearchDeviceActivity.class);
        startActivity(intent);
    }

    public void changeMAC(View v) {
        Intent intent = new Intent(this, SetDeviceActivity.class);
        intent.putExtra(Constants.EXTRA_MESSAGE_DEVICE_NAME, mBluetoothDeviceName);
        intent.putExtra(Constants.EXTRA_MESSAGE_MAC, mBluetoothAddress);
        startActivity(intent);
    }

    public void enableMotion(View v) {
        mMotion = true;
        mSpeedBarLabel.setVisibility(View.VISIBLE);
        mSpeedBar.setVisibility(View.VISIBLE);
        mSensitivityBarLabel.setVisibility(View.VISIBLE);
        mSensitivityBar.setVisibility(View.VISIBLE);
    }

    public void enableTouch(View v) {
        mMotion = false;
        mSpeedBarLabel.setVisibility(View.GONE);
        mSpeedBar.setVisibility(View.GONE);
        mSensitivityBarLabel.setVisibility(View.GONE);
        mSensitivityBar.setVisibility(View.GONE);
    }
}