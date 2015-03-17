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
package utility;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.zip.DataFormatException;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.g2t.TilingException;

/**
 * @author Luke Lambert
 *
 */
public class GdalUtility
{
    /**
     * Converts a GDAL {@link Dataset} into a {@link BufferedImage} <br>
     * <br>
     * Code based on GDAL/SWIG example found <a href=
     * "https://svn.osgeo.org/gdal/trunk/gdal/swig/java/apps/GDALtest.java"
     * >here</a>.
     *
     * @param dataset
     *            A GDAL {@link Dataset}
     * @return Returns a {@link BufferedImage} who's contents matches that of
     *         the input GDAL {@link Dataset}
     */
    public static BufferedImage convert(final Dataset dataset)
    {
        if(dataset == null)
        {
            throw new IllegalArgumentException("Dataset may not be null");
        }

        final int bandCount = dataset.getRasterCount();

        if(bandCount <= 0)
        {
            throw new RuntimeException("Raster contained no bands");
        }

        final int rasterWidth  = dataset.getRasterXSize();
        final int rasterHeight = dataset.getRasterYSize();

        final int pixelCount = rasterWidth * rasterHeight;

        final Band band = dataset.GetRasterBand(1);   // Bands are 1-base indexed

        if(band == null)
        {
            throw new RuntimeException("GDAL returned a null raster band");
        }

        final int bandDataType = band.getDataType();

        if(bandCount == 1)
        {
            if(band.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex && band.GetRasterColorTable() != null)
            {
                final ByteBuffer byteBuffer = band.ReadRaster_Direct(0, 0, band.getXSize(), band.getYSize(), band.getDataType());

                final DataBuffer  dataBuffer     = getDataBuffer(band.getDataType(), bandCount, pixelCount, new ByteBuffer[]{ byteBuffer });
                final int         dataBufferType = getDataBufferType(bandDataType);
                final SampleModel sampleModel    = new BandedSampleModel(dataBufferType, rasterWidth, rasterHeight, bandCount);

                final WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);

                //bufferedImageDataType = BufferedImage.TYPE_BYTE_INDEXED; // This assignment had no effect
                return new BufferedImage(band.GetRasterColorTable()
                                             .getIndexColorModel(gdal.GetDataTypeSize(bandDataType)),
                                         raster,
                                         false,
                                         null);
            }
        }

        final ByteBuffer[] bands = IntStream.range(0, bandCount)
                                            .mapToObj(bandIndex -> { final Band currentBand = dataset.GetRasterBand(bandIndex + 1);
                                                                     return currentBand.ReadRaster_Direct(0, 0, currentBand.getXSize(), currentBand.getYSize(), currentBand.getDataType());
                                                                   })
                                            .toArray(ByteBuffer[]::new);

        final DataBuffer  dataBuffer     = getDataBuffer(bandDataType, bandCount, pixelCount, bands);
        final int         dataBufferType = getDataBufferType(bandDataType);
        final SampleModel sampleModel    = new BandedSampleModel(dataBufferType, rasterWidth, rasterHeight, bandCount);

        final WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);

        if(bandCount > 2)
        {
            return new BufferedImage(new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                                                             bandCount == 4,
                                                             false,
                                                             bandCount == 4 ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
                                                             dataBufferType),
                                     raster,
                                     true,
                                     null);
        }


        final BufferedImage bufferedImage = new BufferedImage(rasterWidth, rasterHeight, getBufferedImageDataType(bandDataType));
        bufferedImage.setData(raster);

        return bufferedImage;
    }

    private static DataBuffer getDataBuffer(final int bandDataType, final int bandCount, final int pixelCount, final ByteBuffer[] bands)
    {
        if(bandDataType == gdalconstConstants.GDT_Byte)
        {
            final byte[][] bytes = new byte[bandCount][];
            for(int i = 0; i < bandCount; i++)
            {
                bytes[i] = new byte[pixelCount];
                bands[i].get(bytes[i]);
            }

            return new DataBufferByte(bytes, pixelCount);
        }
        else if(bandDataType == gdalconstConstants.GDT_Int16)
        {
            final short[][] shorts = new short[bandCount][];
            for(int i = 0; i < bandCount; i++)
            {
                shorts[i] = new short[pixelCount];
                bands[i].asShortBuffer().get(shorts[i]);
            }

            return new DataBufferShort(shorts, pixelCount);
        }
        else if(bandDataType == gdalconstConstants.GDT_Int32)
        {
            final int[][] ints = new int[bandCount][];
            for(int i = 0; i < bandCount; i++)
            {
                ints[i] = new int[pixelCount];
                bands[i].asIntBuffer().get(ints[i]);
            }

            return new DataBufferInt(ints, pixelCount);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported band data type");
        }
    }

    private static int getDataBufferType(final int bandDataType)
    {
        if(bandDataType == gdalconstConstants.GDT_Byte)
        {
            return DataBuffer.TYPE_BYTE;
        }
        else if(bandDataType == gdalconstConstants.GDT_Int16)
        {
            return DataBuffer.TYPE_USHORT;
        }
        else if(bandDataType == gdalconstConstants.GDT_Int32)
        {
            return DataBuffer.TYPE_INT;
        }
        else
        {
            throw new IllegalArgumentException("Unsupported band data type");
        }
    }

    private static int getBufferedImageDataType(final int bandDataType)
    {
        if(bandDataType == gdalconstConstants.GDT_Byte)
        {
            return BufferedImage.TYPE_BYTE_GRAY;
        }
        else if(bandDataType == gdalconstConstants.GDT_Int16)
        {
            return BufferedImage.TYPE_USHORT_GRAY;
        }
        else if(bandDataType == gdalconstConstants.GDT_Int32)
        {
            return BufferedImage.TYPE_CUSTOM;
        }
        else
        {
            throw new IllegalArgumentException("Unsupported band data type");
        }
    }
    
    /**
     * Gets the GDAL {@link SpatialReference} from the input {@link Dataset}
     * 
     * @param dataset A GDAL {@link Dataset}
     * @return Returns a GDAL {@link SpatialReference} that the input dataset has
     * @throws TilingException Thrown when the input Dataset has no spatial reference
     */
    public static SpatialReference getDatasetSrs(final Dataset dataset) throws DataFormatException
    {
		final SpatialReference srs = new SpatialReference();
		// Get the well-known-text of this dataset
		String wkt = dataset.GetProjection();
		if (wkt.isEmpty() && dataset.GetGCPCount() != 0)
		{
			// If the wkt is empty and there are GCPs...
			wkt = dataset.GetGCPProjection();
		}
		if (!wkt.isEmpty())
		{
			// Initialize the srs from the non-empt wkt string
			srs.ImportFromWkt(wkt);
			return srs;
		}
		throw new DataFormatException("Cannot get source file spatial reference system.");
    }
    
    public static SpatialReference getSpatialReferenceFromCrs(final CoordinateReferenceSystem crs)
    {
    	final SpatialReference srs = new SpatialReference();
    	srs.ImportFromEPSG(crs.getIdentifier());
    	return srs;
    }
    
    public static boolean datasetHasGeoReference(final Dataset dataset)
    {
		final double[] emptyGeoReference = { 0.0, 1.0, 0.0, 0.0, 0.0, 1.0 };
		// Compare dataset geotransform to an empty geotransform and ensure there are no GCPs
		//return Arrays.equals(dataset.GetGeoTransform(), emptyGeoReference) && dataset.GetGCPCount() == 0;
		return Arrays.equals(dataset.GetGeoTransform(), emptyGeoReference) || dataset.GetGCPCount() != 0;
    }
    
    public static BoundingBox getBoundsForDataset(final Dataset dataset) throws DataFormatException
    {
		final double[] outputGeotransform = dataset.GetGeoTransform();
		// Report error in case rotation/skew is in geotransform (only for raster profile)
		if (outputGeotransform[2] != 0 && outputGeotransform[4] != 0)
		{
			throw new DataFormatException("Georeference of the raster contains rotation or skew. " +
										  "Such raster is not supported.  Please use gdalwarp first.");
		}
		final double minX = outputGeotransform[0];
		final double maxX = outputGeotransform[0] + dataset.GetRasterXSize() * outputGeotransform[1];
		final double maxY = outputGeotransform[3];
		final double minY = outputGeotransform[3] - dataset.GetRasterYSize() * outputGeotransform[1];
		return new BoundingBox(minX, minY, maxX, maxY);
    }
    
    public static CoordinateReferenceSystem getCoordinateReferenceSystemFromSpatialReference(final SpatialReference srs)
    {
        // Passing null to GetAuthorityName and Code will query the root node of the WKT, not
        // sure if this is what we want
        final String authority = srs.GetAuthorityName(null);
        final String identifier = srs.GetAuthorityCode(null);
        return new CoordinateReferenceSystem(authority, Integer.valueOf(identifier));
    }
}