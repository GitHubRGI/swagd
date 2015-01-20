package com.rgi.common.tile.scheme;

import com.rgi.common.tile.TileOrigin;

public class ZoomTimesTwo implements TileScheme
{
    /**
     * TODO
     *
     * @param initialHeight
     * @param initialWidth
     * @param origin
     */
    public ZoomTimesTwo(final int        initialHeight,
                        final int        initialWidth,
                        final TileOrigin origin)
    {
        if(initialHeight < 1)
        {
            throw new IllegalArgumentException("The initial height must be greater than 0");
        }

        if(initialWidth < 1)
        {
            throw new IllegalArgumentException("The initial width must be greater than 0");
        }

        if(origin == null)
        {
            throw new IllegalArgumentException("Tile origin may not be null");
        }

        for(int zoomLevel = MinZoomLevel; zoomLevel < MaxZoomLevel; ++zoomLevel)
        {
            this.zoomLevels[zoomLevel] = new MatrixDimensions(initialHeight * (int)Math.pow(2.0, zoomLevel),    // For high zoom levels, and an initial height/width > 1, these values can overflow the maximum size of an integer.  It's unlikely that anyone is going to be using zoom levels that high.
                                                              initialWidth  * (int)Math.pow(2.0, zoomLevel));
        }

        this.origin = origin;
    }

    @Override
    public MatrixDimensions dimensions(final int zoomLevel)
    {
        if(zoomLevel < MinZoomLevel || zoomLevel > MaxZoomLevel)
        {
            throw new IllegalArgumentException(String.format("Zoom level must be in the range [%d, %d]",
                                               MinZoomLevel,
                                               MaxZoomLevel));
        }

        return this.zoomLevels[zoomLevel];
    }

    @Override
    public TileOrigin origin()
    {
        return this.origin;
    }

    private final static int MinZoomLevel = 0;
    private final static int MaxZoomLevel = 31;

    private final TileOrigin         origin;
    private final MatrixDimensions[] zoomLevels = new MatrixDimensions[MaxZoomLevel];
}
