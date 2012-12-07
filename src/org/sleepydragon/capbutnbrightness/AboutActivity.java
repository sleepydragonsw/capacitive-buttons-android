/*
 * This file is part of Capacitive Button Brightness.
 * 
 * Capacitive Button Brightness is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Capacitive Button Brightness is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Capacitive Button Brightness.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sleepydragon.capbutnbrightness;

import org.sleepydragon.capbutnbrightness.phone.PhoneInfo;
import org.sleepydragon.capbutnbrightness.phone.PhoneInfoChooser;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.app.Activity;

public class AboutActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        final PhoneInfo phoneInfo = PhoneInfoChooser.getForCurrentPhone();
        final String deviceName;
        if (phoneInfo == null) {
            deviceName = "unknown device (" + Build.DEVICE + ")";
        } else {
            deviceName = phoneInfo.getName();
        }

        final TextView deviceNameTxt =
            (TextView) findViewById(R.id.txtDeviceName);
        deviceNameTxt.setText(" " + deviceName);
    }

}
