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

package com.rgi.common.coordinate;


/**
 * Coordinate within a specific coordinate reference system
 *
 * @author Luke Lambert
 *
 */
public class CrsCoordinate extends Coordinate<Double>
{
    /**
     * Constructor
     *
     * @param x
     *             Horizontal portion of the coordinate
     * @param y
     *             Vertical portion of the coordinate
     * @param authority
     *             The name of the defining authority of the coordinate
     *             reference system (e.g. "EPSG"). This value is converted to
     *             upper case.
     * @param identifier
     *             The identifier as assigned by the the authority of the
     *             coordinate reference system
     */
    public CrsCoordinate(final double x, final double y, final String authority, final int identifier)
    {
        this(x, y, new CoordinateReferenceSystem(authority, identifier));
    }

    /**
     * Constructor
     *
     * @param x
     *             Horizontal portion of the coordinate
     * @param y
     *             Vertical portion of the coordinate
     * @param coordinateReferenceSystem
     *             A coordinate reference system object
     */
    public CrsCoordinate(final double x, final double y, final CoordinateReferenceSystem coordinateReferenceSystem)
    {
        this(new Coordinate<>(x, y), coordinateReferenceSystem);
    }

    /**
     * Constructor
     *
     * @param coordinate
     *             A coordinate in the units of the coordinate reference system
     * @param coordinateReferenceSystem
     *             A coordinate reference system object
     */
    public CrsCoordinate(final Coordinate<Double> coordinate, final CoordinateReferenceSystem coordinateReferenceSystem)
    {
        super(coordinate);

        if(coordinateReferenceSystem == null)
        {
            throw new IllegalArgumentException("Coordinate Reference System may not be null");
        }

        this.coordinateReferenceSystem = coordinateReferenceSystem;
    }

    @Override
    public boolean equals(final Object object)
    {
        if(object == null || object.getClass() != this.getClass())
        {
            return false;
        }

        final CrsCoordinate other = (CrsCoordinate)object;

        return super.equals(other) && this.coordinateReferenceSystem.equals(other.coordinateReferenceSystem);
    }

    @Override
    public int hashCode()
    {
        return super.hashCode() ^ this.coordinateReferenceSystem.hashCode();
    }

    /**
     * @return Returns the coordinate reference system
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.coordinateReferenceSystem;
    }

    private final CoordinateReferenceSystem coordinateReferenceSystem;
}
