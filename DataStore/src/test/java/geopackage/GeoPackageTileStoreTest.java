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

package geopackage;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.GlobalGeodeticCrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.SphericalMercatorCrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.util.ImageUtility;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.tiles.GeoPackageTiles;
import com.rgi.geopackage.tiles.Tile;
import com.rgi.geopackage.tiles.TileMatrix;
import com.rgi.geopackage.tiles.TileMatrixSet;
import com.rgi.geopackage.tiles.TileSet;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.store.tiles.TileHandle;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.geopackage.GeoPackageReader;
import com.rgi.store.tiles.geopackage.GeoPackageWriter;
import org.junit.Test;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings("javadoc")
public class GeoPackageTileStoreTest
{
    private static final double Epsilon = 0.00000001;
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
     * Tests if GeoPackageReader will throw an IllegalArgumentException when
     * passing a null value for TileSet
     */
    @Test(expected = IllegalArgumentException.class)
    public void geopackageReaderIllegalArgumentException() throws TileStoreException, SQLException
    {
        final File testFile = this.getRandomFile(9);

        try(final GeoPackageReader ignored = new GeoPackageReader(testFile, null))
        {
            fail("Expected GeoPackage Reader to throw an IllegalArguementException when passing a null value for tileSet");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageReader will throw an IllegalArgumentException when
     * passing a null value for TileSet
     */
    @Test(expected = IllegalArgumentException.class)
    public void geopackageReaderIllegalArgumentException2() throws TileStoreException, SQLException
    {
        final File testFile = this.getRandomFile(9);

        try(final GeoPackageReader ignored = new GeoPackageReader(null, "tablename"))
        {
            fail("Expected GeoPackage Reader to throw an IllegalArguementException when passing a null value for GeoPackage");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackageReader will throw an IllegalArgumentException when
     * a given a name of a tileSet that doesn't exist
     */
    @Test(expected = TileStoreException.class)
    @SuppressWarnings("ExpectedExceptionNeverThrown") // Intellij bug?
    public void geopackageReaderIllegalArgumentException3() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(11);

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            try(final GeoPackageReader reader = new GeoPackageReader(testFile, "A TileSet that doesn't Exist"))
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
     * Tests if the getBounds method in GeoPackageReader
     * returns the expected bounding box
     */
    @Test
    public void geopackageReaderGetBounds() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(8);

        final BoundingBox bBoxGiven  = new BoundingBox(0.0,0.0,180.0,180.0);

        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            final String tableName = "tablename";
            gpkg.tiles().addTileSet(tableName, "identifier", "description", bBoxGiven, gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
            {
                final BoundingBox bBoxReturned = gpkgReader.getBounds();
                assertEquals("The bounding box returned from GeoPackageReader was not the same that was given.", bBoxGiven, bBoxReturned);
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if countTiles method from GeoPackageReader
     *  returns the expected value
     */
    @Test
    public void geopackageReaderCountTiles() throws ClassNotFoundException, SQLException, ConformanceException, TileStoreException, IOException
    {
        final File testFile = this.getRandomFile(8);
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            final BoundingBox bBox = new BoundingBox(0.0,0.0,180.0,180.0);

            final int zoomLevel    = 2;
            final int matrixWidth  = 3;
            final int matrixHeight = 3;

            final TileMatrix tileMatrix = GeoPackageTileStoreTest.createTileSetAndTileMatrix(gpkg, bBox, zoomLevel, matrixWidth, matrixHeight);
            final TileSet    tileSet    = gpkg.tiles().getTileSet(tileMatrix.getTableName());

            //add tiles
            gpkg.tiles().addTile(tileSet, tileMatrix, 0, 0, createImageBytes(BufferedImage.TYPE_3BYTE_BGR));
            gpkg.tiles().addTile(tileSet, tileMatrix, 1, 0, createImageBytes(BufferedImage.TYPE_3BYTE_BGR));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tileSet.getTableName()))
            {
                final long numberOfTiles = gpkgReader.countTiles();

                assertEquals(String.format("Expected the GeoPackage Reader countTiles to return a value of 2 but instead returned %d",
                                                  numberOfTiles), 2, numberOfTiles);
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageReader Returns the expected
     * value for getByteSize() of the file
     */
    @Test
    public void getByteSize() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(8);

        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            final BoundingBox bBoxGiven = new BoundingBox(0.0,0.0,180.0,180.0);

            final String tableName = "tablename";
            gpkg.tiles().addTileSet(tableName, "identifier", "description", bBoxGiven, gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
            {
                final long byteSizeReturned = gpkgReader.getByteSize();
                final long byteSizeExpected = testFile.getTotalSpace();

                assertEquals(String.format("The GeoPackage Reader did not return the expected value. \nExpected: %d Actual: %d",
                                                  byteSizeReturned,
                                                  byteSizeExpected), byteSizeReturned, byteSizeExpected);
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    @Test
    public void getImage() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(11);

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(0.0,0.0,0.0,0.0),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            try(GeoPackageReader reader = new GeoPackageReader(testFile, tileSet.getTableName()))
            {
                final BufferedImage tile = reader.getTile(0, 0, 0);
                assertNull("Expected getTile to return null when the tile does not exist at that coordinate.", tile);
            }
        }
        finally
        {
            deleteFile(testFile);
        }

    }

    /**
     * Tests if the tile retrieved is the same as it was given
     * (or as expected) using getTile from GeoPackageReader
     * getTile (row, column, zoom)
     */
    @Test
    public void getTile() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(8);

        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            final BoundingBox bBox = new BoundingBox(0.0, 0.0, 180.0, 180.0);
            final int zoomLevel    = 2;
            final int matrixWidth  = 3;
            final int matrixHeight = 3;

            final TileMatrix tileMatrix = GeoPackageTileStoreTest.createTileSetAndTileMatrix(gpkg,  bBox, zoomLevel, matrixWidth, matrixHeight);
            final TileSet    tileSet = gpkg.tiles().getTileSet(tileMatrix.getTableName());

            final int column = 0;
            final int row    = 0;

            final Tile tileExpected = gpkg.tiles().addTile(tileSet, tileMatrix, column, row, createImageBytes(BufferedImage.TYPE_4BYTE_ABGR));

            gpkg.tiles().addTile(tileSet, tileMatrix, 1, 0, createImageBytes(BufferedImage.TYPE_4BYTE_ABGR));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tileSet.getTableName()))
            {
                final BufferedImage returnedImage = gpkgReader.getTile(column, row, zoomLevel);

                assertArrayEquals("The tile image data returned from getTile in GeoPackage reader wasn't the same as the one given.",
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
     */
    @Test
    public void getTile2WithCrsCoordinate() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(8);


        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            final BoundingBox bBox = new BoundingBox(0.0, 0.0, 180.0, 180.0);
            final int zoomLevel    = 2;
            final int matrixWidth  = 3;
            final int matrixHeight = 3;

            final TileMatrix             tileMatrix = GeoPackageTileStoreTest.createTileSetAndTileMatrix(gpkg, bBox, zoomLevel, matrixWidth, matrixHeight);
            final TileSet                tileSet    = gpkg.tiles().getTileSet(tileMatrix.getTableName());

            //add tiles
            final Tile tileExpected = gpkg.tiles().addTile(tileSet, tileMatrix, 0, 0, createImageBytes(BufferedImage.TYPE_4BYTE_ABGR));

            gpkg.tiles().addTile(tileSet, tileMatrix, 1, 0, createImageBytes(BufferedImage.TYPE_4BYTE_ABGR));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tileSet.getTableName()))
            {
                final CrsCoordinate crsTileCoordinate = new CrsCoordinate(60.0, 130.0, "epsg", 4326);
                final BufferedImage returnedImage     = gpkgReader.getTile(crsTileCoordinate, zoomLevel);

                assertArrayEquals("The tile image data returned from getTile in GeoPackage reader wasn't the same as the one given.",
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
     * when given a CrsCoordinate.  This is an edge case.
     */
    @Test
    public void getTile3CrsCoordinate() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(8);


        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            final BoundingBox bBox = new BoundingBox(0.0, 0.0, 180.0, 180.0);
            final int zoomLevel    = 2;
            final int matrixWidth  = 3;
            final int matrixHeight = 3;

            final TileMatrix tileMatrix = GeoPackageTileStoreTest.createTileSetAndTileMatrix(gpkg, bBox, zoomLevel, matrixWidth, matrixHeight);
            final TileSet    tileSet    = gpkg.tiles().getTileSet(tileMatrix.getTableName());

            //add tiles
            gpkg.tiles().addTile(tileSet, tileMatrix, 0, 0, createImageBytes(BufferedImage.TYPE_BYTE_GRAY));
            final Tile tileExpected = gpkg.tiles().addTile(tileSet, tileMatrix, 0, 2, createImageBytes(BufferedImage.TYPE_BYTE_GRAY));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tileSet.getTableName()))
            {
                final CrsCoordinate    crsTileCoordinate = new CrsCoordinate(59.0, 60.0, "epsg", 4326);
                final BufferedImage    returnedImage     = gpkgReader.getTile(crsTileCoordinate, zoomLevel);

                assertArrayEquals("The tile image data returned from getTile in GeoPackage reader wasn't the same as the one given.",
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
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileIllegalArgumentException() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(12);

        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            final String tableName = "tablename";
            gpkg.tiles().addTileSet(tableName,
                                    "identifier",
                                    "description",
                                    new BoundingBox(0.0, 0.0, 60.0, 30.0),
                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

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
     * than the tileSet in the method getTile
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileIllegalArgumentException2() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(12);

        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            final String tableName = "tablename";
            gpkg.tiles().addTileSet(tableName,
                                    "identifier",
                                    "description",
                                    new BoundingBox(0.0,0.0,60.0,30.0),
                                    gpkg.core().addSpatialReferenceSystem("Srs Name", "EPSG", 3857, "definition", "description"));

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
     * in the GeoPackage
     */
    @Test
    public void getTileThatDoesntExist() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);

        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            final String tableName = "tablename";
            gpkg.tiles().addTileSet(tableName,
                                    "identifier",
                                    "description",
                                    new BoundingBox(0.0,0.0,60.0,30.0),
                                    gpkg.core().addSpatialReferenceSystem("Srs Name", "EPSG", 3857, "definition", "description"));

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
            {
                final CrsCoordinate coordinate = new CrsCoordinate(0, 2, "EPSG", 3857);
                final BufferedImage tile = gpkgReader.getTile(coordinate, 4);
                assertNull("Expected the reader to return null if the tile doesn't exist.", tile);
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackageReader returns the expected
     * coordinate reference system
     */
    @Test
    public void getCoordinateReferenceSystem() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(7);

        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            final SpatialReferenceSystem spatialReferenceSystem = gpkg.core().addSpatialReferenceSystem("WebMercator", "epsg", 3857, "definition", "description");

            final String tableName = "tablename";
            gpkg.tiles().addTileSet(tableName, "identifier", "description", new BoundingBox(0.0, 0.0, 60.0, 30.0), spatialReferenceSystem);

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
            {
                final CoordinateReferenceSystem coordinateReferenceSystemReturned = gpkgReader.getCoordinateReferenceSystem();

                assertTrue(String.format("The coordinate reference system returned is not what was expected. Actual authority: %s Actual Identifer: %d\nExpected authority: %s Expected Identifier: %d.",
                                         coordinateReferenceSystemReturned.getAuthority(),
                                         coordinateReferenceSystemReturned.getIdentifier(),
                                         spatialReferenceSystem.getOrganization(),
                                         spatialReferenceSystem.getIdentifier()),
                           coordinateReferenceSystemReturned.getAuthority().equalsIgnoreCase(spatialReferenceSystem.getOrganization()) &&
                           coordinateReferenceSystemReturned.getIdentifier() == spatialReferenceSystem.getOrganizationSrsId());
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackageReader returns the expected zoom levels
     * for a given GeoPackage and tile set
     */
    @Test
    public void getZoomLevels() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(10);

        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            final BoundingBox bBox =  new BoundingBox(0.0,0.0,60.0,30.0);
            final String tableName = "tablename";
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet(tableName,
                                                    "identifier",
                                                    "description",
                                                    bBox,
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final TileSet tileSet2 = gpkg.tiles()
                                         .addTileSet("tabelName2",
                                                     "identifier2",
                                                     "description2",
                                                     bBox,
                                                     gpkg.core().getSpatialReferenceSystem(-1));

            final Collection<Integer> zoomLevelsExpected = new HashSet<>(Arrays.asList(2, 5, 9, 20));

            final int tileWidth = 256;
            final int tileHeight = 256;

            final TileScheme tileScheme = new ZoomTimesTwo(2, 20, 1, 1);
            addTileMatricesToGpkg(zoomLevelsExpected, tileSet, gpkg, tileScheme, tileWidth, tileHeight);

            final TileMatrixSet tileMatrixSet = gpkg.tiles().getTileMatrixSet(tileSet2);

            gpkg.tiles().addTileMatrix(tileMatrixSet, 7, 2, 2, tileWidth, tileHeight); // this one is not included in zooms because it is a different tileset

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
            {
                final Set<Integer> zoomLevelsReturned = gpkgReader.getZoomLevels();

                assertTrue(String.format("The GeoPackage Reader did not return all of the zoom levels expected. Expected Zooms: %s. Actual Zooms: %s",
                                                zoomLevelsExpected.stream().map(Object::toString).collect(Collectors.joining(", ")),
                                                zoomLevelsReturned.stream().map(Object::toString).collect(Collectors.joining(", "))),
                                  zoomLevelsReturned.containsAll(zoomLevelsExpected));
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    /**
     * Tests if the GeoPackageReader returns the correct TileScheme given a GeoPackage
     * with various zoom levels and matrices
     */
    @Test
    public void geoPackageReaderGetTileScheme() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(10);

        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            final BoundingBox bBox =  new BoundingBox(0.0,0.0,90.0,90.0);
            final String tableName = "tablename";
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet(tableName,
                                                    "identifier",
                                                    "description",
                                                    bBox,
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final Collection<Integer> zoomLevelsExpected = Arrays.asList(2, 5, 9, 20);

            final int tileWidth = 256;
            final int tileHeight = 256;

            final TileScheme tileScheme = new ZoomTimesTwo(2, 20, 1, 1);

            final Set<TileMatrixDimensions> tileMatrixDimensionsExpected = new HashSet<>();

            addTileMatricesToGpkg(zoomLevelsExpected, tileSet, gpkg, tileScheme, tileWidth, tileHeight);

            try(final GeoPackageReader gpkgReader = new GeoPackageReader(testFile, tableName))
            {
                final TileScheme tileSchemeReturned = gpkgReader.getTileScheme();

                final List<TileMatrixDimensions> tileMatrixDimensionsReturned =  zoomLevelsExpected.stream().map(tileSchemeReturned::dimensions).collect(Collectors.toList());

                assertTrue("The TileScheme from the gpkgReader did not return all the dimensions expected or they were incorrect from what was given to the geopackage",
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
     * Tests if GeoPackageReader will throw an IllegalArgumentException when
     * asking for dimensions at a zoom level that was not defined in the GeoPackage
     */
    @Test(expected = IllegalArgumentException.class)
    public void geopackageReaderGetTileScheme2() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(8);
        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            final BoundingBox bBox = new BoundingBox(0.0,0.0,90.0,90.0);
            final int zoomLevel = 9;
            final int matrixWidth = 4;
            final int matrixHeight = 5;
            final TileMatrix tileMatrix = createTileSetAndTileMatrix(gpkg, bBox, zoomLevel, matrixWidth, matrixHeight);

            try(GeoPackageReader reader = new GeoPackageReader(testFile,tileMatrix.getTableName()))
            {
                reader.getTileScheme().dimensions(10);
                fail("Expected GeoPackage reader to throw an illegalArgumentException when asking TileScheme for dimensions of a zoom level that was not defined in the GeoPackage");
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
     */
    @Test
    public void getStreamofTileHandles() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(12);

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            // Create a tileSet
            final BoundingBox bBox =  new BoundingBox(0.0,0.0,80.0,40.0);
            final int matrixWidth  = 2;
            final int matrixHeight = 4;
            final int zoomLevel    = 9;

            // Create matrix
            final TileMatrix tileMatrix = GeoPackageTileStoreTest.createTileSetAndTileMatrix(gpkg, bBox, zoomLevel, matrixWidth, matrixHeight);
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
                                                                                         return reader.stream().anyMatch(tileHandle -> areTileAndTileHandleEqual(expectedTile, tileHandle, "png"));
                                                                                    }
                                                                                    catch(final TileStoreException ignored)
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
     */
    @Test
    public void getStreamOfTileHandlesByZoom() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(6);
        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
              final int matrixWidth    = 5;
              final int matrixHeight   = 6;

              final BoundingBox         bBox       = new BoundingBox(-100, -90, 100, 90);
              final TileScheme          tileScheme = new ZoomTimesTwo(0, 8, matrixWidth, matrixHeight);
              final Collection<Integer> zoomLevels = Arrays.asList(1, 3, 6, 8);

              final TileSet tileSet = gpkg.tiles()
                                          .addTileSet("TableName",
                                                      "identifier",
                                                      "description",
                                                      bBox,
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

              addTileMatricesToGpkg(zoomLevels,
                                     tileSet,
                                     gpkg,
                                     tileScheme,
                                     256,
                                     256);

              final Map<Integer, TileMatrix> tileMatrices = gpkg.tiles()
                                                                .getTileMatrices(tileSet)
                                                                .stream()
                                                                .collect(Collectors.toMap(TileMatrix::getZoomLevel,
                                                                                          tileMatrix -> tileMatrix));

              final List<Integer[]> tileCoordinates = Arrays.asList(new Integer[]{2, 1, 1},
                                                                    new Integer[]{4, 2, 3},
                                                                    new Integer[]{7, 5, 6},
                                                                    new Integer[]{10, 9, 8},
                                                                    new Integer[]{5, 2, 6},
                                                                    new Integer[]{1, 0, 1});

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
                                                                               catch(final Exception ignored)
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
                                                                                                  .anyMatch(tileHandle -> areTileAndTileHandleEqual(expectedTile, tileHandle, "png"));
                                                                                 }
                                                                                 catch(final TileStoreException ignored)
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
     */
    @Test
    public void geoPackageReaderGetImageType() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);
        try(final GeoPackage gpkg = new GeoPackage(testFile))
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
     */
    @Test
    public void geoPackageReaderGetImageType2() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);
        try(final GeoPackage gpkg = new GeoPackage(testFile))
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
     */
    @Test
    public void geoPackageReaderGetImageType3() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);
        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            //create tileset and matrix
            final TileMatrix tileMatrix = createTileSetAndTileMatrix(gpkg, new BoundingBox(-90.0, -180.0, 90.0, 180.0), 10, 7, 9);
            //create image
            final byte[] imagebytes = {(byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9};
            //add image to gpkg
            gpkg.tiles().addTile(gpkg.tiles().getTileSet(tileMatrix.getTableName()),
                                 tileMatrix,
                                 5,
                                 6,
                                 imagebytes);


            try(GeoPackageReader reader = new GeoPackageReader(testFile, tileMatrix.getTableName()))
            {
                final String imageTypeReturned = reader.getImageType();
                assertNull(String.format("The image Type returned from the reader was not the format name expected. \nActual: %s\nExpected: null.", imageTypeReturned), imageTypeReturned);
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
     */
    @Test
    public void geoPackageReaderGetImageType4() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);
        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            //create tileset and matrix
            final TileMatrix tileMatrix = createTileSetAndTileMatrix(gpkg, new BoundingBox(-90.0, -180.0, 90.0, 180.0), 10, 7, 9);

            try(GeoPackageReader reader = new GeoPackageReader(testFile, tileMatrix.getTableName()))
            {
                final String imageTypeReturned = reader.getImageType();
                assertNull(String.format("The image Type returned from the reader was not the format name expected. \nActual: %s\nExpected: null.", imageTypeReturned), imageTypeReturned);
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the  TileHandleCrsCoordinate(TileOrigin) returns the expected
     * CrsCoordinate
     */
    @Test
    public void tileHandleCrsCoordinate() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(12);
        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            //createTileSet and matrix
            final BoundingBox bBox = new BoundingBox(-61.6333313, -45.2186333, 83.336965, 89.3212578);

            final TileMatrix tileMatrix = createTileSetAndTileMatrix(gpkg, bBox, 12, 4, 3);
            final Integer row = 1;
            final Integer column = 0;
            gpkg.tiles().addTile(gpkg.tiles().getTileSet(tileMatrix.getTableName()),
                                 tileMatrix,
                                 column,
                                 row,
                                 createImageBytes(BufferedImage.TYPE_INT_BGR));
            //expected CRS coordinates
            final CrsCoordinate expectedLowerLeft = new CrsCoordinate(bBox.getTopLeft().getX() + column*(bBox.getWidth()/tileMatrix.getMatrixWidth()),
                                                                bBox.getTopLeft().getY() - (row+1)*(bBox.getHeight()/tileMatrix.getMatrixHeight()),
                                                                "EPSG",
                                                                4326);
            final CrsCoordinate expectedUpperRight = new CrsCoordinate(bBox.getTopLeft().getX() + (column+1)*(bBox.getWidth()/tileMatrix.getMatrixWidth()),
                                                                 bBox.getTopLeft().getY() - row*(bBox.getHeight()/tileMatrix.getMatrixHeight()),
                                                                 "EPSG",
                                                                 4326);
            final CrsCoordinate expectedLowerRight = new CrsCoordinate(bBox.getTopLeft().getX() + (column+1)*(bBox.getWidth()/tileMatrix.getMatrixWidth()),
                                                                 bBox.getTopLeft().getY() - (row+1)*(bBox.getHeight()/tileMatrix.getMatrixHeight()),
                                                                 "EPSG",
                                                                 4326);
            final CrsCoordinate expectedUpperLeft = new CrsCoordinate(bBox.getTopLeft().getX() + (column)*(bBox.getWidth()/tileMatrix.getMatrixWidth()),
                                                                bBox.getTopLeft().getY() - row*(bBox.getHeight()/tileMatrix.getMatrixHeight()),
                                                                "EPSG",
                                                                4326);

            try(GeoPackageReader reader = new GeoPackageReader(testFile, tileMatrix.getTableName()))
            {
                final TileHandle tileHandle = reader.stream(tileMatrix.getZoomLevel()).collect(Collectors.toList()).get(0);
                final CrsCoordinate lowerLeftCorner  = tileHandle.getCrsCoordinate(TileOrigin.LowerLeft);
                final CrsCoordinate upperLeftCorner  = tileHandle.getCrsCoordinate(TileOrigin.UpperLeft);
                final CrsCoordinate lowerRightCorner = tileHandle.getCrsCoordinate(TileOrigin.LowerRight);
                final CrsCoordinate upperRightCorner = tileHandle.getCrsCoordinate(TileOrigin.UpperRight);

                assertEquals("The CrsCoordiante Returned was not the CrsCoordiante Expected.", expectedLowerLeft,  lowerLeftCorner);
                assertEquals("The CrsCoordiante Returned was not the CrsCoordiante Expected.", expectedUpperLeft,  upperLeftCorner);
                assertEquals("The CrsCoordiante Returned was not the CrsCoordiante Expected.", expectedLowerRight, lowerRightCorner);
                assertEquals("The CrsCoordiante Returned was not the CrsCoordiante Expected.", expectedUpperRight, upperRightCorner);
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }


    /**
     * Tests if the  TileHandleCrsCoordinate() returns the expected
     * CrsCoordinate
     */
    @Test
    public void tileHandleCrsCoordinate2() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(12);
        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            //createTileSet and matrix
            final BoundingBox bBox = new BoundingBox(12.3421, -41.12, 180.234124, 89.328);

            final TileMatrix tileMatrix = createTileSetAndTileMatrix(gpkg, bBox, 12, 4, 3);
            final Integer row = 1;
            final Integer column = 3;
            gpkg.tiles().addTile(gpkg.tiles().getTileSet(tileMatrix.getTableName()),
                                 tileMatrix,
                                 column,
                                 row,
                                 createImageBytes(BufferedImage.TYPE_INT_BGR));

            // expected CRS coordinates
            final CrsCoordinate expectedUpperLeft = new CrsCoordinate(bBox.getTopLeft().getX() + (column)*(bBox.getWidth()/tileMatrix.getMatrixWidth()),
                                                                bBox.getTopLeft().getY() - row*(bBox.getHeight()/tileMatrix.getMatrixHeight()),
                                                                "EPSG",
                                                                4326);

            try(GeoPackageReader reader = new GeoPackageReader(testFile, tileMatrix.getTableName()))
            {
                final TileHandle tileHandle = reader.stream(tileMatrix.getZoomLevel()).collect(Collectors.toList()).get(0);
                final CrsCoordinate upperLeftCorner  = tileHandle.getCrsCoordinate();

                assertEquals("The CrsCoordiante Returned was not the CrsCoordiante Expected.", expectedUpperLeft,  upperLeftCorner);
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     *
     */
    @Test
    public void tileHandleGetMatrix() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(12);
        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            //createTileSet and matrix
            final BoundingBox bBox = new BoundingBox(12.3421, -41.12, 180.234124, 89.328);

            final TileMatrix tileMatrix = createTileSetAndTileMatrix(gpkg, bBox, 12, 4, 3);
            final Integer row = 1;
            final Integer column = 3;
            gpkg.tiles().addTile(gpkg.tiles().getTileSet(tileMatrix.getTableName()),
                                 tileMatrix,
                                 column,
                                 row,
                                 createImageBytes(BufferedImage.TYPE_INT_BGR));

            try(GeoPackageReader reader = new GeoPackageReader(testFile, tileMatrix.getTableName()))
            {
                final TileHandle tileHandle = reader.stream(tileMatrix.getZoomLevel()).collect(Collectors.toList()).get(0);
                final TileMatrixDimensions tileHandleMatrix = tileHandle.getMatrix();
                final boolean dimensionsEqual = tileHandleMatrix.getHeight() == tileMatrix.getMatrixHeight() &&
                                          tileHandleMatrix.getWidth()  == tileMatrix.getMatrixWidth();

                assertTrue("The TileMatrixDimensions from the tileHandle did not return the expected values.", dimensionsEqual);
            }
        }
        finally
        {
            deleteFile(testFile);
        }

    }

    /**
     * Tests if the tileHandle returns the expected bounds of the tile
     */
    @Test
    public void tileHandleGetBounds() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(12);

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            final BoundingBox bBox = new BoundingBox(24.34, -81.25, 178.326, -1.22);
            final TileMatrix tileMatrix = createTileSetAndTileMatrix(gpkg, bBox, 8, 6, 4);
            final Integer row = 1;
            final Integer column = 4;
            //Bounds of the tiles with two coordinates
            final Coordinate<Double> lowerLeftExpected = new Coordinate<>(bBox.getTopLeft().getX() + (column)*(bBox.getWidth()/tileMatrix.getMatrixWidth()),
                                                                    bBox.getTopLeft().getY() -(row+1)*(bBox.getHeight()/tileMatrix.getMatrixHeight()));
            final Coordinate<Double> upperRightExpected = new Coordinate<>(bBox.getTopLeft().getX() + (column+1)*(bBox.getWidth()/tileMatrix.getMatrixWidth()),
                                                                     bBox.getTopLeft().getY() -(row)*(bBox.getHeight()/tileMatrix.getMatrixHeight()));
            gpkg.tiles().addTile(gpkg.tiles().getTileSet(tileMatrix.getTableName()),
                                 tileMatrix,
                                 column,
                                 row,
                                 createImageBytes(BufferedImage.TYPE_INT_BGR));

            // Use reader to get a tileHandle
            try(GeoPackageReader reader = new GeoPackageReader(testFile, tileMatrix.getTableName()))
            {
                final TileHandle  tileHandle     = reader.stream(tileMatrix.getZoomLevel()).collect(Collectors.toList()).get(0);
                final BoundingBox returnedBounds = tileHandle.getBounds();

                final boolean     boundsEqual = returnedBounds.getBottomLeft().equals(lowerLeftExpected) &&
                                                returnedBounds.getTopRight()  .equals(upperRightExpected);//see if the are equivalent to expected values

                assertTrue(String.format("The tileBounds given from tileHandle.getBounds() did not return the expected values.\nActual: %s.\nExpected: %s.",
                                         returnedBounds.toString(),
                                         new BoundingBox(lowerLeftExpected.getX(),
                                                         lowerLeftExpected.getY(),
                                                         upperRightExpected.getX(),
                                                         upperRightExpected.getY()).toString()),
                          boundsEqual);
            }
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackageReader returns the expected dimensions
     * using the method getImageDimensions()
     */
    @Test
    public void geoPackageReaderGetImageDimensions() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);
        try(final GeoPackage gpkg = new GeoPackage(testFile))
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
     * Tests if the GeoPackageReader returns the expected dimensions
     * using the method getImageDimensions()
     */
    @Test
    public void geoPackageReaderGetImageDimensions2() throws SQLException, ClassNotFoundException, ConformanceException, IOException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);
        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            final TileMatrix tileMatrix = createTileSetAndTileMatrix(gpkg, new BoundingBox(-180.0, -90.0, 180.0, 90.0), 10, 7, 9);

            try(GeoPackageReader reader = new GeoPackageReader(testFile, tileMatrix.getTableName()))
            {
                final Dimensions<Integer> dimensionsReturned = reader.getImageDimensions();
                assertNull("There were no tiles in the GeoPackage but the Reader returned a value for imageDimensions instead of returning null.", dimensionsReturned);
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
     */
    @Test
    public void geopackageWriterAddTile() throws MimeTypeParseException, TileStoreException, SQLException
    {
        final File testFile = this.getRandomFile(6);

        final String tableName = "tableName";

        final int row       = 0;
        final int column    = 1;
        final int zoomLevel = 0;

        try(final GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
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

            assertNotNull("GeoPackageWriter was unable to add a tile to a GeoPackage", tile);
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageWriter will throw an Illegal argumentException when
     * adding a tile with a null value for buffered image
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileIllegalArgumentException() throws SQLException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(6);

        try(final GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
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
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileIllegalArgumentException2() throws SQLException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(6);

        try(final GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
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
     * adding a tile with a null value for CrsCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileIllegalArgumentException3() throws SQLException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(6);

        try(final GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
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
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileIllegalArgumentException4() throws SQLException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(6);

        try(final GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
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
     */
    @Test(expected = TileStoreException.class)
    @SuppressWarnings("ExpectedExceptionNeverThrown") // Intellij bug?
    public void geoPackageWriterIllegalArgumentException()  throws TileStoreException, SQLException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(final GeoPackageWriter ignored = new GeoPackageWriter(testFile,
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
     * when trying to create a GeoPackageWriter with a GeoPackage parameter
     * value of null
     */
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException2()  throws TileStoreException, SQLException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(final GeoPackageWriter ignored = new GeoPackageWriter(null,
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
     */
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException3()  throws TileStoreException, SQLException
    {
        final File testFile = this.getRandomFile(8);

        try(final GeoPackageWriter ignored = new GeoPackageWriter(testFile,
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
     */
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException4() throws TileStoreException, SQLException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(final GeoPackageWriter ignored = new GeoPackageWriter(testFile,
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
     */
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException5()  throws TileStoreException, SQLException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(final GeoPackageWriter ignored = new GeoPackageWriter(testFile,
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
     */
    @Test(expected = TileStoreException.class)
    @SuppressWarnings("ExpectedExceptionNeverThrown") // Intellij bug?
    public void geoPackageWriterIllegalArgumentException6() throws ClassNotFoundException, SQLException, ConformanceException, IOException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(8);

        try (GeoPackage gpkg = new GeoPackage(testFile))
        {
            final String                 tableName              = "tableName";
            final String                 identifier             = "identifier";
            final String                 description            = "description";
            final BoundingBox            boundingBox            = new BoundingBox(0.0, 0.0, 90.0, 90.0);
            final SpatialReferenceSystem spatialReferenceSystem = gpkg.core().getSpatialReferenceSystem("EPSG", 4326);

            gpkg.tiles().addTileSet(tableName, identifier, description, boundingBox, spatialReferenceSystem);

            try(final GeoPackageWriter ignored = new GeoPackageWriter(testFile,
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
     * Tests if GeoPackageWriter create a new directory when
     * passed a File that contains a nonexistent directory in
     * its path
     */
    @Test
    public void geoPackageWriterCreatesDirectory() throws ClassNotFoundException, SQLException, ConformanceException, IOException, MimeTypeParseException, TileStoreException
    {
        final File writerFile = this.getRandomFile(13);
        final File testFile = new File("fake_folder/" + writerFile);
        testFile.setWritable(false);

        final CoordinateReferenceSystem coordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 4326);
        final BoundingBox bBox =  new BoundingBox(0.0,0.0,90.0,180.0);

        try(final GeoPackageWriter ignored = new GeoPackageWriter(testFile,
                                                                  coordinateReferenceSystem,
                                                                  "tileSetTableName",
                                                                  "tileSetIdentifier",
                                                                  "tileSetDescription",
                                                                  bBox,
                                                                  new ZoomTimesTwo(1, 10, 2, 3),
                                                                  new MimeType("image/png"),
                                                                  null))
        {
            assertTrue("GeoPackageWriter did not create the directory containing the new GeoPackage",
                       testFile.exists() &&
                       testFile.getParentFile().isDirectory());
        }
        finally
        {
            final File parent = testFile.getParentFile();

            if (testFile.exists())
            {
                testFile.delete();
            }
            if(parent.exists())
            {
                parent.delete();
            }
        }
    }

    /**
     * Tests if a GeoPackage Writer will throw an illegal argument Exception when the
     * tileSet already exists in the GeoPackage
     */
    @Test(expected = TileStoreException.class)
    @SuppressWarnings("ExpectedExceptionNeverThrown") // Intellij bug?
    public void geoPackageWriterIllegalArgumentException7() throws ClassNotFoundException, SQLException, ConformanceException, IOException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);
        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            final CrsProfile spherical = new SphericalMercatorCrsProfile();

            final BoundingBox tileSetBounds = new BoundingBox(SphericalMercatorCrsProfile.Bounds.getMinimumX()/4,
                                                              SphericalMercatorCrsProfile.Bounds.getMinimumY()/2,
                                                              SphericalMercatorCrsProfile.Bounds.getMaximumX()/10,
                                                              SphericalMercatorCrsProfile.Bounds.getMaximumY()/8);
            final String tileSetName = "tableName";
            final int zoomLevel = 6;
            final int tileWidth = 256;
            final int tileHeight = 256;
            final int matrixWidth = 10;
            final int matrixHeight = 6;
            final SpatialReferenceSystem srs = gpkg.core().addSpatialReferenceSystem(spherical.getName(),
                                                                                     spherical.getCoordinateReferenceSystem().getAuthority(),
                                                                                     spherical.getCoordinateReferenceSystem().getIdentifier(),
                                                                                     spherical.getWellKnownText(),
                                                                                     spherical.getDescription());

            GeoPackageTileStoreTest.createTileSetAndTileMatrix(gpkg, srs, tileSetBounds, zoomLevel, matrixWidth, matrixHeight, tileWidth, tileHeight, tileSetName);

            try(final GeoPackageWriter ignored = new GeoPackageWriter(testFile,
                                                                      spherical.getCoordinateReferenceSystem(),
                                                                      tileSetName,
                                                                      tileSetName,
                                                                      "description",
                                                                      tileSetBounds,
                                                                      new ZoomTimesTwo(5, 8, 5, 3),
                                                                      new MimeType("image/png"),
                                                                      null))
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
     * Tests if geoPackage writer transform a crs coordinate
     * to a tile Coordinate
     */
    @Test
    public void gpkgWriterCrsTileCoordinate() throws SQLException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(13);

        final CoordinateReferenceSystem coordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 4326);
        final BoundingBox bBox =  new BoundingBox(0.0,0.0,90.0,180.0);
        final TileScheme tileScheme = new ZoomTimesTwo(1, 10, 2, 3);
        try(final GeoPackageWriter writer = new GeoPackageWriter(testFile,
                                                           coordinateReferenceSystem,
                                                           "tileSetTableName",
                                                           "tileSetIdentifier",
                                                           "tileSetDescription",
                                                           bBox,
                                                           new ZoomTimesTwo(1, 10, 2, 3),
                                                           new MimeType("image/png"),
                                                           null))
        {

            final CrsProfile geodeticCrsProfile = new GlobalGeodeticCrsProfile();

            final int zoomLevel = 4;
            final int row       = 4;
            final int column    = 7;

            writer.addTile(column, row, zoomLevel, createBufferedImage(BufferedImage.TYPE_BYTE_GRAY));

            final Coordinate<Integer> expectedTileCoordinate = new Coordinate<>(column, row);
            final CrsCoordinate       crsCoordinate          = geodeticCrsProfile.tileToCrsCoordinate(column, row, bBox, tileScheme.dimensions(zoomLevel ), GeoPackageTiles.Origin);
            final Coordinate<Integer> tileCoordinate         = writer.crsToTileCoordinate(crsCoordinate, zoomLevel);

            assertEquals("The coordinate returned was not as expected.",expectedTileCoordinate, tileCoordinate);
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if geoPackage writer transform a crs coordinate
     * to a tile Coordinate
     */
    @Test
    public void gpkgWriterCrsTileCoordinate2() throws SQLException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(13);
        final CrsProfile sphericalMercator = new SphericalMercatorCrsProfile();
        final CoordinateReferenceSystem coordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 3857);
        final BoundingBox bBox =  new BoundingBox(sphericalMercator.getBounds().getMinimumX()/2,
                                            sphericalMercator.getBounds().getMinimumY()/5,
                                            sphericalMercator.getBounds().getMaximumX()/3,
                                            sphericalMercator.getBounds().getMaximumY()/4);
        final TileScheme tileScheme = new ZoomTimesTwo(1, 10, 2, 3);
        try(final GeoPackageWriter writer = new GeoPackageWriter(testFile,
                                                           coordinateReferenceSystem,
                                                           "tileSetTableName",
                                                           "tileSetIdentifier",
                                                           "tileSetDescription",
                                                           bBox,
                                                           new ZoomTimesTwo(1, 10, 2, 3),
                                                           new MimeType("image/png"),
                                                           null))
        {

            final int zoomLevel = 4;
            final int row       = 4;
            final int column    = 7;
            writer.addTile(column, row, zoomLevel, createBufferedImage(BufferedImage.TYPE_BYTE_GRAY));
            final Coordinate<Integer> expectedTileCoordinate = new Coordinate<>(column, row);
            final CrsCoordinate       crsCoordinate          = sphericalMercator.tileToCrsCoordinate(column, row, bBox, tileScheme.dimensions(zoomLevel ), GeoPackageTiles.Origin);
            final Coordinate<Integer> tileCoordinate         = writer.crsToTileCoordinate(crsCoordinate, zoomLevel);

            assertEquals("The coordinate returned was not as expected.",expectedTileCoordinate, tileCoordinate);
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the correct CRS coordinate is returned for GeoPackageWriter depending on the origin
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions") // Assertion made in testTileToCrsCoordinate(...)
    public void geoPackageWriterTileToCrsCoordinate() throws SQLException, MimeTypeParseException, TileStoreException
    {

        final File                      testFile                  = this.getRandomFile(13);
        final CrsProfile                sphericalMercator         = new SphericalMercatorCrsProfile();
        final CoordinateReferenceSystem coordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 3857);
        final BoundingBox               bBox                      =  new BoundingBox(sphericalMercator.getBounds().getMinimumX()/2,
                                                                                     sphericalMercator.getBounds().getMinimumY()/5,
                                                                                     sphericalMercator.getBounds().getMaximumX()/3,
                                                                                     sphericalMercator.getBounds().getMaximumY()/4);

        final TileScheme tileScheme = new ZoomTimesTwo(1, 10, 2, 3);
        try
        {
            final TileOrigin origin = TileOrigin.UpperRight;
            final int column = 7;
            final int row = 4;
            final int zoomLevel = 4;
            testTileToCrsCoordinate(testFile,
                                    sphericalMercator,
                                    bBox,
                                    column,
                                    row,
                                    zoomLevel,
                                    origin,
                                    tileScheme,
                                    coordinateReferenceSystem);
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the correct CRS coordinate is returned for GeoPackageWriter depending on the origin
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions") // Assertion made in testTileToCrsCoordinate(...)
    public void geoPackageWriterTileToCrsCoordinate2() throws SQLException, MimeTypeParseException, TileStoreException
    {
        final File                      testFile                  = this.getRandomFile(13);
        final CrsProfile                sphericalMercator         = new SphericalMercatorCrsProfile();
        final CoordinateReferenceSystem coordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 3857);
        final BoundingBox               bBox                      = new BoundingBox(sphericalMercator.getBounds().getMinimumX()/3,
                                                                                    sphericalMercator.getBounds().getMinimumY()/2,
                                                                                    sphericalMercator.getBounds().getMaximumX()/4,
                                                                                    sphericalMercator.getBounds().getMaximumY()/5);
        final TileScheme tileScheme = new ZoomTimesTwo(1, 10, 3, 5);
        try
        {
            final int        zoomLevel = 5;
            final int        row       = 0;
            final int        column    = 0;
            final TileOrigin origin    = TileOrigin.LowerRight;

            testTileToCrsCoordinate(testFile,
                                    sphericalMercator,
                                    bBox,
                                    column,
                                    row,
                                    zoomLevel,
                                    origin,
                                    tileScheme,
                                    coordinateReferenceSystem);
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the correct CRS coordinate is returned for GeoPackageWriter depending on the origin
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions") // Assertion made in testTileToCrsCoordinate(...)
    public void geoPackageWriterTileToCrsCoordinate3() throws SQLException, MimeTypeParseException, TileStoreException
    {
        final File                      testFile                  = this.getRandomFile(13);
        final CrsProfile                globalGeodetic            = new GlobalGeodeticCrsProfile();
        final CoordinateReferenceSystem coordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 4326);
        final BoundingBox               bBox                      = new BoundingBox(globalGeodetic.getBounds().getMinimumX()/2,
                                                                                    globalGeodetic.getBounds().getMinimumY()/3,
                                                                                    globalGeodetic.getBounds().getMaximumX()/4,
                                                                                    globalGeodetic.getBounds().getMaximumY()/5);
        final TileScheme tileScheme = new ZoomTimesTwo(1, 10, 3, 5);
        try
        {
            final int        zoomLevel = 7;
            final int        row       = 2;
            final int        column    = 5;
            final TileOrigin origin    = TileOrigin.LowerLeft;

            testTileToCrsCoordinate(testFile,
                                    globalGeodetic,
                                    bBox,
                                    column,
                                    row,
                                    zoomLevel,
                                    origin,
                                    tileScheme,
                                    coordinateReferenceSystem);
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the correct CRS coordinate is returned for GeoPackageWriter depending on the origin
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions") // Assertion made in testTileToCrsCoordinate(...)
    public void geoPackageWriterTileToCrsCoordinate4() throws SQLException, MimeTypeParseException, TileStoreException
    {
        final File                      testFile                  = this.getRandomFile(13);
        final CrsProfile                globalGeodetic            = new GlobalGeodeticCrsProfile();
        final CoordinateReferenceSystem coordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 4326);
        final BoundingBox               bBox                      = new BoundingBox(globalGeodetic.getBounds().getMinimumX()/5,
                                                                                    globalGeodetic.getBounds().getMinimumY()/4,
                                                                                    globalGeodetic.getBounds().getMaximumX()/3,
                                                                                    globalGeodetic.getBounds().getMaximumY()/2);
        final TileScheme tileScheme = new ZoomTimesTwo(1, 10, 3, 5);
        try
        {
            final int        zoomLevel = 7;
            final int        row       = 3;
            final int        column    = 9;
            final TileOrigin origin    = TileOrigin.UpperLeft;

            testTileToCrsCoordinate(testFile,
                                    globalGeodetic,
                                    bBox,
                                    column,
                                    row,
                                    zoomLevel,
                                    origin,
                                    tileScheme,
                                    coordinateReferenceSystem);
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if an Illegal argument exception is thrown when passing in a null value
     * for TileOrigin to tileToCrsCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterTileToCrsCoordinateIllegalArgumentException() throws SQLException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(8);

        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("EPSG", 4326);
        final BoundingBox bBox =  new BoundingBox(0.0,0.0,0.0,0.0);
        final TileScheme tileScheme = new ZoomTimesTwo(0, 10, 2, 4);
        try(final GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile, crs, "tableName","identifier", "description", bBox, tileScheme, new MimeType("image/png"), null))
        {
            gpkgWriter.tileToCrsCoordinate(1, 1, 1, null);
            fail("Expected an IllegalArgumentException when giving a null value for TileOrigin.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * TODO doc required
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions") // Assertion made in testTileToCrsCoordinate(...)
    public void gpkgWriterGetTileBoundingBox() throws SQLException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(8);

        final CoordinateReferenceSystem crs            = new CoordinateReferenceSystem("EPSG", 4326);
        final CrsProfile                globalGeodetic = new GlobalGeodeticCrsProfile();
        final BoundingBox               bBox           = new BoundingBox(globalGeodetic.getBounds().getMinimumX()/5,
                                                                         globalGeodetic.getBounds().getMinimumY()/4,
                                                                         globalGeodetic.getBounds().getMaximumX()/3,
                                                                         globalGeodetic.getBounds().getMaximumY()/2);
        final TileScheme tileScheme = new ZoomTimesTwo(0, 10, 2, 4);

        try(final GeoPackageWriter gpkgWriter = new GeoPackageWriter(testFile,
                                                               crs,
                                                               "tablename",
                                                               "identifier",
                                                               "description",
                                                               bBox,
                                                               tileScheme,
                                                               new MimeType("image/png"),
                                                               null))
        {
            final int column = 2;
            final int row = 3;
            final int zoom = 5;
            gpkgWriter.addTile(column, row, zoom, createBufferedImage(BufferedImage.TYPE_3BYTE_BGR));

            final BoundingBox tileBBoxReturned = gpkgWriter.getTileBoundingBox(column, row, zoom);
            final CrsCoordinate upperLeftCorner = globalGeodetic.tileToCrsCoordinate(column, row, bBox, tileScheme.dimensions(zoom), TileOrigin.UpperLeft);
            final CrsCoordinate lowerRightCorner = globalGeodetic.tileToCrsCoordinate(column, row, bBox, tileScheme.dimensions(zoom), TileOrigin.LowerRight);

            final CrsCoordinate upperLeftReturned = new CrsCoordinate(tileBBoxReturned.getTopLeft(), globalGeodetic.getCoordinateReferenceSystem());
            final CrsCoordinate lowerRightReturned = new CrsCoordinate(tileBBoxReturned.getBottomRight(), globalGeodetic.getCoordinateReferenceSystem());

            assertCrsCoordinatesEqual(upperLeftReturned, upperLeftCorner);
            assertCrsCoordinatesEqual(lowerRightReturned, lowerRightCorner);
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackageWriter can write a tile to a GeoPackage
     * and if the GeoPackageReader can read that tile by retrieving it by
     * crs coordinate and tile coordinate
     */
    @Test
    public void addTileCrsCoordinate() throws SQLException, MimeTypeParseException, TileStoreException
    {
        final File testFile = this.getRandomFile(9);
        try
        {
            final CrsProfile  spherical     = new SphericalMercatorCrsProfile();
            final BoundingBox tileSetBounds = new BoundingBox(spherical.getBounds().getMinimumX()/3,
                                                              spherical.getBounds().getMinimumY()/2,
                                                              spherical.getBounds().getMaximumX()-100,
                                                              spherical.getBounds().getMaximumY()-100);
            final String tileSetName = "tableName";

            try(final GeoPackageWriter writer = new GeoPackageWriter(testFile,
                                                               spherical.getCoordinateReferenceSystem(),
                                                               tileSetName,
                                                               "identifier",
                                                               "description",
                                                               tileSetBounds,
                                                               new ZoomTimesTwo(5, 8, 5, 3),
                                                               new MimeType("image/png"),
                                                               null))
            {
                final int zoomLevel = 6;
                final BufferedImage  imageExpected = createBufferedImage(BufferedImage.TYPE_BYTE_GRAY);
                final CrsCoordinate crsCoordinate = new CrsCoordinate(tileSetBounds.getMinimumX(), tileSetBounds.getMaximumY(), spherical.getCoordinateReferenceSystem());//upper left tile
                //add an image to the writer
                writer.addTile(crsCoordinate, zoomLevel, imageExpected);
                //create a reader
                try(GeoPackageReader reader = new GeoPackageReader(testFile,tileSetName))
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
    private static void testTileToCrsCoordinate(final File                      testFile,
                                                final CrsProfile                profile,
                                                final BoundingBox               bBox,
                                                final int                       column,
                                                final int                       row,
                                                final int                       zoomLevel,
                                                final TileOrigin                origin,
                                                final TileScheme                tileScheme,
                                                final CoordinateReferenceSystem coordinateReferenceSystem) throws SQLException, MimeTypeParseException, TileStoreException, AssertionError
    {
        try(final GeoPackageWriter writer = new GeoPackageWriter(testFile,
                                                                 coordinateReferenceSystem,
                                                                 "tileSetTableName",
                                                                 "tileSetIdentifier",
                                                                 "tileSetDescription",
                                                                 bBox,
                                                                 tileScheme,
                                                                 new MimeType("image/png"),
                                                                 null))
        {
            writer.addTile(column, row, zoomLevel, createBufferedImage(BufferedImage.TYPE_BYTE_GRAY));
            final CrsCoordinate expectedCrsCoordinate = profile.tileToCrsCoordinate(column, row, bBox, tileScheme.dimensions(zoomLevel), origin);
            final CrsCoordinate returnedCrsCoordinate = writer.tileToCrsCoordinate(column, row, zoomLevel, origin);

            assertCrsCoordinatesEqual(expectedCrsCoordinate, returnedCrsCoordinate);
        }
    }

    private static void assertCrsCoordinatesEqual(final CrsCoordinate crsCoordReturned, final CrsCoordinate crsCoordExpected) throws AssertionError
    {
        assertTrue(String.format("The coordinate returned was not the values expected.\n"
                                   + "Actual Coordinate: (%f, %f) Crs: %s %d\nReturned Coordinate: (%f, %f) Crs: %s %d",
                                   crsCoordReturned.getX(),
                                   crsCoordReturned.getY(),
                                   crsCoordReturned.getCoordinateReferenceSystem().getAuthority(),
                                   crsCoordReturned.getCoordinateReferenceSystem().getIdentifier(),
                                   crsCoordExpected.getX(),
                                   crsCoordReturned.getY(),
                                   crsCoordReturned.getCoordinateReferenceSystem().getAuthority(),
                                   crsCoordReturned.getCoordinateReferenceSystem().getIdentifier()),
                      isEqual(crsCoordExpected, crsCoordReturned) && crsCoordExpected.getCoordinateReferenceSystem().equals(crsCoordReturned.getCoordinateReferenceSystem()));
    }

    private static boolean isEqual(final Coordinate<Double> coordinateExpected, final Coordinate<Double> coordinateReturned)
    {
        final boolean xEqual = Math.abs(coordinateExpected.getX() - coordinateReturned.getX()) <= Epsilon;
        final boolean yEqual = Math.abs(coordinateExpected.getY() - coordinateReturned.getY()) <= Epsilon;
        return xEqual && yEqual;
    }

    private static void deleteFile(final File testFile)
    {
        if(testFile.exists())
        {
            if(!testFile.delete())
            {
                throw new RuntimeException(String.format("Unable to delete test file: %s", testFile));
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
            //noinspection ObjectAllocationInLoop
            testFile = new File(FileSystems.getDefault().getPath(this.getRanString(length)) + ".gpkg");
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

    private static boolean areTileAndTileHandleEqual(final Tile tile, final TileHandle tileHandle, final String outputFormat)
    {
        try
        {
             return tile.getColumn()    == tileHandle.getColumn()     &&
                    tile.getRow()       == tileHandle.getRow()        &&
                    tile.getZoomLevel() ==  tileHandle.getZoomLevel() &&
                    Arrays.equals(tile.getImageData(), ImageUtility.bufferedImageToBytes(tileHandle.getImage(), outputFormat));
        }
        catch(final Exception ignored)
        {
            return false;
        }
    }

    private static Set<TileMatrixDimensions> addTileMatricesToGpkg(final Iterable<Integer> zoomLevels, final TileSet tileSet, final GeoPackage gpkg, final TileScheme tileScheme, final int tileWidth, final int tileHeight) throws SQLException
    {
        final Set<TileMatrixDimensions> tileMatrixDimensionsExpected = new HashSet<>();

        for(final int zoomLevel : zoomLevels)
        {
            final TileMatrixDimensions dimensions = tileScheme.dimensions(zoomLevel);

            tileMatrixDimensionsExpected.add(dimensions);

            final double pixelXSize = (tileSet.getMaximumX() - tileSet.getMinimumX()) / dimensions.getWidth()  / tileWidth;
            final double pixelYSize = (tileSet.getMaximumY() - tileSet.getMinimumY()) / dimensions.getHeight() / tileHeight;

            final TileMatrixSet tileMatrixSet = gpkg.tiles().getTileMatrixSet(tileSet);

            gpkg.tiles().addTileMatrix(tileMatrixSet, zoomLevel, dimensions.getWidth(), dimensions.getHeight(), tileWidth, tileHeight);
        }

        return tileMatrixDimensionsExpected;
    }
    private static TileMatrix createTileSetAndTileMatrix(final GeoPackage gpkg, final BoundingBox bBox, final int zoomLevel, final int matrixWidth, final int matrixHeight) throws SQLException
    {
        return createTileSetAndTileMatrix(gpkg, gpkg.core().getSpatialReferenceSystem("EPSG", 4326), bBox, zoomLevel, matrixWidth, matrixHeight, 256, 256, "tableName");
    }

    private static TileMatrix createTileSetAndTileMatrix(final GeoPackage gpkg, final BoundingBox bBox, final int zoomLevel, final int matrixWidth, final int matrixHeight, final int tileWidth, final int tileHeight, final String identifierTableName) throws SQLException
    {
        return createTileSetAndTileMatrix(gpkg, gpkg.core().getSpatialReferenceSystem("EPSG", 4326), bBox, zoomLevel, matrixWidth, matrixHeight, tileWidth, tileHeight, identifierTableName);
    }

    private static TileMatrix createTileSetAndTileMatrix(final GeoPackage gpkg, final SpatialReferenceSystem srs, final BoundingBox bBox, final int zoomLevel, final int matrixWidth, final int matrixHeight, final int tileWidth, final int tileHeight, final String identifierTableName) throws SQLException
    {
        final TileSet tileSet = gpkg.tiles()
                                    .addTileSet(identifierTableName,
                                                identifierTableName,
                                                "description",
                                                bBox,
                                                srs);

        final TileMatrixSet tileMatrixSet = gpkg.tiles().getTileMatrixSet(tileSet);

        return  gpkg.tiles().addTileMatrix(tileMatrixSet,
                                           zoomLevel,
                                           matrixWidth,
                                           matrixHeight,
                                           tileWidth,
                                           tileHeight
        );
    }
}
