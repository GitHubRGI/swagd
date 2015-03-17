package com.rgi.common.tile.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;

public class TileOriginTest
{
    @Test
    public void transformLowerLeftToLowerLeft()
    {
        Coordinate<Integer>  tileCoordinateExpected = new Coordinate<>(2, 6);
        TileMatrixDimensions matrixDimensions       = new TileMatrixDimensions(85, 97);
        
        Coordinate<Integer>  tileCoordinateReturned = TileOrigin.LowerLeft
                                                                .transform(TileOrigin.LowerLeft, 
                                                                           tileCoordinateExpected.getX(), 
                                                                           tileCoordinateExpected.getY(), 
                                                                           matrixDimensions);
        //There should be no changes in the tile coordinate
        assertTileCoordinates(tileCoordinateExpected, tileCoordinateReturned);
    }
    
    @Test
    public void transformLowerLeftToUpperLeft()
    {
        Coordinate<Integer> initialCoordinate  = new Coordinate<>(9, 5);
        Coordinate<Integer> coordinateExpected = new Coordinate<>(9, 777777);
        TileMatrixDimensions matrixDimensions  = new TileMatrixDimensions(5, 7);
        
        Coordinate<Integer> tileCoordinateReturned = TileOrigin.LowerLeft
                                                               .transform(TileOrigin.UpperLeft, initialCoordinate.getX(), initialCoordinate.getY(), matrixDimensions);
    }
    
    private void assertTileCoordinates(Coordinate<Integer> tileCoordinateExpected, Coordinate<Integer> tileCoordinateReturned)
    {
        assertTrue(String.format("The Coordinate did not return the expected values.\nActual: %s\nExpected: %s", 
                                 tileCoordinateReturned.toString(), 
                                 tileCoordinateExpected.toString()), 
                  tileCoordinateExpected.equals(tileCoordinateReturned));
    }
    
    
}
