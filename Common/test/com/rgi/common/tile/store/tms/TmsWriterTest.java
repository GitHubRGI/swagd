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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.tile.store.TileStoreException;

/**
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
@SuppressWarnings("javadoc")
public class TmsWriterTest
{
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Tests if the tms reader returns the expected tile
     * @throws TileStoreException throws if a tileStoreException occurs
     * @throws MimeTypeParseException throws if the MimeType cannot be detected
     */
    @Test
    public void verifyTileRetrieval() throws TileStoreException, MimeTypeParseException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 4);

        final int zoomLevel = 1;
        final Coordinate<Integer> coordinate = new Coordinate<>(0, 0);
        final CoordinateReferenceSystem coordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 3857);

        final TmsWriter tmsWriter = new TmsWriter(coordinateReferenceSystem, tmsDir, new MimeType("image", "png"));
        final TmsReader tmsReader = new TmsReader(coordinateReferenceSystem, tmsDir);

        final BufferedImage image = createImage();

        tmsWriter.addTile(coordinate.getX(),
                          coordinate.getY(),
                          zoomLevel,
                          image);

        final BufferedImage tileImage = tmsReader.getTile(coordinate.getX(),
                                                          coordinate.getY(),
                                                          zoomLevel);

        assertTrue(bufferedImagesEqual(image, tileImage));
    }

    @Test
    public void verifyTileInsertion() throws TileStoreException, MimeTypeParseException
    {
        final Path tmsDir = TmsUtility.createTMSFolderMercator(this.tempFolder, 4);

        final int zoomLevel = 5;
        final Coordinate<Integer> coordinate = new Coordinate<>(0, 0);

        final CoordinateReferenceSystem coordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 3857);

        final TmsWriter tmsWriter = new TmsWriter(coordinateReferenceSystem,
                                                  tmsDir,
                                                  new MimeType("image", "png"));

        final Path tilePath = tmsDir.resolve(Integer.toString(zoomLevel))
                                    .resolve(Integer.toString(coordinate.getX()))
                                    .resolve(Integer.toString(coordinate.getX()) + ".png");

        final BufferedImage img = createImage();

        tmsWriter.addTile(coordinate.getX(),
                          coordinate.getY(),
                          zoomLevel,
                          img);

        assertTrue(tilePath.toFile().exists());
    }

    private static BufferedImage createImage()
    {
        final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = img.createGraphics();
        graphics.setPaint(new Color(255, 0, 0));
        graphics.fillRect(0, 0, 256, 256);
        return img;
    }

    private static boolean bufferedImagesEqual(final BufferedImage img1, final BufferedImage img2)
    {
        if(img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight())
        {
            return false;
        }

        for(int x = 0; x < img1.getWidth(); x++)
        {
            for(int y = 0; y < img1.getHeight(); y++)
            {
                if(img1.getRGB(x, y) != img2.getRGB(x, y))
                {
                    return false;
                }
            }
        }

        return true;
    }
}
