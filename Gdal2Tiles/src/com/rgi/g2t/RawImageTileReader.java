package com.rgi.g2t;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

import javax.naming.OperationNotSupportedException;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.osr;

import utility.GdalUtility;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.Range;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.tile.store.TileHandle;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;

/**
 * @author Steven D. Lander
 * 
 * Not an actual {@link TileStoreReader} per se, but has some attributes that make GUI building easier.
 * Only getCrs, getZoomLevels, getBounds, and Stream(int) are implemented.
 *
 */
public class RawImageTileReader implements TileStoreReader {
	
	private final File rawImage;
	private final CoordinateReferenceSystem coordinateReferenceSystem;
	private final TileOrigin tileOrigin = TileOrigin.LowerLeft;
	private final int tileSize = 256;
	
	/**
	 * Constructor
	 * 
	 * @param rawImage A raster image
	 * @throws TileStoreException Thrown when GDAL could not get the correct coordinate reference system of the input raster
	 */
	public RawImageTileReader(final File rawImage) throws TileStoreException
	{
		this.rawImage = rawImage;
		final Dataset dataset = gdal.Open(this.rawImage.toPath().toString(), gdalconstConstants.GA_ReadOnly);
		try
		{
			this.coordinateReferenceSystem = GdalUtility.getCoordinateReferenceSystemFromSpatialReference(GdalUtility.getDatasetSrs(dataset));
		}
		catch(DataFormatException dfe)
		{
			throw new TileStoreException(dfe);
		}
		osr.UseExceptions();
		// Register gdal extensions
		gdal.AllRegister();
	}
	
	/**
	 * Constructor
	 * 
	 * @param coordinateReferenceSystem The coordinate reference system of the input raster (specifically)
	 * @param rawImage A raster image
	 */
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
		throw new TileStoreException(new OperationNotSupportedException());
	}

	@Override
	public long getByteSize() throws TileStoreException {
		throw new TileStoreException(new OperationNotSupportedException());
	}

	@Override
	public BufferedImage getTile(int column, int row, int zoomLevel)
			throws TileStoreException {
		throw new TileStoreException(new OperationNotSupportedException());
	}

	@Override
	public BufferedImage getTile(CrsCoordinate coordinate, int zoomLevel)
			throws TileStoreException {
		throw new TileStoreException(new OperationNotSupportedException());
	}

	@Override
	public Set<Integer> getZoomLevels() throws TileStoreException {
		// Open the dataset
		final Dataset dataset = gdal.Open(this.rawImage.toPath().toString(), gdalconstConstants.GA_ReadOnly);
		// Return the Set of zoom levels, with a LowerLeft origin and 256 pixel square tile size
		return GdalUtility.getZoomLevelsForDataset(dataset, this.tileOrigin, this.tileSize);
	}

	@Override
	public Stream<TileHandle> stream() throws TileStoreException {
		throw new TileStoreException(new OperationNotSupportedException());
	}

	@Override
	public Stream<TileHandle> stream(int zoomLevel) throws TileStoreException {
		// Create a new tile scheme
		// zoomLevel should be the minZoom of the raw image (lowest integer zoom)
		// this zoom should only have one tile
		final TileScheme tileScheme = new ZoomTimesTwo(zoomLevel, zoomLevel, 1, 1);
		final Dataset dataset = gdal.Open(this.rawImage.toPath().toString(), gdalconstConstants.GA_ReadOnly);
		final CrsProfile crsProfile = GdalUtility.getCrsProfileForDataset(dataset);
		// Calculate the tile ranges for all the zoom levels (0-32)
		final List<Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRangesForAllZooms(this.getBounds(),
																									   crsProfile,
																									   tileScheme,
																									   TileOrigin.LowerLeft);
		// Pick out the zoom level range for this particular zoom
		final Range<Coordinate<Integer>> zoomInfo = tileRanges.get(zoomLevel);
		// Get the coordinate information
		final Coordinate<Integer> topLeftCoordinate = zoomInfo.getMinimum();
		final Coordinate<Integer> bottomRightCoordinate = zoomInfo.getMaximum();
		// Parse each coordinate into min/max tiles for X/Y
		final int zoomMinXTile = topLeftCoordinate.getX();
		final int zoomMaxXTile = bottomRightCoordinate.getX();
		final int zoomMinYTile = bottomRightCoordinate.getY();
		final int zoomMaxYTile = topLeftCoordinate.getY();
		// Create a tile handle list that we can append to
		List<TileHandle> tileHandles = new ArrayList<TileHandle>();
		for (int tileY = zoomMaxYTile; tileY >= zoomMinYTile; tileY--)
		{
			// Iterate through all Y's
			for (int tileX = zoomMinXTile; tileX <= zoomMaxXTile; tileX++)
			{
				// Iterate through all X's
				// Create a raw image handle with the z/x/y
				final RawImageTileHandle rawImageTileHandle = new RawImageTileHandle(zoomLevel, tileX, tileY);
				// Add to the list
				tileHandles.add(rawImageTileHandle);
			}
		}
		// Return the entire tile handle list as a stream
		return tileHandles.stream();
	}

	@Override
	public CoordinateReferenceSystem getCoordinateReferenceSystem()
	{
		if (this.coordinateReferenceSystem != null)
		{
			// Return the one from the constructor if it exists
			return this.coordinateReferenceSystem;
		}
		else
		{
			// Open the dataset
			final Dataset dataset = gdal.Open(this.rawImage.toPath().toString(), gdalconstConstants.GA_ReadOnly);
			try
			{
				return GdalUtility.getCoordinateReferenceSystemFromSpatialReference(GdalUtility.getDatasetSrs(dataset));
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
		return "";
	}

	@Override
	public String getImageType() throws TileStoreException {
		throw new TileStoreException(new OperationNotSupportedException());
	}

	@Override
	public Dimensions<Integer> getImageDimensions() throws TileStoreException {
		throw new TileStoreException(new OperationNotSupportedException());
	}

	@Override
	public TileScheme getTileScheme() {
		System.err.println("GetTileScheme is not implemented in RawImageTileReader.");
		return null;
	}
	
	private class RawImageTileHandle implements TileHandle
	{
		private final int zoom;
		private final int column;
		private final int row;
		
		RawImageTileHandle(final int zoom, final int column, final int row)
		{
			this.zoom = zoom;
			this.column = column;
			this.row = row;
		}
		
		@Override
		public int getZoomLevel() {
			return this.zoom;
		}

		@Override
		public int getColumn() {
			return this.column;
		}

		@Override
		public int getRow() {
			return this.row;
		}

		@Override
		public TileMatrixDimensions getMatrix() throws TileStoreException {
			throw new TileStoreException(new OperationNotSupportedException());
		}

		@Override
		public CrsCoordinate getCrsCoordinate() throws TileStoreException {
			throw new TileStoreException(new OperationNotSupportedException());
		}

		@Override
		public CrsCoordinate getCrsCoordinate(TileOrigin corner)
				throws TileStoreException {
			throw new TileStoreException(new OperationNotSupportedException());
		}

		@Override
		public BoundingBox getBounds() throws TileStoreException {
			throw new TileStoreException(new OperationNotSupportedException());
		}

		@Override
		public BufferedImage getImage() throws TileStoreException {
			throw new TileStoreException(new OperationNotSupportedException());
		}
	}
}
