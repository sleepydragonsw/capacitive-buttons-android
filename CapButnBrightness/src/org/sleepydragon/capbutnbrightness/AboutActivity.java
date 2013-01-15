/*
 * This file is part of Capacitive Buttons.
 *
 * Capacitive Buttons is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Capacitive Buttons is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Capacitive Buttons.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sleepydragon.capbutnbrightness;

import org.sleepydragon.capbutnbrightness.devices.DeviceInfo;
import org.sleepydragon.capbutnbrightness.devices.DeviceInfoDatabase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AboutActivity extends Activity implements OnClickListener {

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCredits:
                final Intent intent = new Intent(this, CreditsActivity.class);
                this.startActivity(intent);
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_about);

        final DeviceInfoDatabase db = new DeviceInfoDatabase();
        final DeviceInfo device = db.getForCurrentDevice();
        final String deviceName = device.getDisplayName();
        final TextView deviceNameTxt =
            (TextView) this.findViewById(R.id.txtDeviceName);
        final String deviceNameViewText =
            this.getString(R.string.about_device_name, deviceName);
        deviceNameTxt.setText(deviceNameViewText);

        final View creditsBtn = this.findViewById(R.id.btnCredits);
        creditsBtn.setOnClickListener(this);
    }
}
