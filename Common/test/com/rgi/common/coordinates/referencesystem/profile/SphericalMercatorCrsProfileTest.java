package com.rgi.common.coordinates.referencesystem.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.SphericalMercatorCrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;

public class SphericalMercatorCrsProfileTest
{
    /**
     * Tests if spherical mercator throws an IllegalArgumentException
     * when passing a null value to crsToTileCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void crstToTileCoordinateIllegalArgumentException()
    {
        SphericalMercatorCrsProfile sphericalMerCrs = new SphericalMercatorCrsProfile();
        sphericalMerCrs.crsToTileCoordinate(null, new TileMatrixDimensions(5,8), TileOrigin.UpperRight);
        fail("Expected Spherical Mercator to throw an IllegalArgumentException when coordinate is null for crsToTileCoordinate");
    }
    
    /**
     * Tests if spherical mercator throws an IllegalArgumentException
     * when passing a null value to crsToTileCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void crstToTileCoordinateIllegalArgumentException2()
    {
        SphericalMercatorCrsProfile sphericalMerCrs = new SphericalMercatorCrsProfile();
        sphericalMerCrs.crsToTileCoordinate(new CrsCoordinate(90.0,-50.0, "epsg", 3857), null, TileOrigin.UpperRight);
        fail("Expected Spherical Mercator to throw an IllegalArgumentException when dimensions is null for crsToTileCoordinate");
    }
    
    /**
     * Tests if spherical mercator throws an IllegalArgumentException
     * when passing a null value to crsToTileCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void crstToTileCoordinateIllegalArgumentException3()
    {
        SphericalMercatorCrsProfile sphericalMerCrs = new SphericalMercatorCrsProfile();
        sphericalMerCrs.crsToTileCoordinate(new CrsCoordinate(90.0,-50.0, "epsg", 3857), new TileMatrixDimensions(5,8), null);
        fail("Expected Spherical Mercator to throw an IllegalArgumentException when TileOrigin is null for crsToTileCoordinate");
    }
    
    /**
     * Tests if spherical mercator throws an IllegalArgumentException
     * when passing a different crs to crsToTileCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void crstToTileCoordinateIllegalArgumentException4()
    {
        SphericalMercatorCrsProfile sphericalMerCrs = new SphericalMercatorCrsProfile();
        sphericalMerCrs.crsToTileCoordinate(new CrsCoordinate(90.0,-50.0, "epsg", 4326), new TileMatrixDimensions(5,8), TileOrigin.LowerLeft);
        fail("Expected Spherical Mercator to throw an IllegalArgumentException when passing a different crs to crsToTileCoordinate");
    }
    
    /**
     * Tests if spherical mercator throws an IllegalArgumentException
     * when passing a coordinate outside the bounds to crsToTileCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void crstToTileCoordinateIllegalArgumentException5()
    {
        SphericalMercatorCrsProfile sphericalMerCrs = new SphericalMercatorCrsProfile();
        sphericalMerCrs.crsToTileCoordinate(new CrsCoordinate(sphericalMerCrs.getBounds().getMaxY(),SphericalMercatorCrsProfile.Bounds.getMinX(), "epsg", 3857),
                                            new TileMatrixDimensions(5,8), 
                                            TileOrigin.LowerLeft);
        
        fail("Expected Spherical Mercator to throw an IllegalArgumentException when passing a coordinate outside the bounds to crsToTileCoordinate");
    }

    /**
     * Tests if a crs to tile coordinate with an upperleft origin
     *  returns the correct coordinates
     */
    @Test
    public void crsProfileUpperLeftCrsToTileCoordinate()
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
    
    /**
     * Tests if the crs profile can retrieve the correct tile coordinate
     * from a crsCoordinate that is the upper left corner of the matrix
     */
    @Test
    public void upperLeftOriginCrsToTileCoordinateUpperLeftCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.UpperLeft;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        Coordinate<Integer>         tileCoordinate = sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 0 y: 0", 
                                 tileCoordinate.getX(), 
                                 tileCoordinate.getY()),
                  tileCoordinate.getX() == 0 && tileCoordinate.getY() == 0);
    }
    
    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the lower left corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void upperLeftOriginCrsToTileCoordinateLowerLeftCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.UpperLeft;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }
    
    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the upper right corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void upperLeftOriginCrsToTileCoordinateUpperRightCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.UpperLeft;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }
    
    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the lower right corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void upperLeftOriginCrsToTileCoordinateLowerRightCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.UpperLeft;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }
    
    /**
     * Tests if the crsToTileCoordiante can give an accurate tile coordinate
     * when the crs coordiante lies at the center of four tiles
     */
    @Test
    public void upperLeftOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        TileMatrixDimensions dimensions = new TileMatrixDimensions(5, 13);
        
        CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY() - (3*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            SphericalMercatorCrsProfile.Bounds.getMinX() + (8*(SphericalMercatorCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            3857);
        
        TileOrigin           tileOrigin = TileOrigin.UpperLeft;
        
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 8 y: 3", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 8 && newCoordinate.getY() == 3);
    }
    
    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * siting side by side
     */
    @Test
    public void upperLeftOriginCrsToTileCoordinateEdgeCaseBetweenTilesSideBySide()
    {
        TileMatrixDimensions dimensions = new TileMatrixDimensions(5, 13);
        
        CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY() - (2.3*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            SphericalMercatorCrsProfile.Bounds.getMinX() + (5*(SphericalMercatorCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            3857);
        
        TileOrigin           tileOrigin = TileOrigin.UpperLeft;
        
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 5 y: 2", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 5 && newCoordinate.getY() == 2);
    }
    
    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * on top of one another
     */
    @Test
    public void upperLeftOriginCrsToTileCoordinateEdgeCaseBetweenTilesUpAndDown()
    {
 TileMatrixDimensions dimensions = new TileMatrixDimensions(5, 13);
        
        CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY() - (1*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            SphericalMercatorCrsProfile.Bounds.getMinX() + (3.8*(SphericalMercatorCrsProfile.Bounds.getWidth()/ dimensions.getWidth())), 
                                                            "epsg", 
                                                            3857);
        TileOrigin           tileOrigin = TileOrigin.UpperLeft;
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 3 y: 1", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 3 && newCoordinate.getY() == 1);
    }
   
    /**
     * Tests if a crs to tile coordinate with an lower left origin
     *  returns the correct coordinates
     */
    @Test
    public void crsProfileLowerLeftCrsToTileCoordinate()
    {
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(6,11);
        CrsCoordinate         coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY() + (0.3*(SphericalMercatorCrsProfile.Bounds.getHeight()  / dimensions.getHeight())),
                                                             SphericalMercatorCrsProfile.Bounds.getMinX() + (9.123*(SphericalMercatorCrsProfile.Bounds.getWidth() / dimensions.getWidth())), 
                                                             "epsg", 
                                                             3857);
        TileOrigin            tileOrigin = TileOrigin.LowerLeft;
        
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 9 y: 0", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 9 && newCoordinate.getY() == 0);
    }
    
    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * from a crsCoordinate that is the upper left corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void lowerLeftOriginCrsToTileCoordinateUpperLeftCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.LowerLeft;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }
    
    /**
     * Tests if the crs profile will retrieve the correct tile
     * when the crsCoordinate lies at the lower left corner of the matrix
     */
    @Test
    public void lowerLeftOriginCrsToTileCoordinateLowerLeftCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.LowerLeft;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        Coordinate<Integer>         newCoordinate  = sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 0 y: 0", 
                                  newCoordinate.getX(), 
                                  newCoordinate.getY()),
                   newCoordinate.getX() == 0 && newCoordinate.getY() == 0);
    }
    
    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the upper right corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void lowerLeftOriginCrsToTileCoordinateUpperRightCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.LowerLeft;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }
    
    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the lower right corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void lowerLeftOriginCrsToTileCoordinateLowerRightCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.LowerLeft;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }
    
    /**
     * Tests if the crsToTileCoordiante can give an accurate tile coordinate
     * when the crs coordiante lies at the center of four tiles
     */
    @Test
    public void lowerLeftOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        TileMatrixDimensions dimensions = new TileMatrixDimensions(6, 11);
        
        CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY() + (1*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            SphericalMercatorCrsProfile.Bounds.getMinX() + (4*(SphericalMercatorCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            3857);
        
        TileOrigin           tileOrigin = TileOrigin.LowerLeft;
        
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 4 y: 1", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 4 && newCoordinate.getY() == 1);
    }
    
    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * siting side by side
     */
    @Test
    public void lowerLeftOriginCrsToTileCoordinateEdgeCaseBetweenTilesSideBySide()
    {
        TileMatrixDimensions dimensions = new TileMatrixDimensions(6, 11);
        
        CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY() + (1.23*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            SphericalMercatorCrsProfile.Bounds.getMinX() + (4*(SphericalMercatorCrsProfile.Bounds.getWidth()     / dimensions.getWidth())), 
                                                            "epsg", 
                                                            3857);
        
        TileOrigin           tileOrigin = TileOrigin.LowerLeft;
        
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 4 y: 1", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 4 && newCoordinate.getY() == 1);
    }
    
    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * on top of one another
     */
    @Test
    public void lowerLeftOriginCrsToTileCoordinateEdgeCaseBetweenTilesUpAndDown()
    {
 TileMatrixDimensions dimensions = new TileMatrixDimensions(5, 13);
        
        CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY() + (1*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            SphericalMercatorCrsProfile.Bounds.getMinX() + (2.15*(SphericalMercatorCrsProfile.Bounds.getWidth()/ dimensions.getWidth())), 
                                                            "epsg", 
                                                            3857);
        TileOrigin           tileOrigin = TileOrigin.LowerLeft;
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 2 y: 1", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 2 && newCoordinate.getY() == 1);
    }
    
    /**
     * Tests if a crs to tile coordinate with an upper right origin
     *  returns the correct coordinates
     */
    @Test
    public void crsProfileUpperRightCrsToTileCoordinate()
    {
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(9,14);
        CrsCoordinate         coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY() - (2.22*(SphericalMercatorCrsProfile.Bounds.getHeight()  / dimensions.getHeight())),
                                                             SphericalMercatorCrsProfile.Bounds.getMaxX() - (3.123*(SphericalMercatorCrsProfile.Bounds.getWidth() / dimensions.getWidth())), 
                                                             "epsg", 
                                                             3857);
        TileOrigin            tileOrigin = TileOrigin.UpperRight;
        
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 3 y: 2", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 3 && newCoordinate.getY() == 2);
    }
    
    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * from a crsCoordinate that is the upper left corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void upperRightOriginCrsToTileCoordinateUpperLeftCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.UpperRight;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }
    
    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the lower left corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void upperRightOriginCrsToTileCoordinateLowerLeftCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.UpperRight;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }
    
    /**
     * Tests if the crs profile will retrieve the correct tile
     * when the crsCoordinate lies at the upper right corner of the matrix
     */
    @Test
    public void upperRightOriginCrsToTileCoordinateUpperRightCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.UpperRight;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        Coordinate<Integer>         newCoordinate  = sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 0 y: 0", 
                                  newCoordinate.getX(), 
                                  newCoordinate.getY()),
                   newCoordinate.getX() == 0 && newCoordinate.getY() == 0);
    }
    
    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the lower right corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void upperRightOriginCrsToTileCoordinateLowerRightCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.UpperRight;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }
    
    /**
     * Tests if the crsToTileCoordiante can give an accurate tile coordinate
     * when the crs coordiante lies at the center of four tiles
     */
    @Test
    public void upperRightOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 14);
        
        CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY() - (1*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            SphericalMercatorCrsProfile.Bounds.getMaxX() - (4*(SphericalMercatorCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            3857);
        
        TileOrigin           tileOrigin = TileOrigin.UpperRight;
        
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 4 y: 1", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 4 && newCoordinate.getY() == 1);
    }
    
    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * siting side by side
     */
    @Test
    public void upperRightOriginCrsToTileCoordinateEdgeCaseBetweenTilesSideBySide()
    {
        TileMatrixDimensions dimensions = new TileMatrixDimensions(6, 11);
        
        CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY() - (4.23*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            SphericalMercatorCrsProfile.Bounds.getMaxX() - (1*(SphericalMercatorCrsProfile.Bounds.getWidth()     / dimensions.getWidth())), 
                                                            "epsg", 
                                                            3857);
        
        TileOrigin           tileOrigin = TileOrigin.UpperRight;
        
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 1 y: 4", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 1 && newCoordinate.getY() == 4);
    }
    
    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * on top of one another
     */
    @Test
    public void upperRightOriginCrsToTileCoordinateEdgeCaseBetweenTilesUpAndDown()
    {
        TileMatrixDimensions dimensions = new TileMatrixDimensions(5, 13);
        
        CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY() - (1*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            SphericalMercatorCrsProfile.Bounds.getMaxX() - (2.15*(SphericalMercatorCrsProfile.Bounds.getWidth()/ dimensions.getWidth())), 
                                                            "epsg", 
                                                            3857);
        TileOrigin           tileOrigin = TileOrigin.UpperRight;
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 2 y: 1", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 2 && newCoordinate.getY() == 1);
    }
    
    /**
     * Tests if a crs to tile coordinate with an upper right origin
     *  returns the correct coordinates
     */
    @Test
    public void crsProfileLowerRightCrsToTileCoordinate()
    {
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(9,14);
        CrsCoordinate         coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY() + (2.22*(SphericalMercatorCrsProfile.Bounds.getHeight()  / dimensions.getHeight())),
                                                             SphericalMercatorCrsProfile.Bounds.getMaxX() - (3.123*(SphericalMercatorCrsProfile.Bounds.getWidth() / dimensions.getWidth())), 
                                                             "epsg", 
                                                             3857);
        TileOrigin            tileOrigin = TileOrigin.LowerRight;
        
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 3 y: 2", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 3 && newCoordinate.getY() == 2);
    }
    
    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * from a crsCoordinate that is the upper left corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void lowerRightOriginCrsToTileCoordinateUpperLeftCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.LowerRight;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }
    
    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the lower left corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void lowerRightOriginCrsToTileCoordinateLowerLeftCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.LowerRight;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }
    
    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the upper right corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void lowerRightOriginCrsToTileCoordinateUpperRightCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.LowerRight;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
     
    }
    
    /**
     * Tests if the crs profile will retrieve the correct tile
     * when the crsCoordinate lies at the lower right corner of the matrix
     */
    @Test
    public void lowerRightOriginCrsToTileCoordinateLowerRightCorner()
    {
        CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY(), 
                                                                SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                "epsg", 
                                                                3857);
        TileMatrixDimensions  dimensions = new TileMatrixDimensions(19,13);
        TileOrigin            tileOrigin = TileOrigin.LowerRight;
        
        SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        Coordinate<Integer>         newCoordinate  = sphericalCrs.crsToTileCoordinate(crsCoordinate, dimensions, tileOrigin);
        
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
    public void lowerRightOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        TileMatrixDimensions dimensions = new TileMatrixDimensions(9, 14);
        
        CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY() + (1*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            SphericalMercatorCrsProfile.Bounds.getMaxX() - (4*(SphericalMercatorCrsProfile.Bounds.getWidth()  / dimensions.getWidth())), 
                                                            "epsg", 
                                                            3857);
        
        TileOrigin           tileOrigin = TileOrigin.LowerRight;
        
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 4 y: 1", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 4 && newCoordinate.getY() == 1);
    }
    
    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * siting side by side
     */
    @Test
    public void lowerRightOriginCrsToTileCoordinateEdgeCaseBetweenTilesSideBySide()
    {
        TileMatrixDimensions dimensions = new TileMatrixDimensions(6, 11);
        
        CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY() + (4.23*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            SphericalMercatorCrsProfile.Bounds.getMaxX() - (1*(SphericalMercatorCrsProfile.Bounds.getWidth()     / dimensions.getWidth())), 
                                                            "epsg", 
                                                            3857);
        
        TileOrigin           tileOrigin = TileOrigin.LowerRight;
        
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 1 y: 4", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 1 && newCoordinate.getY() == 4);
    }
    
    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * on top of one another
     */
    @Test
    public void lowerRightOriginCrsToTileCoordinateEdgeCaseBetweenTilesUpAndDown()
    {
        TileMatrixDimensions dimensions = new TileMatrixDimensions(5, 13);
        
        CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinY() + (1*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            SphericalMercatorCrsProfile.Bounds.getMaxX() - (2.15*(SphericalMercatorCrsProfile.Bounds.getWidth()/ dimensions.getWidth())), 
                                                            "epsg", 
                                                            3857);
        TileOrigin           tileOrigin = TileOrigin.LowerRight;
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        
        Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, dimensions, tileOrigin);
        
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: 2 y: 1", 
                                 newCoordinate.getX(), 
                                 newCoordinate.getY()),
                  newCoordinate.getX() == 2 && newCoordinate.getY() == 1);
    }
    
    @Test
    public void MIDDLE()
    {
        
    }
    
    /**
     * Tests if we use our tileToCrsCoordinate and use those values to put into crsToTileCoordinate
     * then we would get the same tileCoordiante that we started with.
     */
    @Test
    public void tileCoordinateToCrsBackToTileCoordinate()
    {
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        TileMatrixDimensions     dimensions    = new TileMatrixDimensions(13, 20);
        TileOrigin origin = TileOrigin.UpperLeft;
        Coordinate<Integer>  originalTileCoordinate = new Coordinate<Integer>(3,7);
        
        CrsCoordinate       returnedCrsCoordiante  = sphericalCrs.tileToCrsCoordinate(originalTileCoordinate.getY(), originalTileCoordinate.getX(), dimensions, origin);
        Coordinate<Integer> returnedTileCoordinate = sphericalCrs.crsToTileCoordinate(returnedCrsCoordiante, dimensions, origin);
        
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
    public void tileCoordinateToCrsBackToTileCoordinate2()
    {
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        TileMatrixDimensions     dimensions    = new TileMatrixDimensions(103, 73);
        TileOrigin origin = TileOrigin.UpperRight;
        Coordinate<Integer>  originalTileCoordinate = new Coordinate<Integer>(15,9);
        
        CrsCoordinate       returnedCrsCoordiante  = sphericalCrs.tileToCrsCoordinate(originalTileCoordinate.getY(), originalTileCoordinate.getX(), dimensions, origin);
        Coordinate<Integer> returnedTileCoordinate = sphericalCrs.crsToTileCoordinate(returnedCrsCoordiante, dimensions, origin);
        
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
    public void tileCoordinateToCrsBackToTileCoordinate3()
    {
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        TileMatrixDimensions     dimensions    = new TileMatrixDimensions(103, 73);
        TileOrigin origin = TileOrigin.LowerLeft;
        Coordinate<Integer>  originalTileCoordinate = new Coordinate<Integer>(24,67);
        
        CrsCoordinate       returnedCrsCoordiante  = sphericalCrs.tileToCrsCoordinate(originalTileCoordinate.getY(), originalTileCoordinate.getX(), dimensions, origin);
        Coordinate<Integer> returnedTileCoordinate = sphericalCrs.crsToTileCoordinate(returnedCrsCoordiante, dimensions, origin);
        
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
    public void tileCoordinateToCrsBackToTileCoordinate4()
    {
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        TileMatrixDimensions     dimensions    = new TileMatrixDimensions(103, 73);
        TileOrigin origin = TileOrigin.LowerRight;
        Coordinate<Integer>  originalTileCoordinate = new Coordinate<Integer>(98,32);
        
        CrsCoordinate       returnedCrsCoordiante  = sphericalCrs.tileToCrsCoordinate(originalTileCoordinate.getY(), originalTileCoordinate.getX(), dimensions, origin);
        Coordinate<Integer> returnedTileCoordinate = sphericalCrs.crsToTileCoordinate(returnedCrsCoordiante, dimensions, origin);
        
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
    public void tileCoordinateToCrsBackToTileCoordinateEdgeCase1()
    {
        SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        TileMatrixDimensions     dimensions    = new TileMatrixDimensions(103, 73);
        TileOrigin origin = TileOrigin.UpperRight;
        Coordinate<Integer>  originalTileCoordinate = new Coordinate<Integer>(0,0);
        
        CrsCoordinate       returnedCrsCoordiante  = sphericalCrs.tileToCrsCoordinate(originalTileCoordinate.getY(), originalTileCoordinate.getX(), dimensions, origin);
        Coordinate<Integer> returnedTileCoordinate = sphericalCrs.crsToTileCoordinate(returnedCrsCoordiante, dimensions, origin);
        
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
