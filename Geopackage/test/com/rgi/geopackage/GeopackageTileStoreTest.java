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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import store.GeoPackageReader;
import store.GeoPackageWriter;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.util.ImageUtility;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.tiles.RelativeTileCoordinate;
import com.rgi.geopackage.tiles.Tile;
import com.rgi.geopackage.tiles.TileMatrix;
import com.rgi.geopackage.tiles.TileSet;
import com.rgi.geopackage.verification.ConformanceException;


public class GeopackageTileStoreTest
{
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    private final Random randomGenerator = new Random();

    /**
     * Tests if geopackage reader will throw
     * an IllegalArgumentException when passing
     * a null value for TileSet
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */

    @Test(expected = IllegalArgumentException.class)
    public void geopackageReaderIllegalArgumentException() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(9);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            @SuppressWarnings("unused")
            GeoPackageReader reader = new GeoPackageReader(gpkg, null);
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
     * Tests if geopackage reader will throw
     * an IllegalArgumentException when passing
     * a null value for TileSet
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void geopackageReaderIllegalArgumentException2() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(9);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            TileSet tileSet = gpkg.tiles().addTileSet("tablename", "identifier", "description", new BoundingBox(0.0,0.0,10.0,10.0), gpkg.core().getSpatialReferenceSystem(4326));
            new GeoPackageReader(null, tileSet);
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
    public void geopackageReaderGetBounds() throws SQLException, FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, ConformanceException, TileStoreException
    {
        File testFile = this.getRandomFile(8);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            BoundingBox      bBoxGiven  = new BoundingBox(0.0,0.0,180.0,180.0);
            TileSet          tileSet    = gpkg.tiles().addTileSet("TableName", "identifier", "description", bBoxGiven, gpkg.core().getSpatialReferenceSystem(4326));
            GeoPackageReader gpkgReader = new GeoPackageReader(gpkg, tileSet);

            BoundingBox bBoxReturned = gpkgReader.getBounds();
            Assert.assertTrue("The bounding box returned from GeoPackageReader was not the same that was given.", bBoxGiven.equals(bBoxReturned));
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
        File testFile = this.getRandomFile(8);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            BoundingBox      bBox  = new BoundingBox(0.0,0.0,180.0,180.0);
            TileSet          tileSet    = gpkg.tiles().addTileSet("TableName", "identifier", "description", bBox, gpkg.core().getSpatialReferenceSystem(4326));

            int zoomLevel = 2;
            int matrixWidth = 3;
            int matrixHeight = 3;
            int tileWidth = 256;
            int tileHeight = 256;
            double pixelXSize = bBox.getWidth()/matrixWidth/tileWidth;
            double pixelYSize = bBox.getHeight()/matrixHeight/tileHeight;

            TileMatrix             tileMatrix = gpkg.tiles().addTileMatrix(tileSet, zoomLevel, matrixWidth, matrixHeight, tileWidth, tileHeight, pixelXSize, pixelYSize);
            RelativeTileCoordinate coordinate = new RelativeTileCoordinate(0, 0, 2);
            //add tiles
            gpkg.tiles().addTile(tileSet, tileMatrix, coordinate, createImageBytes(BufferedImage.TYPE_3BYTE_BGR));
            gpkg.tiles().addTile(tileSet, tileMatrix, new RelativeTileCoordinate(0, 1, 2), new byte[]{0,1,2,3});
            GeoPackageReader      gpkgReader    = new GeoPackageReader(gpkg, tileSet);
            long                  numberOfTiles = gpkgReader.countTiles();

            Assert.assertTrue(String.format("Expected the GeoPackage Reader countTiles to return a value of 2 but instead returned %d",
                                            numberOfTiles),
                             numberOfTiles == 2);
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
    public void getByteSize() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, TileStoreException
    {
        File testFile = this.getRandomFile(8);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            BoundingBox      bBoxGiven  = new BoundingBox(0.0,0.0,180.0,180.0);
            TileSet          tileSet    = gpkg.tiles().addTileSet("TableName", "identifier", "description", bBoxGiven, gpkg.core().getSpatialReferenceSystem(4326));
            GeoPackageReader gpkgReader = new GeoPackageReader(gpkg, tileSet);

            long byteSizeReturned = gpkgReader.getByteSize();
            long byteSizeExpected = testFile.getTotalSpace();

            Assert.assertTrue(String.format("The GeoPackage Reader did not return the expected value. \nExpected: %d Actual: %d",
                                            byteSizeReturned,
                                            byteSizeExpected),
                              byteSizeReturned ==  byteSizeExpected);
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

        File testFile = this.getRandomFile(8);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            BoundingBox      bBox  = new BoundingBox(0.0, 0.0, 180.0, 180.0);
            TileSet          tileSet    = gpkg.tiles().addTileSet("TableName", "identifier", "description", bBox, gpkg.core().getSpatialReferenceSystem(4326));

            int zoomLevel = 2;
            int matrixWidth = 3;
            int matrixHeight = 3;
            int tileWidth = 256;
            int tileHeight = 256;
            double pixelXSize = bBox.getWidth()/matrixWidth/tileWidth;
            double pixelYSize = bBox.getHeight()/matrixHeight/tileHeight;

            TileMatrix             tileMatrix = gpkg.tiles().addTileMatrix(tileSet, zoomLevel, matrixWidth, matrixHeight, tileWidth, tileHeight, pixelXSize, pixelYSize);
            RelativeTileCoordinate coordinate = new RelativeTileCoordinate(0, 0, 2);
            //add tiles
            Tile tileExpected = gpkg.tiles().addTile(tileSet, tileMatrix, coordinate, createImageBytes(BufferedImage.TYPE_4BYTE_ABGR));
                                gpkg.tiles().addTile(tileSet, tileMatrix, new RelativeTileCoordinate(0, 1, 2), new byte[]{0,1,2,3});
            GeoPackageReader      gpkgReader    = new GeoPackageReader(gpkg, tileSet);

            BufferedImage         returnedImage = gpkgReader.getTile(coordinate.getRow(), coordinate.getColumn(), coordinate.getZoomLevel());

            Assert.assertArrayEquals("The tile image data returned from getTile in GeoPackage reader wasn't the same as the one given.",
                                     tileExpected.getImageData(),
                                     ImageUtility.bufferedImageToBytes(returnedImage, "PNG"));

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

        File testFile = this.getRandomFile(8);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            BoundingBox      bBox  = new BoundingBox(0.0, 0.0, 180.0, 180.0);
            TileSet          tileSet    = gpkg.tiles().addTileSet("TableName", "identifier", "description", bBox, gpkg.core().getSpatialReferenceSystem(4326));

            int zoomLevel = 2;
            int matrixWidth = 3;
            int matrixHeight = 3;
            int tileWidth = 256;
            int tileHeight = 256;
            double pixelXSize = bBox.getWidth()/matrixWidth/tileWidth;
            double pixelYSize = bBox.getHeight()/matrixHeight/tileHeight;

            TileMatrix             tileMatrix = gpkg.tiles().addTileMatrix(tileSet, zoomLevel, matrixWidth, matrixHeight, tileWidth, tileHeight, pixelXSize, pixelYSize);
            RelativeTileCoordinate coordinate = new RelativeTileCoordinate(0, 0, 2);
            //add tiles
            Tile tileExpected = gpkg.tiles().addTile(tileSet, tileMatrix, coordinate, createImageBytes(BufferedImage.TYPE_4BYTE_ABGR));
                                gpkg.tiles().addTile(tileSet, tileMatrix, new RelativeTileCoordinate(0, 1, 2), createImageBytes(BufferedImage.TYPE_4BYTE_ABGR));

            GeoPackageReader gpkgReader        = new GeoPackageReader(gpkg, tileSet);
            CrsCoordinate    crsTileCoordinate = new CrsCoordinate(130.0, 60.0, "epsg", 4326);
            BufferedImage    returnedImage     = gpkgReader.getTile(crsTileCoordinate, zoomLevel);

            Assert.assertArrayEquals("The tile image data returned from getTile in GeoPackage reader wasn't the same as the one given.",
                                     tileExpected.getImageData(),
                                     ImageUtility.bufferedImageToBytes(returnedImage, "PNG"));

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
        File testFile = this.getRandomFile(8);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            BoundingBox      bBox  = new BoundingBox(0.0, 0.0, 180.0, 180.0);
            TileSet          tileSet    = gpkg.tiles().addTileSet("TableName", "identifier", "description", bBox, gpkg.core().getSpatialReferenceSystem(4326));

            int zoomLevel = 2;
            int matrixWidth = 3;
            int matrixHeight = 3;
            int tileWidth = 256;
            int tileHeight = 256;
            double pixelXSize = bBox.getWidth()/matrixWidth/tileWidth;
            double pixelYSize = bBox.getHeight()/matrixHeight/tileHeight;

            TileMatrix             tileMatrix = gpkg.tiles().addTileMatrix(tileSet, zoomLevel, matrixWidth, matrixHeight, tileWidth, tileHeight, pixelXSize, pixelYSize);
            RelativeTileCoordinate coordinate = new RelativeTileCoordinate(0, 0, 2);
            //add tiles
                                     gpkg.tiles().addTile(tileSet, tileMatrix, coordinate, createImageBytes(BufferedImage.TYPE_BYTE_GRAY));
            Tile tileExpected =      gpkg.tiles().addTile(tileSet, tileMatrix, new RelativeTileCoordinate(2, 0, 2), createImageBytes(BufferedImage.TYPE_BYTE_GRAY));

            GeoPackageReader gpkgReader        = new GeoPackageReader(gpkg, tileSet);
            CrsCoordinate    crsTileCoordinate = new CrsCoordinate(60.0, 59.0, "epsg", 4326);
            BufferedImage    returnedImage     = gpkgReader.getTile(crsTileCoordinate, zoomLevel);

            Assert.assertArrayEquals("The tile image data returned from getTile in GeoPackage reader wasn't the same as the one given.",
                                     tileExpected.getImageData(),
                                     ImageUtility.bufferedImageToBytes(returnedImage, "PNG"));

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
    public void getTileIllegalArgumentException() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, TileStoreException
    {
        File testFile = this.getRandomFile(12);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            TileSet tileSet = gpkg.tiles().addTileSet("tabelName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(0.0,0.0,30.0,60.0),
                                                      gpkg.core().getSpatialReferenceSystem(4326));

            GeoPackageReader gpkgReader = new GeoPackageReader(gpkg, tileSet);
            gpkgReader.getTile(null, 2);
            fail("Expected GeoPackageReader to throw an IllegalArgumentException when passing a null value to coordinate in getTile method");
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
    public void getTileIllegalArgumentException2() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, TileStoreException
    {
        File testFile = this.getRandomFile(12);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            TileSet tileSet = gpkg.tiles().addTileSet("tabelName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(0.0,0.0,30.0,60.0),
                                                      gpkg.core().addSpatialReferenceSystem("Srs Name", 3857, "EPSG", 3857, "definition", "description"));

            GeoPackageReader gpkgReader = new GeoPackageReader(gpkg, tileSet);
            CrsCoordinate crsCoord = new CrsCoordinate(2, 0, "EPSG", 3395);
            gpkgReader.getTile(crsCoord, 2);
            fail("Expected GeoPackageReader to throw an IllegalArgumentException when passing a null value to coordinate in getTile method");
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
    public void getTileThatDoesntExist() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, TileStoreException
    {
        File testFile = this.getRandomFile(9);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            TileSet tileSet = gpkg.tiles().addTileSet("tabelName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(0.0,0.0,30.0,60.0),
                                                      gpkg.core().addSpatialReferenceSystem("Srs Name", 3857, "EPSG", 3857, "definition", "description"));

            GeoPackageReader gpkgReader = new GeoPackageReader(gpkg, tileSet);

            CrsCoordinate coordinate = new CrsCoordinate(2, 0, "EPSG", 3857);
            BufferedImage imageReturned = gpkgReader.getTile(coordinate, 4);

            assertTrue("Asked for a tile that didn't exist in the gpkg and didn't return a null value for the buffer image",imageReturned == null);

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
     * Tests if the GeoPackage Reader returns the expected
     * coordinate reference system
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void getCoordinateReferenceSystem() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(7);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            SpatialReferenceSystem spatialReferenceSystem = gpkg.core().addSpatialReferenceSystem("WebMercator", 3857, "epsg", 3857, "definition", "description");
            TileSet                tileSet                = gpkg.tiles().addTileSet("tabelName", "identifier", "description", new BoundingBox(0.0,0.0,30.0,60.0), spatialReferenceSystem);
            GeoPackageReader       gpkgReader             = new GeoPackageReader(gpkg, tileSet);

            CoordinateReferenceSystem coordinateReferenceSystemReturned = gpkgReader.getCoordinateReferenceSystem();

            Assert.assertTrue(String.format("The coordinate reference system returned is not what was expected. Actual authority: %s Actual Identifer: %d\nExpected authority: %s Expected Identifier: %d.",
                                            coordinateReferenceSystemReturned.getAuthority(),
                                            coordinateReferenceSystemReturned.getIdentifier(),
                                            spatialReferenceSystem.getOrganization(),
                                            spatialReferenceSystem.getIdentifier()),
                              coordinateReferenceSystemReturned.getAuthority().equalsIgnoreCase(spatialReferenceSystem.getOrganization()) &&
                              coordinateReferenceSystemReturned.getIdentifier() ==              spatialReferenceSystem.getIdentifier());
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
    public void getZoomLevels() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, TileStoreException
    {
        File testFile = this.getRandomFile(10);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            BoundingBox bBox =  new BoundingBox(0.0,0.0,30.0,60.0);
            TileSet tileSet = gpkg.tiles()
                                  .addTileSet("tabelName",
                                              "identifier",
                                              "description",
                                              bBox,
                                              gpkg.core().getSpatialReferenceSystem(4326));
            TileSet tileSet2 = gpkg.tiles()
                                   .addTileSet("tabelName2",
                                               "identifier2",
                                               "description2",
                                               bBox,
                                               gpkg.core().getSpatialReferenceSystem(-1));
            Set<Integer> zoomLevelsExpected = new HashSet<>();
            zoomLevelsExpected.add(2);
            zoomLevelsExpected.add(5);
            zoomLevelsExpected.add(9);
            zoomLevelsExpected.add(20);

            int matrixWidth = 2;
            int matrixHeight = 2;
            int tileWidth = 256;
            int tileHeight = 256;
            double pixelXSize = bBox.getWidth()/matrixWidth/tileWidth;
            double pixelYSize = bBox.getHeight()/matrixHeight/tileHeight;

            gpkg.tiles().addTileMatrix(tileSet, 2, matrixWidth, matrixHeight, tileWidth, tileHeight, pixelXSize, pixelYSize);
            gpkg.tiles().addTileMatrix(tileSet, 20, matrixWidth, matrixHeight, tileWidth, tileHeight, pixelXSize, pixelYSize);
            gpkg.tiles().addTileMatrix(tileSet, 9, matrixWidth, matrixHeight, tileWidth, tileHeight, pixelXSize, pixelYSize);
            gpkg.tiles().addTileMatrix(tileSet, 5, matrixWidth, matrixHeight, tileWidth, tileHeight, pixelXSize, pixelYSize);
            gpkg.tiles().addTileMatrix(tileSet2, 7, matrixWidth, matrixHeight, tileWidth, tileHeight, pixelXSize, pixelYSize);//this one is not included in zooms
            GeoPackageReader gpkgReader = new GeoPackageReader(gpkg, tileSet);                                                //because it is a different tileset
            Set<Integer> zoomLevelsReturned = gpkgReader.getZoomLevels();

            Assert.assertTrue(String.format("The GeoPackage Reader did not return all of the zoom levels expected. Expected Zooms: %s. Actual Zooms: %s",
                                            zoomLevelsExpected.stream().map(integer -> integer.toString()).collect(Collectors.joining(", ")),
                                            zoomLevelsReturned.stream().map(integer -> integer.toString()).collect(Collectors.joining(", "))),
                              zoomLevelsReturned.containsAll(zoomLevelsExpected));
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
     * Tests if GeoPackage Writer will be able to add a tile to
     * an exisiting GeoPackage
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     * @throws IOException
     * @throws TileStoreException
     */
    @Test 
    public void geopackageWriterAddTile() throws ClassNotFoundException, SQLException, ConformanceException, MimeTypeParseException, IOException, TileStoreException
    {
        File testFile = this.getRandomFile(6);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            BoundingBox boundingBox = new BoundingBox(0.0,0.0,90.0,90.0);
            TileSet tileSet = gpkg.tiles().addTileSet("tableName", "identifier", "description", boundingBox, gpkg.core().getSpatialReferenceSystem(4326));

            int matrixHeight = 2;
            int matrixWidth = 4;
            int tileHeight = 512;
            int tileWidth = 256;
            
           TileMatrix tileMatrix = gpkg.tiles().addTileMatrix(tileSet, 
                                                              0,  
                                                              matrixWidth, 
                                                              matrixHeight, 
                                                              tileWidth,
                                                              tileHeight, 
                                                              (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                                              (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);
            
            gpkg.tiles().addTile(tileSet, tileMatrix, new RelativeTileCoordinate(0,0,0), createImageBytes("JPEG"));
            MimeType mimeType = new MimeType("image/jpeg");
            GeoPackageWriter gpkgWriter = new GeoPackageWriter(gpkg, tileSet, mimeType);
            
            RelativeTileCoordinate coordinate = new RelativeTileCoordinate(0,1,0);
            gpkgWriter.addTile(coordinate.getRow(), coordinate.getColumn(), coordinate.getZoomLevel(), new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_GRAY));
            
            Tile tile = gpkg.tiles().getTile(tileSet, coordinate);
            
            assertTrue("GeoPackageWriter was unable to add a tile to a GeoPackage",tile != null);
           
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
    //TODO fix bug with zooms times two.  Case where it fails: IF a Geopackage does not have any tiles in it (just tile set) then wants to add a tile, the zoomLevelDimensions
    //field variable is null in the ZoomTimesTwo class.  Therefore when trying to add a matrixSet there is an arrayIndexOutOfBoundsException(which I find strange)
    //I can explain better in person but this is a bug needed to be fixed when adding a single tile a zoomLevel 0.
    /**
     * Tests if geopackage writer can write tiles to
     * a geopackage
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     * @throws TileStoreException
     */
    @Test
    public void geoPackageWriterAddTile2() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, MimeTypeParseException, TileStoreException
    {
        File testFile = this.getRandomFile(6);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            BoundingBox boundingBox = new BoundingBox(0.0,0.0,90.0,90.0);
            TileSet tileSet = gpkg.tiles().addTileSet("tableName", "identifier", "description", boundingBox, gpkg.core().getSpatialReferenceSystem(4326));

            MimeType mimeType = new MimeType("image/jpeg");
            GeoPackageWriter gpkgWriter = new GeoPackageWriter(gpkg, tileSet, mimeType);
            
            CrsCoordinate crsCoordinate = new CrsCoordinate(50.0,30.0,"epsg", 4326);
            int zoomLevel = 0;
            
            gpkgWriter.addTile(crsCoordinate, zoomLevel, new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_GRAY));
            
            Tile tile = gpkg.tiles().getTile(tileSet, crsCoordinate, zoomLevel);
            
            assertTrue("GeoPackageWriter did not write a tile correctly",tile != null);
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
    public void addTileIllegalArgumentException() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, MimeTypeParseException, TileStoreException
    {
        File testFile = this.getRandomFile(6);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            BoundingBox boundingBox = new BoundingBox(0.0,0.0,90.0,90.0);
            TileSet tileSet = gpkg.tiles().addTileSet("tableName", "identifier", "description", boundingBox, gpkg.core().getSpatialReferenceSystem(4326));

            MimeType mimeType = new MimeType("image/jpeg");
            GeoPackageWriter gpkgWriter = new GeoPackageWriter(gpkg, tileSet, mimeType);
            
            RelativeTileCoordinate coordinate = new RelativeTileCoordinate(0,1,0);
            gpkgWriter.addTile(coordinate.getRow(), coordinate.getColumn(), coordinate.getZoomLevel(), null);
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
    public void addTileIllegalArgumentException2() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, MimeTypeParseException, TileStoreException
    {
        File testFile = this.getRandomFile(6);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            BoundingBox boundingBox = new BoundingBox(0.0,0.0,90.0,90.0);
            TileSet tileSet = gpkg.tiles().addTileSet("tableName", "identifier", "description", boundingBox, gpkg.core().getSpatialReferenceSystem(4326));

            MimeType mimeType = new MimeType("image/jpeg");
            GeoPackageWriter gpkgWriter = new GeoPackageWriter(gpkg, tileSet, mimeType);
            
            gpkgWriter.addTile(new CrsCoordinate(20.0,30.0, "epsg", 4326), 0, null);
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
    public void addTileIllegalArgumentException3() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, MimeTypeParseException, TileStoreException
    {
        File testFile = this.getRandomFile(6);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            BoundingBox boundingBox = new BoundingBox(0.0,0.0,90.0,90.0);
            TileSet tileSet = gpkg.tiles().addTileSet("tableName", "identifier", "description", boundingBox, gpkg.core().getSpatialReferenceSystem(4326));

            MimeType mimeType = new MimeType("image/jpeg");
            GeoPackageWriter gpkgWriter = new GeoPackageWriter(gpkg, tileSet, mimeType);
            
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
    public void addTileIllegalArgumentException4() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, MimeTypeParseException, TileStoreException
    {
        File testFile = this.getRandomFile(6);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            BoundingBox boundingBox = new BoundingBox(0.0,0.0,90.0,90.0);
            TileSet tileSet = gpkg.tiles().addTileSet("tableName", "identifier", "description", boundingBox, gpkg.core().getSpatialReferenceSystem(4326));

            MimeType mimeType = new MimeType("image/jpeg");
            GeoPackageWriter gpkgWriter = new GeoPackageWriter(gpkg, tileSet, mimeType);
            
            gpkgWriter.addTile(new CrsCoordinate(20.0, 30.0, "epsg", 3395), 0, new BufferedImage(256,256, BufferedImage.TYPE_INT_ARGB));
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
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, MimeTypeParseException
    {
        File testFile = this.getRandomFile(8);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            new GeoPackageWriter(gpkg, null, new MimeType("image/png"));
            fail("Expected GeoPackageWriter to throw an IllegalArgumentException if a user puts in a null value for the parameter tileSet.");
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
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException2() throws SQLException, FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, ConformanceException, MimeTypeParseException
    {
        File testFile = this.getRandomFile(8);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            TileSet tileSet = gpkg.tiles().addTileSet("tableName", "identifier", "description", new BoundingBox(0.0,0.0,90.0,90.0), gpkg.core().getSpatialReferenceSystem(4326));
            new GeoPackageWriter(null, tileSet, new MimeType("image/png"));
            
            fail("Expected GeoPackageWriter to throw an IllegalArgumentException if a user puts in a null value for the parameter GeoPackage.");
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
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException3() throws SQLException, FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, ConformanceException
    {
        File testFile = this.getRandomFile(8);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            TileSet tileSet = gpkg.tiles().addTileSet("tableName", "identifier", "description", new BoundingBox(0.0,0.0,90.0,90.0), gpkg.core().getSpatialReferenceSystem(4326));
            new GeoPackageWriter(gpkg, tileSet, null);
            
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
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void geoPackageWriterIllegalArgumentException4() throws SQLException, FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, ConformanceException, MimeTypeParseException
    {
        File testFile = this.getRandomFile(8);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            TileSet tileSet = gpkg.tiles().addTileSet("tableName", "identifier", "description", new BoundingBox(0.0,0.0,90.0,90.0), gpkg.core().getSpatialReferenceSystem(4326));
            new GeoPackageWriter(gpkg, tileSet, new MimeType("text/xml"));
            
            fail("Expected GeoPackageWriter to throw an IllegalArgumentException if a user puts in an unsupported image output format.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
    
    
    private static byte[] createImageBytes(String imageType) throws IOException
    {
        return ImageUtility.bufferedImageToBytes(new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB), imageType);
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
    private static byte[] createImageBytes(int bufferedImageType) throws IOException
    {
        return ImageUtility.bufferedImageToBytes(new BufferedImage(256, 256, bufferedImageType), "PNG");
    }
}
