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

import java.io.File;

import org.sleepydragon.capbutnbrightness.IntFileRootHelper;

/**
 * A specialization of CapacitiveButtonsBacklightBrightness for the HTC
 * Sensation.
 */
public class HtcSensation extends CapacitiveButtonsBacklightBrightness {

    public String[] getRequiredFiles() {
        return new String[] { BRIGHTNESS_PATH };
    }

    public void set(int level, int options,
            IntFileRootHelper.OperationNotifier notifier)
            throws IntFileRootHelper.IntWriteException,
            DimBrightnessNotSupportedException {
        if (level < 0 || level > 100) {
            throw new IllegalArgumentException("invalid level: " + level);
        }

        // on the One X+, there is nothing to do when the screen turns on
        // and the brightness level is set to "off".
        final boolean inResponseToScreenOn =
            ((options & OPTION_SCREEN_ON) == OPTION_SCREEN_ON);
        if (inResponseToScreenOn && level == 0) {
            return;
        }

        final boolean dim = (level != 0 && level != 100);
        final boolean dimSupported = this.isDimSupported();
        if (dim && !dimSupported) {
            throw new DimBrightnessNotSupportedException(
                "file does not exist: " + CURRENTS_PATH);
        }

        final boolean backlightOn = (level != 0);
        final IntFileRootHelper intFile = new IntFileRootHelper(notifier);

        try {
            final boolean currentsFileExists = new File(CURRENTS_PATH).isFile();
            if (!backlightOn) {
                intFile.write(BRIGHTNESS_PATH, 0);
                if (currentsFileExists) {
                    intFile.write(CURRENTS_PATH, 0);
                }
            } else {
                final int currents = dim ? 3 : 8;
                if (!inResponseToScreenOn || dim) {
                    if (currentsFileExists) {
                        intFile.write(CURRENTS_PATH, currents);
                    }
                }
                intFile.write(BRIGHTNESS_PATH, 255);
                if (currentsFileExists) {
                    intFile.write(CURRENTS_PATH, currents);
                }
            }
            makeAllFilesReadOnly(intFile);
        } finally {
            intFile.close();
        }
    }

    public String[] getRequiredDimFiles() {
        return new String[] { CURRENTS_PATH };
    }
}
