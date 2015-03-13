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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.junit.Test;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.EllipsoidalMercatorCrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.GlobalGeodeticCrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings({"javadoc"})
public class EllipsoidalMercatorCrsProfileTest
{
    /**
     * @author Jenifer Cochran
     *
     */
    EllipsoidalMercatorCrsProfile ellipsoidalCrs = new EllipsoidalMercatorCrsProfile();
    public class LatLongMetersYMetersX
    {
       private double latitude;
       private double longitude;
       private double metersX;
       private double metersY;
       
       @Override
       public String toString()
       {
        return String.format("Latitude: %f, Longitude: %f, MetersX: %f, MetersY: %f", this.latitude, this.longitude, this.metersX, this.metersY);
           
       }
    }
    
    @Test
    public void tileToCrsCoordinateBackToTileCoordinate()
    {
        
        TileMatrixDimensions matrixDimensions = new TileMatrixDimensions(4, 4);
        Coordinate<Integer> tileCoordinateExpected = new Coordinate<>(2,3);
        
        CrsCoordinate crsCoordinate = this.ellipsoidalCrs.tileToCrsCoordinate(tileCoordinateExpected.getX(), tileCoordinateExpected.getY(), this.ellipsoidalCrs.getBounds(), matrixDimensions, TileOrigin.UpperLeft);
        Coordinate<Integer> tileCoordinateReturned = this.ellipsoidalCrs.crsToTileCoordinate(crsCoordinate,  this.ellipsoidalCrs.getBounds(), matrixDimensions, TileOrigin.UpperLeft);
        
        assertEquals(tileCoordinateExpected, tileCoordinateReturned);
    }
    
    @Test
    public void tileToCrsCoordinateBackToTileCoordinate2()
    {
        TileMatrixDimensions matrixDimensions = new TileMatrixDimensions(17, 13);
        
        for(int row = 0; row < matrixDimensions.getHeight(); row++)
        {
            for(int column = 0; column < matrixDimensions.getWidth(); column++)
            {
                Coordinate<Integer> tileCoordinateExpected = new Coordinate<>(column, row);
                CrsCoordinate crsCoordinate = this.ellipsoidalCrs.tileToCrsCoordinate(tileCoordinateExpected.getX(), tileCoordinateExpected.getY(), this.ellipsoidalCrs.getBounds(), matrixDimensions, TileOrigin.LowerLeft);
                Coordinate<Integer> tileCoordinateReturned = this.ellipsoidalCrs.crsToTileCoordinate(crsCoordinate,  this.ellipsoidalCrs.getBounds(), matrixDimensions, TileOrigin.LowerLeft);
                assertEquals(tileCoordinateExpected, tileCoordinateReturned);
            }
        }
    }
    
    
    @Test
    public void tileToCrsCoordinateBackToTileCoordinate3()
    {
        TileMatrixDimensions matrixDimensions = new TileMatrixDimensions(19, 76);
        BoundingBox bounds = new BoundingBox(this.ellipsoidalCrs.getBounds().getMinX()/2, this.ellipsoidalCrs.getBounds().getMinY()/3, this.ellipsoidalCrs.getBounds().getMaxX()/4, this.ellipsoidalCrs.getBounds().getMaxY()/5);
        
        List<TileOrigin> originList = Arrays.asList(TileOrigin.LowerLeft, TileOrigin.LowerRight,TileOrigin.UpperLeft, TileOrigin.UpperRight);
        
        for(TileOrigin origin: originList)
        {
            for(int row = 0; row < matrixDimensions.getHeight(); row++)
            {
                for(int column = 0; column < matrixDimensions.getWidth(); column++)
                {
                    Coordinate<Integer> tileCoordinateExpected = new Coordinate<>(column, row);
                    CrsCoordinate crsCoordinate = this.ellipsoidalCrs.tileToCrsCoordinate(tileCoordinateExpected.getX(), tileCoordinateExpected.getY(), bounds, matrixDimensions, origin);
                    Coordinate<Integer> tileCoordinateReturned = this.ellipsoidalCrs.crsToTileCoordinate(crsCoordinate,  bounds, matrixDimensions, origin);
                    assertEquals(tileCoordinateExpected, tileCoordinateReturned);
                }
            }
        }
    }
    
    @Test
    public void crsToTileCoordinateBackToCrsCoordinate()
    {
        BoundingBox bounds = new BoundingBox(0,637137*Math.PI, 3189068.5*Math.PI, 3189068.5*Math.PI);
        TileMatrixDimensions matrixDimensions = new TileMatrixDimensions(8, 6);
        TileOrigin origin = TileOrigin.UpperLeft;
        
        this.calculateAndAssertCrsCoordinateEqual(bounds, matrixDimensions, origin, bounds.getTopLeft());
    }

    @Test
    public void tileCoordinateToCrsCoordinateConsistancyTest()
    {
        BoundingBox bounds = new BoundingBox(0,637137*Math.PI, 3189068.5*Math.PI, 3189068.5*Math.PI);
        TileMatrixDimensions matrixDimensions = new TileMatrixDimensions(8, 6);
        List<TileOrigin> originList = Arrays.asList(TileOrigin.LowerLeft, TileOrigin.LowerRight,TileOrigin.UpperLeft, TileOrigin.UpperRight);
        
        for(TileOrigin origin: originList)
        {
            ArrayList<CrsCoordinate> crsList  = getListOfCrsCoordinates(bounds, matrixDimensions, origin);
            
            ArrayList<CrsCoordinate> crsList2 = getListOfCrsCoordinates(bounds, matrixDimensions, origin);
            
            for(int i = 0; i < crsList.size() || i < crsList2.size(); i++)
            {
               assertTrue(crsList.get(i).equals(crsList.get(i)));
            }
        }
    }
    /**
     * Tests if an IllegalArgumentException is thrown
     * when expected
     */
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException()
    {
        this.ellipsoidalCrs.crsToTileCoordinate(null, this.ellipsoidalCrs.getBounds(), new TileMatrixDimensions(10, 10), TileOrigin.LowerLeft);
        fail("Expected an IllegalArgumentException when passing a null value for CrsCoordinate");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException2()
    {
        this.ellipsoidalCrs.crsToTileCoordinate(new CrsCoordinate(0.0,0.0, this.ellipsoidalCrs.getCoordinateReferenceSystem()), null, new TileMatrixDimensions(10,10), TileOrigin.UpperLeft);
        fail("Expected an IllegalArgumentException when passing a null value for the Bounds");
    }
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException3()
    {
        this.ellipsoidalCrs.crsToTileCoordinate(new CrsCoordinate(0.0,0.0, this.ellipsoidalCrs.getCoordinateReferenceSystem()), this.ellipsoidalCrs.getBounds(), null, TileOrigin.UpperLeft);
        fail("Expected an IllegalArgumentException when passing a null value for the TileMatrixDimensions");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException4()
    {
        this.ellipsoidalCrs.crsToTileCoordinate(new CrsCoordinate(0.0,0.0, this.ellipsoidalCrs.getCoordinateReferenceSystem()), this.ellipsoidalCrs.getBounds(), new TileMatrixDimensions(10,10), null);
        fail("Expected an IllegalArgumentException when passing a null value for the TileOrigin");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException5()
    {
        this.ellipsoidalCrs.tileToCrsCoordinate(0, 0, null,  new TileMatrixDimensions(10,10), TileOrigin.LowerLeft);
        fail("Expected an IllegalArgumentException when passing a null value for the Bounds");
    }
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException6()
    {
        this.ellipsoidalCrs.tileToCrsCoordinate(0, 0, this.ellipsoidalCrs.getBounds(),  null, TileOrigin.LowerLeft);
        fail("Expected an IllegalArgumentException when passing a null value for the tileMatrixDimensions");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException7()
    {
        this.ellipsoidalCrs.tileToCrsCoordinate(0, 0, this.ellipsoidalCrs.getBounds(),  new TileMatrixDimensions(10,10), null);
        fail("Expected an IllegalArgumentException when passing a null value for the TileOrigin");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException8()
    {
        this.ellipsoidalCrs.crsToTileCoordinate(new CrsCoordinate(0.0,0.0, (new GlobalGeodeticCrsProfile()).getCoordinateReferenceSystem()), this.ellipsoidalCrs.getBounds(), new TileMatrixDimensions(10,10), TileOrigin.LowerLeft);
        fail("Expected an IllegalArgumentException to occur when passing a different coordinateReferenceSystem than the profile");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException9()
    {
        Coordinate<Double> coordinateBeyondBounds = new Coordinate<>(this.ellipsoidalCrs.getBounds().getBottomRight().getX(), this.ellipsoidalCrs.getBounds().getBottomRight().getY() +1);
        this.ellipsoidalCrs.crsToTileCoordinate(new CrsCoordinate(coordinateBeyondBounds, this.ellipsoidalCrs.getCoordinateReferenceSystem()), this.ellipsoidalCrs.getBounds(), new TileMatrixDimensions(10,10), TileOrigin.LowerLeft);
        fail("Expected an IllegalArgumentException to occur when passing a different coordinateReferenceSystem than the profile");
    }
   
    /**
     * Tests 100 points the NGA uses to verify if the conversion from the crsProfile to global
     * geodetic passes edge cases, flipped x and y values, and other various parts of the world
     * to ensure that the formula used is correct.
     * @throws FileNotFoundException throws if the File object cannot find the file
     * 
     */
    @Test
    public void toGlobalGeodetic() throws FileNotFoundException
    {
        File coordinatePointsFile = new File("EllipsoidalMercatorCoordinatePoints.csv");
        try(Scanner scanner = new Scanner(coordinatePointsFile))
        {
            scanner.useDelimiter("\n");
            
            ArrayList<LatLongMetersYMetersX> coordinatesList = readValuesFromFile(scanner);
            
            List<LatLongMetersYMetersX> inccorrectCoordinates =  coordinatesList.stream()
                                                                                .filter(coordinate -> !verifyCoordinateConversion(coordinate))
                                                                                .collect(Collectors.toList());
            assertTrue(String.format("Number of incorrect coordinates: %d out of 100\n"
                                       + "Following coordinates did not convert correctly.\n%s.",
                                     inccorrectCoordinates.size(),
                                     coordinatesList.stream()
                                                    .filter(coordinate -> !verifyCoordinateConversion(coordinate))
                                                    .map(coordinate -> coordinate.toString())
                                                    .collect(Collectors.joining("\n"))),
                       coordinatesList.stream().allMatch(coordinate -> verifyCoordinateConversion(coordinate)));
        }
    }
    
    private static boolean verifyCoordinateConversion(LatLongMetersYMetersX coordinate)
    {
        CrsCoordinate                 metersCoordinate   = new CrsCoordinate(coordinate.metersX, coordinate.metersY, "epsg", 3395);
        EllipsoidalMercatorCrsProfile ellipsoidalCrs     = new EllipsoidalMercatorCrsProfile();
        Coordinate<Double>            coordinateReturned = ellipsoidalCrs.toGlobalGeodetic(metersCoordinate);
        Coordinate<Double>            coordinateExpected = new Coordinate<>(coordinate.longitude, coordinate.latitude);
        return isEqual(coordinateExpected, coordinateReturned);
    }
    
    private static boolean isEqual(Coordinate<Double> coordinateExpected, Coordinate<Double> coordinateReturned)
    {
        boolean xEqual = Math.abs(coordinateExpected.getX() - coordinateReturned.getX()) < Epsilon;
        boolean yEqual = Math.abs(coordinateExpected.getY() - coordinateReturned.getY()) < Epsilon;
        return xEqual && yEqual;
    }

    private ArrayList<LatLongMetersYMetersX> readValuesFromFile(Scanner scanner)
    {
        ArrayList<LatLongMetersYMetersX> coordinatesList = new ArrayList<>();
        while(scanner.hasNext())
        {
            String line = scanner.next();
            String[] values = line.split(",", 4);
            
            LatLongMetersYMetersX coordinate = new LatLongMetersYMetersX();
            coordinate.latitude  = Double.parseDouble(values[0]);
            coordinate.longitude = Double.parseDouble(values[1]);
            coordinate.metersY   = Double.parseDouble(values[2]);
            coordinate.metersX   = Double.parseDouble(values[3]);
            coordinatesList.add(coordinate);
        }
        return coordinatesList;
    }
    
    private ArrayList<CrsCoordinate> getListOfCrsCoordinates(BoundingBox bounds, TileMatrixDimensions matrixDimensions, TileOrigin origin)
    {
        ArrayList<CrsCoordinate> crsCoordinateList = new ArrayList<>();
        for(int row = 0; row < matrixDimensions.getHeight(); row++)
        {
            for(int column = 0; column < matrixDimensions.getWidth(); column++)
            {
                CrsCoordinate crsCoordinateReturned = this.ellipsoidalCrs.tileToCrsCoordinate(column, row, bounds, matrixDimensions, origin);
                crsCoordinateList.add(crsCoordinateReturned);
            }
        }
        return crsCoordinateList;
    }

    private static void assertCrsCoordinatesEqual(CrsCoordinate crsCoordinate, CrsCoordinate crsCoordinateReturned)
    {
        assertTrue(isEqual(crsCoordinate,crsCoordinateReturned) && crsCoordinate.getCoordinateReferenceSystem().equals(crsCoordinate.getCoordinateReferenceSystem()));
    }
    
    private void calculateAndAssertCrsCoordinateEqual(BoundingBox bounds, TileMatrixDimensions matrixDimensions, TileOrigin origin, Coordinate<Double> coordinate)
    {
        CrsCoordinate crsCoordinate = new CrsCoordinate(coordinate, this.ellipsoidalCrs.getCoordinateReferenceSystem()); 
        Coordinate<Integer> tileCoordinate = this.ellipsoidalCrs.crsToTileCoordinate(crsCoordinate, bounds, matrixDimensions, origin);
        CrsCoordinate crsCoordinateReturned = this.ellipsoidalCrs.tileToCrsCoordinate(tileCoordinate.getX(), tileCoordinate.getY(), bounds, matrixDimensions, origin);
        
        EllipsoidalMercatorCrsProfileTest.assertCrsCoordinatesEqual(crsCoordinate, crsCoordinateReturned);
    }

    
    private static final double Epsilon = 0.0000001;
}
