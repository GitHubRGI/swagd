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

import org.junit.Test;

import static com.rgi.geopackage.features.SqlType.BLOB;
import static com.rgi.geopackage.features.SqlType.BOOLEAN;
import static com.rgi.geopackage.features.SqlType.DATE;
import static com.rgi.geopackage.features.SqlType.DATETIME;
import static com.rgi.geopackage.features.SqlType.DOUBLE;
import static com.rgi.geopackage.features.SqlType.FLOAT;
import static com.rgi.geopackage.features.SqlType.INT;
import static com.rgi.geopackage.features.SqlType.INTEGER;
import static com.rgi.geopackage.features.SqlType.MEDIUMINT;
import static com.rgi.geopackage.features.SqlType.REAL;
import static com.rgi.geopackage.features.SqlType.SMALLINT;
import static com.rgi.geopackage.features.SqlType.TEXT;
import static com.rgi.geopackage.features.SqlType.TINYINT;

/**
 * @author Luke Lambert
 */
public class SqlTypeTest
{
    /**
     * Test SQL types - kind of weird, but the coverage report complains about the Enum not being covered
     */
    @Test
    @SuppressWarnings({"JUnitTestMethodWithNoAssertions", "LocalVariableNamingConvention"})
    public void testTypes()
    {
        final SqlType _bool     = BOOLEAN;
        final SqlType tinyInt   = TINYINT;
        final SqlType smallInt  = SMALLINT;
        final SqlType mediumInt = MEDIUMINT;
        final SqlType _int      = INT;
        final SqlType integer   = INTEGER;
        final SqlType _float    = FLOAT;
        final SqlType _double   = DOUBLE;
        final SqlType real      = REAL;
        final SqlType text      = TEXT;
        final SqlType blob      = BLOB;
        final SqlType date      = DATE;
        final SqlType dateTime  = DATETIME;
    }

}
