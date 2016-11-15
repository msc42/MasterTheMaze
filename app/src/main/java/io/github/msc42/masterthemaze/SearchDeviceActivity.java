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
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

/**
 * Represents the search device activity. It searches for paired (need not turned on and visible)
 * and visible devices.
 *
 * @author Stefan Constantin
 */
public final class SearchDeviceActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;

    private ArrayList<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();
    private ArrayList<String> mDevicesList = new ArrayList<String>();

    private Handler mDiscoveryRepeatHandler;

    private boolean mShowDoNotDisableBluetoothMessage = false;

    private TextView mSearchNoteTextView;
    private ListView mBluetoothDevicesListView;


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (!mDevices.contains(device)) {
                    mDevices.add(device);
                    mDevicesList.add(device.getName() + "\n" + device.getAddress());
                    mBluetoothDevicesListView.setAdapter(new ArrayAdapter<String>(context,
                            android.R.layout.simple_list_item_1, mDevicesList));
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lockScreenOrientation();

        setContentView(R.layout.activity_search_device);

        mSearchNoteTextView = (TextView) findViewById(R.id.searchNoteTextView);

        mBluetoothDevicesListView = (ListView) findViewById(R.id.bluetoothDevicesListView);

        mBluetoothDevicesListView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mDevicesList));

        mBluetoothDevicesListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (mBluetoothAdapter != null) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                BluetoothDevice device = mDevices.get(position);
                startMainActivity(device.getName(), device.getAddress());
            }
        });

        if (startBluetooth()) {
            searchDevices();
        }
    }

    private void lockScreenOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            }
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private boolean startBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
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
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            // if request is satisfied, in onActivityResult startRepeatedDiscovery is called,
            // otherwise there is an alert dialog, which finishes the app when it is confirmed
            return false;
        }

        return true;
    }

    private void searchDevices() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mSearchNoteTextView.setText(R.string.searchNote);
            startRepeatedDiscovery();
        } else {
            ActivityCompat.requestPermissions(SearchDeviceActivity.this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.REQUEST_COARSE_LOCATION_PERMISSIONS);
            // if request is satisfied, in onRequestPermissionsResult startRepeatedDiscovery is
            // called, otherwise there is an alert dialog, which finishes the app when it is confirmed
        }
    }

    private void startRepeatedDiscovery() {
        startDiscovery();

        mDiscoveryRepeatHandler = new Handler();
        mDiscoveryRepeatHandler.postDelayed(new Runnable() {
            public void run() {
                startDiscovery();
                mDiscoveryRepeatHandler.postDelayed(this, Constants.MILLISECONDS_TO_REPEAT_DISCOVERY);
            }
        }, Constants.MILLISECONDS_TO_REPEAT_DISCOVERY);
    }

    private void startDiscovery() {
        if (!mBluetoothAdapter.isEnabled()) {
            showDoNotDisableBluetoothMessage();
        }

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        showBoundedDevices();

        mBluetoothAdapter.startDiscovery();
    }

    private void showBoundedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (!mDevices.contains(device)) {
                mDevices.add(device);
                mDevicesList.add(device.getName() + "\n" + device.getAddress());
                mBluetoothDevicesListView.setAdapter(new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, mDevicesList));
            }
        }
    }

    private void showDoNotDisableBluetoothMessage() {
        if (!mShowDoNotDisableBluetoothMessage) {
            mShowDoNotDisableBluetoothMessage = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.error)
                    .setMessage(R.string.bluetoothTurnedOffWhileUsing)
                    .setCancelable(false)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });

            builder.create().show();
        }
    }

    private void startMainActivity(String deviceName, String macAddress) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constants.EXTRA_MESSAGE_DEVICE_NAME, deviceName);
        intent.putExtra(Constants.EXTRA_MESSAGE_MAC, macAddress);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mReceiver);

        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK || mBluetoothAdapter.isEnabled()) {
                    searchDevices();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.error)
                            .setMessage(R.string.bluetoothOffSearch)
                            .setCancelable(false)
                            .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_COARSE_LOCATION_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mSearchNoteTextView.setText(R.string.searchNote);
                    startRepeatedDiscovery();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.info)
                            .setMessage(R.string.noPermissionLocation)
                            .setCancelable(false)
                            .setPositiveButton(R.string.okay, null);

                    builder.create().show();

                    mSearchNoteTextView.setText(R.string.searchNoteWithoutPermission);
                    showBoundedDevices();
                }
                break;
            default:
                // other request codes are ignored
                break;
        }
    }
}