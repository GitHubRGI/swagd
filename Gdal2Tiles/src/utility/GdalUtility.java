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
import java.util.stream.IntStream;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

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
}
