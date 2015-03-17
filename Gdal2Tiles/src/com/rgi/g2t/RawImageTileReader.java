package com.rgi.g2t;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

import javax.naming.OperationNotSupportedException;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;

import utility.GdalUtility;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.store.TileHandle;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;

/**
 * @author Steven D. Lander
 * 
 * Not an actual {@link TileStoreReader} per se, but has some attributes that make GUI building easier.
 * Only getCrs, getBounds, and Stream(int) are implemented.
 *
 */
public class RawImageTileReader implements TileStoreReader {
	
	private final File rawImage;
	private final CoordinateReferenceSystem coordinateReferenceSystem;
	
	public RawImageTileReader(final File rawImage)
	{
		this.rawImage = rawImage;
		this.coordinateReferenceSystem = null;
		osr.UseExceptions();
		// Register gdal extensions
		gdal.AllRegister();
	}
	
	public RawImageTileReader(final CoordinateReferenceSystem coordinateReferenceSystem, final File rawImage)
	{
		this.rawImage = rawImage;
		this.coordinateReferenceSystem = coordinateReferenceSystem;
		osr.UseExceptions();
		// Register gdal extensions
		gdal.AllRegister();
	}
	
	@Override
	public void close() throws Exception {
		throw new OperationNotSupportedException();
	}

	@Override
	public BoundingBox getBounds() throws TileStoreException {
		// Input dataset should be in the SRS the user wants
		final Dataset dataset = gdal.Open(this.rawImage.toPath().toString(), gdalconstConstants.GA_ReadOnly);
		try
		{
			return GdalUtility.getBoundsForDataset(dataset);
		}
		catch(DataFormatException ex)
		{
			throw new TileStoreException("Cannot get bounds for this dataset.");
		}
	}

	@Override
	public long countTiles() throws TileStoreException {
		return 0;
	}

	@Override
	public long getByteSize() throws TileStoreException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BufferedImage getTile(int column, int row, int zoomLevel)
			throws TileStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedImage getTile(CrsCoordinate coordinate, int zoomLevel)
			throws TileStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Integer> getZoomLevels() throws TileStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<TileHandle> stream() throws TileStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<TileHandle> stream(int zoomLevel) throws TileStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CoordinateReferenceSystem getCoordinateReferenceSystem()
	{
		if (this.coordinateReferenceSystem != null)
		{
			return this.coordinateReferenceSystem;
		}
		else
		{
			final Dataset dataset = gdal.Open(this.rawImage.toPath().toString(), gdalconstConstants.GA_ReadOnly);
			try
			{
				final SpatialReference srs = GdalUtility.getDatasetSrs(dataset);
				return GdalUtility.getCoordinateReferenceSystemFromSpatialReference(srs);
			}
			catch(DataFormatException ex)
			{
				System.err.println("Could not get Coordinate Reference System for this dataset.");
				return null;
			}
		}
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getImageType() throws TileStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimensions<Integer> getImageDimensions() throws TileStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TileScheme getTileScheme() {
		// TODO Auto-generated method stub
		return null;
	}

}
