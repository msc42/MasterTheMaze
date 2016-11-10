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

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

/**
 * Represents the set manually device activity.
 *
 * @author Stefan Constantin
 */
public final class SetDeviceActivity extends AppCompatActivity {

    private TextView mLedsDeviceBeforeSetTextView;
    private EditText mLedsDeviceNewNameEditText;
    private EditText mLedsDeviceNewAddressEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_set_device);

        mLedsDeviceBeforeSetTextView = (TextView) findViewById(R.id.ledsDeviceBeforeSetTextView);
        mLedsDeviceNewAddressEditText = (EditText) findViewById(R.id.ledsDeviceNewAddressEditText);
        mLedsDeviceNewNameEditText = (EditText) findViewById(R.id.ledsDeviceNewNameEditText);

        Intent intent = getIntent();
        String macAddress = intent.getStringExtra(Constants.EXTRA_MESSAGE_MAC);
        String deviceName = intent.getStringExtra(Constants.EXTRA_MESSAGE_DEVICE_NAME);

        if (macAddress != null) {
            if (deviceName == null) {
                mLedsDeviceBeforeSetTextView.setText(getString(
                        R.string.ledDisplaySet, getString(R.string.noLedDisplayName), macAddress));
            } else {
                mLedsDeviceBeforeSetTextView.setText(getString(
                        R.string.ledDisplaySet, deviceName, macAddress));
            }
        } else {
            mLedsDeviceBeforeSetTextView.setText(R.string.noLedDisplaySet);
        }
    }

    public void changeAddress(View v) {
        String newMacAddress = mLedsDeviceNewAddressEditText.getText().toString().trim().
                toUpperCase(Locale.ENGLISH);
        if (newMacAddress.matches("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}")) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(Constants.EXTRA_MESSAGE_MAC, newMacAddress);

            String nameEditText = mLedsDeviceNewNameEditText.getText().toString();
            if (nameEditText.length() != 0) {
                intent.putExtra(Constants.EXTRA_MESSAGE_DEVICE_NAME, nameEditText);
            }

            startActivity(intent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.error)
                    .setMessage(R.string.invalidLedDisplayAddress)
                    .setCancelable(false)
                    .setPositiveButton(R.string.okay, null);

            builder.create().show();
        }
    }
}