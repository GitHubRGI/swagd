package com.rgi.suite.cli;

import javax.activation.MimeType;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

/**
 * Created by matthew.moran on 7/10/15.
 */
public final class HeadlessUtils
{
	private HeadlessUtils()
	{
	}

	/**
	 * returns an image writer for the supplied image type.
	 *
	 * @return
	 */
	private static ImageWriter getImageWriter(final MimeType imageFormat)
	{
		return ImageIO.getImageWritersByMIMEType(imageFormat.toString())
					  .next();
	}

	/**
	 * created an image writer parameter object.
	 *
	 * @return
	 */
	public static ImageWriteParam getImageWriteParameter(final int quality, final String compressionType,
														 final MimeType imageFormat)
	{
		final ImageWriteParam imageWriteParameter = HeadlessUtils.getImageWriter(imageFormat)
																 .getDefaultWriteParam();
		final Float compressionQualityValue = (float)((quality) / 100.00);
		if(compressionType != null
		   && imageWriteParameter.canWriteCompressed())
		{
			imageWriteParameter
					.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			imageWriteParameter.setCompressionType(compressionType.toUpperCase());

			imageWriteParameter
					.setCompressionQuality(compressionQualityValue);
			return imageWriteParameter;
		}
		return null;
	}

}
