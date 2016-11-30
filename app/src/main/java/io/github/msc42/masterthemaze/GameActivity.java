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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import io.github.msc42.maze.Game;
import io.github.msc42.maze.InvalidLevelException;

/**
 * Represents the activity during playing the game.
 * Requires a set screen orientation in the AndroidManifest.xml file.
 *
 * @author Stefan Constantin
 */
public final class GameActivity extends Activity {

    private Intent mIntent;
    private boolean mMotion;
    private int mSpeed;
    private float mSensitivity;
    private int mRotation;

    private Game mGame;
    private ArrayBlockingQueue<Integer> mMotionQueue = new ArrayBlockingQueue<Integer>(Constants.MOTION_QUEUE_SIZE);
    private final CurrentMoveDirection mCurrentMoveDirection = new CurrentMoveDirection();

    private Handler mGameThreadMessageHandler;

    private GameThread mGameThread;
    private AddCurrentDirectionToQueueThread mAddCurrentDirectionToQueueThread;
    private HandlerThread mSensorHandlerThread;
    private Handler mSensorHandler;
    private AccelerometerEventListener mAccelerometerEventListener;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;

    private TextView mMotionDescriptionTextView;
    private TextView mTouchDescriptionTextView;
    private Button mUpButton;
    private Button mRightButton;
    private Button mDownButton;
    private Button mLeftButton;

    ArrayList<AlertDialog> mCausedByHandlerDialogsWhichCallFinish = new ArrayList<AlertDialog>();
    AlertDialog mBluetoothIoErrorDialog;
    AlertDialog mTurnBluetoothOnDialog;
    AlertDialog mExitDialog;

    private boolean mNoFinishGameActivityCalled = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIntent = getIntent();

        initScreen();
        getScreenElements();

        if (checkIfValidAddress()) {
            if (checkIfSensorForMotionControlIsAvailableAndInitMotionControlParameter()) {
                initBluetoothAndStartGame();
            }
        }
    }

    private void initScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mMotion = mIntent.getBooleanExtra(Constants.EXTRA_MESSAGE_CONTROL, Constants.DEFAULT_MOTION);
        if (mMotion) {
            setContentView(R.layout.activity_game_motion);
        } else {
            setContentView(R.layout.activity_game_touch);
        }
    }

    private void getScreenElements() {
        if (mMotion) {
            mMotionDescriptionTextView = (TextView) findViewById(R.id.motionDescriptionTextView);
        } else {
            mTouchDescriptionTextView = (TextView) findViewById(R.id.touchDescriptionTextView);
            mUpButton = (Button) findViewById(R.id.upButton);
            mRightButton = (Button) findViewById(R.id.rightButton);
            mDownButton = (Button) findViewById(R.id.downButton);
            mLeftButton = (Button) findViewById(R.id.leftButton);
        }
    }

    private boolean checkIfValidAddress() {
        String address = mIntent.getStringExtra(Constants.EXTRA_MESSAGE_MAC);

        if (address == null || address.length() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.error)
                    .setMessage(R.string.noLedDisplaySetMain)
                    .setCancelable(false)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });

            builder.create().show();
            return false;
        }
        return true;
    }

    private boolean checkIfSensorForMotionControlIsAvailableAndInitMotionControlParameter() {
        if (mMotion) {
            SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            if (accelerometer == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.error)
                        .setMessage(R.string.noAccelerometerExisting)
                        .setCancelable(false)
                        .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });

                builder.create().show();
                return false;
            }

            mSensitivity = mIntent.getFloatExtra(Constants.EXTRA_MESSAGE_SENSITIVITY,
                    Constants.DEFAULT_SENSOR_SENSITIVITY);
            mSpeed = mIntent.getIntExtra(Constants.EXTRA_MESSAGE_SPEED, Constants.DEFAULT_SPEED);

            mRotation = getWindowManager().getDefaultDisplay().getRotation();
        }

        return true;
    }

    private void initBluetoothAndStartGame() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter != null) {
            enableBluetoothAndStartGame();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.error)
                    .setMessage(R.string.noBluetoothExisting)
                    .setCancelable(false)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });

            builder.create().show();
        }
    }

    private void enableBluetoothAndStartGame() {
        if (mBluetoothAdapter.isEnabled()) {
            requestReadExternalStoragePermissionAndStartGame();
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            // if request is satisfied, in onActivityResult requestReadExternalStoragePermissionAndStartGame
            // is called, otherwise there is an alert dialog, which finishes the app when it is confirmed
        }
    }

    private void requestReadExternalStoragePermissionAndStartGame() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            startGame();
        } else {
            ActivityCompat.requestPermissions(GameActivity.this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.REQUEST_READING_EXTERNAL_STORAGE_PERMISSIONS);
            // if request is satisfied, in onRequestPermissionsResult startGame is called,
            // otherwise there is an alert dialog, which finishes the app when it is confirmed
        }
    }

    private void startGame() {
        String macAddress = mIntent.getStringExtra(Constants.EXTRA_MESSAGE_MAC);
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddress);

        if (initGame()) {
            startGameThreads();
        }
    }

    private boolean initGame() {
        int difficulty = mIntent.getIntExtra(Constants.EXTRA_MESSAGE_DIFFICULTY, Constants.DEFAULT_DIFFICULTY);
        try {
            mGame = new Game(getAssets(), difficulty);
        } catch (InvalidLevelException e) {
            showInvalidLevelDialog(e.getMessage());
            return false;
        } catch (IOException e) {
            showIoErrorDialog();
            return false;
        }

        return true;
    }

    private void showInvalidLevelDialog(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error)
                .setMessage(getString(R.string.invalidLevel, errorMessage))
                .setCancelable(false)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishGameActivity();
                    }
                });

        AlertDialog alertDialog = builder.create();
        mCausedByHandlerDialogsWhichCallFinish.add(alertDialog);

        if (mNoFinishGameActivityCalled) {
            alertDialog.show();
        }
    }

    private void showIoErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error)
                .setMessage(R.string.levelStorageReadingError)
                .setCancelable(false)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishGameActivity();
                    }
                });

        AlertDialog alertDialog = builder.create();
        mCausedByHandlerDialogsWhichCallFinish.add(alertDialog);

        if (mNoFinishGameActivityCalled) {
            alertDialog.show();
        }
    }

    private void startGameThreads() {
        mGameThreadMessageHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                super.handleMessage(message);

                switch (message.what) {
                    case Constants.INVALID_LEVEL_MESSAGE:
                        String errorMessage = (String) message.obj;
                        showInvalidLevelDialog(errorMessage);
                        break;
                    case Constants.IO_ERROR_MESSAGE:
                        showIoErrorDialog();
                        break;
                    case Constants.BT_ESTABLISHED_CONNECTION_MESSAGE:
                        showBluetoothEstablishedConnectionScreen();
                        break;
                    case Constants.BT_CANNOT_ESTABLISH_CONNECTION_MESSAGE:
                        showBluetoothCannotEstablishConnectionDialog();
                        break;
                    case Constants.FAILED_HANDSHAKE_MESSAGE:
                        showFailedHandshakeDialog();
                        break;
                    case Constants.BT_BEGIN_IO_ERROR_MESSAGE:
                        showBtBeginIoErrorDialog();
                        break;
                    case Constants.BT_END_IO_ERROR_MESSAGE:
                        if (mBluetoothIoErrorDialog != null) {
                            mBluetoothIoErrorDialog.cancel();
                        }
                        break;
                    case Constants.BT_IO_ERROR_MESSAGE:
                        showBtSendIoErrorDialog();
                        break;
                    case Constants.DISABLED_BLUETOOTH_MESSAGE:
                        showDoNotDisableBluetoothDialog();
                        break;
                    case Constants.RFCOMM_SOCKET_NOT_SUPPORTED_MESSAGE:
                        showRfcommSocketNotSupportedDialog();
                        break;
                    default:
                        // all possible messages should get a case
                        break;
                }
            }
        };

        mGameThread = new GameThread(mGame, mMotionQueue, mBluetoothDevice, mGameThreadMessageHandler);
        mGameThread.start();

        if (mMotion) {
            mSensorHandlerThread = new HandlerThread("SensorHandlerThread", Process.THREAD_PRIORITY_URGENT_DISPLAY);
            mSensorHandlerThread.start();
            mSensorHandler = new Handler(mSensorHandlerThread.getLooper());
            mAccelerometerEventListener = new AccelerometerEventListener(mRotation, mSensitivity, mCurrentMoveDirection, mMotionQueue);
        }
    }

    private void showBluetoothEstablishedConnectionScreen() {
        if (mMotion) {
            mMotionDescriptionTextView.setText(R.string.motionDescription);
        } else {
            mTouchDescriptionTextView.setText(R.string.touchDescription);
            mUpButton.setVisibility(View.VISIBLE);
            mRightButton.setVisibility(View.VISIBLE);
            mDownButton.setVisibility(View.VISIBLE);
            mLeftButton.setVisibility(View.VISIBLE);
        }
    }

    private void showBluetoothCannotEstablishConnectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error)
                .setMessage(R.string.cannotEstablishConnection)
                .setCancelable(false)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishGameActivity();
                    }
                });

        AlertDialog alertDialog = builder.create();
        mCausedByHandlerDialogsWhichCallFinish.add(alertDialog);

        if (mNoFinishGameActivityCalled) {
            alertDialog.show();
        }
    }

    private void showBtBeginIoErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error)
                .setMessage(R.string.tryToReconnect)
                .setCancelable(false)
                .setPositiveButton(R.string.abort, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mGameThread.stopReconnecting();
                    }
                });

        mBluetoothIoErrorDialog = builder.create();
        mBluetoothIoErrorDialog.show();
    }

    private void showBtSendIoErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error)
                .setMessage(R.string.bluetoothConnectionError)
                .setCancelable(false)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishGameActivity();
                    }
                });

        AlertDialog alertDialog = builder.create();
        mCausedByHandlerDialogsWhichCallFinish.add(alertDialog);

        if (mNoFinishGameActivityCalled) {
            alertDialog.show();
        }

        if (mBluetoothIoErrorDialog != null) {
            mBluetoothIoErrorDialog.dismiss();
        }
    }

    private void showFailedHandshakeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error)
                .setMessage(R.string.failedHandshake)
                .setCancelable(false)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishGameActivity();
                    }
                });

        AlertDialog alertDialog = builder.create();
        mCausedByHandlerDialogsWhichCallFinish.add(alertDialog);

        if (mNoFinishGameActivityCalled) {
            alertDialog.show();
        }
    }

    private void showDoNotDisableBluetoothDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error)
                .setMessage(R.string.bluetoothTurnedOffWhileUsing)
                .setCancelable(false)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishGameActivity();
                    }
                });

        AlertDialog alertDialog = builder.create();
        mCausedByHandlerDialogsWhichCallFinish.add(alertDialog);

        if (mNoFinishGameActivityCalled) {
            alertDialog.show();
        }
    }

    private void showRfcommSocketNotSupportedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error)
                .setMessage(R.string.rfcommNotSupported)
                .setCancelable(false)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishGameActivity();
                    }
                });

        AlertDialog alertDialog = builder.create();
        mCausedByHandlerDialogsWhichCallFinish.add(alertDialog);

        if (mNoFinishGameActivityCalled) {
            alertDialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGameThread != null) {
            mGameThread.restart();
        }

        if (mMotion) {
            if (mSpeed > 0) {
                mAddCurrentDirectionToQueueThread = new AddCurrentDirectionToQueueThread(
                        mMotionQueue, mCurrentMoveDirection, mSpeed);
                mAddCurrentDirectionToQueueThread.start();
            }

            SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mAccelerometerEventListener = new AccelerometerEventListener(mRotation, mSensitivity,
                    mCurrentMoveDirection, mMotionQueue);

            sensorManager.registerListener(mAccelerometerEventListener, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME, mSensorHandler);
        }

        if (mMotion) {
            mMotionDescriptionTextView.setText(R.string.connecting);
        } else {
            mTouchDescriptionTextView.setText(R.string.connecting);
            mUpButton.setVisibility(View.INVISIBLE);
            mRightButton.setVisibility(View.INVISIBLE);
            mDownButton.setVisibility(View.INVISIBLE);
            mLeftButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGameThread != null) {
            mGameThread.pause();
        }

        if (mAddCurrentDirectionToQueueThread != null) {
            mAddCurrentDirectionToQueueThread.terminate();
        }

        if (mMotion) {
            SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.unregisterListener(mAccelerometerEventListener, accelerometer);
        }

        if (mMotion) {
            mMotionDescriptionTextView.setText(R.string.pause);
        } else {
            mTouchDescriptionTextView.setText(R.string.pause);
            mUpButton.setVisibility(View.INVISIBLE);
            mRightButton.setVisibility(View.INVISIBLE);
            mDownButton.setVisibility(View.INVISIBLE);
            mLeftButton.setVisibility(View.INVISIBLE);
        }
    }

    private void finishGameActivity() {
        if (mNoFinishGameActivityCalled) {
            mNoFinishGameActivityCalled = false;
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        finishActivity(Constants.REQUEST_ENABLE_BT);

        if (mTurnBluetoothOnDialog != null) {
            mTurnBluetoothOnDialog.dismiss();
        }

        if (mExitDialog != null) {
            mExitDialog.dismiss();
        }

        if (mBluetoothIoErrorDialog != null) {
            mBluetoothIoErrorDialog.dismiss();
        }

        closeAllAlertDialogs();

        // quiteSafely (added in API 18) is not necessary, because it does not matter if not all messages are delivered
        if (mSensorHandlerThread != null) {
            mSensorHandlerThread.quit();
        }

        if (mAddCurrentDirectionToQueueThread != null) {
            mAddCurrentDirectionToQueueThread.terminate();
        }

        if (mGameThread != null) {
            mGameThread.terminate();
        }
    }

    private void closeAllAlertDialogs() {
        for (AlertDialog alertDialog : mCausedByHandlerDialogsWhichCallFinish) {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK || mBluetoothAdapter.isEnabled()) {
                    requestReadExternalStoragePermissionAndStartGame();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.error)
                            .setMessage(R.string.bluetoothOffGame)
                            .setCancelable(false)
                            .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });

                    mTurnBluetoothOnDialog = builder.create();
                    mTurnBluetoothOnDialog.show();
                }
                break;
            default:
                // other request codes are ignored
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Constants.REQUEST_READING_EXTERNAL_STORAGE_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.info)
                            .setMessage(R.string.getPermissionReadStorageWorkaround)
                            .setCancelable(false)
                            .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startGame();
                                }
                            });
                    builder.create().show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.info)
                            .setMessage(R.string.noPermissionReadStorage)
                            .setCancelable(false)
                            .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startGame();
                                }
                            });
                    builder.create().show();
                }
                break;
            default:
                // other request codes are ignored
                break;
        }
    }

    public void clickUpButton(View v) {
        try {
            mMotionQueue.add(Game.UP);
        } catch (IllegalStateException e) {
            // if queue is full, move is discarded
        }
    }

    public void clickRightButton(View v) {
        try {
            mMotionQueue.add(Game.RIGHT);
        } catch (IllegalStateException e) {
            // if queue is full, move is discarded
        }
    }

    public void clickDownButton(View v) {
        try {
            mMotionQueue.add(Game.DOWN);
        } catch (IllegalStateException e) {
            // if queue is full, move is discarded
        }
    }

    public void clickLeftButton(View v) {
        try {
            mMotionQueue.add(Game.LEFT);
        } catch (IllegalStateException e) {
            // if queue is full, move is discarded
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.exit)
                .setMessage(R.string.exitGameMessage)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, null);

        mExitDialog = builder.create();
        mExitDialog.show();
    }
}