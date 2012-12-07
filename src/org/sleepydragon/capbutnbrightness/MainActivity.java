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

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

public class MainActivity extends Activity implements View.OnClickListener {

    private Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PhoneInfo phoneInfo = PhoneInfoChooser.getForCurrentPhone();
        final int numBrightnessLevels;
        if (phoneInfo == null) {
            // for unknown phones, just show the default 3-button layout
            numBrightnessLevels = 3;
        } else {
            numBrightnessLevels = phoneInfo.getNumLevels();
        }

        final int layoutId;
        switch (numBrightnessLevels) {
            case 2:
                layoutId = R.layout.activity_main_2button;
                break;
            case 3:
                layoutId = R.layout.activity_main_3button;
                break;
            default:
                throw new RuntimeException("unsupported number of buttons: "
                    + numBrightnessLevels);
        }
        this.setContentView(layoutId);

        final View btnDefault = this.findViewById(R.id.btnDefault);
        btnDefault.setOnClickListener(this);
        final View btnOff = this.findViewById(R.id.btnOff);
        btnOff.setOnClickListener(this);
        final View btnBright = this.findViewById(R.id.btnBright);
        btnBright.setOnClickListener(this);

        // no dim button in 2-brightness-levels layout
        if (numBrightnessLevels >= 3) {
            final View btnDim = this.findViewById(R.id.btnDim);
            btnDim.setOnClickListener(this);
        }

        this.settings = new Settings(this);
    }

    public void onClick(View view) {
        final CapButtonBrightness.Level level = getBrightnessLevel(view);

        final CapButtonBrightness.Level levelToSet;
        if (level == null) {
            levelToSet = CapButtonBrightness.Level.BRIGHT;
        } else {
            levelToSet = level;
        }

        try {
            CapButtonBrightness.set(levelToSet);
            this.settings.setLevel(level);
        } catch (InterruptedException e) {
            // odd... ignore it I guess??
        } catch (SetCapButtonBrightnessException e) {
            this.showError(e);
        }
    }

    private void showError(SetCapButtonBrightnessException e) {
        final SetCapButtonBrightnessException.ErrorId errorId = e.getErrorId();
        final String message;
        switch (errorId) {
            case SU_BINARY_MISSING:
                message =
                    "This device is not rooted; it must be rooted "
                        + "to set the capacitive button brightness.";
                break;
            case ROOT_ACCESS_DENIED:
                message =
                    "Root access was denied; root access is required "
                        + "to set the capacitive button brightness.";
                break;
            default:
                message =
                    "Unable to set capacitive button brightness: "
                        + e.getMessage();
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle("Error");
        builder.show();
    }

    private static CapButtonBrightness.Level getBrightnessLevel(View view) {
        if (view == null) {
            return null;
        }

        final int id = view.getId();
        switch (id) {
            case R.id.btnDefault:
                return null;
            case R.id.btnOff:
                return CapButtonBrightness.Level.OFF;
            case R.id.btnDim:
                return CapButtonBrightness.Level.DIM;
            case R.id.btnBright:
                return CapButtonBrightness.Level.BRIGHT;
            default:
                throw new IllegalArgumentException("unknown view: " + view);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
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

}
