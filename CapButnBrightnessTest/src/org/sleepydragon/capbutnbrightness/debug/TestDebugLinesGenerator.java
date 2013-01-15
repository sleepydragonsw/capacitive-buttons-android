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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.sleepydragon.capbutnbrightness.devices.CapacitiveButtonsBacklightBrightness;
import org.sleepydragon.capbutnbrightness.devices.DeviceInfo;

import android.test.AndroidTestCase;

public class TestDebugLinesGenerator extends AndroidTestCase {

    /**
     * Test that the DebugLinesGenerator constructor throws NullPointerException
     * if context==null.
     */
    public void test_Constructor_NullContext() {
        try {
            new DebugLinesGenerator(null, new StubDeviceInfo());
            fail("should have thrown NPE");
        } catch (final NullPointerException e) {
            assertEquals(e.getMessage(), "context==null");
        }
    }

    /**
     * Test that the DebugLinesGenerator constructor throws NullPointerException
     * if deviceInfo==null.
     */
    public void test_Constructor_NullDeviceInfo() {
        try {
            new DebugLinesGenerator(this.getContext(), null);
            fail("should have thrown NPE");
        } catch (final NullPointerException e) {
            assertEquals(e.getMessage(), "device==null");
        }
    }

    /**
     * Test that the DebugLinesGenerator constructor completes successfully
     */
    public void test_Constructor_ValidArgs() {
        new DebugLinesGenerator(this.getContext(), new StubDeviceInfo());
    }

    /**
     * Test that DebugLinesGenerator.hasNext() returns true and then false.
     */
    public void test_hasNext() {
        final DebugLinesGenerator x =
            new DebugLinesGenerator(this.getContext(), new StubDeviceInfo());
        assertTrue(x.hasNext());
        assertTrue(x.hasNext());
        while (x.hasNext()) {
            x.next();
        }
        assertFalse(x.hasNext());
        assertFalse(x.hasNext());
    }

    /**
     * Test that DebugLinesGenerator.iterator() returns the object itself.
     */
    public void test_iterator() {
        final DebugLinesGenerator x =
            new DebugLinesGenerator(this.getContext(), new StubDeviceInfo());
        final Iterator<String> actual = x.iterator();
        assertSame(actual, x);
    }

    /**
     * Test that DebugLinesGenerator.next() returns elements until hasNext()
     * returns false.
     */
    public void test_next() {
        final DebugLinesGenerator x =
            new DebugLinesGenerator(this.getContext(), new StubDeviceInfo());
        while (x.hasNext()) {
            x.next();
        }
    }

    /**
     * Test that DebugLinesGenerator.next() never returns null.
     */
    public void test_next_NeverReturnsNull() {
        final DebugLinesGenerator x =
            new DebugLinesGenerator(this.getContext(), new StubDeviceInfo());
        while (x.hasNext()) {
            final String value = x.next();
            assertNotNull(value);
        }
    }

    /**
     * Test that DebugLinesGenerator.next() throws NoSuchElementException after
     * returning the last element
     */
    public void test_next_ThrowsNoSuchElementException() {
        final DebugLinesGenerator x =
            new DebugLinesGenerator(this.getContext(), new StubDeviceInfo());
        while (x.hasNext()) {
            x.next();
        }
        try {
            x.next();
            fail("next() should have thrown NoSuchElementException");
        } catch (final NoSuchElementException e) {
            final String message = e.getMessage();
            assertNull(message);
        }
    }

    /**
     * Test that DebugLinesGenerator.iterator() returns the object itself.
     */
    public void test_remove() {
        final DebugLinesGenerator x =
            new DebugLinesGenerator(this.getContext(), new StubDeviceInfo());
        try {
            x.remove();
            fail("remove() should have thrown UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            final String message = e.getMessage();
            assertNull(message);
        }
    }

    private static class StubDeviceInfo extends DeviceInfo {
        public StubDeviceInfo() {
            super("id", "name", (CapacitiveButtonsBacklightBrightness) null);
        }
    }

}
