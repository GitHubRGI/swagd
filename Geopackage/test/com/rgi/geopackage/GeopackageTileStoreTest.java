package com.rgi.geopackage;

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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import store.GeoPackageReader;

import com.rgi.common.BoundingBox;
import com.rgi.common.CoordinateReferenceSystem;
import com.rgi.common.coordinates.CrsCoordinate;
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
    
  
   // TODO: need getCoordinateReferenceSystem to be implemented
    @Test
    public void getCoordinateReferenceSystem() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(7);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            SpatialReferenceSystem spatialReferenceSystem = gpkg.core().addSpatialReferenceSystem("SrsName", 555, "organization", 123, "definition", "description");
            TileSet tileSet = gpkg.tiles().addTileSet("tabelName", "identifier", "description", new BoundingBox(0.0,0.0,30.0,60.0), spatialReferenceSystem);
            GeoPackageReader gpkgReader = new GeoPackageReader(gpkg, tileSet);
            
            CoordinateReferenceSystem coordinateReferenceSystemReturned = gpkgReader.getCoordinateReferenceSystem();
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
            Set<Integer> zoomLevelsExpected = new HashSet<Integer>();
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
            GeoPackageReader gpkgReader = new GeoPackageReader(gpkg, tileSet);
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
