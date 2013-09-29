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

import org.sleepydragon.capbutnbrightness.IntFileRootHelper;
import org.sleepydragon.capbutnbrightness.devices.CapacitiveButtonsBacklightBrightness;
import org.sleepydragon.capbutnbrightness.devices.CapacitiveButtonsBacklightBrightness.DimBrightnessNotSupportedException;
import org.sleepydragon.capbutnbrightness.devices.DeviceInfo;
import org.sleepydragon.capbutnbrightness.devices.DeviceInfoDatabase;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener {

    private CapacitiveButtonsBacklightBrightness buttons;
    private Settings settings;

    private Integer getBrightnessLevel(View view) {
        assert view != null;

        final int dimBrightness;
        final CapacitiveButtonsBacklightBrightness buttons = this.buttons;
        if (buttons != null) {
            dimBrightness = buttons.getDefaultDimLevel();
        } else {
            // just choose some arbitrary value in the range [0,100]
            dimBrightness = 50;
        }

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
        if (view.getId() == R.id.btnUpgrade) {
            this.showProVersionInPlayStore();
        } else {
            this.setBrightnessFromButton(view);
        }
    }

    private void setBrightnessFromButton(View view) {
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
                    buttons.set(level, 0);
                }
            } catch (final DimBrightnessNotSupportedException e) {
                this.showError(e);
            } catch (final IntFileRootHelper.IntWriteException e) {
                this.showError(e);
            }
            ButtonBrightnessAppWidgetProvider.postUpdateWidgets(this);
        }
    }

    private void showProVersionInPlayStore() {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(
            "market://details?id=org.sleepydragon.capbutnbrightness.pro"));
        this.startActivity(intent);
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

        final DeviceInfoDatabase devices = new DeviceInfoDatabase();
        final DeviceInfo device = devices.getForCurrentDevice();
        this.buttons = device.getCapacitiveButtonsBacklightBrightness();

        // start the service to respond to the screen turning on
        final Intent serviceIntent = new Intent();
        serviceIntent.setClass(this, ScreenPowerOnService.class);
        this.startService(serviceIntent);

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

    public static String formatSetBrightnessErrorMessage(Exception e,
            Context context) {
        final String exMessage = e.getMessage();
        final String message;
        if (e instanceof DimBrightnessNotSupportedException) {
            message =
                context.getString(R.string.set_error_dim_not_supported,
                    exMessage);
        } else if (e instanceof IntFileRootHelper.ChmodFailedException) {
            final String path =
                ((IntFileRootHelper.ChmodFailedException) e).getPath();
            message =
                context.getString(R.string.set_error_chmod_failed, path,
                    exMessage);
        } else if (e instanceof IntFileRootHelper.ChownExitCodeException) {
            final IntFileRootHelper.ChownExitCodeException e2 =
                (IntFileRootHelper.ChownExitCodeException) e;
            final String output = e2.getMessage();
            final String path = e2.getPath();
            final int exitCode = e2.getExitCode();
            message =
                context.getString(R.string.set_error_chown_exit_code, path,
                    exitCode, output);
        } else if (e instanceof IntFileRootHelper.ChownLaunchException) {
            final IntFileRootHelper.ChownLaunchException e2 =
                (IntFileRootHelper.ChownLaunchException) e;
            final String path = e2.getPath();
            message =
                context.getString(R.string.set_error_chown_launch, path,
                    exMessage);
        } else if (e instanceof IntFileRootHelper.ChownWaitInterruptedException) {
            final IntFileRootHelper.ChownWaitInterruptedException e2 =
                (IntFileRootHelper.ChownWaitInterruptedException) e;
            final String path = e2.getPath();
            message =
                context.getString(R.string.set_error_chown_wait, path,
                    exMessage);
        } else if (e instanceof IntFileRootHelper.StatFailedException) {
            final IntFileRootHelper.StatFailedException e2 =
                (IntFileRootHelper.StatFailedException) e;
            final String path = e2.getPath();
            message =
                context.getString(R.string.set_error_stat, path, exMessage);
        } else if (e instanceof IntFileRootHelper.IntFileNotFoundException) {
            final IntFileRootHelper.IntFileNotFoundException e2 =
                (IntFileRootHelper.IntFileNotFoundException) e;
            final String path = e2.getPath();
            message =
                context.getString(R.string.set_error_file_not_found, path);
        } else if (e instanceof IntFileRootHelper.IntFileIOException) {
            final IntFileRootHelper.IntFileIOException e2 =
                (IntFileRootHelper.IntFileIOException) e;
            final String path = e2.getPath();
            message = context.getString(R.string.set_error_io, path, exMessage);
        } else if (e instanceof IntFileRootHelper.RootShellCreateDeniedException) {
            message =
                context.getString(R.string.set_error_root_denied, exMessage);
        } else if (e instanceof IntFileRootHelper.RootShellCreateIOException) {
            message =
                context.getString(R.string.set_error_root_error, exMessage);
        } else if (e instanceof IntFileRootHelper.RootShellCreateTimeoutException) {
            message =
                context.getString(R.string.set_error_root_timeout, exMessage);
        } else if (e instanceof IntFileRootHelper.RootShellNotRootedException) {
            message = context.getString(R.string.set_error_root_not_rooted);
        } else if (e instanceof IntFileRootHelper.IntFileWriteException) {
            final IntFileRootHelper.IntFileWriteException e2 =
                (IntFileRootHelper.IntFileWriteException) e;
            final String path = e2.getPath();
            message =
                context.getString(R.string.set_error_generic, path, exMessage);
        } else {
            message = context.getString(R.string.set_error_unexpected, e);
        }

        return message;
    }

    private void showError(Exception e) {
        assert e != null;
        Log.e(Constants.LOG_TAG,
            "setting capacitive buttons brightness from main UI failed", e);
        final String message = formatSetBrightnessErrorMessage(e, this);
        Log.e(Constants.LOG_TAG, message);
        this.showError(message);
    }

    private void showError(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle("Error");
        builder.show();
    }

}
