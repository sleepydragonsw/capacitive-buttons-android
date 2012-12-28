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
package org.sleepydragon.capbutnbrightness.debug;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.sleepydragon.capbutnbrightness.R;
import org.sleepydragon.capbutnbrightness.devices.DeviceInfo;

import android.content.Context;
import android.os.Build;

/**
 * An iterator that produces strings whose values are lines that can be written
 * to a file or to the screen that provide useful debugging information about
 * this application.
 * <p>
 * This class is <em>NOT</em> thread safe. Concurrent access to instance of this
 * class produced undefined behaviour, and may corrupt internal data structures.
 * External synchronization is required if an instance of this class will be
 * accessed concurrently by multiple threads.
 */
public class DebugLinesGenerator implements Iterable<String>, Iterator<String> {

    private final Context context;
    private final DeviceInfo device;

    private final Section[] sections;
    private int sectionsIndex;
    private String[] curSection;
    private int curSectionIndex;
    private String next;

    /**
     * Creates a new instance of DebugLinesGenerator.
     *
     * @param context the context used to get strings.
     * @param device the device whose information to include in the generated
     * lines.
     * @throws NullPointerException if context==null or device==null.
     */
    public DebugLinesGenerator(Context context, DeviceInfo device) {
        if (context == null) {
            throw new NullPointerException("context==null");
        } else if (device == null) {
            throw new NullPointerException("device==null");
        }
        this.context = context;
        this.device = device;
        this.sections = this.getSections();
        this.setNext();
    }

    private Section[] getSections() {
        final List<Section> list = new ArrayList<Section>();
        list.add(new AppVersionSection());
        list.add(new BuildSection());
        final Section[] array = new Section[list.size()];
        return list.toArray(array);
    }

    public boolean hasNext() {
        final String next = this.next;
        return (next != null);
    }

    /**
     * Returns this object.
     */
    public Iterator<String> iterator() {
        return this;
    }

    public String next() {
        final String next = this.next;
        if (next == null) {
            throw new NoSuchElementException();
        }
        this.setNext();
        return next;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void setNext() {
        String next = null;

        while (true) {
            final int curSectionLength =
                (this.curSection == null) ? 0 : this.curSection.length;
            while (next == null && this.curSectionIndex < curSectionLength) {
                next = this.curSection[this.curSectionIndex];
                this.curSectionIndex++;
            }

            if (next != null) {
                break;
            }

            Section section = null;
            final int sectionsLength =
                (this.sections == null) ? 0 : this.sections.length;
            while (section == null && this.sectionsIndex < sectionsLength) {
                section = this.sections[this.sectionsIndex];
                this.sectionsIndex++;
            }

            if (section == null) {
                break;
            } else {
                final String[] lines = section.getLines();
                this.curSection = lines;
                this.curSectionIndex = 0;
            }
        }

        this.next = next;
    }

    private class AppVersionSection implements Section {

        public String[] getLines() {
            final String version =
                DebugLinesGenerator.this.context
                    .getString(R.string.app_version);
            final String line =
                DebugLinesGenerator.this.context.getString(
                    R.string.debug_app_version, version);
            return new String[] { line };
        }

    }

    private class BuildSection implements Section {

        public String[] getLines() {
            final Context context = DebugLinesGenerator.this.context;
            final String boardValue = Build.BOARD;
            final String board =
                context.getString(R.string.debug_build_board, boardValue);
            final String bootloaderValue = Build.BOOTLOADER;
            final String bootloader =
                context.getString(R.string.debug_build_bootloader,
                    bootloaderValue);
            final String brandValue = Build.BRAND;
            final String brand =
                context.getString(R.string.debug_build_brand, brandValue);
            final String deviceValue = Build.DEVICE;
            final String device =
                context.getString(R.string.debug_build_device, deviceValue);
            final String hardwareValue = Build.HARDWARE;
            final String hardware =
                context.getString(R.string.debug_build_hardware, hardwareValue);
            final String displayValue = Build.DISPLAY;
            final String display =
                context.getString(R.string.debug_build_display, displayValue);
            final String manufacturerValue = Build.MANUFACTURER;
            final String manufacturer =
                context.getString(R.string.debug_build_manufacturer,
                    manufacturerValue);
            final String modelValue = Build.MODEL;
            final String model =
                context.getString(R.string.debug_build_model, modelValue);
            final String productValue = Build.PRODUCT;
            final String product =
                context.getString(R.string.debug_build_product, productValue);
            final String versionValue = Build.VERSION.RELEASE;
            final String version =
                context.getString(R.string.debug_build_version_release,
                    versionValue);
            return new String[] { board, bootloader, brand, device, display,
                hardware, manufacturer, model, product, version };
        }

    }

    private static interface Section {
        public String[] getLines();
    }
}
