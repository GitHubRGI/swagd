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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.GlobalGeodeticCrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.SphericalMercatorCrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;

public class CrsProfileTest
{
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
     * Tests if using the maxX value on lower left tile origin tile
     * would throw an illegalArgumentException because tile requested would
     * be outside the bounds
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileUpperLeftCrsToTileCoordinateEdgeCaseUpperRight()
    {
        CrsCoordinate        coordinate = new CrsCoordinate(GlobalGeodeticCrsProfile.Bounds.getMaxY() -3.5, //expect upper right tile
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
    public void globalGeodeticCrsProfileUpperLeftCrsToTileCoordinateEdgeCaseUpperLeft()
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
     * Tests if using the minY value on lower left tile origin tile
     * would throw an illegalArgumentException because tile requested would
     * be outside the bounds
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileUpperLeftCrsToTileCoordinateEdgeCaseLowerLeft()
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
     * Tests if using the minY and maxX value on lower left tile origin tile
     * would throw an illegalArgumentException because tile requested would
     * be outside the bounds
     */
    @Test(expected = IllegalArgumentException.class)
    public void globalGeodeticCrsProfileUpperLeftCrsToTileCoordinateEdgeCaseLowerRight()
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
     * Tests if a crs to tile coordinate with an upperleft origin
     *  returns the correct coordinates
     */
    @Test
    public void sphericalMercatorCrsProfileUpperLeftCrsToTileCoordinate()
    {
        CrsCoordinate         coordinate = new CrsCoordinate(0.0, (SphericalMercatorCrsProfile.EarthEquatorialCircumfrence/2.0)-1.0,"epsg", 3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(2,3);
        TileOrigin            tileOrigin = TileOrigin.UpperLeft;
        
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 2 y: 1", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 2 && newCoordinate.getY() == 1);
    }
}
