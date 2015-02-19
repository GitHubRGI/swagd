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

package com.rgi.common.tile.store;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.store.tms.TmsReader;
import com.rgi.common.tile.store.tms.TmsWriter;

/**
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 * TODO this was written before the TileStore split into TileStoreReader and TileStoreWriter.  Tests may need to be expanded or rewritten.
 *
 */
public class TMSTileStoreTest {

    /**
     * The folder that the files will be saved to
     */
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    private final Random randomGenerator = new Random();
    private Path tmsDir;

    private String getRanString(final int length) {
        final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(this.randomGenerator.nextInt(characters.length()));
        }
        return new String(text);
    }

    private Path createTMSFolderMercator(final int zooms) {
        try {
            final File tmsFolder = this.testFolder.newFolder(this.getRanString(8));
            for (int i = 0; i < zooms; i++) {
                for (int j = 0; j < Math.pow(2, i); j++) {
                    final String[] rowPath = { tmsFolder.getName().toString(), String.valueOf(i), String.valueOf(j) };
                    final File thisRow = this.testFolder.newFolder(rowPath);
                    for (int k = 0; k < Math.pow(2, i); k++) {
                        final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
                        final Path thisColumn = thisRow.toPath().resolve(String.valueOf(k) + ".png");
                        ImageIO.write(img, "PNG", thisColumn.toFile());
                    }
                }
            }
            return tmsFolder.toPath();
        } catch (final IOException ioException) {
            System.err.println("Could not generate TMS directory structure." + "\n" + ioException.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unused")
    private Path createInvalidTMSFolderMercator(final int zooms) {
        try {
            final File tmsFolder = this.testFolder.newFolder(this.getRanString(8));
            for (int i = 0; i < zooms; i++) {
                for (int j = 0; j < Math.pow(2, i); j++) {
                    final String[] rowPath = { tmsFolder.getName().toString(), String.valueOf(i), String.valueOf(j) };
                    final File thisRow = this.testFolder.newFolder(rowPath);
                    for (int k = 0; k < Math.pow(2, i); k++) {
                        final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
                        final Path thisColumn = thisRow.toPath().resolve(this.getRanString(3) + ".png");
                        ImageIO.write(img, "PNG", thisColumn.toFile());
                    }
                }
            }
            return tmsFolder.toPath();
        } catch (final IOException ioException) {
            System.err.println("Could not generate TMS directory structure." + "\n" + ioException.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unused")
    private Path createEmptyTMSFolderMercator(final int zooms) {
        try {
            final File tmsFolder = this.testFolder.newFolder(this.getRanString(8));
            for (int i = 0; i < zooms; i++) {
                for (int j = 0; j < Math.pow(2, i); j++) {
                    final String[] rowPath = { tmsFolder.getName().toString(), String.valueOf(i), String.valueOf(j) };
                    final File thisRow = this.testFolder.newFolder(rowPath);
                }
            }
            return tmsFolder.toPath();
        } catch (final IOException ioException) {
            System.err.println("Could not generate TMS directory structure." + "\n" + ioException.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unused")
    private Path createInvalidTMSRowFoldersMercator(final int zooms) {
        try {
            final File tmsFolder = this.testFolder.newFolder(this.getRanString(8));
            for (int i = 0; i < zooms; i++) {
                for (int j = 0; j < Math.pow(2, i); j++) {
                    final String[] rowPath = { tmsFolder.getName().toString(), String.valueOf(i), this.getRanString(3) };
                    final File thisRow = this.testFolder.newFolder(rowPath);
                }
            }
            return tmsFolder.toPath();
        } catch (final IOException ioException) {
            System.err.println("Could not generate TMS directory structure." + "\n" + ioException.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unused")
    private Path createEmptyTMSRowFoldersMercator(final int zooms) {
        try {
            final File tmsFolder = this.testFolder.newFolder(this.getRanString(8));
            for (int i = 0; i < zooms; i++) {
                final String[] zoomPath = { tmsFolder.getName().toString(), String.valueOf(i) };
                final File thisZoom = this.testFolder.newFolder(zoomPath);
            }
            return tmsFolder.toPath();
        } catch (final IOException ioException) {
            System.err.println("Could not generate TMS directory structure." + "\n" + ioException.getMessage());
            return null;
        }
    }

    private Path createTMSFolderGeodetic(final int zooms) {
        try {
            final File tmsFolder = this.testFolder.newFolder(this.getRanString(8));
            for (int i = 0; i < zooms; i++) {
                for (int j = 0; j < Math.pow(2, i); j++) {
                    final String[] rowPath = { tmsFolder.getName().toString(), String.valueOf(i), String.valueOf(j) };
                    final File thisRow = this.testFolder.newFolder(rowPath);
                    for (int k = 0; k < Math.pow(2, (i - 1)); k++) {
                        final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
                        final Path thisColumn = thisRow.toPath().resolve(String.valueOf(k) + ".png");
                        ImageIO.write(img, "PNG", thisColumn.toFile());
                    }
                }
            }
            return tmsFolder.toPath();
        } catch (final IOException ioException) {
            System.err.println("Could not generate TMS directory structure." + "\n" + ioException.getMessage());
            return null;
        }
    }


    /**
     * Tests if the TMS reader will throw when giving a null parameter
     */
    @Test(expected = IllegalArgumentException.class)
    public void breakConstructorProfile() {
        this.tmsDir = this.createTMSFolderMercator(4);
        @SuppressWarnings("unused")
        TmsReader reader = new TmsReader(null, this.tmsDir);
    }

    /**
     * Tests if the TMS reader will throw when giving a null parameter
     */
    @Test(expected = IllegalArgumentException.class)
    public void breakConstructorLocation() {
        this.tmsDir = this.createTMSFolderMercator(4);
        @SuppressWarnings("unused")
        TmsReader reader = new TmsReader(CrsProfileFactory.create("EPSG", 3857), null);
    }

    /**
     * Tests if the tmsReader will return the correct minimum X value of a bounding box from a tmsFolder
     * @throws TileStoreException throws if a tileStoreException occurs
     */
    @Test
    public void verifyMercBoundsCalcLeft() throws TileStoreException {
        this.tmsDir = this.createTMSFolderMercator(4);
        TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 3857), this.tmsDir);
        assertTrue(tmsReader.getBounds().getMinX() == -20037508.342789244);
    }

    /**
     * Tests if the tmsReader will return the correct maximum X value of a bounding box from a tmsFolder
     * @throws TileStoreException throws if a tileStoreException occurs
     */
    @Test
    public void verifyMercBoundsCalcRight() throws TileStoreException {
        this.tmsDir = this.createTMSFolderMercator(4);
        TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 3857), this.tmsDir);
        assertTrue(tmsReader.getBounds().getMaxX() == 20037508.342789244);
    }
    
    /**
     * Tests if the tmsReader will return the correct maximum Y value of a bounding box from a tmsFolder
     * @throws TileStoreException throws if a tileStoreException occurs
     */
    @Test
    public void verifyMercBoundsCalcTop() throws TileStoreException {
        this.tmsDir = this.createTMSFolderMercator(4);
        TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 3857), this.tmsDir);
        assertTrue(tmsReader.getBounds().getMaxY() == 20037508.342789244);
    }
    
    /**
     * Tests if the tmsReader will return the correct minimum Y value of a bounding box from a tmsFolder
     * @throws TileStoreException throws if a tileStoreException occurs
     */
    @Test
    public void verifyMercBoundsCalcBottom() throws TileStoreException {
        this.tmsDir = this.createTMSFolderMercator(4);
        TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 3857), this.tmsDir);
        assertTrue(tmsReader.getBounds().getMinY() == -20037508.342789244);
    }
    
    /**
     * Tests if the tmsReader will return the correct minimum X value of a bounding box from a tmsFolder
     * @throws TileStoreException throws if a tileStoreException occurs
     */
    @Test
    public void verifyGeodBoundsCalcLeft() throws TileStoreException {
        this.tmsDir = this.createTMSFolderGeodetic(4);
        TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 4326), this.tmsDir);
        assertTrue(tmsReader.getBounds().getMinX() == -180.0);
    }
    /**
     * Tests if the tmsReader will return the correct maximum X value of a bounding box from a tmsFolder
     * @throws TileStoreException throws if a tileStoreException occurs
     */
    @Test
    public void verifyGeodBoundsCalcRight() throws TileStoreException {
        this.tmsDir = this.createTMSFolderGeodetic(4);
        TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 4326), this.tmsDir);
        assertTrue(tmsReader.getBounds().getMaxX() == 180.0);
    }

    /**
     * Tests if the tmsReader will return the correct maximum Y value of a bounding box from a tmsFolder
     * @throws TileStoreException throws if a tileStoreException occurs
     */
    @Test
    public void verifyGeodBoundsCalcTop() throws TileStoreException {
        this.tmsDir = this.createTMSFolderGeodetic(4);
        TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 4326), this.tmsDir);
        assertTrue(tmsReader.getBounds().getMaxY() == 90.0);
    }
    /**
     * Tests if the tmsReader will return the correct minimum Y value of a bounding box from a tmsFolder
     * @throws TileStoreException throws if a tileStoreException occurs
     */
    @Test
    public void verifyGeodBoundsCalcBottom() throws TileStoreException {
        this.tmsDir = this.createTMSFolderGeodetic(4);
        TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 4326), this.tmsDir);
        assertTrue(tmsReader.getBounds().getMinY() == -90.0);
    }

    /**
     * Tests if the tms reader returns the expected tile
     * @throws TileStoreException throws if a tileStoreException occurs
     * @throws MimeTypeParseException throws if the MimeType cannot be detected
     */
    @Test
    public void verifyTileRetrieval() throws TileStoreException, MimeTypeParseException
    {
        this.tmsDir = this.createTMSFolderMercator(4);

        final int zoomLevel = 1;
        final Coordinate<Integer> coordinate = new Coordinate<>(0, 0);
        final CrsProfile crsProfile = CrsProfileFactory.create("EPSG", 3857);

        TmsWriter tmsWriter = new TmsWriter(crsProfile, this.tmsDir, new MimeType("image", "png"));
        TmsReader tmsReader = new TmsReader(crsProfile, this.tmsDir);

        final BufferedImage image = createImage();

        tmsWriter.addTile(coordinate.getY(),
                          coordinate.getX(),
                          zoomLevel,
                          image);

        final BufferedImage tileImage = tmsReader.getTile(coordinate.getY(),
                                                          coordinate.getX(),
                                                          zoomLevel);

        assertTrue(bufferedImagesEqual(image, tileImage));
    }

    /**
     * 
     * @throws TileStoreException
     * @throws MimeTypeParseException
     */
    @SuppressWarnings("javadoc")
    @Test
    public void verifyTileInsertion() throws TileStoreException, MimeTypeParseException
    {
        this.tmsDir = this.createTMSFolderMercator(4);

        final int zoomLevel = 5;
        final Coordinate<Integer> coordinate = new Coordinate<>(0, 0);

        TmsWriter tmsWriter = new TmsWriter(CrsProfileFactory.create("EPSG", 3857),
                                            this.tmsDir,
                                            new MimeType("image", "png"));

        final Path tilePath = this.tmsDir
                                  .resolve(Integer.toString(zoomLevel))
                                  .resolve(Integer.toString(coordinate.getX()))
                                  .resolve(Integer.toString(coordinate.getX()) + ".png");

        final BufferedImage img = createImage();

        tmsWriter.addTile(coordinate.getY(),
                          coordinate.getX(),
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
