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

package common.coordinates.referencesystem.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.junit.Test;

import com.rgi.android.common.BoundingBox;
import com.rgi.android.common.coordinate.Coordinate;
import com.rgi.android.common.coordinate.CrsCoordinate;
import com.rgi.android.common.coordinate.referencesystem.profile.EllipsoidalMercatorCrsProfile;
import com.rgi.android.common.coordinate.referencesystem.profile.GlobalGeodeticCrsProfile;
import com.rgi.android.common.tile.TileOrigin;
import com.rgi.android.common.tile.scheme.TileMatrixDimensions;

/**
 * @author Jenifer Cochran
 * @author Mary Carome
 *
 */
@SuppressWarnings({"javadoc", "static-method"})
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
        return String.format(Locale.getDefault(), "Latitude: %f, Longitude: %f, MetersX: %f, MetersY: %f", this.latitude, this.longitude, this.metersX, this.metersY);

       }
    }

    @Test
    public void tileToCrsCoordinateBackToTileCoordinate()
    {

        final TileMatrixDimensions matrixDimensions = new TileMatrixDimensions(4, 4);
        final Coordinate<Integer> tileCoordinateExpected = new Coordinate<Integer>(2,3);

        final CrsCoordinate crsCoordinate = this.ellipsoidalCrs.tileToCrsCoordinate(tileCoordinateExpected.getX(), tileCoordinateExpected.getY(), this.ellipsoidalCrs.getBounds(), matrixDimensions, TileOrigin.UpperLeft);
        final Coordinate<Integer> tileCoordinateReturned = this.ellipsoidalCrs.crsToTileCoordinate(crsCoordinate,  this.ellipsoidalCrs.getBounds(), matrixDimensions, TileOrigin.UpperLeft);

        assertEquals(tileCoordinateExpected, tileCoordinateReturned);
    }

    @Test
    public void tileToCrsCoordinateBackToTileCoordinate2()
    {
        final TileMatrixDimensions matrixDimensions = new TileMatrixDimensions(17, 13);

        for(int row = 0; row < matrixDimensions.getHeight(); row++)
        {
            for(int column = 0; column < matrixDimensions.getWidth(); column++)
            {
                final Coordinate<Integer> tileCoordinateExpected = new Coordinate<Integer>(column, row);
                final CrsCoordinate crsCoordinate = this.ellipsoidalCrs.tileToCrsCoordinate(tileCoordinateExpected.getX(), tileCoordinateExpected.getY(), this.ellipsoidalCrs.getBounds(), matrixDimensions, TileOrigin.LowerLeft);
                final Coordinate<Integer> tileCoordinateReturned = this.ellipsoidalCrs.crsToTileCoordinate(crsCoordinate,  this.ellipsoidalCrs.getBounds(), matrixDimensions, TileOrigin.LowerLeft);
                assertEquals(tileCoordinateExpected, tileCoordinateReturned);
            }
        }
    }


    @Test
    public void tileToCrsCoordinateBackToTileCoordinate3()
    {
        final TileMatrixDimensions matrixDimensions = new TileMatrixDimensions(19, 76);
        final BoundingBox bounds = new BoundingBox(this.ellipsoidalCrs.getBounds().getMinX()/2, this.ellipsoidalCrs.getBounds().getMinY()/3, this.ellipsoidalCrs.getBounds().getMaxX()/4, this.ellipsoidalCrs.getBounds().getMaxY()/5);

        final List<TileOrigin> originList = Arrays.asList(TileOrigin.LowerLeft, TileOrigin.LowerRight,TileOrigin.UpperLeft, TileOrigin.UpperRight);

        for(final TileOrigin origin: originList)
        {
            for(int row = 0; row < matrixDimensions.getHeight(); row++)
            {
                for(int column = 0; column < matrixDimensions.getWidth(); column++)
                {
                    final Coordinate<Integer> tileCoordinateExpected = new Coordinate<Integer>(column, row);
                    final CrsCoordinate crsCoordinate = this.ellipsoidalCrs.tileToCrsCoordinate(tileCoordinateExpected.getX(), tileCoordinateExpected.getY(), bounds, matrixDimensions, origin);
                    final Coordinate<Integer> tileCoordinateReturned = this.ellipsoidalCrs.crsToTileCoordinate(crsCoordinate,  bounds, matrixDimensions, origin);
                    assertEquals(tileCoordinateExpected, tileCoordinateReturned);
                }
            }
        }
    }

    @Test
    public void crsToTileCoordinateBackToCrsCoordinate()
    {
        final BoundingBox bounds = new BoundingBox(0,637137*Math.PI, 3189068.5*Math.PI, 3189068.5*Math.PI);
        final TileMatrixDimensions matrixDimensions = new TileMatrixDimensions(8, 6);
        final TileOrigin origin = TileOrigin.UpperLeft;

        this.calculateAndAssertCrsCoordinateEqual(bounds, matrixDimensions, origin, bounds.getTopLeft());
    }

    @Test
    public void tileCoordinateToCrsCoordinateConsistancyTest()
    {
        final BoundingBox bounds = new BoundingBox(0,637137*Math.PI, 3189068.5*Math.PI, 3189068.5*Math.PI);
        final TileMatrixDimensions matrixDimensions = new TileMatrixDimensions(8, 6);
        final List<TileOrigin> originList = Arrays.asList(TileOrigin.LowerLeft, TileOrigin.LowerRight,TileOrigin.UpperLeft, TileOrigin.UpperRight);

        for(final TileOrigin origin: originList)
        {
            final ArrayList<CrsCoordinate> crsList  = this.getListOfCrsCoordinates(bounds, matrixDimensions, origin);

            final ArrayList<CrsCoordinate> crsList2 = this.getListOfCrsCoordinates(bounds, matrixDimensions, origin);

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
        final Coordinate<Double> coordinateBeyondBounds = new Coordinate<Double>(this.ellipsoidalCrs.getBounds().getBottomRight().getX(), this.ellipsoidalCrs.getBounds().getBottomRight().getY() +1);
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
        final File coordinatePointsFile = new File("EllipsoidalMercatorCoordinatePoints.csv");
        final Scanner scanner = new Scanner(coordinatePointsFile);
        try
        {
            scanner.useDelimiter("\n");

            final ArrayList<LatLongMetersYMetersX> coordinatesList = this.readValuesFromFile(scanner);

            final List<LatLongMetersYMetersX> incorrectCoordinates = new ArrayList<LatLongMetersYMetersX>();

            for(final LatLongMetersYMetersX coordinate: coordinatesList)
            {
                if(!verifyCoordinateConversion(coordinate))
                {
                    incorrectCoordinates.add(coordinate);
                }
            }

            final StringBuilder incorrectCoordinate = new StringBuilder();

            if(!incorrectCoordinates.isEmpty())
            {
               for(final LatLongMetersYMetersX coordinate: incorrectCoordinates)
               {
                   incorrectCoordinate.append(coordinate.toString());
                   incorrectCoordinate.append("\n");
               }
            }
            assertTrue(String.format(Locale.getDefault(),
                                     "Number of incorrect coordinates: %d out of 100\nFollowing coordinates did not convert correctly.\n%s.",
                                     incorrectCoordinates.size(),
                                     incorrectCoordinate),
                       incorrectCoordinates.isEmpty());
        }
        finally
        {
            scanner.close();
        }
    }

    /**
     * Tests getTileBounds throws an IllegalArgumentException when one of the
     * parameters is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileBoundsIllegalArgumentException1()
    {
        final EllipsoidalMercatorCrsProfile profile = new EllipsoidalMercatorCrsProfile();
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(10, 10);
        final TileOrigin origin = TileOrigin.UpperRight;

        profile.getTileBounds(10, 12, null, dimensions, origin);
        fail("Expected EllipsoidalMercatorCrsProfile method getTileBounds to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests getTileBounds throws an IllegalArgumentException when one of the
     * parameters is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileBoundsIllegalArgumentException2()
    {
        final EllipsoidalMercatorCrsProfile profile = new EllipsoidalMercatorCrsProfile();
        final BoundingBox box = new BoundingBox(0, 10, 12, 24);
        final TileOrigin origin = TileOrigin.UpperRight;

        profile.getTileBounds(10, 12, box, null, origin);
        fail("Expected EllipsoidalMercatorCrsProfile method getTileBounds to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests getTileBounds throws an IllegalArgumentException when the row and
     * column are not in the BoundingBox
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileBoundsIllegalArgumentException3()
    {
        final EllipsoidalMercatorCrsProfile profile = new EllipsoidalMercatorCrsProfile();
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(10, 10);
        final BoundingBox box = new BoundingBox(0, 10, 12, 24);
        final TileOrigin origin = TileOrigin.UpperRight;

        profile.getTileBounds(10, 12, box, dimensions, origin);
        fail("Expected EllisoidalMercatorCrsProfile method getTileBounds to throw an IllegalArgumentException when row and column are not in the BoundingBox.");
    }

    /**
     * Tests getTileBounds throws an IllegalArgumentException when the column is
     * a negative number
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileBoundsIllegalArgumentException4()
    {
        final EllipsoidalMercatorCrsProfile profile = new EllipsoidalMercatorCrsProfile();
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(10, 10);
        final BoundingBox box = new BoundingBox(-20, 10, -13, 24);
        final TileOrigin origin = TileOrigin.UpperRight;
        final int row = 12;
        final int col = -10;

        profile.getTileBounds(col, row, box, dimensions, origin);
        fail("Expected EllipsoidalMercatorCrsProfile method getTileBounds to throw an IllegalArgumentException when the column value is negative.");
    }

    /**
     * Tests getTileBounds throws an IllegalArgumentException when the row is a
     * negative number
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileBoundsIllegalArgumentException5()
    {
        final EllipsoidalMercatorCrsProfile profile = new EllipsoidalMercatorCrsProfile();
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(10, 10);
        final BoundingBox box = new BoundingBox(-20, 10, -13, 24);
        final TileOrigin origin = TileOrigin.UpperRight;
        final int row = -12;
        final int col = 10;

        profile.getTileBounds(col, row, box, dimensions, origin);
        fail("Expected EllipsoidalMercatorCrsProfile method getTileBounds to throw an IllegalArgumentException when the row value is negative.");
    }

    /**
     * Tests getTileBounds throws an IllegalArgumentException when the row is a
     * negative number
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileBoundsIllegalArgumentException6()
    {
        final EllipsoidalMercatorCrsProfile profile = new EllipsoidalMercatorCrsProfile();
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(10, 10);
        final BoundingBox box = new BoundingBox(0, 10, 0, 10);

        profile.getTileBounds(5, 5, box, dimensions, null);
        fail("Expected EllipsoidalMercatorCrsProfile method getTileBounds to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests that getName returns the correct String name for the
     * GlobalGeodeticCrsProfile
     */
    @Test
    public void testGetName()
    {
        final EllipsoidalMercatorCrsProfile profile = new EllipsoidalMercatorCrsProfile();
        final String name = "World Mercator";
        assertTrue(String.format("Expected the EllipsoidalMercatorCrsProfile method getName() to return %s, but %s was returned", name, profile.getName()), profile.getName().equals(name));
    }

    /**
     * Tests that getWellKnownText returns the correct String name for the
     * GlobalGeodeticCrsProfile
     */
    @Test
    public void testGetWellKnownText()
    {
        final EllipsoidalMercatorCrsProfile profile = new EllipsoidalMercatorCrsProfile();
        final String text = "PROJCS[\"WGS 84 / World Mercator\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],PROJECTION[\"Mercator_1SP\"],PARAMETER[\"central_meridian\",0],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",0],PARAMETER[\"false_northing\",0],AUTHORITY[\"EPSG\",\"3395\"],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH]]";
        assertTrue(String.format("Expected the EllipsoidalMercatorCrsProfile method getWellKnownTest() to return %s, but %s was returned", text, profile.getWellKnownText()),
                   profile.getWellKnownText().equals(text));
    }

    /**
     * Tests that getDescription returns the correct String name for the
     * GlobalGeodeticCrsProfile
     */
    @Test
    public void testGetDescription()
    {
        final EllipsoidalMercatorCrsProfile profile = new EllipsoidalMercatorCrsProfile();
        final String description = "World (Ellipsoidal) Mercator";
        assertTrue(String.format("Expected the EllipsoidalMercatorCrsProfile method getDescription() to return %s, but %s was returned.", description, profile.getDescription()),
                   profile.getDescription().equals(description));
    }

    private static boolean verifyCoordinateConversion(final LatLongMetersYMetersX coordinate)
    {
        final CrsCoordinate                 metersCoordinate   = new CrsCoordinate(coordinate.metersX, coordinate.metersY, "epsg", 3395);
        final EllipsoidalMercatorCrsProfile ellipsoidalCrs     = new EllipsoidalMercatorCrsProfile();
        final Coordinate<Double>            coordinateReturned = ellipsoidalCrs.toGlobalGeodetic(metersCoordinate);
        final Coordinate<Double>            coordinateExpected = new Coordinate<Double>(coordinate.longitude, coordinate.latitude);
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
        final ArrayList<LatLongMetersYMetersX> coordinatesList = new ArrayList<LatLongMetersYMetersX>();
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

    private ArrayList<CrsCoordinate> getListOfCrsCoordinates(final BoundingBox bounds, final TileMatrixDimensions matrixDimensions, final TileOrigin origin)
    {
        final ArrayList<CrsCoordinate> crsCoordinateList = new ArrayList<CrsCoordinate>();
        for(int row = 0; row < matrixDimensions.getHeight(); row++)
        {
            for(int column = 0; column < matrixDimensions.getWidth(); column++)
            {
                final CrsCoordinate crsCoordinateReturned = this.ellipsoidalCrs.tileToCrsCoordinate(column, row, bounds, matrixDimensions, origin);
                crsCoordinateList.add(crsCoordinateReturned);
            }
        }
        return crsCoordinateList;
    }

    private static void assertCrsCoordinatesEqual(final CrsCoordinate crsCoordinate, final CrsCoordinate crsCoordinateReturned)
    {
        assertTrue(isEqual(crsCoordinate,crsCoordinateReturned) && crsCoordinate.getCoordinateReferenceSystem().equals(crsCoordinate.getCoordinateReferenceSystem()));
    }

    private void calculateAndAssertCrsCoordinateEqual(final BoundingBox bounds, final TileMatrixDimensions matrixDimensions, final TileOrigin origin, final Coordinate<Double> coordinate)
    {
        final CrsCoordinate crsCoordinate = new CrsCoordinate(coordinate, this.ellipsoidalCrs.getCoordinateReferenceSystem());
        final Coordinate<Integer> tileCoordinate = this.ellipsoidalCrs.crsToTileCoordinate(crsCoordinate, bounds, matrixDimensions, origin);
        final CrsCoordinate crsCoordinateReturned = this.ellipsoidalCrs.tileToCrsCoordinate(tileCoordinate.getX(), tileCoordinate.getY(), bounds, matrixDimensions, origin);

        EllipsoidalMercatorCrsProfileTest.assertCrsCoordinatesEqual(crsCoordinate, crsCoordinateReturned);
    }


    private static final double Epsilon = 0.0000001;
}