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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * A service whose sole purpose is to register a broadcast receiver to respond
 * to screen power on events. For some reason, the "screen power on" broadcast
 * does not work if defined in the AndroidManifest.xml (just do a quick search
 * on stackoverflow.com for ACTION_SCREEN_ON) and to work around this issue this
 * service acts as the "host" for the broadcast receiver.
 */
public class ScreenPowerOnService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null; // clients cannot bind to this service
    }

    @Override
    public void onCreate() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        final BroadcastReceiver recvr =
            new SetCapButtonBrightnessBroadcastReceiver();
        this.registerReceiver(recvr, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

}
