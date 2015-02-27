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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.junit.Test;

import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.EllipsoidalMercatorCrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings({"static-method", "javadoc"})
public class EllipsoidalMercatorCrsProfileTest
{
    /**
     * @author Jenifer Cochran
     *
     */
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
        EllipsoidalMercatorCrsProfile ellipsoidalCrs = new EllipsoidalMercatorCrsProfile();
        Coordinate<Integer> tileCoordinateExpected = new Coordinate<>(1,5);
        CrsCoordinate crsCoordinate = ellipsoidalCrs.tileToCrsCoordinate(tileCoordinateExpected.getX(), tileCoordinateExpected.getY(), ellipsoidalCrs.getBounds(), new TileMatrixDimensions(2^3, 2^3), TileOrigin.LowerLeft);
        System.out.println(String.format("x: %f, y: %f", crsCoordinate.getX(),  crsCoordinate.getY()));
        Coordinate<Integer> tileCoordinateReturned = ellipsoidalCrs.crsToTileCoordinate(crsCoordinate,  ellipsoidalCrs.getBounds(), new TileMatrixDimensions(2^3, 2^3), TileOrigin.LowerLeft);
        
        assertEquals(tileCoordinateExpected, tileCoordinateReturned);
    }
    
//    @Test
//    public void crsToTileCoordinateBackToCrs()
//    {
//        EllipsoidalMercatorCrsProfile ellipsoidalCrs = new EllipsoidalMercatorCrsProfile();
//        Coordinate<Integer> tileCoordinateExpected = new Coordinate<>(1,5);
//        CrsCoordinate crsCoordinate = ellipsoidalCrs.tileToCrsCoordinate(tileCoordinateExpected.getX(), tileCoordinateExpected.getY(), ellipsoidalCrs.getBounds(), new TileMatrixDimensions(2^3, 2^3), TileOrigin.LowerLeft);
//        System.out.println(String.format("x: %f, y: %f", crsCoordinate.getX(),  crsCoordinate.getY()));
//        Coordinate<Integer> tileCoordinateReturned = ellipsoidalCrs.crsToTileCoordinate(crsCoordinate,  ellipsoidalCrs.getBounds(), new TileMatrixDimensions(2^3, 2^3), TileOrigin.LowerLeft);
//        
//        assertEquals(tileCoordinateExpected, tileCoordinateReturned);
//    }
    
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
        CrsCoordinate                 metersCoordinate   = new CrsCoordinate(coordinate.metersX, coordinate.metersY, "epsg", 3857);
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
    
    private static final double Epsilon = 0.0000001;
}
