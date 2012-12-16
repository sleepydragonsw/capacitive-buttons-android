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

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Persistently stores the capacitive button brightness setting.
 */
public class Settings {

    public static final String PREFS_NAME = "CapButtonBrightness";

    private static final int PREFS_MODE = Context.MODE_MULTI_PROCESS;
    private static final String PREFS_KEY_BRIGHTNESS_LEVEL = "levelInt";
    private static final String PREFS_KEY_DIM_LEVEL = "dimLevelInt";
    private static final String PREFS_KEY_SET_BRIGHTNESS_ON_BOOT =
        "setBrightnessOnBoot";

    private final Context context;

    /**
     * Creates a new Settings object.
     *
     * @param context the context to use to read and write the persistent
     * setting.
     * @throws NullPointerException if context==null
     */
    public Settings(Context context) {
        if (context == null) {
            throw new NullPointerException("context==null");
        }
        this.context = context;
    }

    /**
     * Gets the brightness level to use when "dim" is selected as the
     * brightness.
     *
     * @param defaultLevel the value to return if the configuration option is
     * not set; this value will be clamped between 0 and 100, inclusive, if
     * used.
     * @return the level to use when "dim" is selected as the brightness; will
     * be between 0 and 100, inclusive, defaulting to 20 if not set.
     */
    public int getDimLevel(int defaultLevel) {
        final SharedPreferences prefs = this.getSharedPreferences();
        final int value = prefs.getInt(PREFS_KEY_DIM_LEVEL, defaultLevel);

        // clamp level to the valid range
        final int level;
        if (value < 0) {
            level = 0;
        } else if (value > 100) {
            level = 100;
        } else {
            level = value;
        }

        return level;
    }

    /**
     * Gets the saved value of the capacitive button brightness.
     *
     * @return the saved level, or null, if not set.
     * @see #setLevel(Integer)
     */
    public Integer getLevel() {
        final SharedPreferences prefs = this.getSharedPreferences();
        if (!prefs.contains(PREFS_KEY_BRIGHTNESS_LEVEL)) {
            return null;
        }
        final int value = prefs.getInt(PREFS_KEY_BRIGHTNESS_LEVEL, 100);
        return value;
    }

    /**
     * Gets the SharedPreferences object from this.context from which the
     * settings are retrieved and to which the settings are written.
     *
     * @return the SharedPreferences
     */
    private SharedPreferences getSharedPreferences() {
        final SharedPreferences prefs =
            this.context.getSharedPreferences(PREFS_NAME, PREFS_MODE);
        return prefs;
    }

    /**
     * Gets whether or not the capacitive button brightness should be set when
     * the device boots up.
     *
     * @return true if the capacitive button brightness should be set when the
     * device boots up; false if it should not.
     * @see #setSetBrightnessOnBootEnabled(boolean)
     */
    public boolean isSetBrightnessOnBootEnabled() {
        final SharedPreferences prefs = this.getSharedPreferences();
        final boolean enabled =
            prefs.getBoolean(PREFS_KEY_SET_BRIGHTNESS_ON_BOOT, true);
        return enabled;
    }

    /**
     * Sets the brightness level to use when "dim" is selected as the
     * brightness.
     *
     * @param level the level to use when "dim" is selected as the brightness;
     * must be between 0 and 100, inclusive.
     * @throws IllegalArgumentException if the given level is outside its valid
     * range.
     */
    public void setDimLevel(int level) {
        if (level < 0 || level > 100) {
            throw new IllegalArgumentException("invalid level: " + level);
        }
        final SharedPreferences prefs = this.getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFS_KEY_DIM_LEVEL, level);
        editor.commit();
    }

    /**
     * Sets the saved value of the capacitive button brightness.
     *
     * @param level the level to set; may be null, in which case any saved level
     * will be cleared.
     * @see #getLevel()
     */
    public void setLevel(Integer level) {
        final SharedPreferences prefs = this.getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        if (level == null) {
            editor.remove(PREFS_KEY_BRIGHTNESS_LEVEL);
        } else {
            editor.putInt(PREFS_KEY_BRIGHTNESS_LEVEL, level);
        }
        editor.commit();
    }

    /**
     * Sets whether or not the capacitive button brightness should be set when
     * the device boots up.
     *
     * @param enabled true indicates that the capacitive button brightness
     * should be set when the device boots up; false indicates that it should
     * not.
     * @see #isSetBrightnessOnBootEnabled()
     */
    public void setSetBrightnessOnBootEnabled(boolean enabled) {
        final SharedPreferences prefs = this.getSharedPreferences();
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFS_KEY_SET_BRIGHTNESS_ON_BOOT, enabled);
        editor.commit();
    }
}
