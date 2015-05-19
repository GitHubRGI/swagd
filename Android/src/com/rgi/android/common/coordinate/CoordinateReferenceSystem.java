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

package com.rgi.android.common.coordinate;

/**
 * A means of uniquely identifying a coordinate reference system
 *
 * @author Luke Lambert
 *
 */
public class CoordinateReferenceSystem implements Comparable<CoordinateReferenceSystem>
{
    /**
     * Constructor
     *
     * @param authority
     *             The name of the defining authority of the coordinate
     *             reference system (e.g. "EPSG"). This value is converted to
     *             upper case.
     * @param identifier
     *             The identifier as assigned by the the authority of the
     *             coordinate reference system
     */
    public CoordinateReferenceSystem(final String authority, final int identifier)
    {
        this(null, authority, identifier);
    }

    /**
     * Constructor
     *
     * @param name
     *             Name of the coordinate reference system.  Non-null values
     *             may not be empty.
     * @param authority
     *             The name of the defining authority of the coordinate
     *             reference system (e.g. "EPSG"). This value is converted to
     *             upper case.
     * @param identifier
     *             The identifier as assigned by the the authority of the
     *             coordinate reference system
     */
    public CoordinateReferenceSystem(final String name, final String authority, final int identifier)
    {
        if(name != null && name.isEmpty())
        {
            throw new IllegalArgumentException("A non-null name may not be empty");
        }

        if(authority == null || authority.isEmpty())
        {
            throw new IllegalArgumentException("Authority string may not be null or empty");
        }

        this.name       = name;
        this.authority  = authority.toUpperCase();
        this.identifier = identifier;
    }

    /**
     * @return Returns the defining authority's name
     */
    public String getAuthority()
    {
        return this.authority;
    }

    /**
     * @return Returns the identifier as assigned by the the authority
     */
    public int getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public String toString()
    {
        final String shortName = String.format("%s:%d",
                                               this.authority,
                                               this.identifier);

        if(this.name == null)
        {
            return shortName;
        }

        return String.format("%s - %s",
                             shortName,
                             this.name);
    }

    @Override
    public boolean equals(final Object object)
    {
        if(object == null || object.getClass() != this.getClass())
        {
            return false;
        }

        final CoordinateReferenceSystem other = (CoordinateReferenceSystem)object;

        return this.authority.equals(other.authority) && this.identifier == other.identifier;
    }

    @Override
    public int hashCode()
    {
        return this.authority.hashCode() ^ this.identifier;
    }

    @Override
    public int compareTo(final CoordinateReferenceSystem other)
    {
        final int EQUAL = 0;

        if(other == null)
        {
            throw new NullPointerException();
        }

        final int comparison = this.authority.compareTo(other.authority);
        if(comparison != EQUAL)
        {
            return comparison;
        }

        return this.identifier - other.identifier;
    }

    private final String name;
    private final String authority;
    private final int    identifier;
}
