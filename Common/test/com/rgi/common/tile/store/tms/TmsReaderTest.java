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

package com.rgi.common.tile.store.tms;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.store.TileStoreException;

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
        @SuppressWarnings("unused")
        final TmsReader reader = new TmsReader(null, tmsDir);
    }

    /**
     * Tests if the TMS reader will throw when giving a null parameter
     */
    @SuppressWarnings("static-method")
    @Test(expected = IllegalArgumentException.class)
    public void breakConstructorLocation()
    {
        @SuppressWarnings("unused")
        final TmsReader reader = new TmsReader(new CoordinateReferenceSystem("EPSG", 3857), null);
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
        final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 3857), tmsDir);
        assertTrue(tmsReader.getBounds().getMinX() == -20037508.342789244);
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
        final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 3857), tmsDir);
        assertTrue(tmsReader.getBounds().getMaxX() == 20037508.342789244);
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
        final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 3857), tmsDir);
        assertTrue(tmsReader.getBounds().getMaxY() == 20037508.342789244);
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
        final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 3857), tmsDir);
        assertTrue(tmsReader.getBounds().getMinY() == -20037508.342789244);
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
        final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 4326), tmsDir);
        assertTrue(tmsReader.getBounds().getMinX() == -180.0);
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
        final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 4326), tmsDir);
        assertTrue(tmsReader.getBounds().getMaxX() == 180.0);
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
        final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 4326), tmsDir);
        assertTrue(tmsReader.getBounds().getMaxY() == 90.0);
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
        final TmsReader tmsReader = new TmsReader(new CoordinateReferenceSystem("EPSG", 4326), tmsDir);
        assertTrue(tmsReader.getBounds().getMinY() == -90.0);
    }

    @Test
    public void verifyStream1() throws TileStoreException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 1);

        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("EPSG", 3857);

        final TmsReader reader = new TmsReader(crs, tmsDir);

        final BoundingBox bounds = reader.stream(0).findFirst().get().getBounds();

        assertTrue(CrsProfileFactory.create(crs).getBounds().equals(bounds));
    }
}
