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
    Boolean,    // A boolean value representing true or false
    TinyInt,    // 1 byte, signed
    SmallInt,   // 2 bytes, signed
    MEDIUMINT,  // 4 bytes, signed
    INT,        // 8 bytes, signed
    INTEGER,    // 8 bytes, signed
    FLOAT,      // 4 byte IEEE floating point number
    DOUBLE,     // 8 byte IEEE floating point number
    REAL,       // 8 byte IEEE floating point number
    TEXT,       // Variable length string encoded in either UTF-8 or UTF-16
    BLOB,       // Variable length binary data
    DATE,       // ISO-8601 date string in the form YYYY-MM-DD encoded in either UTF-8 or UTF-16
    DATETIME;   // ISO-8601 date/time string in the form YYYY-MM-DDTHH:MM:SS.SSSZ with T separator character and Z suffix for coordinated universal time (UTC) encoded in either UTF-8 or UTF-16
}
