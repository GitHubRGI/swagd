package com.rgi.suite.cli;

import org.junit.rules.TemporaryFolder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

public class HeadlessTestUtility {

	protected static String getRandomString(final int length) {
		final Random randomGenerator = new Random();
		final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		final char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = characters.charAt(randomGenerator.nextInt(characters
					.length()));
		}
		return new String(text);
	}

	protected static File getRandomFile(final int length, String extension, TemporaryFolder tempFolder) {
		File testFile;
		try {
				testFile = tempFolder.newFile(getRandomString(length)
						.toString() + extension);
			return testFile;
		} catch (IOException e) {
			// do nothing
		}
		return tempFolder.getRoot();
	}

	public static Path createTMSFolderGeodetic(
			final TemporaryFolder tempFolder, final int zooms) {
		try {
			final File tmsFolder = tempFolder.newFolder(getRandomString(8));
			for (int i = 0; i < zooms; i++) {
				for (int j = 0; j < Math.pow(2, i); j++) {
					final String[] rowPath = { tmsFolder.getName().toString(),
							String.valueOf(i), String.valueOf(j) };
					final File thisRow = tempFolder.newFolder(rowPath);
					for (int k = 0; k < Math.pow(2, (i - 1)); k++) {
						final BufferedImage img = new BufferedImage(256, 256,
								BufferedImage.TYPE_INT_ARGB);
						final Path thisColumn = thisRow.toPath().resolve(
								String.valueOf(k) + ".png");
						ImageIO.write(img, "PNG", thisColumn.toFile());
					}
				}
			}
			return tmsFolder.toPath();
		} catch (final IOException ioException) {
			System.err.println("Could not generate TMS directory structure."
					+ "\n" + ioException.getMessage());
			return null;
		}
	}
}
