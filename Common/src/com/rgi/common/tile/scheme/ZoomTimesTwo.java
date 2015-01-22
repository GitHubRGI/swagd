package com.rgi.common.tile.scheme;

import com.rgi.common.tile.TileOrigin;

public class ZoomTimesTwo implements TileScheme
{

    /**
     * TODO
     *
     * @param minimumZoomLevel
     * @param maximumZoomLevel
     * @param initialHeight
     * @param initialWidth
     * @param origin
     */
    public ZoomTimesTwo(final int        minimumZoomLevel,
                        final int        maximumZoomLevel,
                        final int        initialHeight,
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

        if(minimumZoomLevel >= maximumZoomLevel)
        {
            throw new IllegalArgumentException("Minimum zoom level must be less than or equal to the maximum");
        }

        if(Integer.MAX_VALUE < (double)initialHeight * (int)Math.pow(2.0, maximumZoomLevel))
        {
            throw new IllegalArgumentException("This combination of initial height and maximum zoom level will cause an integer overflow for tile numbering");
        }

        if(Integer.MAX_VALUE < (double)initialWidth * (int)Math.pow(2.0, maximumZoomLevel))
        {
            throw new IllegalArgumentException("This combination of initial width and maximum zoom level will cause an integer overflow for tile numbering");
        }

        this.zoomLevelDimensions = new MatrixDimensions[maximumZoomLevel - minimumZoomLevel];

        for(int zoomLevel = minimumZoomLevel; zoomLevel < maximumZoomLevel; ++zoomLevel)
        {
            this.zoomLevelDimensions[zoomLevel - minimumZoomLevel] = new MatrixDimensions(initialHeight * (int)Math.pow(2.0, zoomLevel),
                                                                                          initialWidth  * (int)Math.pow(2.0, zoomLevel));
        }

        this.origin           = origin;
        this.minimumZoomLevel = minimumZoomLevel;
        this.maximumZoomLevel = maximumZoomLevel;
    }

    @Override
    public MatrixDimensions dimensions(final int zoomLevel)
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
    public TileOrigin origin()
    {
        return this.origin;
    }

    /**
     * @return the minimumZoomLevel
     */
    public int getMinimumZoomLevel()
    {
        return this.minimumZoomLevel;
    }

    /**
     * @return the maximumZoomLevel
     */
    public int getMaximumZoomLevel()
    {
        return this.maximumZoomLevel;
    }

    private final TileOrigin origin;
    private final int        minimumZoomLevel;
    private final int        maximumZoomLevel;

    private final MatrixDimensions[] zoomLevelDimensions;
}
