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

package com.rgi.android.geopackage.utility;

import java.util.Locale;

/**
 * @author Luke.Lambert
 *
 */
public class DatabaseVersion implements Comparable<DatabaseVersion>
{
    /**
     * @param major
     *             major version number
     * @param minor
     *             minor version number
     * @param revision
     *             revision
     */
    public DatabaseVersion(final int major, final int minor, final int revision)
    {
        this.major    = major;
        this.minor    = minor;
        this.revision = revision;
    }

    @Override
    public String toString()
    {
        return String.format(Locale.getDefault(),
                             "%d.%d.%d",
                             this.major,
                             this.minor,
                             this.revision);
    }

    @Override
    public int compareTo(final DatabaseVersion another)
    {
        final int compareMajor = Integer.valueOf(this.getMajor()).compareTo(another.getMajor());

        if(compareMajor != 0)
        {
            return compareMajor;
        }

        final int compareMinor = Integer.valueOf(this.getMinor()).compareTo(another.getMinor());

        if(compareMinor != 0)
        {
            return compareMinor;
        }

        return Integer.valueOf(this.getRevision()).compareTo(another.getRevision()); 
    }

    /**
     * @return the major
     */
    public int getMajor()
    {
        return this.major;
    }
    /**
     * @return the minor
     */
    public int getMinor()
    {
        return this.minor;
    }
    /**
     * @return the revision
     */
    public int getRevision()
    {
        return this.revision;
    }

    private final int major;
    private final int minor;
    private final int revision;
}
