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
package org.sleepydragon.capbutnbrightness.devices;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.os.Build;

/**
 * A collection of DeviceInfo objects.
 */
public class DeviceInfoDatabase {

    private final Map<String, DeviceInfo> devices;
    private final DeviceInfo unknownDeviceInfo;

    /**
     * Creates a new DeviceInfoDatabase object.
     */
    public DeviceInfoDatabase() {
        // create a list containing information about known devices
        final Collection<DeviceInfo> list = new ArrayList<DeviceInfo>();
        list.add(new DeviceInfo("evita", "HTC One X", new HtcOneXEvita()));
        list.add(new DeviceInfo("endeavoru", "HTC One X",
            new HtcOneXEndeavoru()));
        list.add(new DeviceInfo("evitareul", "HTC One X+", new HtcOneXPlus()));
        list.add(new DeviceInfo("enrc2b", "HTC One X+", new HtcOneXPlus()));
        list.add(new DeviceInfo("m7", "HTC One", new HtcOne()));
        list.add(new DeviceInfo("ville", "HTC One S", new HtcOneXPlus()));
        list.add(new DeviceInfo("pyramid", "HTC Sensation", new HtcSensation()));

        // create a sentinel object that represents an unknown device
        this.unknownDeviceInfo = new UnknownDeviceInfo();

        // convert the list into a map for more efficient lookup
        final Map<String, DeviceInfo> map = new HashMap<String, DeviceInfo>();
        for (final DeviceInfo deviceInfo : list) {
            final String id = deviceInfo.getId();
            map.put(id, deviceInfo);
        }
        this.devices = map;
    }

    /**
     * Gets a DeviceInfo object by ID.
     *
     * @param id the ID of the DeviceInfo object to get; "unknown" is a special
     * ID that will return a DeviceInfo object for the current device as if it
     * is unknown.
     * @return the DeviceInfo object with the given ID, or null if the given ID
     * is not known, including if id==null; if "unknown" is given then an
     * instance of {@link UnknownDeviceInfo} will be returned.
     */
    public DeviceInfo getById(String id) {
        final DeviceInfo deviceInfo;
        if (id == null) {
            deviceInfo = null;
        } else if (id.equals("unknown")) {
            deviceInfo = this.unknownDeviceInfo;
        } else {
            deviceInfo = this.devices.get(id);
        }
        return deviceInfo;
    }

    /**
     * Gets a DeviceInfo object suitable for the current device.
     *
     * @return the DeviceInfo object for the current device; never returns null;
     * if the current device is not known then an instance of
     * {@link UnknownDeviceInfo} will be returned.
     */
    public DeviceInfo getForCurrentDevice() {
        // try to find an exact match
        final String deviceId = Build.DEVICE;
        for (final String curDeviceId : this.devices.keySet()) {
            if (curDeviceId.equals(deviceId)) {
                final DeviceInfo deviceInfo = this.devices.get(curDeviceId);
                return deviceInfo;
            }
        }

        // try to find one whose capacitive buttons are supported
        for (final DeviceInfo deviceInfo : this.devices.values()) {
            final CapacitiveButtonsBacklightBrightness buttons =
                deviceInfo.getCapacitiveButtonsBacklightBrightness();
            if (buttons != null && buttons.isSupported()) {
                return deviceInfo;
            }
        }

        // return the "unknown" device
        return this.unknownDeviceInfo;
    }

    /**
     * A specialization of DeviceInfo that represents an "unknown" device.
     */
    public static class UnknownDeviceInfo extends DeviceInfo {

        /**
         * Creates a new UnknownDeviceInfo whose ID will be the Build.DEVICE of
         * the current device.
         */
        public UnknownDeviceInfo() {
            super(Build.DEVICE, "Unknown Device", null);
        }
    }
}
