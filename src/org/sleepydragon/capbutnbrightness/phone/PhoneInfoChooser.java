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
package org.sleepydragon.capbutnbrightness.phone;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.os.Build;

/**
 * Helper class for selecting the PhoneInfo object for the current phone.
 */
public class PhoneInfoChooser {

    /**
     * Private constructor to prevent instantiation.
     */
    private PhoneInfoChooser() {
    }

    /**
     * Gets the PhoneInfo object for the current phone.
     *
     * @return the PhoneInfo object for the current phone, or null if the
     * current phone is unknown.
     */
    public static PhoneInfo getForCurrentPhone() {
        final Map<String, PhoneInfo> map = new HashMap<String, PhoneInfo>();
        map.put("evita", new OnexEvitaPhoneInfo());
        map.put("endeavoru", new OnexEndeavoruPhoneInfo());
        map.put("evitareul", new OnexPlusPhoneInfo());

        // first check the device name
        final String deviceName = Build.DEVICE;
        if (map.containsKey(deviceName)) {
            final PhoneInfo phoneInfo = map.get(deviceName);
            return phoneInfo;
        }

        // next check whether or not the target file exists
        for (final PhoneInfo phoneInfo : map.values()) {
            final String path = phoneInfo.getPath();
            final File file = new File(path);
            if (file.exists()) {
                return phoneInfo;
            }
        }

        // okay, no idea
        return null;
    }
}
