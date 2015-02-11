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

package com.rgi.common.coordinates.referencesystem.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.GlobalGeodeticCrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;

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
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        globalCrs.crsToTileCoordinate(null, new TileMatrixDimensions(5,4), TileOrigin.LowerLeft);
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
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        globalCrs.crsToTileCoordinate(new CrsCoordinate(2.0,4.0, "epsg", 4326), null, TileOrigin.LowerLeft);
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
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        globalCrs.crsToTileCoordinate(new CrsCoordinate(2.0,4.0, "epsg", 4326), new TileMatrixDimensions(5, 7), null);
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
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        globalCrs.crsToTileCoordinate(new CrsCoordinate(2.0,4.0, "epsg", 9999), new TileMatrixDimensions(5, 7), TileOrigin.LowerLeft);
        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException when crs is different from the global geodetic in crs to tile coordinate.");
    }
    
    /**
     * Tests if GlobalGeodetic tileToCrsCoordinate will throw
     * an illegal argument exception when the row value is negative
     */
    @Test(expected = IllegalArgumentException.class)
    public void tileToCrsCoordinateIllegalArgumentException()
    {
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        globalCrs.tileToCrsCoordinate(-5, 8, new TileMatrixDimensions(100,100), TileOrigin.LowerLeft);
        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException when row is negative in tileToCrsCoordinate.");
    }
    
    /**
    * Tests if GlobalGeodetic tileToCrsCoordinate will throw
    * an illegal argument exception when the column value is negative
    */
   @Test(expected = IllegalArgumentException.class)
   public void tileToCrsCoordinateIllegalArgumentException2()
   {
       GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
       globalCrs.tileToCrsCoordinate(5, -8, new TileMatrixDimensions(100,100), TileOrigin.LowerLeft);
       fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException when column is negative in tileToCrsCoordinate.");
   }
   
   /**
     * Tests if GlobalGeodetic tileToCrsCoordinate will throw
     * an illegal argument exception when the tile matrix dimensions is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void tileToCrsCoordinateIllegalArgumentException3()
    {
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        globalCrs.tileToCrsCoordinate(5, 8, null, TileOrigin.LowerLeft);
        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException when the tile matrix dimensions is null in tileToCrsCoordinate.");
    }
    
    /**
     * Tests if GlobalGeodetic tileToCrsCoordinate will throw
     * an illegal argument exception when the tileOrigin is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void tileToCrsCoordinateIllegalArgumentException4()
    {
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        globalCrs.tileToCrsCoordinate(5, 8, new TileMatrixDimensions(100,100), null);
        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException when the tile Origin is null in tileToCrsCoordinate.");
    }
    
   
    @Test
    public void toGlobalGeodetic()
    {
        Coordinate<Double> expectedCoordinate = new Coordinate<>(85.5, -100.0);
        
        Coordinate<Double> returnedCoordinate = (new GlobalGeodeticCrsProfile()).toGlobalGeodetic(expectedCoordinate);
        assertEquals("The method toGlobalGeodetic did not return the expected coordinate.",expectedCoordinate, returnedCoordinate);
    }
    /**
     * Tests if crs to tile coordinate with a lowerleft origin 
     * returns the correct coordinates
     */
    @Test
    public void globalGeodeticCrsProfileLowerLeftCrsToTileCoordinate()
    {
        CrsCoordinate        coordinate = new CrsCoordinate(50.0, 
                                                            150.0, 
                                                            "epsg", 
                                                            4326);//upper right tile
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(3, 5);
        TileOrigin           tileOrigin = TileOrigin.LowerLeft;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        CrsCoordinate        coordinate = new CrsCoordinate(globalCrs.getBounds().getMaxY(), //expect upper left tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMinX() , 
                                                            "epsg", 
                                                            4326);

        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.LowerLeft;
        
        globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxY(), //expect upper right corner
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxX() , 
                                                            "epsg", 
                                                            4326);
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.LowerLeft;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }
    
    /**
     * Tests if crs to tile coordinate would return the lower left tile
     * with the correct values
     */
    @Test
    public void globalGeodeticCrsProfileLowerLeftCrsToTileCoordinateEdgeCaseLowerLeftCorner()
    {
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinY(), //expect lower left corner
                                                            GlobalGeodeticCrsProfile.Bounds.getMinX() , 
                                                            "epsg", 
                                                            4326);
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.LowerLeft;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinY(), //expect lower right corner
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxX() , 
                                                            "epsg", 
                                                            4326);
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.LowerLeft;

        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);

        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }
    
    /**
     * Tests if the crsToTileCoordiante can give an accurate tile coordinate
     * when the crs coordiante lies at the center of four tiles
     */
    @Test
    public void globalGeodeticCrsProfileLowerLeftOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinY() + (5*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMinX() + (4*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            4326);
        
        TileOrigin           tileOrigin = TileOrigin.LowerLeft;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinY() + (3.5*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMinX() + (2*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            4326);
        
        TileOrigin           tileOrigin = TileOrigin.LowerLeft;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinY() + (5*  (GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMinX() + (7.5*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            4326);
        
        TileOrigin           tileOrigin = TileOrigin.LowerLeft;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(40.0, 
                                                            140.0, 
                                                            "epsg", 
                                                            4326);//should be at 1,8
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.UpperLeft;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxY(), //expect upper right corner
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxX() , 
                                                            "epsg", 
                                                            4326);
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.UpperLeft;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }
    
    /**
     * asks for upper left tile (at the very origin of the tile at (0,0))
     * and tests if it returns correct tile
     */
    @Test
    public void globalGeodeticCrsProfileUpperLeftCrsToTileCoordinateEdgeCaseUpperLeftCorner()
    {
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxY(), //expect upper left tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMinX() , 
                                                            "epsg", 
                                                            4326);
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.UpperLeft;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinY(), //expect lower left tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMinX() , 
                                                            "epsg", 
                                                            4326);
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.UpperLeft;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinY(), //expect lower right tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxX() , 
                                                            "epsg", 
                                                            4326);
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.UpperLeft;

        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);

        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }
    
    /**
     * Tests if the crsToTileCoordiante can give an accurate tile coordinate
     * when the crs coordiante lies at the center of four tiles
     */
    @Test
    public void globalGeodeticCrsProfileUpperLeftOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxY()-(5*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMinX()+(7*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            4326);
        
        TileOrigin           tileOrigin = TileOrigin.UpperLeft;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxY()-(5.5*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMinX()+(7*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            4326);
        
        TileOrigin           tileOrigin = TileOrigin.UpperLeft;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxY() - (5*  (GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMinX() + (7.5*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            4326);
        
        TileOrigin           tileOrigin = TileOrigin.UpperLeft;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(-80.0, 
                                                            120.0, 
                                                            "epsg", 
                                                            4326);//upper right tile

        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.UpperRight;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxY(), //expect upper right corner
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxX() , 
                                                            "epsg", 
                                                            4326);
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.UpperRight;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxY(), //expect upper left tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMinX() , 
                                                            "epsg", 
                                                            4326);
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.UpperRight;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinY(), //expect lower left tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMinX() , 
                                                            "epsg", 
                                                            4326);
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.UpperRight;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinY(), //expect lower right tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxX() , 
                                                            "epsg", 
                                                            4326);
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.UpperRight;

        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);

        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }
    
    /**
     * Tests if the crsToTileCoordiante can give an accurate tile coordinate
     * when the crs coordiante lies at the center of four tiles
     */
    @Test
    public void globalGeodeticCrsProfileUpperRightOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxY()-(2*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxX()-(8*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            4326);
        
        TileOrigin           tileOrigin = TileOrigin.UpperRight;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxY()-(5.5*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxX()-(1*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            4326);
        
        TileOrigin           tileOrigin = TileOrigin.UpperRight;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxY() - (5*  (GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxX() - (7.5*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            4326);
        
        TileOrigin           tileOrigin = TileOrigin.UpperRight;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(-80.0, 
                                                            120.0, 
                                                            "epsg", 
                                                            4326);//upper right tile

        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.LowerRight;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxY(), //expect upper right corner
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxX() , 
                                                            "epsg", 
                                                            4326);
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.LowerRight;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxY(), //expect upper left tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMinX() , 
                                                            "epsg", 
                                                            4326);
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.LowerRight;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        fail("Expected GlobalGeodeticCrsProfile to throw an IllegalArgumentException based on the fact that the crsCoordinate is beyond the bounds.");
    }
    
    /**
     * Tests if globalGeodeticCrsProfile Would return the correct tile coordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileLowerRightCrsToTileCoordinateEdgeCaseLowerLeftCorner()
    {
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinY(), //expect lower left tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMinX() , 
                                                            "epsg", 
                                                            4326);
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.LowerRight;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinY(), //expect lower right tile
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxX() , 
                                                            "epsg", 
                                                            4326);
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        TileOrigin           tileOrigin = TileOrigin.LowerRight;

        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();

        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);

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
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinY()+(2*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxX()-(1*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            4326);
        
        TileOrigin           tileOrigin = TileOrigin.LowerRight;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinY()+(4.75*(GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxX()-(3*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            4326);
        
        TileOrigin           tileOrigin = TileOrigin.LowerRight;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        TileMatrixDimensions dimensions = new TileMatrixDimensions(7, 9);
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMinY() + (4*  (GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            GlobalGeodeticCrsProfile.Bounds.getMaxX() - (0.5*(GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            4326);
        
        TileOrigin           tileOrigin = TileOrigin.LowerRight;
        
        GlobalGeodeticCrsProfile globalCrs = new GlobalGeodeticCrsProfile();
        
        Coordinate<Integer> newCoordinate = globalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
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
        GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        TileMatrixDimensions     dimensions    = new TileMatrixDimensions(13, 20);
        TileOrigin origin = TileOrigin.UpperLeft;
        Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(3,7);
        
        CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getY(), originalTileCoordinate.getX(), dimensions, origin);
        Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, dimensions, origin);
        
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
        GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        TileMatrixDimensions     dimensions    = new TileMatrixDimensions(20, 11);
        TileOrigin origin = TileOrigin.UpperRight;
        Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(15,9);
        
        CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getY(), originalTileCoordinate.getX(), dimensions, origin);
        Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, dimensions, origin);
        
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
        GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        TileMatrixDimensions     dimensions    = new TileMatrixDimensions(103, 73);
        TileOrigin origin = TileOrigin.LowerLeft;
        Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(24,67);
        
        CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getY(), originalTileCoordinate.getX(), dimensions, origin);
        Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, dimensions, origin);
        
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
        GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        TileMatrixDimensions     dimensions    = new TileMatrixDimensions(103, 73);
        TileOrigin origin = TileOrigin.LowerRight;
        Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(98,32);
        
        CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getY(), originalTileCoordinate.getX(), dimensions, origin);
        Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, dimensions, origin);
        
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
        GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        TileMatrixDimensions     dimensions    = new TileMatrixDimensions(103, 73);
        TileOrigin origin = TileOrigin.UpperRight;
        Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(0,0);
        
        CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getY(), originalTileCoordinate.getX(), dimensions, origin);
        Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, dimensions, origin);
        
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
        GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        TileMatrixDimensions     dimensions    = new TileMatrixDimensions(103, 73);
        TileOrigin origin = TileOrigin.UpperRight;
        Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(102,72);
        
        CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getY(), originalTileCoordinate.getX(), dimensions, origin);
        Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, dimensions, origin);
        
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
        GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        TileMatrixDimensions     dimensions    = new TileMatrixDimensions(10, 10);
        TileOrigin origin = TileOrigin.UpperRight;
        Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(5,5);
        
        CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getY(), originalTileCoordinate.getX(), dimensions, origin);
        Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, dimensions, origin);
        
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
        GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        TileMatrixDimensions     dimensions    = new TileMatrixDimensions(10, 10);
        TileOrigin origin = TileOrigin.UpperRight;
        Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(6,5);
        
        CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getY(), originalTileCoordinate.getX(), dimensions, origin);
        Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, dimensions, origin);
        
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
        GlobalGeodeticCrsProfile globalCrs     = new GlobalGeodeticCrsProfile();
        TileMatrixDimensions     dimensions    = new TileMatrixDimensions(10, 10);
        TileOrigin origin = TileOrigin.UpperRight;
        Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(5,6);
        
        CrsCoordinate       returnedCrsCoordiante  = globalCrs.tileToCrsCoordinate(originalTileCoordinate.getY(), originalTileCoordinate.getX(), dimensions, origin);
        Coordinate<Integer> returnedTileCoordinate = globalCrs.crsToTileCoordinate(returnedCrsCoordiante, dimensions, origin);
        
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
