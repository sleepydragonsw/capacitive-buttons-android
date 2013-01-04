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
package org.sleepydragon.capbutnbrightness.debug;

import org.sleepydragon.capbutnbrightness.R;
import org.sleepydragon.capbutnbrightness.devices.DeviceInfo;
import org.sleepydragon.capbutnbrightness.devices.DeviceInfoDatabase;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class DebugActivity extends Activity {

    private String debugText;

    private void copyTextToClipboard() {
        final Object service = this.getSystemService(CLIPBOARD_SERVICE);
        final ClipboardManager clipboard = (ClipboardManager) service;
        final ClipData clipData =
            ClipData.newPlainText("Debug Information", this.debugText);
        clipboard.setPrimaryClip(clipData);
        Toast.makeText(this, R.string.debug_copied, Toast.LENGTH_SHORT).show();
    }

    private String getDebugText() {
        final DeviceInfoDatabase db = new DeviceInfoDatabase();
        final DeviceInfo device = db.getForCurrentDevice();
        final DebugLinesGenerator lines = new DebugLinesGenerator(this, device);
        final StringBuilder sb = new StringBuilder();
        for (final String line : lines) {
            sb.append(line).append('\n');
        }
        final String text = sb.toString();
        return text;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_debug);
        final String debugText = this.getDebugText();
        final TextView textView =
            (TextView) this.findViewById(R.id.txtDebugText);
        textView.setText(debugText);
        this.debugText = debugText;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.activity_debug, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.menu_copy:
                this.copyTextToClipboard();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
