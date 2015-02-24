/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package com.rgi.geopackage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.junit.Assert;
import org.junit.Test;

import store.GeoPackageReader;
import store.GeoPackageWriter;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.SphericalMercatorCrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.tile.store.TileHandle;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.util.ImageUtility;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.tiles.Tile;
import com.rgi.geopackage.tiles.TileMatrix;
import com.rgi.geopackage.tiles.TileSet;
import com.rgi.geopackage.verification.ConformanceException;


/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings("javadoc")
public class GeopackageTileStoreTest
{
    private final Random randomGenerator = new Random();
    private final List<Integer> bufferedImageList = Arrays.asList(BufferedImage.TYPE_3BYTE_BGR,
                                                                  BufferedImage.TYPE_4BYTE_ABGR,
                                                                  BufferedImage.TYPE_4BYTE_ABGR_PRE,
                                                                  BufferedImage.TYPE_BYTE_BINARY,
                                                                  BufferedImage.TYPE_BYTE_GRAY,
                                                                  BufferedImage.TYPE_BYTE_INDEXED,
                                                                  BufferedImage.TYPE_INT_ARGB,
                                                                  BufferedImage.TYPE_INT_BGR,
                                                                  BufferedImage.TYPE_INT_RGB,
                                                                  BufferedImage.TYPE_USHORT_555_RGB,
                                                                  BufferedImage.TYPE_USHORT_565_RGB,
                                                                  BufferedImage.TYPE_USHORT_GRAY);


    /**
     * Tests if geopackage reader will throw an IllegalArgumentException when
     * passing a null value for TileSet
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */

    @Test(expected = IllegalArgumentException.class)
    public void geopackageReaderIllegalArgumentException() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = this.getRandomFile(9);

        try(final GeoPackageReader reader = new GeoPackageReader(testFile, null))
        {
            fail("Expected GeoPackage Reader to throw an IllegalArguementException when passing a null value for tileSet");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
        }
    }

    /**
     * Tests if geopackage reader will throw an IllegalArgumentException when
     * passing a null value for TileSet
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void geopackageReaderIllegalArgumentException2() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = this.getRandomFile(9);

        try(final GeoPackageReader reader = new GeoPackageReader(null, "tablename"))
        {
            fail("Expected GeoPackage Reader to throw an IllegalArguementException when passing a null value for GeoPackage");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
        }
    }

    /**
     * Tests if the GeoPackage Reader will throw an IllegalArgumentException when
     * a given a name of a tileSet that doesn't exist
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void geopackageReaderIllegalArgumentException3() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = this.getRandomFile(11);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            try(GeoPackageReader reader = new GeoPackageReader(testFile, "A TileSet that doesn't Exist");)
            {
                fail("Expected GeoPackageReader to throw an illegalArgumentException when trying to read from a tileSet that doesn't exist");
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the getBounds method in geopackage reader
     * returns the expected bounding box
     * @throws SQLException
     * @throws ConformanceException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws FileAlreadyExistsException
     * @throws TileStoreException
     *
     */
    @Test
    public void geopackageReaderGetBounds()throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(8);

        final String tableName = "tablename";

        final BoundingBox bBoxGiven  = new BoundingBox(0.0,0.0,180.0,180.0);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            gpkg.tiles().addTileSet(tableName, "identifier", "description", bBoxGiven, gpkg.core().getSpatialReferenceSystem(4326));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
            {
                final BoundingBox bBoxReturned = gpkgReader.getBounds();
                Assert.assertTrue("The bounding box returned from GeoPackageReader was not the same that was given.", bBoxGiven.equals(bBoxReturned));
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if countTiles method from GeoPackage Reader
     *  returns the expected value
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws TileStoreException
     * @throws IOException
     */
    @Test
    public void geopackageReaderCountTiles() throws ClassNotFoundException, SQLException, ConformanceException, TileStoreException, IOException
    {
        final File testFile = this.getRandomFile(8);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final BoundingBox bBox = new BoundingBox(0.0,0.0,180.0,180.0);

            final int zoomLevel    = 2;
            final int matrixWidth  = 3;
            final int matrixHeight = 3;

            final TileMatrix tileMatrix = GeopackageTileStoreTest.createTileSetAndTileMatrix(gpkg, bBox, zoomLevel, matrixWidth, matrixHeight);
            final TileSet    tileSet    = gpkg.tiles().getTileSet(tileMatrix.getTableName());

            //add tiles
            gpkg.tiles().addTile(tileSet, tileMatrix, 0, 0, createImageBytes(BufferedImage.TYPE_3BYTE_BGR));
            gpkg.tiles().addTile(tileSet, tileMatrix, 1, 0, createImageBytes(BufferedImage.TYPE_3BYTE_BGR));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tileSet.getTableName()))
            {
                final long numberOfTiles = gpkgReader.countTiles();

                Assert.assertTrue(String.format("Expected the GeoPackage Reader countTiles to return a value of 2 but instead returned %d",
                                                numberOfTiles),
                                 numberOfTiles == 2);
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Reader Returns the expected
     * value for getByteSize() of the file
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws TileStoreException
     */
    @Test
    public void getByteSize() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(8);

        final String tableName = "tablename";

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final BoundingBox bBoxGiven = new BoundingBox(0.0,0.0,180.0,180.0);

            gpkg.tiles().addTileSet(tableName, "identifier", "description", bBoxGiven, gpkg.core().getSpatialReferenceSystem(4326));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
            {
                final long byteSizeReturned = gpkgReader.getByteSize();
                final long byteSizeExpected = testFile.getTotalSpace();

                Assert.assertTrue(String.format("The GeoPackage Reader did not return the expected value. \nExpected: %d Actual: %d",
                                                byteSizeReturned,
                                                byteSizeExpected),
                                  byteSizeReturned ==  byteSizeExpected);
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the tile retrieved is the same as it was given
     * (or as expected) using getTile from GeoPackage Reader
     * getTile (row, column, zoom)
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws IOException
     * @throws TileStoreException
     */
    @Test
    public void getTile() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final BoundingBox bBox = new BoundingBox(0.0, 0.0, 180.0, 180.0);
            final int zoomLevel    = 2;
            final int matrixWidth  = 3;
            final int matrixHeight = 3;

            final TileMatrix tileMatrix = GeopackageTileStoreTest.createTileSetAndTileMatrix(gpkg,  bBox, zoomLevel, matrixWidth, matrixHeight);
            final TileSet    tileSet = gpkg.tiles().getTileSet(tileMatrix.getTableName());

            final int column = 0;
            final int row    = 0;

            final Tile tileExpected = gpkg.tiles().addTile(tileSet, tileMatrix, column, row, createImageBytes(BufferedImage.TYPE_4BYTE_ABGR));

            gpkg.tiles().addTile(tileSet, tileMatrix, 1, 0, createImageBytes(BufferedImage.TYPE_4BYTE_ABGR));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tileSet.getTableName()))
            {
                final BufferedImage returnedImage = gpkgReader.getTile(column, row, zoomLevel);

                Assert.assertArrayEquals("The tile image data returned from getTile in GeoPackage reader wasn't the same as the one given.",
                                         tileExpected.getImageData(),
                                         ImageUtility.bufferedImageToBytes(returnedImage, "PNG"));
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if it will return the correct tile given the
     * crs tile coordinate
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws IOException
     * @throws TileStoreException
     */
    @Test
    public void getTile2WithCrsCoordinate() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(8);


        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final BoundingBox bBox = new BoundingBox(0.0, 0.0, 180.0, 180.0);
            final int zoomLevel    = 2;
            final int matrixWidth  = 3;
            final int matrixHeight = 3;

            final TileMatrix             tileMatrix = GeopackageTileStoreTest.createTileSetAndTileMatrix(gpkg, bBox, zoomLevel, matrixWidth, matrixHeight);
            final TileSet                tileSet    = gpkg.tiles().getTileSet(tileMatrix.getTableName());

            //add tiles
            final Tile tileExpected = gpkg.tiles().addTile(tileSet, tileMatrix, 0, 0, createImageBytes(BufferedImage.TYPE_4BYTE_ABGR));

            gpkg.tiles().addTile(tileSet, tileMatrix, 1, 0, createImageBytes(BufferedImage.TYPE_4BYTE_ABGR));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tileSet.getTableName()))
            {
                final CrsCoordinate crsTileCoordinate = new CrsCoordinate(60.0, 130.0, "epsg", 4326);
                final BufferedImage returnedImage     = gpkgReader.getTile(crsTileCoordinate, zoomLevel);

                Assert.assertArrayEquals("The tile image data returned from getTile in GeoPackage reader wasn't the same as the one given.",
                                         tileExpected.getImageData(),
                                         ImageUtility.bufferedImageToBytes(returnedImage, "PNG"));
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    /**
     * Tests if it will return the correct Tile
     * when given a Crscoordinate.  This is an edge case.
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws IOException
     * @throws TileStoreException
     */
    @Test
    public void getTile3CrsCoordinate() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(8);


        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final BoundingBox bBox = new BoundingBox(0.0, 0.0, 180.0, 180.0);
            final int zoomLevel    = 2;
            final int matrixWidth  = 3;
            final int matrixHeight = 3;

            final TileMatrix tileMatrix = GeopackageTileStoreTest.createTileSetAndTileMatrix(gpkg, bBox, zoomLevel, matrixWidth, matrixHeight);
            final TileSet    tileSet    = gpkg.tiles().getTileSet(tileMatrix.getTableName());

            //add tiles
            gpkg.tiles().addTile(tileSet, tileMatrix, 0, 0, createImageBytes(BufferedImage.TYPE_BYTE_GRAY));
            final Tile tileExpected = gpkg.tiles().addTile(tileSet, tileMatrix, 0, 2, createImageBytes(BufferedImage.TYPE_BYTE_GRAY));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tileSet.getTableName()))
            {
                final CrsCoordinate    crsTileCoordinate = new CrsCoordinate(59.0, 60.0, "epsg", 4326);
                final BufferedImage    returnedImage     = gpkgReader.getTile(crsTileCoordinate, zoomLevel);

                Assert.assertArrayEquals("The tile image data returned from getTile in GeoPackage reader wasn't the same as the one given.",
                                         tileExpected.getImageData(),
                                         ImageUtility.bufferedImageToBytes(returnedImage, "PNG"));
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageReader throws an IllegalArgumentException
     * if passed a null value for coordinate when using the method
     * getTile
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws TileStoreException
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileIllegalArgumentException() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(12);

        final String tableName = "tablename";

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            gpkg.tiles().addTileSet(tableName,
                                    "identifier",
                                    "description",
                                    new BoundingBox(0.0,0.0,60.0,30.0),
                                    gpkg.core().getSpatialReferenceSystem(4326));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
            {
                gpkgReader.getTile(null, 2);
                fail("Expected GeoPackageReader to throw an IllegalArgumentException when passing a null value to coordinate in getTile method");
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageReader throws an IllegalArgumentException
     * if passed a value of a coordinate with a different coordinate Reference System
     * than the tileSet
     * in the method getTile
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws TileStoreException
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileIllegalArgumentException2() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(12);

        final String tableName = "tablename";

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            gpkg.tiles().addTileSet(tableName,
                                    "identifier",
                                    "description",
                                    new BoundingBox(0.0,0.0,60.0,30.0),
                                    gpkg.core().addSpatialReferenceSystem("Srs Name", 3857, "EPSG", 3857, "definition", "description"));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
            {
                final CrsCoordinate crsCoord = new CrsCoordinate(0, 2, "EPSG", 3395);
                gpkgReader.getTile(crsCoord, 2);
                fail("Expected GeoPackageReader to throw an IllegalArgumentException when passing a null value to coordinate in getTile method");
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageReader returns a null value for
     * a buffered image when asking for a tile that is not
     * in the geopackage
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws TileStoreException
     */
    @Test
    public void getTileThatDoesntExist() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);

        final String tableName = "tablename";

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            gpkg.tiles().addTileSet(tableName,
                                    "identifier",
                                    "description",
                                    new BoundingBox(0.0,0.0,60.0,30.0),
                                    gpkg.core().addSpatialReferenceSystem("Srs Name", 3857, "EPSG", 3857, "definition", "description"));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
            {
                final CrsCoordinate coordinate = new CrsCoordinate(0, 2, "EPSG", 3857);
                final BufferedImage imageReturned = gpkgReader.getTile(coordinate, 4);

                assertTrue("Asked for a tile that didn't exist in the gpkg and didn't return a null value for the buffer image", imageReturned == null);
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage Reader returns the expected
     * coordinate reference system
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void getCoordinateReferenceSystem() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = this.getRandomFile(7);

        final String tableName = "tablename";

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final SpatialReferenceSystem spatialReferenceSystem = gpkg.core().addSpatialReferenceSystem("WebMercator", 3857, "epsg", 3857, "definition", "description");

            gpkg.tiles().addTileSet(tableName, "identifier", "description", new BoundingBox(0.0,0.0,60.0,30.0), spatialReferenceSystem);

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
            {
                final CoordinateReferenceSystem coordinateReferenceSystemReturned = gpkgReader.getCoordinateReferenceSystem();

                Assert.assertTrue(String.format("The coordinate reference system returned is not what was expected. Actual authority: %s Actual Identifer: %d\nExpected authority: %s Expected Identifier: %d.",
                                                coordinateReferenceSystemReturned.getAuthority(),
                                                coordinateReferenceSystemReturned.getIdentifier(),
                                                spatialReferenceSystem.getOrganization(),
                                                spatialReferenceSystem.getIdentifier()),
                                  coordinateReferenceSystemReturned.getAuthority().equalsIgnoreCase(spatialReferenceSystem.getOrganization()) &&
                                  coordinateReferenceSystemReturned.getIdentifier() ==              spatialReferenceSystem.getIdentifier());
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage Reader returns the expected zoom levels
     * for a given geopackage and tile set
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws TileStoreException
     */
    @Test
    public void getZoomLevels() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(10);

        final String tableName = "tablename";

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final BoundingBox bBox =  new BoundingBox(0.0,0.0,60.0,30.0);
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet(tableName,
                                                    "identifier",
                                                    "description",
                                                    bBox,
                                                    gpkg.core().getSpatialReferenceSystem(4326));
            final TileSet tileSet2 = gpkg.tiles()
                                         .addTileSet("tabelName2",
                                                     "identifier2",
                                                     "description2",
                                                     bBox,
                                                     gpkg.core().getSpatialReferenceSystem(-1));

            final Set<Integer> zoomLevelsExpected = new HashSet<>(Arrays.asList(2,5,9,20));

            final int tileWidth = 256;
            final int tileHeight = 256;

            final TileScheme tileScheme = new ZoomTimesTwo(2, 20, 1, 1);
            addTileMatriciesToGpkg(zoomLevelsExpected, tileSet, gpkg, tileScheme, tileWidth, tileHeight);

            gpkg.tiles().addTileMatrix(tileSet2,  7, 2, 2, tileWidth, tileHeight, bBox.getWidth() / 2 / tileWidth, bBox.getHeight() / 2 / tileHeight); //this one is not included in zooms because it is a different tileset

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
            {
                final Set<Integer> zoomLevelsReturned = gpkgReader.getZoomLevels();

                Assert.assertTrue(String.format("The GeoPackage Reader did not return all of the zoom levels expected. Expected Zooms: %s. Actual Zooms: %s",
                                                zoomLevelsExpected.stream().map(integer -> integer.toString()).collect(Collectors.joining(", ")),
                                                zoomLevelsReturned.stream().map(integer -> integer.toString()).collect(Collectors.joining(", "))),
                                  zoomLevelsReturned.containsAll(zoomLevelsExpected));
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    /**
     * Tests if the GeoPackage Reader returns the correct TileScheme given a GeoPackage
     * with various zoom levels and matrices
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws IOException
     */
    @Test
    public void geoPackageReaderGetTileScheme() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = this.getRandomFile(10);

        final String tableName = "tablename";

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final BoundingBox bBox =  new BoundingBox(0.0,0.0,90.0,90.0);
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet(tableName,
                                                    "identifier",
                                                    "description",
                                                    bBox,
                                                    gpkg.core().getSpatialReferenceSystem(4326));

            final Set<Integer> zoomLevelsExpected = new HashSet<>(Arrays.asList(2,5,9,20));

            final int tileWidth = 256;
            final int tileHeight = 256;

            final TileScheme tileScheme = new ZoomTimesTwo(2, 20, 1, 1);
            final Set<TileMatrixDimensions> tileMatrixDimensionsExpected = new HashSet<>();
            addTileMatriciesToGpkg(zoomLevelsExpected, tileSet, gpkg, tileScheme, tileWidth, tileHeight);

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
            {
                final TileScheme tileSchemeReturned = gpkgReader.getTileScheme();

                final List<TileMatrixDimensions> tileMatrixDimensionsReturned =  zoomLevelsExpected.stream().map(zoomLevel -> tileSchemeReturned.dimensions(zoomLevel)).collect(Collectors.toList());

                assertTrue(String.format("The TileScheme from the gpkgReader did not return all the dimensions expected or they were incorrect from what was given to the geopackage"),
                           tileMatrixDimensionsExpected.stream().allMatch(expectedDimension -> tileMatrixDimensionsReturned.stream().anyMatch(returnedDimension -> expectedDimension.getWidth() == returnedDimension.getWidth() &&
                                                                                                                                                                   expectedDimension.getHeight() == returnedDimension.getWidth())));
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the method .stream() in GeoPackageReader returns the expected tile handles
     * with the correct values
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws IOException
     * @throws TileStoreException
     */
    @Test
    public void getStreamofTileHandles() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = this.getRandomFile(12);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            // Create a tileSet
            final BoundingBox bBox =  new BoundingBox(0.0,0.0,80.0,40.0);
            final int matrixWidth  = 2;
            final int matrixHeight = 4;
            final int zoomLevel    = 9;

            // Create matrix
            final TileMatrix tileMatrix = GeopackageTileStoreTest.createTileSetAndTileMatrix(gpkg, bBox, zoomLevel, matrixWidth, matrixHeight);
            final TileSet    tileSet    = gpkg.tiles().getTileSet(tileMatrix.getTableName());


            // Add three tiles
            final Tile tile  = gpkg.tiles().addTile(tileSet, tileMatrix, 0, 0, createImageBytes(BufferedImage.TYPE_INT_ARGB));
            final Tile tile2 = gpkg.tiles().addTile(tileSet, tileMatrix, 0, 1, createImageBytes(BufferedImage.TYPE_3BYTE_BGR));
            final Tile tile3 = gpkg.tiles().addTile(tileSet, tileMatrix, 1, 0, createImageBytes(BufferedImage.TYPE_BYTE_GRAY));
            
            

            final List<Tile> expectedTiles = Arrays.asList(tile, tile2, tile3); // Create a list of the expected tiles

            try(GeoPackageReader reader = new GeoPackageReader(testFile, tileSet.getTableName()))
            {
                // Check to see if tiles match
                final boolean tilesEqual = expectedTiles.stream()
                                                        .allMatch(expectedTile -> { try
                                                                                    {
                                                                                         return reader.stream().anyMatch(tileHandle -> tileAndTileHandleEqual(expectedTile, tileHandle, "png"));
                                                                                    }
                                                                                    catch(final TileStoreException ex)
                                                                                    {
                                                                                         return false;
                                                                                    }
                                                                                  });
                assertTrue("GeoPackage reader returned tiles that were not equal to the ones in the GeoPackage.", tilesEqual);
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    /**
     * Tests if the stream(int zoomLevel) returns all the correct
     * tiles for that particular zoom level
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws IOException
     * @throws TileStoreException
     */
    @Test
    public void getStreamOfTileHandlesByZoom() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = this.getRandomFile(6);
        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
              final int matrixWidth    = 5;
              final int matrixHeight   = 6;

              final BoundingBox  bBox       = new BoundingBox(-100, -90, 100, 90);
              final TileScheme   tileScheme = new ZoomTimesTwo(0, 8, matrixWidth, matrixHeight);
              final Set<Integer> zoomLevels = new HashSet<>(Arrays.asList(1,3,6,8));

              final TileSet tileSet = gpkg.tiles()
                                          .addTileSet("TableName",
                                                      "identifier",
                                                      "description",
                                                      bBox,
                                                      gpkg.core().getSpatialReferenceSystem(4326));

              addTileMatriciesToGpkg(zoomLevels,
                                     tileSet,
                                     gpkg,
                                     tileScheme,
                                     256,
                                     256);

              final Map<Integer, TileMatrix> tileMatrices = gpkg.tiles()
                                                                .getTileMatrices(tileSet)
                                                                .stream()
                                                                .collect(Collectors.toMap(tileMatrix -> tileMatrix.getZoomLevel(),
                                                                                          tileMatrix -> tileMatrix));

              final List<Integer[]> tileCoordinates = Arrays.asList(new Integer[]{ 2, 1, 1},
                                                                    new Integer[]{ 4, 2, 3},
                                                                    new Integer[]{ 7, 5, 6},
                                                                    new Integer[]{10, 9, 8},
                                                                    new Integer[]{ 5, 2, 6},
                                                                    new Integer[]{ 1, 0, 1});

              final List<Tile> tiles = tileCoordinates.stream()
                                                      .map(tileCoordinate -> { try
                                                                               {
                                                                                  final int randomIndex = this.randomGenerator.nextInt(this.bufferedImageList.size());

                                                                                  final int column    = tileCoordinate[0];
                                                                                  final int row       = tileCoordinate[1];
                                                                                  final int zoomLevel = tileCoordinate[2];

                                                                                  return gpkg.tiles()
                                                                                             .addTile(tileSet,
                                                                                                      tileMatrices.get(zoomLevel),
                                                                                                      column,
                                                                                                      row,
                                                                                                      createImageBytes(this.bufferedImageList.get(randomIndex)));
                                                                               }
                                                                               catch(final Exception ex)
                                                                               {
                                                                                  return null;
                                                                               }
                                                                             })
                                                      .filter(Objects::nonNull)
                                                      .collect(Collectors.toList());

              try(final GeoPackageReader reader = new GeoPackageReader(testFile, tileSet.getTableName()))
              {
                  for(final int zoomLevel : zoomLevels)
                  {
                         final boolean valid =  tiles.stream()
                                                     .filter(tile -> tile.getZoomLevel() == zoomLevel)
                                                     .allMatch(expectedTile -> { try
                                                                                 {
                                                                                     return reader.stream(zoomLevel)
                                                                                                  .anyMatch(tileHandle -> tileAndTileHandleEqual(expectedTile, tileHandle, "png"));
                                                                                 }
                                                                                 catch(final TileStoreException ex)
                                                                                 {
                                                                                     return false;
                                                                                 }
                                                                                });
                         assertTrue(String.format("For the zoom level %d the GeoPackageReader.stream(int zoom) did not return the expected tileHandle values.",
                                                  zoomLevel),
                                    valid);
                  }
             }
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
    /**
     * Tests if GeoPackageReader image type can return
     * the correct format name.
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     * @throws TileStoreException
     */
    @Test
    public void geoPackageReaderGetImageType() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            //create tileset and matrix
            final TileMatrix tileMatrix = createTileSetAndTileMatrix(gpkg, new BoundingBox(-90.0, -180.0, 90.0, 180.0), 10, 7, 9);
            final String formatName = "PNG";
            //create image
            final byte[] imagebytes = createImageBytes(BufferedImage.TYPE_INT_ARGB, formatName);
            //add image to gpkg
            gpkg.tiles().addTile(gpkg.tiles().getTileSet(tileMatrix.getTableName()),
                                 tileMatrix,
                                 5,
                                 6,
                                 imagebytes);

            try(GeoPackageReader reader = new GeoPackageReader(testFile, tileMatrix.getTableName()))
            {
                final String imageTypeReturned = reader.getImageType();
                assertTrue(String.format("The image Type returned from the reader was not the format name expected. \nActual: %s\nExpected: %s.", imageTypeReturned, formatName),
                           formatName.equalsIgnoreCase(imageTypeReturned));
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
    /**
     * Tests if GeoPackageReader image type can return
     * the correct format name.
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     * @throws TileStoreException
     */
    @Test
    public void geoPackageReaderGetImageType2() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            //create tileset and matrix
            final TileMatrix tileMatrix = createTileSetAndTileMatrix(gpkg, new BoundingBox(-90.0, -180.0, 90.0, 180.0), 10, 7, 9);
            final String formatName = "jpeg";
            //create image
            final byte[] imagebytes = createImageBytes(BufferedImage.TYPE_INT_ARGB, formatName);
            //add image to gpkg
            gpkg.tiles().addTile(gpkg.tiles().getTileSet(tileMatrix.getTableName()),
                                 tileMatrix,
                                 5,
                                 6,
                                 imagebytes);

            try(GeoPackageReader reader = new GeoPackageReader(testFile, tileMatrix.getTableName()))
            {
                final String imageTypeReturned = reader.getImageType();
                assertTrue(String.format("The image Type returned from the reader was not the format name expected. \nActual: %s\nExpected: %s.", imageTypeReturned, formatName),
                           formatName.equalsIgnoreCase(imageTypeReturned));
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
    /**
     * Tests if GeoPackageReader image type can return
     * the correct format name.
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     * @throws TileStoreException
     */
    @Test
    public void geoPackageReaderGetImageType3() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            //create tileset and matrix
            final TileMatrix tileMatrix = createTileSetAndTileMatrix(gpkg, new BoundingBox(-90.0, -180.0, 90.0, 180.0), 10, 7, 9);

            try(GeoPackageReader reader = new GeoPackageReader(testFile, tileMatrix.getTableName()))
            {
                final String imageTypeReturned = reader.getImageType();
                assertTrue(String.format("The image Type returned from the reader was not the format name expected. \nActual: %s\nExpected: null.", imageTypeReturned),
                           imageTypeReturned == null);
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }
 //TODO finish test   
    @Test
    public void tileHandleCrsCoordinate() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        File testFile = this.getRandomFile(12);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            //createTileSet and matrix
            BoundingBox bBox = new BoundingBox(0.0, 0.0, 80.0, 90.0);
            
            TileMatrix tileMatrix = createTileSetAndTileMatrix(gpkg, bBox, 12, 4, 3);
            Integer row = 1;
            Integer column = 0;
            gpkg.tiles().addTile(gpkg.tiles().getTileSet(tileMatrix.getTableName()), 
                                 tileMatrix, 
                                 column,
                                 row,
                                 createImageBytes(BufferedImage.TYPE_INT_BGR));
            //expected CrsCoords
            CrsCoordinate expectedLowerLeft = new CrsCoordinate(bBox.getTopLeft().getX() + column*(bBox.getWidth()/tileMatrix.getMatrixWidth()), 
                                                                bBox.getTopLeft().getY() - (row+1)*(bBox.getHeight()/tileMatrix.getMatrixHeight()), 
                                                                "EPSG", 
                                                                4326);
            CrsCoordinate expectedUpperRight = new CrsCoordinate(bBox.getTopLeft().getX() + (column+1)*(bBox.getWidth()/tileMatrix.getMatrixWidth()), 
                                                                 bBox.getTopLeft().getY() - row*(bBox.getHeight()/tileMatrix.getMatrixHeight()), 
                                                                 "EPSG", 
                                                                 4326);
            CrsCoordinate expectedLowerRight = new CrsCoordinate(bBox.getTopLeft().getX() + (column+1)*(bBox.getWidth()/tileMatrix.getMatrixWidth()), 
                                                                 bBox.getTopLeft().getY() - (row+1)*(bBox.getHeight()/tileMatrix.getMatrixHeight()), 
                                                                 "EPSG", 
                                                                 4326);
            CrsCoordinate expectedUpperLeft = new CrsCoordinate(bBox.getTopLeft().getX() + (column)*(bBox.getWidth()/tileMatrix.getMatrixWidth()), 
                                                                bBox.getTopLeft().getY() - row*(bBox.getHeight()/tileMatrix.getMatrixHeight()), 
                                                                "EPSG", 
                                                                4326);
            
            try(GeoPackageReader reader = new GeoPackageReader(testFile, tileMatrix.getTableName()))
            {
                TileHandle tileHandle = reader.stream(tileMatrix.getZoomLevel()).collect(Collectors.toList()).get(0);
                CrsCoordinate lowerLeftCorner = tileHandle.getCrsCoordinate(TileOrigin.LowerLeft);
                CrsCoordinate upperLeftCorner = tileHandle.getCrsCoordinate(TileOrigin.UpperLeft);
                CrsCoordinate lowerRightCorner = tileHandle.getCrsCoordinate(TileOrigin.LowerRight);
                CrsCoordinate upperRightCorner = tileHandle.getCrsCoordinate(TileOrigin.UpperRight);
               
                assertEquals(expectedLowerLeft,  lowerLeftCorner);
                assertEquals(expectedUpperLeft,  upperLeftCorner);
                assertEquals(expectedLowerRight, lowerRightCorner);
                assertEquals(expectedUpperRight, upperRightCorner);
            }
            
        
        }
        finally
        {
            deleteFile(testFile);
        }
    }
            

    /**
     * Tests if the GeoPackage Reader returns the expected dimensions
     * using the method getImageDimensions()
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     * @throws TileStoreException
     */
    @Test
    public void geoPackageReaderGetImageDimensions() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            final TileMatrix tileMatrix = createTileSetAndTileMatrix(gpkg, new BoundingBox(-180.0, -90.0, 180.0, 90.0), 10, 7, 9);
            final String formatName = "PNG";
            final Dimensions<Integer> dimensions = new Dimensions<>(256, 512);
            gpkg.tiles().addTile(gpkg.tiles().getTileSet(tileMatrix.getTableName()),
                                 tileMatrix,
                                 6,
                                 5,
                                 createImageBytes(BufferedImage.TYPE_3BYTE_BGR, formatName, dimensions));

            try(GeoPackageReader reader = new GeoPackageReader(testFile, tileMatrix.getTableName()))
            {
                final Dimensions<Integer> dimensionsReturned = reader.getImageDimensions();
                assertTrue(String.format("The dimensions returned from getImageDimensions were not as expected. Actual: height %d width %d\nExpected: height %d width %d.",
                                         dimensionsReturned.getHeight(),
                                         dimensionsReturned.getWidth(),
                                         dimensions.getHeight(),
                                         dimensions.getWidth()),
                           dimensionsReturned.getHeight().equals(dimensions.getHeight()) &&
                           dimensionsReturned.getWidth() .equals(dimensions.getWidth()));
            }

        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Writer will be able to add a tile to
     * an existing GeoPackage
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     * @throws IOException
     * @throws TileStoreException
     */
    @Test
    public void geopackageWriterAddTile() throws Exception
    {
        final File testFile = this.getRandomFile(6);

        final String tableName = "tableName";

        final int row       = 0;
        final int column    = 1;
        final int zoomLevel = 0;

        try(GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
                                                               new CoordinateReferenceSystem("EPSG", 4326),
                                                               tableName,
                                                               "identifier",
                                                               "description",
                                                               new BoundingBox(0.0,0.0,90.0,90.0),
                                                               new ZoomTimesTwo(0, 0, 4, 2),
                                                               new MimeType("image/jpeg"),
                                                               null))
        {
            final BufferedImage bufferedImage = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_GRAY);

            gpkgWriter.addTile(column, row, zoomLevel, bufferedImage);
        }

        try(GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
        {
            final BufferedImage tile = gpkgReader.getTile(column, row, zoomLevel);

            assertTrue("GeoPackageWriter was unable to add a tile to a GeoPackage", tile != null);
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageWriter will throw an Illegal argumentException when
     * adding a tile with a null value for buffered image
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     * @throws TileStoreException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileIllegalArgumentException() throws SQLException, ClassNotFoundException, ConformanceException, IOException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(6);

        try(GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
                                                               new CoordinateReferenceSystem("EPSG", 4326),
                                                               "foo",
                                                               "identifier",
                                                               "description",
                                                               new BoundingBox(0.0,0.0,90.0,90.0),
                                                               new ZoomTimesTwo(0, 0, 4, 2),
                                                               new MimeType("image/jpeg"),
                                                               null))
        {
            gpkgWriter.addTile(0, 0, 0, null);
            fail("Expected GeoPackageWriter to throw an IllegalArgumentException if a user puts in a null value for the parameter image.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageWriter will throw an Illegal argumentException when
     * adding a tile with a null value for buffered image
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     * @throws TileStoreException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileIllegalArgumentException2() throws SQLException, ClassNotFoundException, ConformanceException, IOException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(6);

        try(GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
                                                               new CoordinateReferenceSystem("EPSG", 4326),
                                                               "foo",
                                                               "identifier",
                                                               "description",
                                                               new BoundingBox(0.0,0.0,90.0,90.0),
                                                               new ZoomTimesTwo(0, 0, 4, 2),
                                                               new MimeType("image/jpeg"),
                                                               null))
        {
            gpkgWriter.addTile(new CrsCoordinate(30.0,20.0, "epsg", 4326), 0, null);
            fail("Expected GeoPackageWriter to throw an IllegalArgumentException if a user puts in a null value for the parameter image.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageWriter will throw an Illegal argumentException when
     * adding a tile with a null value for crsCoordinate
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     * @throws TileStoreException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileIllegalArgumentException3() throws SQLException, ClassNotFoundException, ConformanceException, IOException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(6);

        try(GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
                                                               new CoordinateReferenceSystem("EPSG", 4326),
                                                               "foo",
                                                               "identifier",
                                                               "description",
                                                               new BoundingBox(0.0,0.0,90.0,90.0),
                                                               new ZoomTimesTwo(0, 0, 4, 2),
                                                               new MimeType("image/jpeg"),
                                                               null))
        {

            gpkgWriter.addTile(null, 0, new BufferedImage(256,256, BufferedImage.TYPE_INT_ARGB));
            fail("Expected GeoPackageWriter to throw an IllegalArgumentException if a user puts in a null value for the parameter crsCoordinate.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageWriter will throw an Illegal argumentException when
     * adding a tile with a null value for buffered image
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     * @throws TileStoreException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileIllegalArgumentException4() throws SQLException, ClassNotFoundException, ConformanceException, IOException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(6);

        try(GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
                                                               new CoordinateReferenceSystem("EPSG", 4326),
                                                               "foo",
                                                               "identifier",
                                                               "description",
                                                               new BoundingBox(0.0,0.0,90.0,90.0),
                                                               new ZoomTimesTwo(0, 0, 4, 2),
                                                               new MimeType("image/jpeg"),
                                                               null))
        {

            gpkgWriter.addTile(new CrsCoordinate(30.0, 20.0, "epsg", 3395), 0, new BufferedImage(256,256, BufferedImage.TYPE_INT_ARGB));
            fail("Expected GeoPackageWriter to throw an IllegalArgumentException if a user adds a tile that is a different crs coordinate reference system than the profile.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    /**
     * Tests if GeoPackageWriter throws an IllegalArgumentException
     * when trying to create a GeoPackageWriter with a tileSet parameter
     * value of null
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException() throws SQLException, ClassNotFoundException, ConformanceException, IOException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
                                                               new CoordinateReferenceSystem("EPSG", 4326),
                                                               null,
                                                               "identifier",
                                                               "description",
                                                               new BoundingBox(0.0,0.0,90.0,90.0),
                                                               new ZoomTimesTwo(0, 0, 4, 2),
                                                               new MimeType("image/jpeg"),
                                                               null))
        {
            fail("Expected GeoPackageWriter to throw an IllegalArgumentException if a user puts in a null value for the parameter tile set table name.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }


    /**
     * Tests if GeoPackageWriter throws an IllegalArgumentException
     * when trying to create a GeoPackageWriter with a geopackage parameter
     * value of null
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException2() throws SQLException, ClassNotFoundException, ConformanceException, IOException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackageWriter gpkgWriter = new GeoPackageWriter(null,
                                                               new CoordinateReferenceSystem("EPSG", 4326),
                                                               "foo",
                                                               "identifier",
                                                               "description",
                                                               new BoundingBox(0.0,0.0,90.0,90.0),
                                                               new ZoomTimesTwo(0, 0, 4, 2),
                                                               new MimeType("image/jpeg"),
                                                               null))
        {
            fail("Expected GeoPackageWriter to throw an IllegalArgumentException if a user puts in a null value for the parameter geo package file.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageWriter throws an IllegalArgumentException
     * when trying to create a GeoPackageWriter with a imageOutputFormat parameter
     * value of null
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException3() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
                                                               new CoordinateReferenceSystem("EPSG", 4326),
                                                               "foo",
                                                               "identifier",
                                                               "description",
                                                               new BoundingBox(0.0,0.0,90.0,90.0),
                                                               new ZoomTimesTwo(0, 0, 4, 2),
                                                               null,
                                                               null))
        {
            fail("Expected GeoPackageWriter to throw an IllegalArgumentException if a user puts in a null value for the parameter imageOutputFormat.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }


    /**
     * Tests if GeoPackageWriter throws an IllegalArgumentException
     * when trying to create a GeoPackageWriter with an unsupported output
     * image format
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException4() throws SQLException, ClassNotFoundException, ConformanceException, IOException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
                                                               new CoordinateReferenceSystem("EPSG", 4326),
                                                               "foo",
                                                               "identifier",
                                                               "description",
                                                               new BoundingBox(0.0,0.0,90.0,90.0),
                                                               new ZoomTimesTwo(0, 0, 4, 2),
                                                               new MimeType("text/xml"),
                                                               null))
        {
            fail("Expected GeoPackageWriter to throw an IllegalArgumentException if a user puts in an unsupported image output format.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageWriter throws an IllegalArgumentException
     * when trying to create a GeoPackageWriter with a null value
     * for crs
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException5() throws ClassNotFoundException, SQLException, ConformanceException, IOException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
                                                               null,
                                                               "foo",
                                                               "identifier",
                                                               "description",
                                                               new BoundingBox(0.0,0.0,90.0,90.0),
                                                               new ZoomTimesTwo(0, 0, 4, 2),
                                                               new MimeType("image/jpeg"),
                                                               null))
        {
            fail("Expected GeoPackageWriter to throw an IllegalArgumentException if a user puts in null for the crs.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageWriter throws an IllegalArgumentException
     * when trying to create a GeoPackageWriter with a null value
     * for crs
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException6() throws ClassNotFoundException, SQLException, ConformanceException, IOException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try (GeoPackage gpkg = new GeoPackage(testFile))
        {
            final String                 tableName              = "tableName";
            final String                 identifier             = "identifier";
            final String                 description            = "description";
            final BoundingBox            boundingBox            = new BoundingBox(0.0, 0.0, 90.0, 90.0);
            final SpatialReferenceSystem spatialReferenceSystem = gpkg.core().getSpatialReferenceSystem(4326);

            gpkg.tiles().addTileSet(tableName, identifier, description, boundingBox, spatialReferenceSystem);

            try (GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
                                                                    new CoordinateReferenceSystem("EPSG", 4326),
                                                                    tableName,
                                                                    identifier,
                                                                    description,
                                                                    boundingBox,
                                                                    new ZoomTimesTwo(0, 0, 4, 2),
                                                                    new MimeType("image/jpeg"),
                                                                    null))
            {
                fail("Expected GeoPackageWriter to throw an IllegalArgumentException if a user puts in null for the crs.");
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage Writer will throw an illegal argument Exception when the
     * tileSet already exists in the GeoPackage
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws IOException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException7() throws ClassNotFoundException, SQLException, ConformanceException, IOException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(9);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
        final SphericalMercatorCrsProfile spherical = new SphericalMercatorCrsProfile();
        final BoundingBox tileSetBounds = new BoundingBox(SphericalMercatorCrsProfile.Bounds.getMinX()/4,
                                                    SphericalMercatorCrsProfile.Bounds.getMinY()/2,
                                                    SphericalMercatorCrsProfile.Bounds.getMaxX()/10,
                                                    SphericalMercatorCrsProfile.Bounds.getMaxY()/8);
        final String tileSetName = "tableName";
        final int zoomLevel = 6;
        final int tileWidth = 256;
        final int tileHeight = 256;
        final int matrixWidth = 10;
        final int matrixHeight = 6;
        final SpatialReferenceSystem srs = gpkg.core().addSpatialReferenceSystem(spherical.getName(),
                                                                           spherical.getCoordinateReferenceSystem().getIdentifier(),
                                                                           spherical.getCoordinateReferenceSystem().getAuthority(),
                                                                           spherical.getCoordinateReferenceSystem().getIdentifier(),
                                                                           spherical.getWellKnownText(),
                                                                           spherical.getDescription());

        GeopackageTileStoreTest.createTileSetAndTileMatrix(gpkg, srs, tileSetBounds, zoomLevel, matrixWidth, matrixHeight, tileWidth, tileHeight, tileSetName);

        try(GeoPackageWriter writer = new GeoPackageWriter(testFile,
                                                           spherical.getCoordinateReferenceSystem(),
                                                           tileSetName,
                                                           tileSetName,
                                                           "description",
                                                           tileSetBounds,
                                                           new ZoomTimesTwo(5, 8, 5, 3),
                                                           new MimeType("image/png"),
                                                           null);)
            {
                fail("Expected GeoPackageWriter to throw when the tileSet already exists in the GeoPackage");
            }
        }
       finally
       {
           deleteFile(testFile);
       }
    }


    /**
     * Tests if the GeoPackageWriter can write a tile to a GeopPackage
     * and if the GeoPackageReader can read that tile by retrieving it by
     * crs coordinate and tile coordinate
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     * @throws SQLException
     * @throws MimeTypeParseException
     * @throws TileStoreException
     */
    @Test
    public void addTileCrsCoordinate() throws ClassNotFoundException, ConformanceException, IOException, SQLException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);
        try
        {
            final SphericalMercatorCrsProfile spherical = new SphericalMercatorCrsProfile();
            final BoundingBox tileSetBounds = new BoundingBox(spherical.getBounds().getMinX()/3,
                                                        spherical.getBounds().getMinY()/2,
                                                        spherical.getBounds().getMaxX()-100,
                                                        spherical.getBounds().getMaxY()-100);
            final String tileSetName = "tableName";
            //create a geopackage writer
            try(GeoPackageWriter writer = new GeoPackageWriter(testFile,
                                                               spherical.getCoordinateReferenceSystem(),
                                                               tileSetName,
                                                               "identifier",
                                                               "description",
                                                               tileSetBounds,
                                                               new ZoomTimesTwo(5, 8, 5, 3),
                                                               new MimeType("image/png"),
                                                               null);)
            {
                final int zoomLevel = 6;
                final BufferedImage  imageExpected = createBufferedImage(BufferedImage.TYPE_BYTE_GRAY);
                final CrsCoordinate crsCoordinate = new CrsCoordinate(tileSetBounds.getMinX(), tileSetBounds.getMaxY(), spherical.getCoordinateReferenceSystem());//upper left tile
                //add an image to the writer
                writer.addTile(crsCoordinate, zoomLevel, imageExpected);
                //create a reader
                try(GeoPackageReader reader = new GeoPackageReader(testFile,tileSetName);)
                {
                    //check if the images are returned as expected from a crs coordinate and relative tile coordinate
                    final BufferedImage imageReturnedCrs = reader.getTile(crsCoordinate, zoomLevel);
                    final BufferedImage imageReturnedTileCoordinate = reader.getTile(0, 0, zoomLevel); //upper left tile

                    assertTrue("The images returned by the reader were null when they should have returned a buffered image.",imageReturnedCrs != null && imageReturnedTileCoordinate != null);
                }
           }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    private static void deleteFile(final File testFile)
    {
        if (testFile.exists())
        {
            if (!testFile.delete())
            {
                throw new RuntimeException(String.format(
                        "Unable to delete testFile. testFile: %s", testFile));
            }
        }
    }

    private String getRanString(final int length)
    {
        final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(this.randomGenerator.nextInt(characters.length()));
        }
        return new String(text);
    }

    private File getRandomFile(final int length)
    {
        File testFile;

        do
        {
            testFile = new File(String.format(FileSystems.getDefault().getPath(this.getRanString(length)).toString() + ".gpkg"));
        }
        while (testFile.exists());

        return testFile;
    }

    private static BufferedImage createBufferedImage(final int bufferedImageType)
    {
        return new BufferedImage(256,256, bufferedImageType);
    }

    private static byte[] createImageBytes(final int bufferedImageType, final String outputFormat, final Dimensions<Integer> dimensions) throws IOException
    {
        return ImageUtility.bufferedImageToBytes(new BufferedImage(dimensions.getWidth(), dimensions.getHeight(), bufferedImageType), outputFormat);
    }
    
    private static byte[] createImageBytes(final int bufferedImageType, final String outputFormat) throws IOException
    {
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);
        return createImageBytes(bufferedImageType, outputFormat, dimensions);
    }
    
    private static byte[] createImageBytes(final int bufferedImageType) throws IOException
    {
        return createImageBytes(bufferedImageType, "png");
    }

    private static boolean tileAndTileHandleEqual(final Tile tile, final TileHandle tileHandle, final String outputFormat)
    {
        try
        {
             return tile.getColumn()    == tileHandle.getColumn() &&
                    tile.getRow()       == tileHandle.getRow()    &&
                    tile.getZoomLevel() ==  tileHandle.getZoomLevel() &&
                    Arrays.equals(tile.getImageData(), ImageUtility.bufferedImageToBytes(tileHandle.getImage(), outputFormat));
        }
        catch(final Exception ex)
        {
            return false;
        }
    }

    private static Set<TileMatrixDimensions> addTileMatriciesToGpkg(final Set<Integer> zoomLevels, final TileSet tileSet, final GeoPackage gpkg, final TileScheme tileScheme, final int tileWidth, final int tileHeight) throws SQLException
    {
        final Set<TileMatrixDimensions> tileMatrixDimensionsExpected = new HashSet<>();
        final BoundingBox bBox = tileSet.getBoundingBox();
        for(final int zoomLevel : zoomLevels)
        {
            final TileMatrixDimensions dimensions = tileScheme.dimensions(zoomLevel);

            tileMatrixDimensionsExpected.add(dimensions);

            final double pixelXSize = bBox.getWidth()  / dimensions.getWidth()  / tileWidth;
            final double pixelYSize = bBox.getHeight() / dimensions.getHeight() / tileHeight;

            gpkg.tiles().addTileMatrix(tileSet, zoomLevel, dimensions.getWidth(), dimensions.getHeight(), tileWidth, tileHeight, pixelXSize, pixelYSize);
        }
        return tileMatrixDimensionsExpected;
    }
    private static TileMatrix createTileSetAndTileMatrix(final GeoPackage gpkg, final BoundingBox bBox, final int zoomLevel, final int matrixWidth, final int matrixHeight) throws SQLException
    {
        return createTileSetAndTileMatrix(gpkg, gpkg.core().getSpatialReferenceSystem(4326), bBox, zoomLevel, matrixWidth, matrixHeight, 256, 256, "tableName");
    }

    @SuppressWarnings("unused")
    private static TileMatrix createTileSetAndTileMatrix(final GeoPackage gpkg, final BoundingBox bBox, final int zoomLevel, final int matrixWidth, final int matrixHeight, final int tileWidth, final int tileHeight, final String identifierTableName) throws SQLException
    {
        return createTileSetAndTileMatrix(gpkg, gpkg.core().getSpatialReferenceSystem(4326), bBox, zoomLevel, matrixWidth, matrixHeight, tileWidth, tileHeight, identifierTableName);
    }

    private static TileMatrix createTileSetAndTileMatrix(final GeoPackage gpkg, final SpatialReferenceSystem srs, final BoundingBox bBox, final int zoomLevel, final int matrixWidth, final int matrixHeight, final int tileWidth, final int tileHeight, final String identifierTableName) throws SQLException
    {
      //create a tileSet
        final TileSet tileSet = gpkg.tiles()
                                    .addTileSet(identifierTableName,
                                                identifierTableName,
                                                "description",
                                                bBox,
                                                srs);
        //create matrix
        return  gpkg.tiles().addTileMatrix(tileSet,
                                           zoomLevel,
                                           matrixWidth,
                                           matrixHeight,
                                           tileWidth,
                                           tileHeight,
                                           bBox.getWidth()  / matrixWidth / tileWidth,
                                           bBox.getHeight() / matrixHeight / tileHeight);
    }
    
    @SuppressWarnings("unused")
    private static List<Tile> addTilesToGpkg(GeoPackage gpkg, TileSet tileSet, TileMatrix tileMatrix, Map<Coordinate<Integer>, Integer> coordinates) throws SQLException, IOException
    {
        List<Tile> tiles =  new ArrayList<>();
        for(Coordinate<Integer> coordinate: coordinates.keySet())
        {
            Tile tile = gpkg.tiles().addTile(tileSet, tileMatrix, coordinate.getX().intValue(), coordinate.getY().intValue(), createImageBytes(coordinates.get(coordinate)));
            tiles.add(tile);
        }
            
        return tiles;
    }

}
