package com.rgi.view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

import com.rgi.common.BoundingBox;
import com.rgi.common.Range;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.util.ImageUtility;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;

/**
 * @author Jenifer Cochran
 *
 */
public class TileLoaderBridge
{
    private final        TileStoreReader selectedReader;
    private final        Integer         minimumZoomLevel;
    private final        Integer         maximumZoomLevel;
    private final        CrsProfile      crsProfile;
    private final        Range           zoomRange;
    private final static TileOrigin      leafletOrigin = TileOrigin.LowerLeft;
    private final static TileScheme      leafletTileScheme = new ZoomTimesTwo(0, 31, 1, 1);

    /**
     * @param tileStoreReader
     * @throws TileStoreException
     */
    public TileLoaderBridge(final TileStoreReader tileStoreReader) throws TileStoreException
    {
        this.selectedReader = tileStoreReader;

        this.crsProfile = CrsProfileFactory.create(this.selectedReader.getCoordinateReferenceSystem());

        if(!this.selectedReader.getZoomLevels().isEmpty())
        {
            this.zoomRange = new Range<>(this.selectedReader.getZoomLevels(), Integer::compare);

            this.minimumZoomLevel = (Integer) this.zoomRange.getMinimum();
            this.maximumZoomLevel = (Integer) this.zoomRange.getMaximum();
        }
        else
        {
            //TODO:: when base maps are given throw error here instead
            this.zoomRange        = new Range<>(-1, -1);
            this.maximumZoomLevel = -1;
            this.minimumZoomLevel = -1;
        }
    }

    /**
     * @param z
     * @param x
     * @param y
     * @return
     * @throws TileStoreException
     * @throws IOException
     */
    public byte[] getTile(final int z, final int x, final int y) throws TileStoreException, IOException
    {
        Coordinate<Integer> transformedCoordinate = leafletOrigin.transform(this.selectedReader.getTileOrigin(), x, y, leafletTileScheme.dimensions(z));

        CrsCoordinate crsCoordinate = this.crsProfile.tileToCrsCoordinate(transformedCoordinate.getX(),
                                                                          transformedCoordinate.getY(),
                                                                          this.selectedReader.getBounds(),
                                                                          leafletTileScheme.dimensions(z),
                                                                          this.selectedReader.getTileOrigin());

        BufferedImage tile = this.selectedReader.getTile(crsCoordinate, z);

        return ImageUtility.bufferedImageToBytes(tile, this.selectedReader.getImageType());
    }

    /**
     * @return
     * @throws TileStoreException
     */
    public BoundingBox getBounds() throws TileStoreException
    {
        return this.selectedReader.getBounds();
    }

    /**
     * @return
     * @throws TileStoreException
     */
    public Set<Integer> getZooms() throws TileStoreException
    {
        return this.selectedReader.getZoomLevels();
    }

    /**
     * @return
     */
    public int getMinZoom()
    {
        return this.minimumZoomLevel;
    }

    /**
     * @return
     */
    public int getMaxZoom()
    {
        return this.maximumZoomLevel;
    }

    public TileStoreReader getTileStoreReader()
    {
        return this.selectedReader;
    }

    @Override
    public String toString()
    {
        return this.selectedReader.getName();
    }
}
