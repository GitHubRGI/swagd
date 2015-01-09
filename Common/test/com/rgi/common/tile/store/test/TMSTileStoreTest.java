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

package com.rgi.common.tile.store.test;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.rgi.common.coordinates.AbsoluteTileCoordinate;
import com.rgi.common.tile.Tile;
import com.rgi.common.tile.TileException;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.profile.TileProfileFactory;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TmsTileStore;

public class TMSTileStoreTest {
	// TODO fix these tests
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();
	private final Random randomGenerator = new Random();
	private TmsTileStore tmsStore;
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
		this.tmsStore = new TmsTileStore(null, this.tmsDir);
	}

	@Test(expected = IllegalArgumentException.class)
	public void breakConstructorLocation() {
		this.tmsDir = this.createTMSFolderMercator(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 3857), null);
	}

	@Test(expected = TileStoreException.class)
	public void breakEmptyTMSDirectory() {
		this.tmsDir = this.createEmptyTMSFolderMercator(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 3857), this.tmsDir);
	}
	@Test(expected = TileStoreException.class)
	public void breakInvalidTMSRowFoldersLowerLeft() {
		this.tmsDir = this.createInvalidTMSRowFoldersMercator(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 3857), this.tmsDir);
	}

	@Test(expected = TileStoreException.class)
	public void breakInvalidTMSRowFoldersUpperLeft() {
		this.tmsDir = this.createInvalidTMSRowFoldersMercator(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 3857), this.tmsDir);
	}

	@Test(expected = TileStoreException.class)
	public void breakEmptyTMSRowFoldersLowerLeft() {
		this.tmsDir = this.createEmptyTMSRowFoldersMercator(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 3857), this.tmsDir);
	}

	@Test(expected = TileStoreException.class)
	public void breakEmptyTMSRowFoldersUpperLeft() {
		this.tmsDir = this.createEmptyTMSRowFoldersMercator(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 3857), this.tmsDir);
	}

	@Test(expected = TileStoreException.class)
	public void breakMismatchedMimeTypes() throws TileStoreException, IOException {
		this.tmsDir = this.createTMSFolderMercator(4);
		final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		final Path zoomOneRowZero = this.tmsDir.resolve("1").resolve("0").resolve("0" + ".jpeg");
		ImageIO.write(img, "JPEG", zoomOneRowZero.toFile());
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 3857), this.tmsDir);
	}

	@Test(expected = TileStoreException.class)
	public void breakBoundsCalc() throws TileStoreException {
		this.tmsDir = this.createTMSFolderMercator(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 3857), this.tmsDir);
	}

	@Test
	public void verifyMercBoundsCalcLeft() throws TileStoreException {
		this.tmsDir = this.createTMSFolderMercator(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 3857), this.tmsDir);
		assertTrue(this.tmsStore.calculateBounds().getMinX().doubleValue() == -20037508.342789244);
	}

	@Test
	public void verifyMercBoundsCalcRight() throws TileStoreException {
		this.tmsDir = this.createTMSFolderMercator(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 3857), this.tmsDir);
		assertTrue(this.tmsStore.calculateBounds().getMaxX().doubleValue() == 20037508.342789244);
	}

	@Test
	public void verifyMercBoundsCalcTop() throws TileStoreException {
		this.tmsDir = this.createTMSFolderMercator(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 3857), this.tmsDir);
		assertTrue(this.tmsStore.calculateBounds().getMaxY().doubleValue() == 20037508.342789244);
	}

	@Test
	public void verifyMercBoundsCalcBottom() throws TileStoreException {
		this.tmsDir = this.createTMSFolderMercator(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 3857), this.tmsDir);
		assertTrue(this.tmsStore.calculateBounds().getMinY().doubleValue() == -20037508.342789244);
	}

	@Test
	public void verifyGeodBoundsCalcLeft() throws TileStoreException {
		this.tmsDir = this.createTMSFolderGeodetic(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 4326), this.tmsDir);
		assertTrue(this.tmsStore.calculateBounds().getMinX().doubleValue() == -180.0);
	}

	@Test
	public void verifyGeodBoundsCalcRight() throws TileStoreException {
		this.tmsDir = this.createTMSFolderGeodetic(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 4326), this.tmsDir);
		assertTrue(this.tmsStore.calculateBounds().getMaxX().doubleValue() == 180.0);
	}

	@Test
	public void verifyGeodBoundsCalcTop() throws TileStoreException {
		this.tmsDir = this.createTMSFolderGeodetic(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 4326), this.tmsDir);
		assertTrue(this.tmsStore.calculateBounds().getMaxY().doubleValue() == 90.0);
	}

	@Test
	public void verifyGeodBoundsCalcBottom() throws TileStoreException {
		this.tmsDir = this.createTMSFolderGeodetic(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 4326), this.tmsDir);
		assertTrue(this.tmsStore.calculateBounds().getMinY().doubleValue() == -90.0);
	}

	@Test
	public void verifyTileRetrieval() throws TileException {
		this.tmsDir = this.createTMSFolderMercator(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 3857), this.tmsDir);
		final AbsoluteTileCoordinate tileCoord = new AbsoluteTileCoordinate(0, 0, 1, TileOrigin.LowerLeft);
		final Tile testTile = this.tmsStore.getTile(tileCoord);
		assertTrue(testTile.getTileRow() == 0 && testTile.getTileColumn() == 0 && testTile.getZoomLevel() == 1);
	}

	@Test
	public void verifyTileInsertion() throws TileException {
		this.tmsDir = this.createTMSFolderMercator(4);
		this.tmsStore = new TmsTileStore(TileProfileFactory.create("EPSG", 3857), this.tmsDir);
		final Path tilePath = this.tmsDir.resolve("5").resolve("0").resolve("0.png");
		final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = img.createGraphics();
		graphics.setPaint(new Color(255, 0, 0));
		graphics.fillRect(0, 0, 256, 256);
		final AbsoluteTileCoordinate tileCoord = new AbsoluteTileCoordinate(0, 0, 5, TileOrigin.LowerLeft);
		this.tmsStore.addTile(tileCoord, img);
		assertTrue(tilePath.toFile().exists());
	}
}
