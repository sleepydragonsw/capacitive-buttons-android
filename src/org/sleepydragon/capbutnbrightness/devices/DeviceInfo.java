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
package org.sleepydragon.capbutnbrightness.devices;

/**
 * Stores information about a device model.
 */
public class DeviceInfo {

    private final String id;
    private final String name;
    private final CapacitiveButtonsBacklightBrightness buttons;

    /**
     * Creates a new DeviceInfo object.
     *
     * @param id an ID for this device.
     * @param name a display name for this device.
     * @param buttons an object that can be used to control the brightness of
     * the capacitive buttons backlight on this device; may be null if there is
     * no known mechanism to do this.
     * @throws NullPointerException if id==null or name==null.
     */
    public DeviceInfo(String id, String name,
            CapacitiveButtonsBacklightBrightness buttons) {
        if (id == null) {
            throw new NullPointerException("id==null");
        } else if (name == null) {
            throw new NullPointerException("name==null");
        }
        this.id = id;
        this.name = name;
        this.buttons = buttons;
    }

    /**
     * Returns an object that can be used to control the brightness of the
     * capacitive buttons backlight.
     *
     * @return an object that can be used to control the brightness of the
     * capacitive buttons backlight; may be null, which indicates that there is
     * no known mechanism for controlling the the capacitive buttons backlight
     * on this device.
     */
    public CapacitiveButtonsBacklightBrightness getCapacitiveButtonsBacklightBrightness() {
        return this.buttons;
    }

    /**
     * Returns a name for this device that is appropriate for displaying to
     * users in a user interface. The returned value will include both the name
     * and ID.
     *
     * @return a display name for this device; never returns null.
     */
    public String getDisplayName() {
        return this.getName() + " (" + this.getId() + ")";
    }

    /**
     * Returns the ID this device.
     *
     * @return the ID of this device; never returns null.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the name of this device.
     *
     * @return the name of this device; never returns null.
     */
    public String getName() {
        return this.name;
    }
}
