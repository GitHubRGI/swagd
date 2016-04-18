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

package com.rgi.common.tile.scheme;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * A {@link TileScheme} implementation of a common convention where a tile
 * set's tile matrix doubles width and height between successive zoom levels.
 *
 * @author Luke Lambert
 *
 */
public class ZoomTimesTwo implements TileScheme
{

    public static final double BASE_TWO = 2.0;

    /**
     * Constructor
     *
     * @param minimumZoomLevel
     *             Lowest valid level of zoom for this tile scheme. Must be 0 or greater, and less than or equal to maximumZoomLevel.
     * @param maximumZoomLevel
     *             Highest valid level of zoom for this tile scheme. Must be 0 or greater, and greater than or equal to minimumZoomLevel.
     * @param baseZoomLevelTileMatrixWidth
     *             The number of tiles along the x axis for the lowest zoom level
     * @param baseZoomLevelTileMatrixHeight
     *             The number of tiles along the y axis for the lowest zoom level
     */
    public ZoomTimesTwo(final int minimumZoomLevel,
                        final int maximumZoomLevel,
                        final int baseZoomLevelTileMatrixWidth,
                        final int baseZoomLevelTileMatrixHeight)
    {
        if(minimumZoomLevel < 0)
        {
            throw new IllegalArgumentException("Minimum zoom level must be at least 0");
        }

        if(maximumZoomLevel < 0)
        {
            throw new IllegalArgumentException("Maximum zoom level must be at least 0");
        }

        if(baseZoomLevelTileMatrixHeight < 1)
        {
            throw new IllegalArgumentException("The initial height must be greater than 0");
        }

        if(baseZoomLevelTileMatrixWidth < 1)
        {
            throw new IllegalArgumentException("The initial width must be greater than 0");
        }

        if(minimumZoomLevel > maximumZoomLevel)
        {
            throw new IllegalArgumentException("Minimum zoom level must be less than or equal to the maximum");
        }

        //if(Integer.MAX_VALUE < baseZoomLevelTileMatrixHeight * Math.pow(2.0, maximumZoomLevel) - 1)
        if(baseZoomLevelTileMatrixHeight * StrictMath.pow(BASE_TWO, maximumZoomLevel) - 1 > Integer.MAX_VALUE)
        {
            throw new IllegalArgumentException("This combination of initial height and maximum zoom level will cause an integer overflow for tile numbering");
        }

        if(baseZoomLevelTileMatrixWidth * StrictMath.pow(BASE_TWO, maximumZoomLevel) - 1 > Integer.MAX_VALUE)
        {
            throw new IllegalArgumentException("This combination of initial width and maximum zoom level will cause an integer overflow for tile numbering");
        }

        this.minimumZoomLevel = minimumZoomLevel;
        this.maximumZoomLevel = maximumZoomLevel;

        this.zoomLevelDimensions = new TileMatrixDimensions[maximumZoomLevel - minimumZoomLevel + 1];

        IntStream.rangeClosed(minimumZoomLevel, maximumZoomLevel).forEach(zoomLevel ->
        {
            final int zoom = zoomLevel - minimumZoomLevel;
            final int twoToTheZoomPower = (int) StrictMath.pow(BASE_TWO, zoom);
            this.zoomLevelDimensions[zoom] = new TileMatrixDimensions(baseZoomLevelTileMatrixWidth  * twoToTheZoomPower,
                    baseZoomLevelTileMatrixHeight * twoToTheZoomPower);
        });
    }

    @Override
    public TileMatrixDimensions dimensions(final int zoomLevel)
    {
        if(zoomLevel < this.minimumZoomLevel || zoomLevel > this.maximumZoomLevel)
        {
            throw new IllegalArgumentException(String.format("Zoom level must be in the range [%d, %d]",
                                               this.minimumZoomLevel,
                                               this.maximumZoomLevel));
        }

        return this.zoomLevelDimensions[zoomLevel - this.minimumZoomLevel];
    }

    @Override
    public Collection<Integer> getZoomLevels()
    {
        return IntStream.rangeClosed(this.minimumZoomLevel,
                                     this.maximumZoomLevel)
                        .boxed()
                        .collect(Collectors.toList());
    }

    private final TileMatrixDimensions[] zoomLevelDimensions;

    private final int minimumZoomLevel;
    private final int maximumZoomLevel;
}
