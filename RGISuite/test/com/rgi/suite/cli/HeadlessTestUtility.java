package com.rgi.suite.cli;

import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;

@SuppressWarnings("UnsecureRandomNumberGeneration")
public final class HeadlessTestUtility
{

	private HeadlessTestUtility()
	{
	}

	public static String getRandomString(final int length)
	{
		final Random randomGenerator = new Random();
		final String characters      = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		final char[] text            = new char[length];
		for( int i = 0; i < length; i++ )
		{
			text[i] = characters.charAt( randomGenerator.nextInt( characters
																		  .length() ) );
		}
		return new String( text );
	}

	public static File getRandomFile(final int length, final String extension, final TemporaryFolder tempFolder)
	{
		try
		{
			return tempFolder.newFile( String.format( "%s%s", HeadlessTestUtility.getRandomString( length ),
													  extension ) );
		}
		catch( final IOException ignored )
		{
			// do nothing
		}
		return tempFolder.getRoot();
	}

	@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
	public static String getNonExistantFileString(final TemporaryFolder tempFolder, final String extension)
	{
		return Paths.get( tempFolder.getRoot().getAbsolutePath(),
						  String.format( "%s%s", HeadlessTestUtility.getRandomString( 6 ), extension ) ).toString();
	}

}
