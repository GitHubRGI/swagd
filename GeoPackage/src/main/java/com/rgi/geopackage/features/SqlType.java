/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.rgi.geopackage.features;

/**
 * Basic database types allowed by the GeoPackage specification
 * <br>
 * <br>
 * <b>Note:</b> This enum (currently) has no means of specifying sized TEXT or
 * BLOB types.  It also does not represent allowed geometry types
 *
 * @see "http://www.geopackage.org/spec/#table_column_data_types"
 *
 * @author Luke Lambert
 *
 */
public enum SqlType
{
    /**
     * A boolean value representing true or false. Stored as SQLite INTEGER with value 0 for false or 1 for true
     */
    BOOLEAN,

    /**
     * 8-bit signed two’s complement integer. Stored as SQLite INTEGER with values in the range [-128, 127]
     */
    TINYINT,

    /**
     * 16-bit signed two’s complement integer. Stored as SQLite INTEGER with values in the range [-32768, 32767]
     */
    SMALLINT,

    /**
     * 32-bit signed two’s complement integer. Stored as SQLite INTEGER with values in the range [-2147483648, 2147483647]
     */
    MEDIUMINT,

    /**
     * 64-bit signed two’s complement integer. Stored as SQLite INTEGER with values in the range [-9223372036854775808, 9223372036854775807]
     */
    INT,

    /**
     * 64-bit signed two’s complement integer. Stored as SQLite INTEGER with values in the range [-9223372036854775808, 9223372036854775807]
     */
    INTEGER,

    /**
     * 32-bit IEEE floating point number. Stored as SQLite REAL limited to values that can be represented as a 4-byte IEEE floating point number
     */
    FLOAT,

    /**
     * 64-bit IEEE floating point number. Stored as SQLite REAL
     */
    DOUBLE,

    /**
     * 64-bit IEEE floating point number. Stored as SQLite REAL
     */
    REAL,

    /**
     * Variable length string encoded in either UTF-8 or UTF-16, determined by PRAGMA encoding; see {@link "http://www.sqlite.org/pragma.html#pragma_encoding"}.
     */
    TEXT,

    /**
     * Variable length binary data.
     */
    BLOB,

    /**
     * ISO-8601 date string in the form YYYY-MM-DD encoded in either UTF-8 or UTF-16. See TEXT. Stored as SQLite TEXT
     */
    DATE,

    /**
     * ISO-8601 date/time string in the form YYYY-MM-DDTHH:MM:SS.SSSZ with T separator character and Z suffix for coordinated universal time (UTC) encoded in either UTF-8 or UTF-16. See TEXT. Stored as SQLite TEXT
     */
    DATETIME
}
