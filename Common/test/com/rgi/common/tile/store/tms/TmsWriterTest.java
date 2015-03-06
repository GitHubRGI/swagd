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

package com.rgi.common.tile.store.tms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageWriteParam;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.coordinate.referencesystem.profile.GlobalGeodeticCrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.SphericalMercatorCrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.store.TileStoreException;

/**
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
@SuppressWarnings("javadoc")
public class TmsWriterTest
{
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Tests if the tms reader returns the expected tile
     * @throws TileStoreException throws if a tileStoreException occurs
     * @throws MimeTypeParseException throws if the MimeType cannot be detected
     */
    @Test
    public void verifyTileRetrieval() throws TileStoreException, MimeTypeParseException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 4);

        final int zoomLevel = 1;
        final Coordinate<Integer> coordinate = new Coordinate<>(0, 0);
        final CoordinateReferenceSystem coordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 3857);

        final TmsWriter tmsWriter = new TmsWriter(coordinateReferenceSystem, tmsDir, new MimeType("image", "png"));
        final TmsReader tmsReader = new TmsReader(coordinateReferenceSystem, tmsDir);

        final BufferedImage image = createImage();

        tmsWriter.addTile(coordinate.getX(),
                          coordinate.getY(),
                          zoomLevel,
                          image);

        final BufferedImage tileImage = tmsReader.getTile(coordinate.getX(),
                                                          coordinate.getY(),
                                                          zoomLevel);

        assertTrue(bufferedImagesEqual(image, tileImage));
    }

    @Test
    public void verifyTileInsertion() throws TileStoreException, MimeTypeParseException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 4);

        final int zoomLevel = 5;
        final Coordinate<Integer> coordinate = new Coordinate<>(0, 0);

        final CoordinateReferenceSystem coordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 3857);

        final TmsWriter tmsWriter = new TmsWriter(coordinateReferenceSystem,
                                                  tmsDir,
                                                  new MimeType("image", "png"));

        final Path tilePath = tmsDir.resolve(Integer.toString(zoomLevel))
                                    .resolve(Integer.toString(coordinate.getX()))
                                    .resolve(Integer.toString(coordinate.getX()) + ".png");

        final BufferedImage img = createImage();

        tmsWriter.addTile(coordinate.getX(),
                          coordinate.getY(),
                          zoomLevel,
                          img);

        assertTrue(tilePath.toFile().exists());
    }
    
    @Test
    public void verifyTileInsertion2() throws TileStoreException, MimeTypeParseException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 4);

        final int zoomLevel = 5;
        final Coordinate<Integer> coordinate = new Coordinate<>(0, 0);
        final CrsProfile mercator = CrsProfileFactory.create("EPSG", 3857);
        
        final TmsWriter tmsWriter = new TmsWriter(mercator,
                                                  tmsDir,
                                                  new MimeType("image", "png"));

        final Path tilePath = tmsDir.resolve(Integer.toString(zoomLevel))
                                    .resolve(Integer.toString(coordinate.getX()))
                                    .resolve(Integer.toString(coordinate.getX()) + ".png");

        final BufferedImage img = createImage();
        
        CrsCoordinate crsCoordinate = new CrsCoordinate(mercator.getBounds().getMinX(), 
                                                        mercator.getBounds().getMinY(), 
                                                        mercator.getCoordinateReferenceSystem());
        
        tmsWriter.addTile(crsCoordinate,
                          zoomLevel,
                          img);

        assertTrue(tilePath.toFile().exists());
    }
    
    /**
     * Tests if TmsWriter can return the expected boundingBox for a particular tile
     * @throws MimeTypeParseException
     */
    @Test
    public void verifyTileBoundingBox() throws MimeTypeParseException
    {
        final Path tmsDir = TmsUtility.createTMSFolderGeodetic(this.tempFolder, 3);
        
        final TmsWriter tmsWriter = new TmsWriter(CrsProfileFactory.create("EPSG", 4326),
                                                  tmsDir,
                                                  new MimeType("image/png"));
        int column = 3;
        int row    = 2;
        int zoom   = 3;
        
        BoundingBox bBoxReturned = tmsWriter.getTileBoundingBox(column, row, zoom);
        
        BoundingBox bBoxExpected = getExpectedBoundingBox(column, row, zoom, new GlobalGeodeticCrsProfile());
       
        assertBoundingBoxesEqual(bBoxExpected, bBoxReturned);
    }
    
    /**
     * Tests if TmsWriter can return the expected boundingBox for a particular tile
     * @throws MimeTypeParseException
     */
    @Test
    public void verifyTileBoundingBox2() throws MimeTypeParseException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 4);
        
        final TmsWriter tmsWriter = new TmsWriter(CrsProfileFactory.create("EPSG", 3857),
                                                  tmsDir,
                                                  new MimeType("image/png"));
        int column = 15;
        int row    = 0;
        int zoom   = 4;
        
        BoundingBox bBoxReturned = tmsWriter.getTileBoundingBox(column, row, zoom);
        
        BoundingBox bBoxExpected = getExpectedBoundingBox(column, row, zoom, new SphericalMercatorCrsProfile());
       
        assertBoundingBoxesEqual(bBoxExpected, bBoxReturned);
    }
    
    /**
     * Tests if TmsWriter can return the expected boundingBox for a particular tile
     * @throws MimeTypeParseException
     */
    @Test
    public void verifyTileBoundingBox3() throws MimeTypeParseException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 2);
        
        final TmsWriter tmsWriter = new TmsWriter(CrsProfileFactory.create("EPSG", 3857),
                                                  tmsDir,
                                                  new MimeType("image/png"));
        int column = 0;
        int row    = 0;
        int zoom   = 0;
        
        BoundingBox bBoxReturned = tmsWriter.getTileBoundingBox(column, row, zoom);
        
        BoundingBox bBoxExpected = getExpectedBoundingBox(column, row, zoom, new SphericalMercatorCrsProfile());
       
        assertBoundingBoxesEqual(bBoxExpected, bBoxReturned);
    }
    
    @Test
    public void verifyTileToCrsCoordinate() throws MimeTypeParseException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 3);
        
        final TmsWriter tmsWriter = new TmsWriter(CrsProfileFactory.create("EPSG", 3857),
                                                  tmsDir,
                                                  new MimeType("image/png"));
        int column = 2;
        int row    = 7;
        int zoom   = 3;
        
        TileOrigin    corner           = TileOrigin.UpperRight;
        CrsProfile    crsProfile       = new SphericalMercatorCrsProfile();
        
        CrsCoordinate crsCoordReturned = tmsWriter.tileToCrsCoordinate (column, row, zoom, corner);
        CrsCoordinate crsCoordExpected = getExpectedCrsCoordinate((column + 1), (row + 1), zoom, crsProfile); //since it is upper right
                                                                                                              // add one to both column and row
        assertEquals(crsCoordExpected, crsCoordReturned);
    }
    
    @Test
    public void verifyTileToCrsCoordinate2() throws MimeTypeParseException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 3);
        
        final TmsWriter tmsWriter = new TmsWriter(CrsProfileFactory.create("EPSG", 3857),
                                                  tmsDir,
                                                  new MimeType("image/png"));
        int column = 2;
        int row    = 7;
        int zoom   = 3;
        
        TileOrigin    corner           = TileOrigin.UpperLeft;
        CrsProfile    crsProfile       = new SphericalMercatorCrsProfile();
        
        CrsCoordinate crsCoordReturned = tmsWriter.tileToCrsCoordinate (column, row, zoom, corner);
        CrsCoordinate crsCoordExpected = getExpectedCrsCoordinate(column, (row + 1), zoom, crsProfile); //since it is upper 
                                                                                                        // add one to row
        assertEquals(crsCoordExpected, crsCoordReturned);
    }
    
    @Test
    public void verifyTileToCrsCoordinate3() throws MimeTypeParseException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 3);
        
        final TmsWriter tmsWriter = new TmsWriter(CrsProfileFactory.create("EPSG", 3857),
                                                  tmsDir,
                                                  new MimeType("image/png"));
        int column = 2;
        int row    = 7;
        int zoom   = 3;
        
        TileOrigin    corner           = TileOrigin.LowerRight;
        CrsProfile    crsProfile       = new SphericalMercatorCrsProfile();
        
        CrsCoordinate crsCoordReturned = tmsWriter.tileToCrsCoordinate(column, row, zoom, corner);
        CrsCoordinate crsCoordExpected = getExpectedCrsCoordinate((column + 1), row, zoom, crsProfile); //since it is right 
                                                                                                        // add one to column
        assertEquals(crsCoordExpected, crsCoordReturned);
    }
    
    @Test
    public void verifyTileToCrsCoordinate4() throws MimeTypeParseException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 3);
        
        final TmsWriter tmsWriter = new TmsWriter(CrsProfileFactory.create("EPSG", 3857),
                                                  tmsDir,
                                                  new MimeType("image/png"));
        int column = 2;
        int row    = 7;
        int zoom   = 3;
        
        TileOrigin    corner           = TileOrigin.LowerLeft;
        CrsProfile    crsProfile       = new SphericalMercatorCrsProfile();
        
        CrsCoordinate crsCoordReturned = tmsWriter.tileToCrsCoordinate(column, row, zoom, corner);
        CrsCoordinate crsCoordExpected = getExpectedCrsCoordinate(column, row, zoom, crsProfile);
        
        assertEquals(crsCoordExpected, crsCoordReturned);
    }
    
    @Test
    public void verifyCrsToTileCoordinate() throws MimeTypeParseException
    {

        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 3);
        
        final TmsWriter tmsWriter = new TmsWriter(CrsProfileFactory.create("EPSG", 3857),
                                                  tmsDir,
                                                  new MimeType("image/png"),
                                                  new ImageWriteParam(null));
        int zoom = 3;
        
        Coordinate<Integer> tileCoordinateExpected = new Coordinate<>(3, 5);
        CrsCoordinate       crsCoordinate          = tmsWriter.tileToCrsCoordinate(tileCoordinateExpected.getX(), tileCoordinateExpected.getY(), zoom, TmsTileStore.Origin);
        Coordinate<Integer> tileCoordinateReturned = tmsWriter.crsToTileCoordinate(crsCoordinate, zoom);
        
        assertEquals(tileCoordinateExpected, tileCoordinateReturned);
    }
    
    @Test
    public void verifyCrsToTileCoordinate2() throws MimeTypeParseException
    {

        final Path tmsDir = TmsUtility.createTMSFolderGeodetic(this.tempFolder, 3);
        
        final TmsWriter tmsWriter = new TmsWriter(CrsProfileFactory.create("EPSG", 4326),
                                                  tmsDir,
                                                  new MimeType("image/png"),
                                                  null);
        int zoom = 2;
        
        Coordinate<Integer> tileCoordinateExpected = new Coordinate<>(0, 1);
        CrsCoordinate       crsCoordinate          = tmsWriter.tileToCrsCoordinate(tileCoordinateExpected.getX(), tileCoordinateExpected.getY(), zoom, tmsWriter.getTileOrigin());
        Coordinate<Integer> tileCoordinateReturned = tmsWriter.crsToTileCoordinate(crsCoordinate, zoom);
        
        assertEquals(tileCoordinateExpected, tileCoordinateReturned);
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void writerConstructorIllegalArgumentException() throws MimeTypeParseException
    {
        final Path tmsDir = TmsUtility.createTMSFolderGeodetic(this.tempFolder, 3);
        
        new TmsWriter(null,
                      tmsDir,
                      new MimeType("image/png"));
        
        fail("Expected an IllegalArgumentException when passing a null value for CrsProfle.");
    }
    
    @SuppressWarnings({ "unused", "static-method" })
    @Test(expected = IllegalArgumentException.class)
    public void writerConstructorIllegalArgumentException2() throws MimeTypeParseException
    {
        new TmsWriter(CrsProfileFactory.create("EPSG", 4326),
                      null,
                      new MimeType("image/png"));
        
        fail("Expected an IllegalArgumentException when passing a null value for location.");
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void writerConstructorIllegalArgumentException3() throws MimeTypeParseException, IOException
    {
        final Path badPath  = this.tempFolder.newFile().toPath();
        new TmsWriter(CrsProfileFactory.create("EPSG", 4326),
                      badPath,
                      new MimeType("image/png"));
        
        fail("Expected an IllegalArgumentException when passing a path to a file for location.");
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void writerConstructorIllegalArgumentException4()
    {
        final Path tmsDir = TmsUtility.createTMSFolderGeodetic(this.tempFolder, 3);
        
        new TmsWriter(CrsProfileFactory.create("EPSG", 3857),
                      tmsDir,
                      null);
        fail("Expected an IllegalArgumentException when OutputFormat paramter is null.");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void tileToCrsCoordinateIllegalArgumentException() throws MimeTypeParseException
    {
        final Path tmsDir = TmsUtility.createTMSFolderGeodetic(this.tempFolder, 3);
        
        final TmsWriter tmsWriter = new TmsWriter(CrsProfileFactory.create("EPSG", 4326),
                                                  tmsDir,
                                                  new MimeType("image/png"));
        tmsWriter.tileToCrsCoordinate(2, 3, 1, null);
        fail("Expected an IllegalArgumentException when passing a null value for TileOrigin corner.");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void addTileIllegalArgumentException() throws MimeTypeParseException, TileStoreException
    {
        final Path tmsDir = TmsUtility.createTMSFolderGeodetic(this.tempFolder, 3);
        
        final TmsWriter tmsWriter = new TmsWriter(CrsProfileFactory.create("EPSG", 4326),
                                                  tmsDir,
                                                  new MimeType("image/png"));
        tmsWriter.addTile(null, 3, createImage());
        fail("Expected an IllegalArgumentException when passing a null value for coordinate");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void addTileIllegalArgumentException2() throws MimeTypeParseException, TileStoreException
    {
        final Path tmsDir = TmsUtility.createTMSFolderGeodetic(this.tempFolder, 3);
        CrsProfile crs = CrsProfileFactory.create("EPSG", 4326);
        
        final TmsWriter tmsWriter = new TmsWriter(CrsProfileFactory.create("EPSG", 4326),
                                                  tmsDir,
                                                  new MimeType("image/png"));
        
        CrsCoordinate crsCoordinate = new CrsCoordinate(getExpectedCrsCoordinate(0, 2, 3, crs), crs.getCoordinateReferenceSystem());
        
        tmsWriter.addTile(crsCoordinate, 3, null);
        
        fail("Expected an IllegalArgumentException when passing a null value for image");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void addTileIllegalArgumentException3() throws MimeTypeParseException, TileStoreException
    {
        final Path tmsDir = TmsUtility.createTMSFolderGeodetic(this.tempFolder, 3);
        CrsProfile crs = CrsProfileFactory.create("EPSG", 4326);
        
        final TmsWriter tmsWriter = new TmsWriter(crs,
                                                  tmsDir,
                                                  new MimeType("image/png"));
        
        CrsCoordinate crsCoordinate = new CrsCoordinate(getExpectedCrsCoordinate(0, 2, 3, new SphericalMercatorCrsProfile()),
                                                        new SphericalMercatorCrsProfile().getCoordinateReferenceSystem());
        
        tmsWriter.addTile(crsCoordinate, 3, createImage());
        
        fail("Expected an IllegalArgumentException when passing CrsCoordinate with a different coordinateReferenceSystem than the TMS.");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void addTileIllegalArgumentException4() throws MimeTypeParseException, TileStoreException
    {
        final Path tmsDir = TmsUtility.createTMSFolderGeodetic(this.tempFolder, 3);
        
        CrsProfile crs = CrsProfileFactory.create("EPSG", 4326);
        
        final TmsWriter tmsWriter = new TmsWriter(crs,
                                                  tmsDir,
                                                  new MimeType("image/png"));
        
        
        tmsWriter.addTile(0, 0, 3, null);
        
        fail("Expected an IllegalArgumentException when passing a null value for image");
    }

    private static BoundingBox getExpectedBoundingBox(int column, int row, int zoom, CrsProfile crsProfile)
    {
        Coordinate<Double> lowerLeftCoord  = new Coordinate<>(getExpectedCrsCoordinate(column, row, zoom, crsProfile));
        
        Coordinate<Double> upperRightCoord = new Coordinate<>(getExpectedCrsCoordinate((column + 1), (row + 1), zoom, crsProfile));
        
        return new BoundingBox(lowerLeftCoord.getX(),
                               lowerLeftCoord.getY(),
                               upperRightCoord.getX(),
                               upperRightCoord.getY());
    }
    
    private static CrsCoordinate getExpectedCrsCoordinate(int column, int row, int zoom, CrsProfile crsProfile)
    {
        BoundingBox bounds         = crsProfile.getBounds();
        double      crsTileWidth   = bounds.getWidth() /Math.pow(2, zoom);
        double      crsTileHeight  = bounds.getHeight()/Math.pow(2, zoom);
        
        double  xCoord = bounds.getMinX() + (column * crsTileWidth);
        double  yCoord = bounds.getMinY() + (row    * crsTileHeight);
        
        return new CrsCoordinate(xCoord, yCoord, crsProfile.getCoordinateReferenceSystem());
    }
    
    private static boolean boundingBoxesEqual(BoundingBox bBoxExpected, BoundingBox bBoxReturned)
    {
        
        Coordinate<Double> lowerLeftExpected  = bBoxExpected.getBottomLeft();
        Coordinate<Double> upperRightExpected = bBoxExpected.getTopRight();
        
        Coordinate<Double> lowerLeftReturned  = bBoxReturned.getBottomLeft();
        Coordinate<Double> upperRightReturned = bBoxReturned.getTopRight();
        
        return lowerLeftExpected.equals(lowerLeftReturned) && upperRightExpected.equals(upperRightReturned);
    }
    
    private static void assertBoundingBoxesEqual(BoundingBox bBoxExpected, BoundingBox bBoxReturned)
    {
        assertTrue(String.format("The BoundingBox returned is not the boundingBox expected.\nActual min/max x/y: (%f, %f, %f, %f)\nReturned min/max x/y: (%f, %f, %f, %f).",
                                  bBoxExpected.getMinX(),
                                  bBoxExpected.getMinY(),
                                  bBoxExpected.getMaxX(),
                                  bBoxExpected.getMaxY(),
                                  bBoxReturned.getMinX(),
                                  bBoxReturned.getMinY(),
                                  bBoxReturned.getMaxX(),
                                  bBoxReturned.getMaxY()),
                 boundingBoxesEqual(bBoxExpected, bBoxReturned));
    }

    private static BufferedImage createImage()
    {
        final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = img.createGraphics();
        graphics.setPaint(new Color(255, 0, 0));
        graphics.fillRect(0, 0, 256, 256);
        return img;
    }

    private static boolean bufferedImagesEqual(final BufferedImage img1, final BufferedImage img2)
    {
        if(img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight())
        {
            return false;
        }

        for(int x = 0; x < img1.getWidth(); x++)
        {
            for(int y = 0; y < img1.getHeight(); y++)
            {
                if(img1.getRGB(x, y) != img2.getRGB(x, y))
                {
                    return false;
                }
            }
        }

        return true;
    }
}
