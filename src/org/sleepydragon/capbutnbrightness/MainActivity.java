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

import org.sleepydragon.capbutnbrightness.devices.CapacitiveButtonsBacklightBrightness;
import org.sleepydragon.capbutnbrightness.devices.DeviceInfo;
import org.sleepydragon.capbutnbrightness.devices.DeviceInfoDatabase;
import org.sleepydragon.capbutnbrightness.devices.FileCapacitiveButtonsBacklightBrightness.NotRootedException;
import org.sleepydragon.capbutnbrightness.devices.FileCapacitiveButtonsBacklightBrightness.RootAccessDeniedException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener {

    private CapacitiveButtonsBacklightBrightness buttons;
    private Settings settings;

    private Integer getBrightnessLevel(View view) {
        assert view != null;

        final int defaultDimBrightness;
        final CapacitiveButtonsBacklightBrightness buttons = this.buttons;
        if (buttons != null) {
            defaultDimBrightness = buttons.getDefaultDimLevel();
        } else {
            // just choose some arbitrary value in the range [0,100]
            defaultDimBrightness = 50;
        }
        final int dimBrightness =
            this.settings.getDimLevel(defaultDimBrightness);

        final int id = view.getId();
        switch (id) {
            case R.id.btnDefault:
                return null;
            case R.id.btnOff:
                return 0;
            case R.id.btnDim:
                return dimBrightness;
            case R.id.btnBright:
                return 100;
            default:
                throw new IllegalArgumentException("unknown view: " + view);
        }
    }

    public void onClick(View view) {
        final CapacitiveButtonsBacklightBrightness buttons = this.buttons;
        if (buttons == null) {
            this.showError("This device is not supported by this application");
        } else {
            final Integer level = this.getBrightnessLevel(view);
            this.settings.setLevel(level);
            try {
                if (level == null) {
                    buttons.setDefault();
                } else {
                    buttons.set(level);
                }
            } catch (final CapacitiveButtonsBacklightBrightness.SetException e) {
                this.showError(e);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        final View btnDefault = this.findViewById(R.id.btnDefault);
        btnDefault.setOnClickListener(this);
        final View btnOff = this.findViewById(R.id.btnOff);
        btnOff.setOnClickListener(this);
        final View btnBright = this.findViewById(R.id.btnBright);
        btnBright.setOnClickListener(this);
        final View btnDim = this.findViewById(R.id.btnDim);
        btnDim.setOnClickListener(this);

        final DeviceInfoDatabase devices = new DeviceInfoDatabase();
        final DeviceInfo device = devices.getForCurrentDevice();
        this.buttons = device.getCapacitiveButtonsBacklightBrightness();

        this.settings = new Settings(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.menu_about:
                this.startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.menu_settings:
                this.startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showError(CapacitiveButtonsBacklightBrightness.SetException e) {
        assert e != null;
        final String message;
        if (e instanceof NotRootedException) {
            message =
                "This device is not rooted; it must be rooted in order for "
                    + "this application to function correctly";
        } else if (e instanceof RootAccessDeniedException) {
            message =
                "Root access was denied; this application requires root "
                    + " permissions in order to function correctly";
        } else {
            message = e.getMessage();
        }
        this.showError(message);
    }

    private void showError(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle("Error");
        builder.show();
    }

}
