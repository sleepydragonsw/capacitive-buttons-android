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

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener {

    private SetBrightnessService.Level getBrightnessLevel(View view) {
        assert view != null;

        final int id = view.getId();
        switch (id) {
            case R.id.btnDefault:
                return SetBrightnessService.Level.DEFAULT;
            case R.id.btnOff:
                return SetBrightnessService.Level.OFF;
            case R.id.btnDim:
                return SetBrightnessService.Level.DIM;
            case R.id.btnBright:
                return SetBrightnessService.Level.BRIGHT;
            default:
                throw new IllegalArgumentException("unknown view: " + view);
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btnUpgrade) {
            this.showProVersionInPlayStore();
        } else {
            this.setBrightnessFromButton(view);
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

        // btnUpgrade==null in the pro version
        final View btnUpgrade = this.findViewById(R.id.btnUpgrade);
        if (btnUpgrade != null) {
            btnUpgrade.setOnClickListener(this);
        }
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

    private void setBrightnessFromButton(View view) {
        final SetBrightnessService.Level level = this.getBrightnessLevel(view);
        final Intent intent = new Intent();
        intent.setAction(SetBrightnessService.ACTION_SET_BRIGHTNESS);
        intent.putExtra(SetBrightnessService.EXTRA_NAME_LEVEL, level.name());
        intent.setClass(this, SetBrightnessService.class);

        final Handler handler = new SetBrightnessMessageHandler(this);
        final Messenger messenger = new Messenger(handler);
        intent.putExtra(SetBrightnessService.EXTRA_NAME_MESSENGER, messenger);

        this.startService(intent);
    }

    private void showError(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle("Error");
        builder.show();
    }

    private void showProVersionInPlayStore() {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent
            .setData(Uri
                .parse("market://details?id=org.sleepydragon.capbutnbrightness.pro"));
        this.startActivity(intent);
    }

    private static class SetBrightnessMessageHandler extends Handler {

        private final WeakReference<MainActivity> ref;

        public SetBrightnessMessageHandler(MainActivity activity) {
            this.ref = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (message.what == SetBrightnessService.WHAT_FAILED) {
                final Bundle data = message.getData();
                final String messageText =
                    data.getString(SetBrightnessService.KEY_MESSAGE);
                final MainActivity activity = this.ref.get();
                if (activity != null) {
                    activity.showError(messageText);
                }
            }
        }
    }

}
