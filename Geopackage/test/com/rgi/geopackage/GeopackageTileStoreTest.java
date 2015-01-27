package com.rgi.geopackage;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.rgi.common.BoundingBox;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.util.ImageUtility;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.tiles.RelativeTileCoordinate;
import com.rgi.geopackage.tiles.Tile;
import com.rgi.geopackage.tiles.TileMatrix;
import com.rgi.geopackage.tiles.TileSet;
import com.rgi.geopackage.verification.ConformanceException;

import store.GeoPackageReader;

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
            double pixelXSize = bBox.getWidth()/tileWidth;
            double pixelYSize = bBox.getHeight()/tileHeight;
            
            TileMatrix             tileMatrix = gpkg.tiles().addTileMatrix(tileSet, zoomLevel, matrixWidth, matrixHeight, tileWidth, tileHeight, pixelXSize, pixelYSize);
            RelativeTileCoordinate coordinate = new RelativeTileCoordinate(0, 0, 2);
            //add tiles
            gpkg.tiles().addTile(tileSet, tileMatrix, coordinate, createImageBytes());
            gpkg.tiles().addTile(tileSet, tileMatrix, new RelativeTileCoordinate(0, 1, 2), createImageBytes());
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
    
    
//    @Test
//    public void getTile() throws ClassNotFoundException, SQLException, ConformanceException, IOException, TileStoreException
//    {
//
//        File testFile = this.getRandomFile(8);
//        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
//        {
//            BoundingBox      bBox  = new BoundingBox(0.0,0.0,180.0,180.0);
//            TileSet          tileSet    = gpkg.tiles().addTileSet("TableName", "identifier", "description", bBox, gpkg.core().getSpatialReferenceSystem(4326));
//            
//            int zoomLevel = 2;
//            int matrixWidth = 3;
//            int matrixHeight = 3;
//            int tileWidth = 256;
//            int tileHeight = 256;
//            double pixelXSize = bBox.getWidth()/tileWidth;
//            double pixelYSize = bBox.getHeight()/tileHeight;
//            
//            TileMatrix             tileMatrix = gpkg.tiles().addTileMatrix(tileSet, zoomLevel, matrixWidth, matrixHeight, tileWidth, tileHeight, pixelXSize, pixelYSize);
//            RelativeTileCoordinate coordinate = new RelativeTileCoordinate(0, 0, 2);
//            //add tiles
//            Tile tileExpected = gpkg.tiles().addTile(tileSet, tileMatrix, coordinate, createImageBytes());
//            gpkg.tiles().addTile(tileSet, tileMatrix, new RelativeTileCoordinate(0, 1, 2), createImageBytes());
//            GeoPackageReader      gpkgReader    = new GeoPackageReader(gpkg, tileSet);
//            BufferedImage         returnedImage = gpkgReader.getTile(coordinate.getRow(), coordinate.getColumn(), coordinate.getZoomLevel());
//           
//            BufferedImage         expectedImage = ImageIO.read(new ByteArrayInputStream(tileExpected.getImageData()));
//            //byte[] returnedTileInBytes = ((DataBufferByte) returnedImage.getData().getDataBuffer()).getData();
//            
//            Assert.assertEquals("The tile image data returned from getTile in GeoPackage reader wasn't the same as the one given.", expectedImage, returnedImage);
//            
//        }
//        finally
//        {
//            if(testFile.exists())
//            {
//                if(!testFile.delete())
//                {
//                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
//                }
//            }
//        }
//    }
    
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
    private static byte[] createImageBytes() throws IOException
    {
        return ImageUtility.bufferedImageToBytes(new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB), "PNG");
    }

    
    
}
