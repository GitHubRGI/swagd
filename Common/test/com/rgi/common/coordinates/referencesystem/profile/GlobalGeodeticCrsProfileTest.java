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

package com.rgi.common.coordinates.referencesystem.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.GlobalGeodeticCrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings("static-method")
public class GlobalGeodeticCrsProfileTest
{
    /**
     * Tests if GlobalGeodeticCrsProfile will throw an IllegalArgumentException
     * when null is passed as one of the parameters to the method
     * crsToTileCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void crsToTileCoordinateIllegalArgumentException()
    {
        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        globalCrs.crsToTileCoordinate(null, null, new TileMatrixDimensions(4,5), TileOrigin.LowerLeft);
        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException when coordinate is null for crsToTileCoordinate.");
    }

    /**
     * Tests if GlobalGeodeticCrsProfile will throw an IllegalArgumentException
     * when null is passed as one of the parameters to the method
     * crsToTileCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void crsToTileCoordinateIllegalArgumentException2()
    {
        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        globalCrs.crsToTileCoordinate(new CrsCoordinate(4.0,2.0, "epsg", 4326), new BoundingBox(0.0, 0.0, 0.0, 0.0), null, TileOrigin.LowerLeft);
        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException when dimensions is null for crsToTileCoordinate.");
    }

    /**
     * Tests if GlobalGeodeticCrsProfile will throw an IllegalArgumentException
     * when null is passed as one of the parameters to the method
     * crsToTileCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void crsToTileCoordinateIllegalArgumentException3()
    {
        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        globalCrs.crsToTileCoordinate(new CrsCoordinate(4.0,2.0, "epsg", 4326), new BoundingBox(0.0, 0.0, 0.0, 0.0), new TileMatrixDimensions(7, 5), null);
        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException when tileOrigin is null for crsToTileCoordinate.");
    }

    /**
     * Tests if it will throw an illegalArgumentException
     * when a user tries to pass in a different crs than the one
     * being used (global geodetic)
     */
    @Test(expected = IllegalArgumentException.class)
    public void crsToTileCoordinateIllegalArgumentException4()
    {
        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        globalCrs.crsToTileCoordinate(new CrsCoordinate(4.0,2.0, "epsg", 9999), new BoundingBox(0.0, 0.0, 0.0, 0.0), new TileMatrixDimensions(7, 5), TileOrigin.LowerLeft);
        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException when crs is different from the global geodetic in crs to tile coordinate.");
    }

    /**
     * Tests if GlobalGeodetic tileToCrsCoordinate will throw
     * an illegal argument exception when the row value is negative
     */
    @Test(expected = IllegalArgumentException.class)
    public void tileToCrsCoordinateIllegalArgumentException()
    {
        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        globalCrs.tileToCrsCoordinate(8, -5, new BoundingBox(0.0, 0.0, 0.0, 0.0), new TileMatrixDimensions(100,100), TileOrigin.LowerLeft);
        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException when row is negative in tileToCrsCoordinate.");
    }

    /**
    * Tests if GlobalGeodetic tileToCrsCoordinate will throw
    * an illegal argument exception when the column value is negative
    */
   @Test(expected = IllegalArgumentException.class)
   public void tileToCrsCoordinateIllegalArgumentException2()
   {
       final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
       globalCrs.tileToCrsCoordinate(-8, 5, new BoundingBox(0.0, 0.0, 0.0, 0.0), new TileMatrixDimensions(100,100), TileOrigin.LowerLeft);
       fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException when column is negative in tileToCrsCoordinate.");
   }

   /**
     * Tests if GlobalGeodetic tileToCrsCoordinate will throw
     * an illegal argument exception when the tile matrix dimensions is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void tileToCrsCoordinateIllegalArgumentException3()
    {
        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        globalCrs.tileToCrsCoordinate(8, 5, new BoundingBox(0.0, 0.0, 0.0, 0.0), null, TileOrigin.LowerLeft);
        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException when the tile matrix dimensions is null in tileToCrsCoordinate.");
    }

    /**
     * Tests if GlobalGeodetic tileToCrsCoordinate will throw
     * an illegal argument exception when the tileOrigin is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void tileToCrsCoordinateIllegalArgumentException4()
    {
        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        globalCrs.tileToCrsCoordinate(8, 5, new BoundingBox(0.0, 0.0, 0.0, 0.0), new TileMatrixDimensions(100,100), null);
        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException when the tile Origin is null in tileToCrsCoordinate.");
    }


    /**
     * Tests if the values is returned when using the method to global geodetic
     */
    @Test
    public void toGlobalGeodetic()
    {
        final Coordinate<Double> expectedCoordinate = new Coordinate<>(-100.0, 85.5);

        final Coordinate<Double> returnedCoordinate = (new GlobalGeodeticCrsProfile()).toGlobalGeodetic(expectedCoordinate);
        assertEquals("The method toGlobalGeodetic did not return the expected coordinate.",expectedCoordinate, returnedCoordinate);
    }
    /**
     * Tests if crs to tile coordinate with a lowerleft origin
     * returns the correct coordinates
     */
    @Test
    public void globalGeodeticCrsProfileLowerLeftCrsToTileCoordinate()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(150.0,
                                                                  50.0,
                                                                  "epsg",
                                                                  4326);//upper right tile

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(5, 3);
        final TileOrigin           tileOrigin = TileOrigin.LowerLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 4 y: 2",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                   newCoordinate.getX() == 4 && newCoordinate.getY() == 2);
    }

    /**
     * Tests if GlobalGeodeticCrsProfile throws an IllegalArgumentException
     * when the crs coordinate is the upperLeftCorner of the boundingbox and
     * since the tileOrigin is LowerLeft, this tile lies outside the bounds
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileLowerLeftOriginCrsToTileCoordinateUpperLeftCorner()
    {
        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinX(), //expect upper left tile
                                                            globalCrs.getBounds().getMaxY() ,
                                                            "epsg",
                                                            4326);

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.LowerLeft;

        globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected Global Geodetic Crs Profile to throw an IllegalArgumentException since the crsCoordinate lies out of the bounds.");
    }

    /**
     * Tests if using the maxX and maxY value on lower left tile origin tile
     * would throw an illegalArgumentException because tile requested would
     * be outside the bounds
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileLowerLeftCrsToTileCoordinateEdgeCaseUpperRightCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxX(), //expect upper right corner
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxY() ,
                                                            "epsg",
                                                            4326);
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.LowerLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }

    /**
     * Tests if crs to tile coordinate would return the lower left tile
     * with the correct values
     */
    @Test
    public void globalGeodeticCrsProfileLowerLeftCrsToTileCoordinateEdgeCaseLowerLeftCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinX(), //expect lower left corner
                                                            GlobalGeodeticCrsProfile.Bounds.getMinY() ,
                                                            "epsg",
                                                            4326);

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.LowerLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 0 y: 0",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                   newCoordinate.getX() == 0 && newCoordinate.getY() == 0);
    }

    /**
     * Tests if using the maxX value on lower left tile origin tile
     * would throw an illegalArgumentException because tile requested would
     * be outside the bounds
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileLowerLeftCrsToTileCoordinateEdgeCaseLowerRightCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxX(), //expect lower right corner
                                                            GlobalGeodeticCrsProfile.Bounds.getMinY() ,
                                                            "epsg",
                                                            4326);

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.LowerLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }

    /**
     * Tests if the crsToTileCoordiante can give an accurate tile coordinate
     * when the crs coordiante lies at the center of four tiles
     */
    @Test
    public void globalGeodeticCrsProfileLowerLeftOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);

        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinX() + (4*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMinY() + (5*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            4326);

        final TileOrigin           tileOrigin = TileOrigin.LowerLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 4 y: 5",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 4 && newCoordinate.getY() == 5);
    }

    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * siting side by side
     */
    @Test
    public void globalGeodeticCrsProfileLowerLeftOriginCrsToTileCoordinateEdgeCaseBetweenTilesSideBySide()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);

        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinX() + (2*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMinY() + (3.5*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            4326);

        final TileOrigin           tileOrigin = TileOrigin.LowerLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 2 y: 3",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 2 && newCoordinate.getY() == 3);
    }

    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * on top of one another
     */
    @Test
    public void globalGeodeticCrsProfileLowerLeftOriginCrsToTileCoordinateEdgeCaseBetweenTilesUpAndDown()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinX() + (7.5*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMinY() + (5*  (GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            4326);

        final TileOrigin           tileOrigin = TileOrigin.LowerLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 7 y: 5",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 7 && newCoordinate.getY() == 5);
    }




    /**
     * Tests with more divisions if crs to tile coordinate can
     * return the correct coordinate
     */
    @Test
    public void globalGeodeticCrsProfileUpperLeftCrsToTileCoordinate()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(140.0,
                                                            40.0,
                                                            "epsg",
                                                            4326);//should be at 1,8

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.UpperLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 8 y: 1",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                   newCoordinate.getX() == 8 && newCoordinate.getY() == 1);
    }

    /**
     * Tests if using the maxX value on upper left tile origin tile
     * would throw an illegalArgumentException because tile requested would
     * be outside the bounds
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileUpperLeftCrsToTileCoordinateEdgeCaseUpperRightCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxX(), //expect upper right corner
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxY() ,
                                                            "epsg",
                                                            4326);
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.UpperLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }

    /**
     * asks for upper left tile (at the very origin of the tile at (0,0))
     * and tests if it returns correct tile
     */
    @Test
    public void globalGeodeticCrsProfileUpperLeftCrsToTileCoordinateEdgeCaseUpperLeftCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinX(), //expect upper left tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxY() ,
                                                            "epsg",
                                                            4326);

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.UpperLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 0 y: 0",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                   newCoordinate.getX() == 0 && newCoordinate.getY() == 0);
    }

    /**
     * Tests if using the minY value on upper left tile origin tile
     * would throw an illegalArgumentException because tile requested would
     * be outside the bounds
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileUpperLeftCrsToTileCoordinateEdgeCaseLowerLeftCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinX(), //expect lower left tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMinY() ,
                                                            "epsg",
                                                            4326);

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.UpperLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }

    /**
     * Tests if using the minY and maxX value on upper left tile origin tile
     * would throw an illegalArgumentException because tile requested would
     * be outside the bounds
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileUpperLeftCrsToTileCoordinateEdgeCaseLowerRightCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxX(), //expect lower right tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMinY() ,
                                                            "epsg",
                                                            4326);

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.UpperLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }

    /**
     * Tests if the crsToTileCoordiante can give an accurate tile coordinate
     * when the crs coordiante lies at the center of four tiles
     */
    @Test
    public void globalGeodeticCrsProfileUpperLeftOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);

        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinX()+(7*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxY()-(5*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            4326);

        final TileOrigin           tileOrigin = TileOrigin.UpperLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 7 y: 5",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 7 && newCoordinate.getY() == 5);
    }

    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * siting side by side
     */
    @Test
    public void globalGeodeticCrsProfileUpperLeftOriginCrsToTileCoordinateEdgeCaseBetweenTilesSideBySide()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);

        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinX()+(7*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxY()-(5.5*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            4326);

        final TileOrigin           tileOrigin = TileOrigin.UpperLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 7 y: 5",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 7 && newCoordinate.getY() == 5);
    }

    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * on top of one another
     */
    @Test
    public void globalGeodeticCrsProfileUpperLeftOriginCrsToTileCoordinateEdgeCaseBetweenTilesUpAndDown()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinX() + (7.5*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxY() - (5*  (GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            4326);

        final TileOrigin           tileOrigin = TileOrigin.UpperLeft;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 7 y: 5",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 7 && newCoordinate.getY() == 5);
    }

    /**
     * Tests if globalGeodetic can retrieve the correct
     * coordinate for a tile with an origin of upperRight
     */
    @Test
    public void globalGeodeticCrsProfileUpperRightOriginCrsToTileCoordinate()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(120.0,
                                                            -80.0,
                                                            "epsg",
                                                            4326);//upper right tile

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.UpperRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 1 y: 6",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                                 newCoordinate.getX() == 1 && newCoordinate.getY() == 6);
    }


    /**
     * Tests if globalGeodetic can return the correct coordinate
     * with a tile origin of upper right
     */
    @Test
    public void globalGeodeticCrsProfileUpperRightCrsToTileCoordinateEdgeCaseUpperRightCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxX(), //expect upper right corner
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxY() ,
                                                            "epsg",
                                                            4326);
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.UpperRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 0 y: 0",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                                 newCoordinate.getX() == 0 && newCoordinate.getY() == 0);
    }

    /**
     * Tests if using the minY and minX value on upper Right tile origin tile
     * would throw an illegalArgumentException because tile requested would
     * be outside the bounds
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileUpperRightCrsToTileCoordinateEdgeCaseUpperLeftCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinX(), //expect upper left tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxY() ,
                                                            "epsg",
                                                            4326);

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.UpperRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }

    /**
     * Tests if using the minY and minX value on upper Right tile origin tile
     * would throw an illegalArgumentException because tile requested would
     * be outside the bounds
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileUpperRightCrsToTileCoordinateEdgeCaseLowerLeftCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinX(), //expect lower left tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMinY() ,
                                                            "epsg",
                                                            4326);

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.UpperRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }

    /**
     * Tests if using the minY value on upper Right tile origin tile
     * would throw an illegalArgumentException because tile requested would
     * be outside the bounds
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileUpperRightCrsToTileCoordinateEdgeCaseLowerRightCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxX(), //expect lower right tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMinY() ,
                                                            "epsg",
                                                            4326);

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.UpperRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }

    /**
     * Tests if the crsToTileCoordiante can give an accurate tile coordinate
     * when the crs coordiante lies at the center of four tiles
     */
    @Test
    public void globalGeodeticCrsProfileUpperRightOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);

        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxX()-(8*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxY()-(2*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            4326);

        final TileOrigin           tileOrigin = TileOrigin.UpperRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 8 y: 2",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 8 && newCoordinate.getY() == 2);
    }

    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * siting side by side
     */
    @Test
    public void globalGeodeticCrsProfileUpperRightOriginCrsToTileCoordinateEdgeCaseBetweenTilesSideBySide()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);

        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxX()-(1*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxY()-(5.5*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            4326);

        final TileOrigin           tileOrigin = TileOrigin.UpperRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 1 y: 5",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 1 && newCoordinate.getY() == 5);
    }

    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * on top of one another
     */
    @Test
    public void globalGeodeticCrsProfileUpperRightOriginCrsToTileCoordinateEdgeCaseBetweenTilesUpAndDown()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxX() - (7.5*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxY() - (5*  (GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            4326);

        final TileOrigin           tileOrigin = TileOrigin.UpperRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 7 y: 5",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 7 && newCoordinate.getY() == 5);
    }

    /**
     * Tests if globalGeodetic can retrieve the correct
     * coordinate for a tile with an origin of upperRight
     */
    @Test
    public void globalGeodeticCrsProfileLowerRightOriginCrsToTileCoordinate()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(120.0,
                                                            -80.0,
                                                            "epsg",
                                                            4326);//upper right tile

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.LowerRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 1 y: 0",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                                 newCoordinate.getX() == 1 && newCoordinate.getY() == 0);
    }


    /**
     * Tests if using the maxY value on lower Right tile origin tile
     * would throw an illegalArgumentException because tile requested would
     * be outside the bounds
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileLowerRightCrsToTileCoordinateEdgeCaseUpperRightCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxX(), //expect upper right corner
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxY() ,
                                                            "epsg",
                                                            4326);
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.LowerRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }

    /**
     * Tests if using the minX and maxY value on lower Right tile origin tile
     * would throw an illegalArgumentException because tile requested would
     * be outside the bounds
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileLowerRightCrsToTileCoordinateEdgeCaseUpperLeftCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinX(), //expect upper left tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxY() ,
                                                            "epsg",
                                                            4326);

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.LowerRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }

    /**
     * Tests if globalGeodeticCrsProfile Would return the correct tile coordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileLowerRightCrsToTileCoordinateEdgeCaseLowerLeftCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinX(), //expect lower left tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMinY() ,
                                                            "epsg",
                                                            4326);

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.LowerRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }

    /**
     * Tests if using the minY value on upper Right tile origin tile
     * would throw an illegalArgumentException because tile requested would
     * be outside the bounds
     */
    @Test
    public void globalGeodeticCrsProfileLowerRightCrsToTileCoordinateEdgeCaseLowerRightCorner()
    {
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxX(), //expect lower right tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMinY() ,
                                                            "epsg",
                                                            4326);

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final TileOrigin           tileOrigin = TileOrigin.LowerRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 0 y: 0",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                                 newCoordinate.getX() == 0 && newCoordinate.getY() == 0);
    }

    /**
     * Tests if the crsToTileCoordiante can give an accurate tile coordinate
     * when the crs coordiante lies at the center of four tiles
     */
    @Test
    public void globalGeodeticCrsProfileLowerRightOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);

        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxX()-(1*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMinY()+(2*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            4326);

        final TileOrigin           tileOrigin = TileOrigin.LowerRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 1 y: 2",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 1 && newCoordinate.getY() == 2);
    }

    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * siting side by side
     */
    @Test
    public void globalGeodeticCrsProfileLowerRightOriginCrsToTileCoordinateEdgeCaseBetweenTilesSideBySide()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);

        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxX()-(3*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMinY()+(4.75*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            4326);

        final TileOrigin           tileOrigin = TileOrigin.LowerRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 3 y: 4",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 3 && newCoordinate.getY() == 4);
    }

    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * on top of one another
     */
    @Test
    public void globalGeodeticCrsProfileLowerRightOriginCrsToTileCoordinateEdgeCaseBetweenTilesUpAndDown()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 7);
        final CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxX() - (0.5*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMinY() + (4*  (GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            4326);

        final TileOrigin           tileOrigin = TileOrigin.LowerRight;

        final GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        final Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, globalCrs.getBounds(), dimensions, tileOrigin);

        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 0 y: 4",
                                 newCoordinate.getX(),
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 0 && newCoordinate.getY() == 4);
    }

    /**
     * Tests if we use our tileToCrsCoordinate and use those values to put into crsToTileCoordinate
     * then we would get the same tileCoordiante that we started with.
     */
    @Test
    public void globalGeodeticCrsProfileTileCoordinateToCrsBackToTileCoordinate()
    {
        final GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        final TileMatrixDimensions     dimensions    = new TileMatrixDimensions(20, 13);
        final TileOrigin origin = TileOrigin.UpperLeft;
        final Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(7,3);

        final CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getX(), originalTileCoordinate.getY(), globalCrs.getBounds(), dimensions, origin);
        final Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, globalCrs.getBounds(), dimensions, origin);

        assertEquals(String.format("The tile coordinate did not return as expected.\nExpected Tile Coordinate: (x,y)-> (%d,%d)"
                                    + "\nActual Tile Coordinate: (x,y)-> (%d,%d)\nActual CrsCoordinate: (x,y)->(%f, %f)",
                                   originalTileCoordinate.getX(),
                                   originalTileCoordinate.getY(),
                                   returnedTileCoordinate.getX(),
                                   returnedTileCoordinate.getY(),
                                   returnedCrsCoordiante.getX(),
                                   returnedCrsCoordiante.getY()), originalTileCoordinate, returnedTileCoordinate);
    }

    /**
     * Tests if tile is in upperRight and can be transformed from tile coordinate to crs back to
     * tile coordinate and give back the original tile coordinate values
     */
    @Test
    public void globalGeodeticCrsProfileTileCoordinateToCrsBackToTileCoordinate2()
    {
        final GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        final TileMatrixDimensions     dimensions    = new TileMatrixDimensions(11, 20);
        final TileOrigin origin = TileOrigin.UpperRight;
        final Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(9,15);

        final CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getX(), originalTileCoordinate.getY(), globalCrs.getBounds(), dimensions, origin);
        final Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, globalCrs.getBounds(), dimensions, origin);

        assertEquals(String.format("The tile coordinate did not return as expected.\nExpected Tile Coordinate: (x,y)-> (%d,%d)"
                                    + "\nActual Tile Coordinate: (x,y)-> (%d,%d)\nActual CrsCoordinate: (x,y)->(%f, %f)",
                                   originalTileCoordinate.getX(),
                                   originalTileCoordinate.getY(),
                                   returnedTileCoordinate.getX(),
                                   returnedTileCoordinate.getY(),
                                   returnedCrsCoordiante.getX(),
                                   returnedCrsCoordiante.getY()), originalTileCoordinate, returnedTileCoordinate);

    }

    /**
     * Tests if tile is in upperRight and can be transformed from tile coordinate to crs back to
     * tile coordinate and give back the original tile coordinate values
     */
    @Test
    public void globalGeodeticCrsProfileTileCoordinateToCrsBackToTileCoordinate3()
    {
        final GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        final TileMatrixDimensions     dimensions    = new TileMatrixDimensions(73, 103);
        final TileOrigin origin = TileOrigin.LowerLeft;
        final Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(67,24);

        final CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getX(), originalTileCoordinate.getY(), globalCrs.getBounds(), dimensions, origin);
        final Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, globalCrs.getBounds(), dimensions, origin);

        assertEquals(String.format("The tile coordinate did not return as expected.\nExpected Tile Coordinate: (x,y)-> (%d,%d)"
                                    + "\nActual Tile Coordinate: (x,y)-> (%d,%d)\nActual CrsCoordinate: (x,y)->(%f, %f)",
                                   originalTileCoordinate.getX(),
                                   originalTileCoordinate.getY(),
                                   returnedTileCoordinate.getX(),
                                   returnedTileCoordinate.getY(),
                                   returnedCrsCoordiante.getX(),
                                   returnedCrsCoordiante.getY()),
                    originalTileCoordinate,
                    returnedTileCoordinate);

    }


    /**
     * Tests if tile is in upperRight and can be transformed from tile coordinate to crs back to
     * tile coordinate and give back the original tile coordinate values
     */
    @Test
    public void globalGeodeticCrsProfileTileCoordinateToCrsBackToTileCoordinate4()
    {
        final GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        final TileMatrixDimensions     dimensions    = new TileMatrixDimensions(73, 103);
        final TileOrigin origin = TileOrigin.LowerRight;
        final Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(32,98);

        final CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getX(), originalTileCoordinate.getY(), globalCrs.getBounds(), dimensions, origin);
        final Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, globalCrs.getBounds(), dimensions, origin);

        assertEquals(String.format("The tile coordinate did not return as expected.\nExpected Tile Coordinate: (x,y)-> (%d,%d)"
                                    + "\nActual Tile Coordinate: (x,y)-> (%d,%d)\nActual CrsCoordinate: (x,y)->(%f, %f)",
                                   originalTileCoordinate.getX(),
                                   originalTileCoordinate.getY(),
                                   returnedTileCoordinate.getX(),
                                   returnedTileCoordinate.getY(),
                                   returnedCrsCoordiante.getX(),
                                   returnedCrsCoordiante.getY()),
                    originalTileCoordinate,
                    returnedTileCoordinate);

    }

    /**
     * Tests if the transformation from tile to crs back to tile can work if given
     * a corner tile
     */
    @Test
    public void globalGeodeticCrsProfileTileCoordinateToCrsBackToTileCoordinateEdgeCase1()
    {
        final GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        final TileMatrixDimensions     dimensions    = new TileMatrixDimensions(73, 103);
        final TileOrigin origin = TileOrigin.UpperRight;
        final Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(0,0);

        final CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getX(), originalTileCoordinate.getY(), globalCrs.getBounds(), dimensions, origin);
        final Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, globalCrs.getBounds(), dimensions, origin);

        assertEquals(String.format("The tile coordinate did not return as expected.\nExpected Tile Coordinate: (x,y)-> (%d,%d)"
                                    + "\nActual Tile Coordinate: (x,y)-> (%d,%d)\nActual CrsCoordinate: (x,y)->(%f, %f)",
                                   originalTileCoordinate.getX(),
                                   originalTileCoordinate.getY(),
                                   returnedTileCoordinate.getX(),
                                   returnedTileCoordinate.getY(),
                                   returnedCrsCoordiante.getX(),
                                   returnedCrsCoordiante.getY()),
                    originalTileCoordinate,
                    returnedTileCoordinate);
    }

    /**
     * Tests if the transformation from tile to crs back to tile can work if given
     * a corner tile
     */
    @Test
    public void globalGeodeticCrsProfileTileCoordinateToCrsBackToTileCoordinateEdgeCase2()
    {
        final GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        final TileMatrixDimensions     dimensions    = new TileMatrixDimensions(73, 103);
        final TileOrigin origin = TileOrigin.UpperRight;
        final Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(72,102);

        final CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getX(), originalTileCoordinate.getY(), globalCrs.getBounds(), dimensions, origin);
        final Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, globalCrs.getBounds(), dimensions, origin);

        assertEquals(String.format("The tile coordinate did not return as expected.\nExpected Tile Coordinate: (x,y)-> (%d,%d)"
                                    + "\nActual Tile Coordinate: (x,y)-> (%d,%d)\nActual CrsCoordinate: (x,y)->(%f, %f)",
                                   originalTileCoordinate.getX(),
                                   originalTileCoordinate.getY(),
                                   returnedTileCoordinate.getX(),
                                   returnedTileCoordinate.getY(),
                                   returnedCrsCoordiante.getX(),
                                   returnedCrsCoordiante.getY()),
                    originalTileCoordinate,
                    returnedTileCoordinate);
    }

    /**
     * Tests if the transformation from tile to crs back to tile can work if given
     * a corner tile
     */
    @Test
    public void globalGeodeticCrsProfileTileCoordinateToCrsBackToTileCoordinateCenter()
    {
        final GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        final TileMatrixDimensions     dimensions    = new TileMatrixDimensions(10, 10);
        final TileOrigin origin = TileOrigin.UpperRight;
        final Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(5,5);

        final CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getX(), originalTileCoordinate.getY(), globalCrs.getBounds(), dimensions, origin);
        final Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, globalCrs.getBounds(), dimensions, origin);

        assertEquals(String.format("The tile coordinate did not return as expected.\nExpected Tile Coordinate: (x,y)-> (%d,%d)"
                                    + "\nActual Tile Coordinate: (x,y)-> (%d,%d)\nActual CrsCoordinate: (x,y)->(%f, %f)",
                                   originalTileCoordinate.getX(),
                                   originalTileCoordinate.getY(),
                                   returnedTileCoordinate.getX(),
                                   returnedTileCoordinate.getY(),
                                   returnedCrsCoordiante.getX(),
                                   returnedCrsCoordiante.getY()),
                    originalTileCoordinate,
                    returnedTileCoordinate);
    }

    /**
     * Tests if the transformation from tile to crs back to tile can work if given
     * a corner tile
     */
    @Test
    public void globalGeodeticCrsProfileTileCoordinateToCrsBackToTileCoordinateEdgeCase3()
    {
        final GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        final TileMatrixDimensions     dimensions    = new TileMatrixDimensions(10, 10);
        final TileOrigin origin = TileOrigin.UpperRight;
        final Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(5,6);

        final CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getX(), originalTileCoordinate.getY(), globalCrs.getBounds(), dimensions, origin);
        final Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, globalCrs.getBounds(), dimensions, origin);

        assertEquals(String.format("The tile coordinate did not return as expected.\nExpected Tile Coordinate: (x,y)-> (%d,%d)"
                                    + "\nActual Tile Coordinate: (x,y)-> (%d,%d)\nActual CrsCoordinate: (x,y)->(%f, %f)",
                                   originalTileCoordinate.getX(),
                                   originalTileCoordinate.getY(),
                                   returnedTileCoordinate.getX(),
                                   returnedTileCoordinate.getY(),
                                   returnedCrsCoordiante.getX(),
                                   returnedCrsCoordiante.getY()),
                    originalTileCoordinate,
                    returnedTileCoordinate);
    }

    /**
     * Tests if the transformation from tile to crs back to tile can work if given
     * a corner tile
     */
    @Test
    public void globalGeodeticCrsProfileTileCoordinateToCrsBackToTileCoordinateEdgeCase4()
    {
        final GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        final TileMatrixDimensions     dimensions    = new TileMatrixDimensions(10, 10);
        final TileOrigin origin = TileOrigin.UpperRight;
        final Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(6,5);

        final CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getX(), originalTileCoordinate.getY(), globalCrs.getBounds(), dimensions, origin);
        final Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, globalCrs.getBounds(), dimensions, origin);

        assertEquals(String.format("The tile coordinate did not return as expected.\nExpected Tile Coordinate: (x,y)-> (%d,%d)"
                                    + "\nActual Tile Coordinate: (x,y)-> (%d,%d)\nActual CrsCoordinate: (x,y)->(%f, %f)",
                                   originalTileCoordinate.getX(),
                                   originalTileCoordinate.getY(),
                                   returnedTileCoordinate.getX(),
                                   returnedTileCoordinate.getY(),
                                   returnedCrsCoordiante.getX(),
                                   returnedCrsCoordiante.getY()),
                    originalTileCoordinate,
                    returnedTileCoordinate);
    }
}
