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

package tms;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.tms.TmsReader;

/**
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
@SuppressWarnings("javadoc")
public class TmsReaderTest
{
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Tests if the TMS reader will throw when giving a null parameter
     */
    @Test(expected = IllegalArgumentException.class)
    public void breakConstructorProfile()
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 4);
        try(final TmsReader reader = new TmsReader(null, tmsDir))
        {
            // constructor should throw
        }
    }

    /**
     * Tests if the TMS reader will throw when giving a null parameter
     */
    @SuppressWarnings("static-method")
    @Test(expected = IllegalArgumentException.class)
    public void breakConstructorLocation()
    {
        try(final TmsReader reader = new TmsReader(new CoordinateReferenceSystem("EPSG", 3857), null))
        {
            // constructor should throw
        }
    }

    /**
     * Tests if the tmsReader will return the correct minimum X value of a
     * bounding box from a tmsFolder
     *
     * @throws TileStoreException
     *             throws if a tileStoreException occurs
     */
    @Test
    public void verifyMercBoundsCalcLeft() throws TileStoreException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 4);
        try(final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 3857), tmsDir))
        {
            assertTrue(tmsReader.getBounds().getMinX() == -20037508.342789244);
        }
    }

    /**
     * Tests if the tmsReader will return the correct maximum X value of a
     * bounding box from a tmsFolder
     *
     * @throws TileStoreException
     *             throws if a tileStoreException occurs
     */
    @Test
    public void verifyMercBoundsCalcRight() throws TileStoreException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 4);
        try(final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 3857), tmsDir))
        {
            assertTrue(tmsReader.getBounds().getMaxX() == 20037508.342789244);
        }
    }

    /**
     * Tests if the tmsReader will return the correct maximum Y value of a
     * bounding box from a tmsFolder
     *
     * @throws TileStoreException
     *             throws if a tileStoreException occurs
     */
    @Test
    public void verifyMercBoundsCalcTop() throws TileStoreException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 4);
        try(final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 3857), tmsDir))
        {
            assertTrue(tmsReader.getBounds().getMaxY() == 20037508.342789244);
        }
    }

    /**
     * Tests if the tmsReader will return the correct minimum Y value of a
     * bounding box from a tmsFolder
     *
     * @throws TileStoreException
     *             throws if a tileStoreException occurs
     */
    @Test
    public void verifyMercBoundsCalcBottom() throws TileStoreException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 4);
        try(final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 3857), tmsDir))
        {
            assertTrue(tmsReader.getBounds().getMinY() == -20037508.342789244);
        }
    }

    /**
     * Tests if the tmsReader will return the correct minimum X value of a
     * bounding box from a tmsFolder
     *
     * @throws TileStoreException
     *             throws if a tileStoreException occurs
     */
    @Test
    public void verifyGeodBoundsCalcLeft() throws TileStoreException
    {
        final Path tmsDir = TmsUtility.createTMSFolderGeodetic(this.tempFolder, 4);
        try(final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 4326), tmsDir))
        {
            assertTrue(tmsReader.getBounds().getMinX() == -180.0);
        }
    }

    /**
     * Tests if the tmsReader will return the correct maximum X value of a
     * bounding box from a tmsFolder
     *
     * @throws TileStoreException
     *             throws if a tileStoreException occurs
     */
    @Test
    public void verifyGeodBoundsCalcRight() throws TileStoreException
    {
        final Path tmsDir = TmsUtility.createTMSFolderGeodetic(this.tempFolder, 4);
        try(final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 4326), tmsDir))
        {
            assertTrue(tmsReader.getBounds().getMaxX() == 180.0);
        }
    }

    /**
     * Tests if the tmsReader will return the correct maximum Y value of a
     * bounding box from a tmsFolder
     *
     * @throws TileStoreException
     *             throws if a tileStoreException occurs
     */
    @Test
    public void verifyGeodBoundsCalcTop() throws TileStoreException
    {
        final Path tmsDir = TmsUtility.createTMSFolderGeodetic(this.tempFolder, 4);
        try(final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 4326), tmsDir))
        {
            assertTrue(tmsReader.getBounds().getMaxY() == 90.0);
        }
    }

    /**
     * Tests if the tmsReader will return the correct minimum Y value of a
     * bounding box from a tmsFolder
     *
     * @throws TileStoreException
     *             throws if a tileStoreException occurs
     */
    @Test
    public void verifyGeodBoundsCalcBottom() throws TileStoreException
    {
        final Path tmsDir = TmsUtility.createTMSFolderGeodetic(this.tempFolder, 4);
        try(final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 4326), tmsDir))
        {
            assertTrue(tmsReader.getBounds().getMinY() == -90.0);
        }
    }

    @Test
    public void verifyStream1() throws TileStoreException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 1);

        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("EPSG", 3857);

        try(final TmsReader reader = new TmsReader(crs, tmsDir))
        {
            final BoundingBox bounds = reader.stream(0).findFirst().get().getBounds();
            assertTrue(CrsProfileFactory.create(crs).getBounds().equals(bounds));
        }
    }
}
