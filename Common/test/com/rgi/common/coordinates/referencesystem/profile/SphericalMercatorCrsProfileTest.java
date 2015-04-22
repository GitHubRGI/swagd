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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.junit.Test;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.SphericalMercatorCrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings("static-method")
public class SphericalMercatorCrsProfileTest
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

    /**
     * Tests if spherical mercator throws an IllegalArgumentException
     * when passing a null value to crsToTileCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void crstToTileCoordinateIllegalArgumentException()
    {
        final SphericalMercatorCrsProfile sphericalMerCrs = new SphericalMercatorCrsProfile();
        sphericalMerCrs.crsToTileCoordinate(null, null, new TileMatrixDimensions(8,5), TileOrigin.UpperRight);
        fail("Expected Spherical Mercator to throw an IllegalArgumentException when coordinate is null for crsToTileCoordinate");
    }

    /**
     * Tests if spherical mercator throws an IllegalArgumentException
     * when passing a null value to crsToTileCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void crstToTileCoordinateIllegalArgumentException2()
    {
        final SphericalMercatorCrsProfile sphericalMerCrs = new SphericalMercatorCrsProfile();
        sphericalMerCrs.crsToTileCoordinate(new CrsCoordinate(-50.0,90.0, "epsg", 3857), new BoundingBox(0.0, 0.0, 0.0, 0.0), null, TileOrigin.UpperRight);
        fail("Expected Spherical Mercator to throw an IllegalArgumentException when dimensions is null for crsToTileCoordinate");
    }

    /**
     * Tests if spherical mercator throws an IllegalArgumentException
     * when passing a null value to crsToTileCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void crstToTileCoordinateIllegalArgumentException3()
    {
        final SphericalMercatorCrsProfile sphericalMerCrs = new SphericalMercatorCrsProfile();
        sphericalMerCrs.crsToTileCoordinate(new CrsCoordinate(-50.0,90.0, "epsg", 3857), new BoundingBox(0.0, 0.0, 0.0, 0.0), new TileMatrixDimensions(8,5), null);
        fail("Expected Spherical Mercator to throw an IllegalArgumentException when TileOrigin is null for crsToTileCoordinate");
    }

    /**
     * Tests if spherical mercator throws an IllegalArgumentException
     * when passing a different crs to crsToTileCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void crstToTileCoordinateIllegalArgumentException4()
    {
        final SphericalMercatorCrsProfile sphericalMerCrs = new SphericalMercatorCrsProfile();
        sphericalMerCrs.crsToTileCoordinate(new CrsCoordinate(-50.0,90.0, "epsg", 4326), new BoundingBox(0.0, 0.0, 0.0, 0.0), new TileMatrixDimensions(8,5), TileOrigin.LowerLeft);
        fail("Expected Spherical Mercator to throw an IllegalArgumentException when passing a different crs to crsToTileCoordinate");
    }

    /**
     * Tests if spherical mercator throws an IllegalArgumentException
     * when passing a coordinate outside the bounds to crsToTileCoordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void crstToTileCoordinateIllegalArgumentException5()
    {
        final SphericalMercatorCrsProfile sphericalMerCrs = new SphericalMercatorCrsProfile();
        sphericalMerCrs.crsToTileCoordinate(new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX(),sphericalMerCrs.getBounds().getMaxY(), "epsg", 3857),
                                            new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                            new TileMatrixDimensions(8,5),
                                            TileOrigin.LowerLeft);

        fail("Expected Spherical Mercator to throw an IllegalArgumentException when passing a coordinate outside the bounds to crsToTileCoordinate");
    }


    /**
     * Tests if Spherical Mercator tileToCrsCoordinate will throw
     * an illegal argument exception when the row value is negative
     */
    @Test(expected = IllegalArgumentException.class)
    public void tileToCrsCoordinateIllegalArgumentException()
    {
        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        sphericalCrs.tileToCrsCoordinate(8, -5, new BoundingBox(0.0, 0.0, 0.0, 0.0), new TileMatrixDimensions(100,100), TileOrigin.LowerLeft);
        fail("Expected Spherical Mercator CrsProfile to throw an IllegalArgumentException when row is negative in tileToCrsCoordinate.");
    }

    /**
    * Tests if Spherical Mercator tileToCrsCoordinate will throw
    * an illegal argument exception when the column value is negative
    */
   @Test(expected = IllegalArgumentException.class)
   public void tileToCrsCoordinateIllegalArgumentException2()
   {
       final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
       sphericalCrs.tileToCrsCoordinate(-8, 5, new BoundingBox(0.0, 0.0, 0.0, 0.0), new TileMatrixDimensions(100,100), TileOrigin.LowerLeft);
       fail("Expected Spherical Mercator CrsProfile to throw an IllegalArgumentException when column is negative in tileToCrsCoordinate.");
   }

   /**
     * Tests if Spherical Mercator tileToCrsCoordinate will throw
     * an illegal argument exception when the tile matrix dimensions is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void tileToCrsCoordinateIllegalArgumentException3()
    {
        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        sphericalCrs.tileToCrsCoordinate(8, 5, new BoundingBox(0.0, 0.0, 0.0, 0.0), null, TileOrigin.LowerLeft);
        fail("Expected Spherical Mercator CrsProfile to throw an IllegalArgumentException when the tile matrix dimensions is null in tileToCrsCoordinate.");
    }

    /**
     * Tests if Spherical Mercator tileToCrsCoordinate will throw
     * an illegal argument exception when the tileOrigin is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void tileToCrsCoordinateIllegalArgumentException4()
    {
        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        sphericalCrs.tileToCrsCoordinate(8, 5, new BoundingBox(0.0, 0.0, 0.0, 0.0), new TileMatrixDimensions(100,100), null);
        fail("Expected Spherical Mercator CrsProfile to throw an IllegalArgumentException when the tile Origin is null in tileToCrsCoordinate.");
    }

    /**
     * Tests 100 points the NGA uses to verify if the conversion from the crsProfile to global
     * geodetic passes edge cases, flipped x and y values, and other various parts of the world
     * to ensure that the formula used is correct.
     * @throws FileNotFoundException throws if the File object cannot find specified file
     */
    @Test
    public void toGlobalGeodetic() throws FileNotFoundException
    {
        final File coordinatePointsFile = new File("SphericalMercatorCoordinatePoints.csv");
        try(Scanner scanner = new Scanner(coordinatePointsFile))
        {
            scanner.useDelimiter("\n");

            final ArrayList<LatLongMetersYMetersX> coordinatesList = this.readValuesFromFile(scanner);

            final List<LatLongMetersYMetersX> inccorrectCoordinates =  coordinatesList.stream()
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

    private static boolean verifyCoordinateConversion(final LatLongMetersYMetersX coordinate)
    {
        final CrsCoordinate               metersCoordinate   = new CrsCoordinate(coordinate.metersX, coordinate.metersY, "epsg", 3857);
        final SphericalMercatorCrsProfile sphericalCrs       = new SphericalMercatorCrsProfile();
        final Coordinate<Double>          coordinateReturned = sphericalCrs.toGlobalGeodetic(metersCoordinate);
        final Coordinate<Double>          coordinateExpected = new Coordinate<>(coordinate.longitude, coordinate.latitude);
        return isEqual(coordinateExpected, coordinateReturned);
    }

    private static boolean isEqual(final Coordinate<Double> coordinateExpected, final Coordinate<Double> coordinateReturned)
    {
        final boolean xEqual = Math.abs(coordinateExpected.getX() - coordinateReturned.getX()) < Epsilon;
        final boolean yEqual = Math.abs(coordinateExpected.getY() - coordinateReturned.getY()) < Epsilon;
        return xEqual && yEqual;
    }

    private ArrayList<LatLongMetersYMetersX> readValuesFromFile(final Scanner scanner)
    {
        final ArrayList<LatLongMetersYMetersX> coordinatesList = new ArrayList<>();
        while(scanner.hasNext())
        {
            final String line = scanner.next();
            final String[] values = line.split(",", 4);

            final LatLongMetersYMetersX coordinate = new LatLongMetersYMetersX();
            coordinate.latitude  = Double.parseDouble(values[0]);
            coordinate.longitude = Double.parseDouble(values[1]);
            coordinate.metersY   = Double.parseDouble(values[2]);
            coordinate.metersX   = Double.parseDouble(values[3]);
            coordinatesList.add(coordinate);
        }
        return coordinatesList;
    }

    /**
     * Tests if a crs to tile coordinate with an upperleft origin
     *  returns the correct coordinates
     */
    @Test
    public void crsProfileUpperLeftCrsToTileCoordinate()
    {
        final CrsCoordinate         coordinate = new CrsCoordinate((SphericalMercatorCrsProfile.EarthEquatorialCircumfrence/2.0)-1.0, 0.0, "epsg", 3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(3,2);
        final TileOrigin            tileOrigin = TileOrigin.UpperLeft;

        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

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
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMaxY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.UpperLeft;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        final Coordinate<Integer>         tileCoordinate = sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

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
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMinY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.UpperLeft;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }

    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the upper right corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void upperLeftOriginCrsToTileCoordinateUpperRightCorner()
    {
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMaxY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.UpperLeft;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }

    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the lower right corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void upperLeftOriginCrsToTileCoordinateLowerRightCorner()
    {
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMinY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.UpperLeft;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }

    /**
     * Tests if the crsToTileCoordiante can give an accurate tile coordinate
     * when the crs coordiante lies at the center of four tiles
     */
    @Test
    public void upperLeftOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(13, 5);

        final CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX() + (8*(SphericalMercatorCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            SphericalMercatorCrsProfile.Bounds.getMaxY() - (3*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            3857);

        final TileOrigin           tileOrigin = TileOrigin.UpperLeft;

        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();

        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

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
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(13, 5);

        final CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX() + (5*(SphericalMercatorCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            SphericalMercatorCrsProfile.Bounds.getMaxY() - (2.3*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            3857);

        final TileOrigin           tileOrigin = TileOrigin.UpperLeft;

        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();

        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

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
 final TileMatrixDimensions dimensions = new TileMatrixDimensions(13, 5);

        final CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX() + (3.8*(SphericalMercatorCrsProfile.Bounds.getWidth()/ dimensions.getWidth())),
                                                            SphericalMercatorCrsProfile.Bounds.getMaxY() - (1*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            3857);
        final TileOrigin           tileOrigin = TileOrigin.UpperLeft;
        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();

        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

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
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(11,6);
        final CrsCoordinate         coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX() + (9.123*(SphericalMercatorCrsProfile.Bounds.getWidth() / dimensions.getWidth())),
                                                             SphericalMercatorCrsProfile.Bounds.getMinY() + (0.3*(SphericalMercatorCrsProfile.Bounds.getHeight()  / dimensions.getHeight())),
                                                             "epsg",
                                                             3857);
        final TileOrigin            tileOrigin = TileOrigin.LowerLeft;

        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(9, 0, newCoordinate);
    }

    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * from a crsCoordinate that is the upper left corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void lowerLeftOriginCrsToTileCoordinateUpperLeftCorner()
    {
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMaxY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.LowerLeft;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }

    /**
     * Tests if the crs profile will retrieve the correct tile
     * when the crsCoordinate lies at the lower left corner of the matrix
     */
    @Test
    public void lowerLeftOriginCrsToTileCoordinateLowerLeftCorner()
    {
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMinY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.LowerLeft;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        final Coordinate<Integer>         newCoordinate  = sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(0, 0, newCoordinate);
    }

    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the upper right corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void lowerLeftOriginCrsToTileCoordinateUpperRightCorner()
    {
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMaxY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.LowerLeft;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }

    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the lower right corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void lowerLeftOriginCrsToTileCoordinateLowerRightCorner()
    {
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMinY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.LowerLeft;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }

    /**
     * Tests if the crsToTileCoordiante can give an accurate tile coordinate
     * when the crs coordiante lies at the center of four tiles
     */
    @Test
    public void lowerLeftOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(11, 6);

        final CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX() + (4*(SphericalMercatorCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            SphericalMercatorCrsProfile.Bounds.getMinY() + (1*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            3857);

        final TileOrigin           tileOrigin = TileOrigin.LowerLeft;

        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();

        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(4, 1, newCoordinate);
    }

    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * siting side by side
     */
    @Test
    public void lowerLeftOriginCrsToTileCoordinateEdgeCaseBetweenTilesSideBySide()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(11, 6);

        final CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX() + (4*(SphericalMercatorCrsProfile.Bounds.getWidth()     / dimensions.getWidth())),
                                                            SphericalMercatorCrsProfile.Bounds.getMinY() + (1.23*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            3857);

        final TileOrigin           tileOrigin = TileOrigin.LowerLeft;

        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();

        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(4, 1, newCoordinate);
    }

    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * on top of one another
     */
    @Test
    public void lowerLeftOriginCrsToTileCoordinateEdgeCaseBetweenTilesUpAndDown()
    {
 final TileMatrixDimensions dimensions = new TileMatrixDimensions(13, 5);

        final CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX() + (2.15*(SphericalMercatorCrsProfile.Bounds.getWidth()/ dimensions.getWidth())),
                                                            SphericalMercatorCrsProfile.Bounds.getMinY() + (1*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            3857);
        final TileOrigin           tileOrigin = TileOrigin.LowerLeft;
        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();

        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(2, 1, newCoordinate);
    }

    /**
     * Tests if a crs to tile coordinate with an upper right origin
     *  returns the correct coordinates
     */
    @Test
    public void crsProfileUpperRightCrsToTileCoordinate()
    {
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(14,9);
        final CrsCoordinate         coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX() - (3.123*(SphericalMercatorCrsProfile.Bounds.getWidth() / dimensions.getWidth())),
                                                             SphericalMercatorCrsProfile.Bounds.getMaxY() - (2.22*(SphericalMercatorCrsProfile.Bounds.getHeight()  / dimensions.getHeight())),
                                                             "epsg",
                                                             3857);
        final TileOrigin            tileOrigin = TileOrigin.UpperRight;

        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(3, 2, newCoordinate);
    }

    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * from a crsCoordinate that is the upper left corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void upperRightOriginCrsToTileCoordinateUpperLeftCorner()
    {
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMaxY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.UpperRight;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }

    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the lower left corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void upperRightOriginCrsToTileCoordinateLowerLeftCorner()
    {
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMinY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.UpperRight;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }

    /**
     * Tests if the crs profile will retrieve the correct tile
     * when the crsCoordinate lies at the upper right corner of the matrix
     */
    @Test
    public void upperRightOriginCrsToTileCoordinateUpperRightCorner()
    {
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMaxY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.UpperRight;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        final Coordinate<Integer>         newCoordinate  = sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(0, 0, newCoordinate);
    }

    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the lower right corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void upperRightOriginCrsToTileCoordinateLowerRightCorner()
    {
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMinY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.UpperRight;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }

    /**
     * Tests if the crsToTileCoordiante can give an accurate tile coordinate
     * when the crs coordiante lies at the center of four tiles
     */
    @Test
    public void upperRightOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(14, 9);

        final CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX() - (4*(SphericalMercatorCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            SphericalMercatorCrsProfile.Bounds.getMaxY() - (1*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            3857);

        final TileOrigin           tileOrigin = TileOrigin.UpperRight;

        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();

        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(4, 1, newCoordinate);
    }

    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * siting side by side
     */
    @Test
    public void upperRightOriginCrsToTileCoordinateEdgeCaseBetweenTilesSideBySide()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(11, 6);

        final CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX() - (1*(SphericalMercatorCrsProfile.Bounds.getWidth()     / dimensions.getWidth())),
                                                            SphericalMercatorCrsProfile.Bounds.getMaxY() - (4.23*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            3857);

        final TileOrigin           tileOrigin = TileOrigin.UpperRight;

        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();

        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(1, 4, newCoordinate);
    }

    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * on top of one another
     */
    @Test
    public void upperRightOriginCrsToTileCoordinateEdgeCaseBetweenTilesUpAndDown()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(13, 5);

        final CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX() - (2.15*(SphericalMercatorCrsProfile.Bounds.getWidth()/ dimensions.getWidth())),
                                                            SphericalMercatorCrsProfile.Bounds.getMaxY() - (1*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            3857);
        final TileOrigin           tileOrigin = TileOrigin.UpperRight;
        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();

        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(2, 1, newCoordinate);
    }

    /**
     * Tests if a crs to tile coordinate with an upper right origin
     *  returns the correct coordinates
     */
    @Test
    public void crsProfileLowerRightCrsToTileCoordinate()
    {
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(14,9);
        final CrsCoordinate         coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX() - (3.123*(SphericalMercatorCrsProfile.Bounds.getWidth() / dimensions.getWidth())),
                                                             SphericalMercatorCrsProfile.Bounds.getMinY() + (2.22*(SphericalMercatorCrsProfile.Bounds.getHeight()  / dimensions.getHeight())),
                                                             "epsg",
                                                             3857);
        final TileOrigin            tileOrigin = TileOrigin.LowerRight;

        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(3, 2, newCoordinate);
    }

    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * from a crsCoordinate that is the upper left corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void lowerRightOriginCrsToTileCoordinateUpperLeftCorner()
    {
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMaxY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.LowerRight;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }

    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the lower left corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void lowerRightOriginCrsToTileCoordinateLowerLeftCorner()
    {
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMinX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMinY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.LowerRight;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");
    }

    /**
     * Tests if the crs profile will throw an illegalArgumentException
     * when the crsCoordinate lies at the upper right corner of the matrix
     */
    @Test(expected = IllegalArgumentException.class)
    public void lowerRightOriginCrsToTileCoordinateUpperRightCorner()
    {
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMaxY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.LowerRight;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);
        fail("Expected crsProfile to throw an IllegalArgumentException when the crsCoordinate is out of bounds");

    }

    /**
     * Tests if the crs profile will retrieve the correct tile
     * when the crsCoordinate lies at the lower right corner of the matrix
     */
    @Test
    public void lowerRightOriginCrsToTileCoordinateLowerRightCorner()
    {
        final CrsCoordinate         crsCoordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX(),
                                                                SphericalMercatorCrsProfile.Bounds.getMinY(),
                                                                "epsg",
                                                                3857);
        final TileMatrixDimensions  dimensions = new TileMatrixDimensions(13,19);
        final TileOrigin            tileOrigin = TileOrigin.LowerRight;

        final SphericalMercatorCrsProfile sphericalCrs   = new SphericalMercatorCrsProfile();
        final Coordinate<Integer>         newCoordinate  = sphericalCrs.crsToTileCoordinate(crsCoordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(0, 0, newCoordinate);
    }

    /**
     * Tests if the crsToTileCoordiante can give an accurate tile coordinate
     * when the crs coordiante lies at the center of four tiles
     */
    @Test
    public void lowerRightOriginCrsToTileCoordinateEdgeCaseMiddleOfFourTiles()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(14, 9);

        final CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX() - (4*(SphericalMercatorCrsProfile.Bounds.getWidth()  / dimensions.getWidth())),
                                                            SphericalMercatorCrsProfile.Bounds.getMinY() + (1*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            3857);

        final TileOrigin           tileOrigin = TileOrigin.LowerRight;

        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();

        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(4, 1, newCoordinate);
    }

    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * siting side by side
     */
    @Test
    public void lowerRightOriginCrsToTileCoordinateEdgeCaseBetweenTilesSideBySide()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(11, 6);

        final CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX() - (1*(SphericalMercatorCrsProfile.Bounds.getWidth()     / dimensions.getWidth())),
                                                            SphericalMercatorCrsProfile.Bounds.getMinY() + (4.23*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            3857);

        final TileOrigin           tileOrigin = TileOrigin.LowerRight;

        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();

        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(1, 4, newCoordinate);
    }

    /**
     * Tests if the crsToTileCoordinate would return the correct tile
     * coordinate when the crs coordinate would lie between two tiles
     * on top of one another
     */
    @Test
    public void lowerRightOriginCrsToTileCoordinateEdgeCaseBetweenTilesUpAndDown()
    {
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(13, 5);

        final CrsCoordinate        coordinate = new CrsCoordinate(SphericalMercatorCrsProfile.Bounds.getMaxX() - (2.15*(SphericalMercatorCrsProfile.Bounds.getWidth()/ dimensions.getWidth())),
                                                            SphericalMercatorCrsProfile.Bounds.getMinY() + (1*(SphericalMercatorCrsProfile.Bounds.getHeight() / dimensions.getHeight())),
                                                            "epsg",
                                                            3857);
        final TileOrigin           tileOrigin = TileOrigin.LowerRight;
        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();

        final Coordinate<Integer> newCoordinate = sphericalCrs.crsToTileCoordinate(coordinate, sphericalCrs.getBounds(), dimensions, tileOrigin);

        this.assertTileCoordinate(2, 1, newCoordinate);
    }

    /**
     * Tests if we use our tileToCrsCoordinate and use those values to put into crsToTileCoordinate
     * then we would get the same tileCoordiante that we started with.
     */
    @Test
    public void tileCoordinateToCrsBackToTileCoordinate()
    {
        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        final TileMatrixDimensions     dimensions    = new TileMatrixDimensions(20, 13);
        final TileOrigin origin = TileOrigin.UpperLeft;
        final Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(7,3);

        final CrsCoordinate       returnedCrsCoordiante  = sphericalCrs.tileToCrsCoordinate(originalTileCoordinate.getX(), originalTileCoordinate.getY(), sphericalCrs.getBounds(), dimensions, origin);
        final Coordinate<Integer> returnedTileCoordinate = sphericalCrs.crsToTileCoordinate(returnedCrsCoordiante, sphericalCrs.getBounds(), dimensions, origin);

        this.assertCoordinates( originalTileCoordinate, returnedTileCoordinate,  returnedCrsCoordiante);
    }



    /**
     * Tests if tile is in upperRight and can be transformed from tile coordinate to crs back to
     * tile coordinate and give back the original tile coordinate values
     */
    @Test
    public void tileCoordinateToCrsBackToTileCoordinate2()
    {
        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        final TileMatrixDimensions     dimensions    = new TileMatrixDimensions(73, 60);
        final TileOrigin origin = TileOrigin.UpperRight;
        final Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(9,50); //15, 9

        final CrsCoordinate       returnedCrsCoordiante  = sphericalCrs.tileToCrsCoordinate(originalTileCoordinate.getX(), originalTileCoordinate.getY(), sphericalCrs.getBounds(), dimensions, origin);
        final Coordinate<Integer> returnedTileCoordinate = sphericalCrs.crsToTileCoordinate(returnedCrsCoordiante, sphericalCrs.getBounds(), dimensions, origin);

        this.assertCoordinates( originalTileCoordinate, returnedTileCoordinate,  returnedCrsCoordiante);

    }

    /**
     * Tests if tile is in upperRight and can be transformed from tile coordinate to crs back to
     * tile coordinate and give back the original tile coordinate values
     */
    @Test
    public void tileCoordinateToCrsBackToTileCoordinate3()
    {
        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        final TileMatrixDimensions     dimensions    = new TileMatrixDimensions(73, 103);
        final TileOrigin origin = TileOrigin.LowerLeft;
        final Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(67,24);

        final CrsCoordinate       returnedCrsCoordiante  = sphericalCrs.tileToCrsCoordinate(originalTileCoordinate.getX(), originalTileCoordinate.getY(), sphericalCrs.getBounds(), dimensions, origin);
        final Coordinate<Integer> returnedTileCoordinate = sphericalCrs.crsToTileCoordinate(returnedCrsCoordiante, sphericalCrs.getBounds(), dimensions, origin);

        this.assertCoordinates( originalTileCoordinate, returnedTileCoordinate,  returnedCrsCoordiante);

    }


    /**
     * Tests if tile is in upperRight and can be transformed from tile coordinate to crs back to
     * tile coordinate and give back the original tile coordinate values
     */
    @Test
    public void tileCoordinateToCrsBackToTileCoordinate4()
    {
        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        final TileMatrixDimensions     dimensions    = new TileMatrixDimensions(73, 80);
        final TileOrigin origin = TileOrigin.LowerRight;
        final Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(32,79);

        final CrsCoordinate       returnedCrsCoordiante  = sphericalCrs.tileToCrsCoordinate(originalTileCoordinate.getX(), originalTileCoordinate.getY(), sphericalCrs.getBounds(), dimensions, origin);
        final Coordinate<Integer> returnedTileCoordinate = sphericalCrs.crsToTileCoordinate(returnedCrsCoordiante, sphericalCrs.getBounds(), dimensions, origin);

        this.assertCoordinates( originalTileCoordinate, returnedTileCoordinate,  returnedCrsCoordiante);

    }

    /**
     * Tests if the transformation from tile to crs back to tile can work if given
     * a corner tile
     */
    @Test
    public void tileCoordinateToCrsBackToTileCoordinateEdgeCase1()
    {
        final SphericalMercatorCrsProfile sphericalCrs = new SphericalMercatorCrsProfile();
        final TileMatrixDimensions     dimensions    = new TileMatrixDimensions(73, 103);
        final TileOrigin origin = TileOrigin.UpperRight;
        final Coordinate<Integer>  originalTileCoordinate = new Coordinate<>(0,0);

        final CrsCoordinate       returnedCrsCoordiante  = sphericalCrs.tileToCrsCoordinate(originalTileCoordinate.getX(), originalTileCoordinate.getY(), sphericalCrs.getBounds(), dimensions, origin);
        final Coordinate<Integer> returnedTileCoordinate = sphericalCrs.crsToTileCoordinate(returnedCrsCoordiante, sphericalCrs.getBounds(), dimensions, origin);

        this.assertCoordinates( originalTileCoordinate, returnedTileCoordinate,  returnedCrsCoordiante);
    }

    private void assertCoordinates(final Coordinate<Integer> originalTileCoordinate, final Coordinate<Integer> returnedTileCoordinate, final CrsCoordinate returnedCrsCoordiante)
    {
        assertEquals(String.format("The tile coordinate did not return as expected.\nExpected Tile Coordinate: (x,y)-> (%d,%d)"
                + "\nActual Tile Coordinate: (x,y)-> (%d,%d)\nActual CrsCoordinate: (x,y)->(%f, %f)",
               originalTileCoordinate.getX(),
               originalTileCoordinate.getY(),
               returnedTileCoordinate.getX(),
               returnedTileCoordinate.getY(),
               returnedCrsCoordiante.getX(),
               returnedCrsCoordiante.getY()), originalTileCoordinate, returnedTileCoordinate);
    }

    private void assertTileCoordinate(final int x, final int y, final Coordinate<Integer> newCoordinate)
    {
        assertTrue(String.format("Actual: x: %d y: %d\nExpected: x: %d y: %d",
                newCoordinate.getX(),
                newCoordinate.getY(),
                x,
                y),
                newCoordinate.getX() == x && newCoordinate.getY() == y);
    }

    private static final double Epsilon = 0.0000001;

}
