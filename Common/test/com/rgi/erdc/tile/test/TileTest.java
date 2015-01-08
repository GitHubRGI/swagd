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

package com.rgi.erdc.tile.test;


/**
 * Unit tests for the Tile object.
 *
 * @author Steven D. Lander
 *
 */
public class TileTest
{
    // TODO fix these tests
//	@Rule
//	public TemporaryFolder testFolder = new TemporaryFolder();
//	private Random randomGenerator = new Random();
//	private Tile tile;
//
//	private String getRanShort() {
//		int value = this.randomGenerator.nextInt(Short.MAX_VALUE);
//		return (Integer.valueOf(value)).toString();
//	}
//
//	private String getRanShortNeg() {
//		int value = this.randomGenerator.nextInt(Short.MAX_VALUE);
//		value -= Short.MAX_VALUE;
//		return (Integer.valueOf(value)).toString();
//	}
//
//	private String getRanInt() {
//		int value = this.randomGenerator.nextInt(Integer.MAX_VALUE);
//		return (Integer.valueOf(value)).toString();
//	}
//
//	private String getRanFlt() {
//		float value = this.randomGenerator.nextFloat();
//		return (Float.valueOf(value)).toString();
//	}
//
//	private String getRanIntNeg() {
//		int value = this.randomGenerator.nextInt(Integer.MAX_VALUE);
//		value -= Integer.MAX_VALUE;
//		return (Integer.valueOf(value)).toString();
//	}
//
//	private String getRanString(int length) {
//		String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
//		char[] text = new char[5];
//		for (int i = 0; i < length; i++) {
//			text[i] = characters.charAt(this.randomGenerator.nextInt(characters.length()));
//		}
//		return new String(text);
//	}
//
//	private String getRanString() {
//		return this.getRanString(5);
//	}
//
//	private static boolean imagesEqual(BufferedImage img1, BufferedImage img2) {
//		// Pixel by pixel comparison for image equality
//		if (img1.getHeight() != img2.getHeight()) {
//			// Height dimensions are not the same
//			return false;
//		}
//		if (img1.getWidth() != img2.getWidth()) {
//			// Width dimensions are not the same
//			return false;
//		}
//		for (int y = 0; y < img1.getHeight(); y++) {
//			for (int x = 0; x < img1.getWidth(); x++) {
//				if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
//					return false;
//				}
//			}
//		}
//		return true;
//	}
//
//	@Test(expected = TileException.class)
//	public void breakDirTileInit() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanString());
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		fail("Tile class should throw exception on a path that is not TMS.");
//	}
//
//	@Test
//	public void parseTileZoomFromPath() throws TileException {
//		String z = this.getRanShort();
//		Path filePath = FileSystems.getDefault().getPath(z, this.getRanInt(), this.getRanInt() + ".png");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		assertTrue(this.tile.getZoomLevel() == Integer.valueOf(z).intValue());
//	}
//
//	@Test(expected = TileException.class)
//	public void breakStrParseTileZoomFromPath() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanString(), this.getRanInt(),
//				this.getRanInt() + ".png");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		fail("Tile class should throw exception when incorrect zoom path given.");
//	}
//
//	@Test(expected = TileException.class)
//	public void breakFltParseTileZoomFromPath() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanFlt(), this.getRanInt(), this.getRanInt() + ".png");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		fail("Tile class should throw exception when incorrect zoom path given.");
//	}
//
//	@Test(expected = TileException.class)
//	public void breakNegValParseTileZoomFromPath() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShortNeg(), this.getRanInt(),
//				this.getRanInt() + ".png");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		fail("Tile class should throw exception when incorrect zoom path given.");
//	}
//
//	@Test
//	public void parseTileRowFromPath() throws TileException {
//		String x = this.getRanInt();
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), x, this.getRanInt() + ".png");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		assertTrue(this.tile.getTileRow() == Integer.valueOf(x).intValue());
//	}
//
//	@Test(expected = TileException.class)
//	public void breakStrParseTileRowFromPath() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanInt(), this.getRanString(),
//				this.getRanInt() + ".png");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		fail("Tile class should throw exception when incorrect row path given.");
//	}
//
//	@Test(expected = TileException.class)
//	public void breakFltParseTileRowFromPath() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), this.getRanFlt(),
//				this.getRanInt() + ".png");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		fail("Tile class should throw exception when incorrect row path given.");
//	}
//
//	@Test(expected = TileException.class)
//	public void breakNegParseTileRowFromPath() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), this.getRanIntNeg(),
//				this.getRanInt() + ".png");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		fail("Tile class should throw exception when incorrect row path given.");
//	}
//
//	@Test
//	public void parseTileColumnFromPath() throws TileException {
//		String y = this.getRanInt();
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), this.getRanInt(), y + ".jpg");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		assertTrue(this.tile.getTileColumn() == Integer.valueOf(y).intValue());
//	}
//
//	@Test(expected = TileException.class)
//	public void breakStrParseTileColumnFromPath() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), this.getRanInt(),
//				this.getRanString() + ".png");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		fail("Tile class should throw exception when incorrect column path given.");
//	}
//
//	@Test(expected = TileException.class)
//	public void breakFltParseTileColumnFromPath() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), this.getRanInt(),
//				this.getRanFlt() + ".png");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		fail("Tile class should throw exception when incorrect column path given.");
//	}
//
//	@Test(expected = TileException.class)
//	public void breakNoExtParseTileColumnFromPath() throws TileException, IOException {
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = img.createGraphics();
//		graphics.setPaint(new Color(255, 0, 0));
//		graphics.fillRect(0, 0, tileSize, tileSize);
//		String[] folders = { this.getRanShort(), this.getRanInt() };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(this.getRanInt());
//		ImageIO.write(img, "PNG", tmpFile.toFile());
//		this.tile = new Tile(tmpFile, TileOrigin.LowerLeft, 256, 256);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakNegValParseTileColumnFromPath() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), this.getRanInt(),
//				this.getRanIntNeg() + ".png");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakHugeValParseTileColumnFromPath() throws TileException {
//		String bigNumber = this.getRanInt() + this.getRanInt();
//		BigInteger y = new BigInteger(bigNumber);
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), this.getRanInt(), y.toString() + ".png");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//	}
//
//	@Test
//	public void verifyTileMimeTypeDetectedJpg() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), this.getRanInt(),
//				this.getRanInt() + ".jpg");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		assertTrue(this.tile.getMimeType() == TileMimeType.JPEG);
//	}
//
//	@Test
//	public void verifyTileMimeTypeDetectedJpeg() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), this.getRanInt(),
//				this.getRanInt() + ".jpeg");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		assertTrue(this.tile.getMimeType() == TileMimeType.JPEG);
//	}
//
//	@Test
//	public void verifyTileMimeTypeDetectedWebp() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), this.getRanInt(),
//				this.getRanInt() + ".webp");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		assertTrue(this.tile.getMimeType() == TileMimeType.WEBP);
//	}
//
//	@Test
//	public void verifyTileMimeTypeDetectedBmp() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), this.getRanInt(),
//				this.getRanInt() + ".bmp");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		assertTrue(this.tile.getMimeType() == TileMimeType.BMP);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakVerifyTileMimeTypeDetected() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), this.getRanInt(),
//				this.getRanInt() + ".jpeg");
//		this.tile = new Tile(filePath, null, 256, 256);
//		fail("Tile object should throw exception when null tile origin is given.");
//	}
//
//	@Test(expected = TileException.class)
//	public void breakDetectMimetype() throws TileException, IOException {
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = img.createGraphics();
//		graphics.setPaint(new Color(255, 0, 0));
//		graphics.fillRect(0, 0, tileSize, tileSize);
//		String[] folders = { this.getRanShort(), this.getRanInt() };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(this.getRanInt() + ".FOO");
//		ImageIO.write(img, "WEBP", tmpFile.toFile());
//		this.tile = new Tile(tmpFile, TileOrigin.UpperLeft, (short) 256, (short) 256);
//	}
//
//	@Test
//	public void verify256DetectTileWidth() throws TileException, IOException {
//		int width = 256;
//		int height = 512;
//		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		String[] folders = { this.getRanShort(), this.getRanInt() };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(this.getRanInt() + ".png");
//		ImageIO.write(img, "PNG", tmpFile.toFile());
//		this.tile = new Tile(tmpFile, TileOrigin.LowerLeft, 256, 256);
//		assertTrue(this.tile.getWidth() == width);
//	}
//
//
//	@Test
//	public void verify512DetectTileWidth() throws TileException, IOException {
//		int width = 512;
//		int height = 256;
//		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		String[] folders = { this.getRanShort(), this.getRanInt() };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(this.getRanInt() + ".png");
//		ImageIO.write(img, "PNG", tmpFile.toFile());
//		this.tile = new Tile(tmpFile, TileOrigin.LowerLeft, 256, 256);
//		assertTrue(this.tile.getWidth() == width);
//	}
//
//	@Test
//	public void verify256DetectTileHeight() throws TileException, IOException {
//		int width = 512;
//		int height = 256;
//		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		String[] folders = { this.getRanShort(), this.getRanInt() };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(this.getRanInt() + ".png");
//		ImageIO.write(img, "PNG", tmpFile.toFile());
//		this.tile = new Tile(tmpFile, TileOrigin.LowerLeft, 256, 256);
//		assertTrue(this.tile.getHeight() == height);
//	}
//
//	@Test
//	public void verify512DetectTileHeight() throws TileException, IOException {
//		int width = 256;
//		int height = 512;
//		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		String[] folders = { this.getRanShort(), this.getRanInt() };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(this.getRanInt() + ".png");
//		ImageIO.write(img, "PNG", tmpFile.toFile());
//		this.tile = new Tile(tmpFile, TileOrigin.LowerLeft, 256, 256);
//		assertTrue(this.tile.getHeight() == height);
//	}
//
//	@Test
//	public void verifyAssignTileOrigin() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), this.getRanInt(),
//				this.getRanInt() + ".png");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		assertTrue(this.tile.getTileOrigin() == TileOrigin.LowerLeft);
//	}
//
//	@Test
//	public void verifyAssignFilePath() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanShort(), this.getRanInt(),
//				this.getRanInt() + ".jpg");
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		assertTrue(this.tile.getFilePath() == filePath);
//	}
//
//	@Test
//	public void verifySetImageContents() throws TileException, IOException {
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = img.createGraphics();
//		graphics.setPaint(new Color(255, 0, 0));
//		graphics.fillRect(0, 0, tileSize, tileSize);
//		String[] folders = { this.getRanShort(), this.getRanInt() };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(this.getRanInt() + ".png");
//		ImageIO.write(img, "PNG", tmpFile.toFile());
//		this.tile = new Tile(tmpFile, TileOrigin.LowerLeft, 256, 256);
//		this.tile.setImageContents(img);
//		assertTrue(TileTest.imagesEqual(this.tile.getImageContents(), img));
//	}
//
//	@Test(expected = TileException.class)
//	public void breakSetImageContents() throws TileException {
//		Path filePath = FileSystems.getDefault().getPath(this.getRanString());
//		this.tile = new Tile(filePath, TileOrigin.LowerLeft, 256, 256);
//		this.tile.setImageContents(filePath);
//	}
//
//	@Test
//	public void verifyDiffSetImageContents() throws TileException, IOException {
//		int tileSize = 512;
//		// Red Image
//		BufferedImage red = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D rgraphics = red.createGraphics();
//		rgraphics.setPaint(new Color(255, 0, 0));
//		rgraphics.fillRect(0, 0, tileSize, tileSize);
//		// Blue Image
//		BufferedImage blue = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D bgraphics = blue.createGraphics();
//		bgraphics.setPaint(new Color(0, 0, 255));
//		bgraphics.fillRect(0, 0, tileSize, tileSize);
//		String[] folders = { this.getRanShort(), this.getRanInt() };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path redFile = tmsFolders.toPath().resolve(this.getRanInt() + ".png");
//		ImageIO.write(red, "PNG", redFile.toFile());
//		// Set the Tile constructor so that the image file is loaded by default
//		this.tile = new Tile(redFile, TileOrigin.LowerLeft, 256, 256);
//		assertFalse(TileTest.imagesEqual(this.tile.getImageContents(), blue));
//	}
//
//	@Test
//	public void verifyConstructorMinimalNoImageLoad() throws TileException, IOException {
//		// Make a transparent tile
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		String[] folders = { "0", "0" };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve("0.png");
//		this.tile = new Tile(tmpFile, TileOrigin.LowerLeft, 256, 256);
//		assertTrue(TileTest.imagesEqual(img, this.tile.getImageContents()));
//	}
//
//	@Test
//	public void verifyConstructorImageLoad() throws TileException, IOException {
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = img.createGraphics();
//		graphics.setPaint(new Color(255, 0, 0));
//		graphics.fillRect(0, 0, tileSize, tileSize);
//		String[] folders = { this.getRanShort(), this.getRanInt() };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(this.getRanInt() + ".png");
//		ImageIO.write(img, "PNG", tmpFile.toFile());
//		this.tile = new Tile(0, 0, 0, TileOrigin.LowerLeft, TileMimeType.PNG, 256, 256, tmpFile);
//		assertTrue(TileTest.imagesEqual(this.tile.getImageContents(), img));
//	}
//
//	@Test(expected = TileException.class)
//	public void breakConstructorZoom() throws TileException, IOException {
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = img.createGraphics();
//		graphics.setPaint(new Color(255, 0, 0));
//		graphics.fillRect(0, 0, tileSize, tileSize);
//		int zoom = -1;
//		String x = this.getRanShort();
//		String y = this.getRanShort();
//		String[] folders = { String.valueOf(zoom), x };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(y + ".png");
//		this.tile = new Tile(zoom, Integer.valueOf(x), Integer.valueOf(y), TileOrigin.LowerLeft, TileMimeType.PNG, 256,
//				256, tmpFile);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakConstructorRow() throws TileException, IOException {
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = img.createGraphics();
//		graphics.setPaint(new Color(255, 0, 0));
//		graphics.fillRect(0, 0, tileSize, tileSize);
//		String z = this.getRanShort();
//		String x = "-1";
//		String y = this.getRanShort();
//		String[] folders = { z, x };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(y + ".png");
//		this.tile = new Tile(Integer.valueOf(z), -1, Integer.valueOf(y), TileOrigin.LowerLeft, TileMimeType.PNG, 256,
//				256, tmpFile);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakConstructorColumn() throws TileException, IOException {
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = img.createGraphics();
//		graphics.setPaint(new Color(255, 0, 0));
//		graphics.fillRect(0, 0, tileSize, tileSize);
//		String z = this.getRanShort();
//		String x = this.getRanShort();
//		String y = "-1";
//		String[] folders = { z, x };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(y + ".png");
//		this.tile = new Tile(Integer.valueOf(z), Integer.valueOf(x), -1, TileOrigin.LowerLeft, TileMimeType.PNG, 256,
//				256, tmpFile);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakConstructorOrigin() throws TileException, IOException {
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = img.createGraphics();
//		graphics.setPaint(new Color(255, 0, 0));
//		graphics.fillRect(0, 0, tileSize, tileSize);
//		String z = this.getRanShort();
//		String x = this.getRanShort();
//		String y = this.getRanShort();
//		String[] folders = { z, x };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(y + ".png");
//		this.tile = new Tile(Integer.valueOf(z), Integer.valueOf(x), Integer.valueOf(y), null, TileMimeType.PNG, 256,
//				256, tmpFile);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakConstructorMime() throws TileException, IOException {
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = img.createGraphics();
//		graphics.setPaint(new Color(255, 0, 0));
//		graphics.fillRect(0, 0, tileSize, tileSize);
//		String z = this.getRanShort();
//		String x = this.getRanShort();
//		String y = this.getRanShort();
//		String[] folders = { z, x };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(y + ".png");
//		this.tile = new Tile(Integer.valueOf(z), Integer.valueOf(x), Integer.valueOf(y), TileOrigin.UpperLeft, null,
//				256, 256, tmpFile);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakConstructorWidth() throws TileException, IOException {
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = img.createGraphics();
//		graphics.setPaint(new Color(255, 0, 0));
//		graphics.fillRect(0, 0, tileSize, tileSize);
//		String z = this.getRanShort();
//		String x = this.getRanShort();
//		String y = this.getRanShort();
//		String[] folders = { z, x };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(y + ".png");
//		this.tile = new Tile(Integer.valueOf(z), Integer.valueOf(x), Integer.valueOf(y), TileOrigin.UpperLeft,
//				TileMimeType.PNG, 0, 256, tmpFile);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakConstructorHeight() throws TileException, IOException {
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = img.createGraphics();
//		graphics.setPaint(new Color(255, 0, 0));
//		graphics.fillRect(0, 0, tileSize, tileSize);
//		String z = this.getRanShort();
//		String x = this.getRanShort();
//		String y = this.getRanShort();
//		String[] folders = { z, x };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(y + ".png");
//		this.tile = new Tile(Integer.valueOf(z), Integer.valueOf(x), Integer.valueOf(y), TileOrigin.UpperLeft,
//				TileMimeType.PNG, 256, 0, tmpFile);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakConstructorPath() throws TileException, IOException {
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = img.createGraphics();
//		graphics.setPaint(new Color(255, 0, 0));
//		graphics.fillRect(0, 0, tileSize, tileSize);
//		String z = this.getRanShort();
//		String x = this.getRanShort();
//		String y = this.getRanShort();
//		String[] folders = { z, x };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		tmsFolders.toPath().resolve(y + ".png");
//		this.tile = new Tile(Integer.valueOf(z), Integer.valueOf(x), Integer.valueOf(y), TileOrigin.UpperLeft,
//				TileMimeType.PNG, 256, 256, null);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakConstructorPathSize() throws TileException, IOException {
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = img.createGraphics();
//		graphics.setPaint(new Color(255, 0, 0));
//		graphics.fillRect(0, 0, tileSize, tileSize);
//		String z = this.getRanShort();
//		String x = this.getRanShort();
//		String y = this.getRanShort();
//		String[] folders = { z, x };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		tmsFolders.toPath().resolve(y + ".png");
//		Path badPath = Paths.get(System.getProperty("java.io.tmpdir"));
//		this.tile = new Tile(Integer.valueOf(z), Integer.valueOf(x), Integer.valueOf(y), TileOrigin.UpperLeft,
//				TileMimeType.PNG, 256, 256, badPath);
//	}
//
//	@Test
//	public void breakConstructorNoLoad() throws TileException, IOException {
//		int tileSize = 256;
//		BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics = img.createGraphics();
//		graphics.setPaint(new Color(255, 0, 0));
//		graphics.fillRect(0, 0, tileSize, tileSize);
//		String z = this.getRanShort();
//		String x = this.getRanShort();
//		String y = this.getRanShort();
//		String[] folders = { z, x };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(y + ".png");
//		this.tile = new Tile(Integer.valueOf(z), Integer.valueOf(x), Integer.valueOf(y), TileOrigin.UpperLeft,
//				TileMimeType.PNG, 256, 256, tmpFile);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakConstructorLightPath() throws TileException {
//		this.tile = new Tile(null, TileOrigin.UpperLeft, 256, 256);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakConstructorLightOrigin() throws TileException, IOException {
//		String z = this.getRanShort();
//		String x = this.getRanShort();
//		String y = this.getRanShort();
//		String[] folders = { z, x };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(y + ".png");
//		this.tile = new Tile(tmpFile, null, 256, 256);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakConstructorLightWidth() throws TileException, IOException {
//		String z = this.getRanShort();
//		String x = this.getRanShort();
//		String y = this.getRanShort();
//		String[] folders = { z, x };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(y + ".png");
//		this.tile = new Tile(tmpFile, TileOrigin.LowerLeft, 0, 256);
//	}
//
//	@Test(expected = TileException.class)
//	public void breakConstructorLightHeight() throws TileException, IOException {
//		String z = this.getRanShort();
//		String x = this.getRanShort();
//		String y = this.getRanShort();
//		String[] folders = { z, x };
//		File tmsFolders = this.testFolder.newFolder(folders);
//		Path tmpFile = tmsFolders.toPath().resolve(y + ".png");
//		this.tile = new Tile(tmpFile, TileOrigin.LowerLeft, 256, 0);
//	}
}
