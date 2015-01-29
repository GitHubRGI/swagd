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

import com.rgi.common.coordinates.AbsoluteTileCoordinate;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.coordinates.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinates.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.TileOrigin;
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

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();
	private final Random randomGenerator = new Random();
//	private TmsReader tmsReader;
//	private TmsWriter tmsWriter;
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


    @Test(expected = IllegalArgumentException.class)
	public void breakConstructorProfile() {
		this.tmsDir = this.createTMSFolderMercator(4);
		@SuppressWarnings("unused")
		TmsReader reader = new TmsReader(null, this.tmsDir);
	}

	@Test(expected = IllegalArgumentException.class)
	public void breakConstructorLocation() {
		this.tmsDir = this.createTMSFolderMercator(4);
		@SuppressWarnings("unused")
        TmsReader reader = new TmsReader(CrsProfileFactory.create("EPSG", 3857), null);
	}

	@Test
	public void verifyMercBoundsCalcLeft() throws TileStoreException {
		this.tmsDir = this.createTMSFolderMercator(4);
		TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 3857), this.tmsDir);
		assertTrue(tmsReader.getBounds().getMinX().doubleValue() == -20037508.342789244);
	}

	@Test
	public void verifyMercBoundsCalcRight() throws TileStoreException {
		this.tmsDir = this.createTMSFolderMercator(4);
		TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 3857), this.tmsDir);
		assertTrue(tmsReader.getBounds().getMaxX().doubleValue() == 20037508.342789244);
	}

	@Test
	public void verifyMercBoundsCalcTop() throws TileStoreException {
		this.tmsDir = this.createTMSFolderMercator(4);
		TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 3857), this.tmsDir);
		assertTrue(tmsReader.getBounds().getMaxY().doubleValue() == 20037508.342789244);
	}

	@Test
	public void verifyMercBoundsCalcBottom() throws TileStoreException {
		this.tmsDir = this.createTMSFolderMercator(4);
		TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 3857), this.tmsDir);
		assertTrue(tmsReader.getBounds().getMinY().doubleValue() == -20037508.342789244);
	}

	@Test
	public void verifyGeodBoundsCalcLeft() throws TileStoreException {
		this.tmsDir = this.createTMSFolderGeodetic(4);
		TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 4326), this.tmsDir);
		assertTrue(tmsReader.getBounds().getMinX().doubleValue() == -180.0);
	}

	@Test
	public void verifyGeodBoundsCalcRight() throws TileStoreException {
		this.tmsDir = this.createTMSFolderGeodetic(4);
		TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 4326), this.tmsDir);
		assertTrue(tmsReader.getBounds().getMaxX().doubleValue() == 180.0);
	}

	@Test
	public void verifyGeodBoundsCalcTop() throws TileStoreException {
		this.tmsDir = this.createTMSFolderGeodetic(4);
		TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 4326), this.tmsDir);
		assertTrue(tmsReader.getBounds().getMaxY().doubleValue() == 90.0);
	}

	@Test
	public void verifyGeodBoundsCalcBottom() throws TileStoreException {
		this.tmsDir = this.createTMSFolderGeodetic(4);
		TmsReader tmsReader = new TmsReader(CrsProfileFactory.create("EPSG", 4326), this.tmsDir);
		assertTrue(tmsReader.getBounds().getMinY().doubleValue() == -90.0);
	}

	@Test
	public void verifyTileRetrieval() throws TileStoreException, MimeTypeParseException {
		this.tmsDir = this.createTMSFolderMercator(4);

		final int zoomLevel = 1;
		final CrsProfile crsProfile = CrsProfileFactory.create("EPSG", 3857);

		TmsWriter tmsWriter = new TmsWriter(crsProfile, this.tmsDir, new MimeType("image", "jpeg"));
		TmsReader tmsReader = new TmsReader(crsProfile, this.tmsDir);

		final AbsoluteTileCoordinate tileCoord = new AbsoluteTileCoordinate(0, 0, zoomLevel, TileOrigin.LowerLeft);

		final CrsCoordinate crsCoordinate = crsProfile.tileToCrsCoordinate(tileCoord);

		final BufferedImage image = createImage();

		tmsWriter.addTile(crsCoordinate, zoomLevel, image);

		final BufferedImage tileImage = tmsReader.getTile(crsCoordinate, zoomLevel);

		assertTrue(bufferedImagesEqual(image, tileImage));
	}

	@Test
	public void verifyTileInsertion() throws TileStoreException, MimeTypeParseException {
		this.tmsDir = this.createTMSFolderMercator(4);

		final CrsProfile crsProfile = CrsProfileFactory.create("EPSG", 3857);

		TmsWriter tmsWriter = new TmsWriter(crsProfile, this.tmsDir, new MimeType("image", "jpeg"));
		final Path tilePath = this.tmsDir.resolve("5").resolve("0").resolve("0.png");
		final BufferedImage img = createImage();
		final AbsoluteTileCoordinate tileCoord = new AbsoluteTileCoordinate(0, 0, 5, TileOrigin.LowerLeft);
		tmsWriter.addTile(crsProfile.tileToCrsCoordinate(tileCoord), 5, img);
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
