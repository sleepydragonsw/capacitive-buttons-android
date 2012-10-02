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

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final View btnOff = findViewById(R.id.btnOff);
        final View btnDim = findViewById(R.id.btnDim);
        final View btnBright = findViewById(R.id.btnBright);
        btnOff.setOnClickListener(this);
        btnDim.setOnClickListener(this);
        btnBright.setOnClickListener(this);
    }

    public void onClick(View view) {
        final CapButtonBrightness.Level level = getBrightnessLevel(view);
        if (level != null) {
            try {
                CapButtonBrightness.set(level);
            } catch (InterruptedException e) {
                // odd... ignore it I guess??
            } catch (SetCapButtonBrightnessException e) {
                this.showError(e);
            }
        }
    }

    private void showError(SetCapButtonBrightnessException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Unable to set capacitive button brightness: "
            + e.getMessage());
        builder.setTitle("Error");
        builder.show();
    }

    private static CapButtonBrightness.Level getBrightnessLevel(View view) {
        if (view == null) {
            return null;
        }

        final int id = view.getId();
        switch (id) {
            case R.id.btnOff:
                return CapButtonBrightness.Level.OFF;
            case R.id.btnDim:
                return CapButtonBrightness.Level.DIM;
            case R.id.btnBright:
                return CapButtonBrightness.Level.BRIGHT;
            default:
                return null;
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
                final Intent intent = new Intent(this, AboutActivity.class);
                this.startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
