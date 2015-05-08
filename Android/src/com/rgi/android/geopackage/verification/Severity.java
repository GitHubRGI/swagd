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

package com.rgi.android.geopackage.verification;

/**
 * Indication of the level of problem encountered when testing the conformity
 * of a file to the <a href="http://www.geopackage.org/spec/">GeoPackage
 * Standards</a> specification
 *
 * @author Luke Lambert
 *
 */
public enum Severity
{
    /**
     * Unexpected (non-test) error
     */
    Unknown,

    /**
     * An indication that a test was skipped
     */
    Skipped,

    /**
     * A minor violation of the <a href="http://www.geopackage.org/spec/">GeoPackage Standards</a>
     */
    Warning,

    /**
     * A major violation of the <a href="http://www.geopackage.org/spec/">GeoPackage Standards</a>
     */
    Error
}