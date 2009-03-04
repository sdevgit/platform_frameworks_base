/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.database;

import junit.framework.TestCase;

import java.util.*;

public class MatrixCursorTest extends TestCase {

    public void testEmptyCursor() {
        Cursor cursor = new MatrixCursor(new String[] { "a" });
        assertEquals(0, cursor.getCount());
    }

    public void testNullValue() {
        MatrixCursor cursor = new MatrixCursor(new String[] { "a" });
        cursor.newRow().add(null);
        cursor.moveToNext();
        assertTrue(cursor.isNull(0));
    }

    public void testMatrixCursor() {
        MatrixCursor cursor = newMatrixCursor();

        cursor.newRow()
                .add("a")
                .add(1)
                .add(2)
                .add(3)
                .add(4)
                .add(5);

        cursor.moveToNext();

        checkValues(cursor);

        cursor.newRow()
                .add("a")
                .add("1")
                .add("2")
                .add("3")
                .add("4")
                .add("5");

        cursor.moveToNext();
        checkValues(cursor);

        cursor.moveToPrevious();
        checkValues(cursor);
    }

    public void testAddArray() {
        MatrixCursor cursor = newMatrixCursor();

        cursor.addRow(new Object[] { "a", 1, 2, 3, 4, 5 });
        cursor.moveToNext();
        checkValues(cursor);

        try {
            cursor.addRow(new Object[0]);
            fail();
        } catch (IllegalArgumentException e) { /* expected */ }
    }

    public void testAddIterable() {
        MatrixCursor cursor = newMatrixCursor();

        cursor.addRow(Arrays.asList("a", 1, 2, 3, 4, 5));
        cursor.moveToNext();
        checkValues(cursor);

        try {
            cursor.addRow(Collections.emptyList());
            fail();
        } catch (IllegalArgumentException e) { /* expected */ }

        try {
            cursor.addRow(Arrays.asList("a", 1, 2, 3, 4, 5, "Too many!"));
            fail();
        } catch (IllegalArgumentException e) { /* expected */ }
    }

    public void testAddArrayList() {
        MatrixCursor cursor = newMatrixCursor();

        cursor.addRow(new NonIterableArrayList<Object>(
                Arrays.asList("a", 1, 2, 3, 4, 5)));
        cursor.moveToNext();
        checkValues(cursor);

        try {
            cursor.addRow(new NonIterableArrayList<Object>());
            fail();
        } catch (IllegalArgumentException e) { /* expected */ }

        try {
            cursor.addRow(new NonIterableArrayList<Object>(
                    Arrays.asList("a", 1, 2, 3, 4, 5, "Too many!")));
            fail();
        } catch (IllegalArgumentException e) { /* expected */ }
    }

    static class NonIterableArrayList<T> extends ArrayList<T> {

        NonIterableArrayList() {}

        NonIterableArrayList(Collection<? extends T> ts) {
            super(ts);
        }

        @Override
        public Iterator<T> iterator() {
            throw new AssertionError();
        }
    }

    private MatrixCursor newMatrixCursor() {
        return new MatrixCursor(new String[] {
                "string", "short", "int", "long", "float", "double" });
    }

    private void checkValues(MatrixCursor cursor) {
        assertEquals("a", cursor.getString(0));
        assertEquals(1, cursor.getShort(1));
        assertEquals(2, cursor.getInt(2));
        assertEquals(3, cursor.getLong(3));
        assertEquals(4.0f, cursor.getFloat(4));
        assertEquals(5.0D, cursor.getDouble(5));
    }

}
